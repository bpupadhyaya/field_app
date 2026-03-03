package com.fieldapp.config;

import com.fieldapp.model.AppUser;
import com.fieldapp.model.Device;
import com.fieldapp.model.Role;
import com.fieldapp.model.RoleName;
import com.fieldapp.model.UserType;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.DeviceEssentialDataRepo;
import com.fieldapp.repo.DeviceHealthRepo;
import com.fieldapp.repo.DevicePriceHistoryRepo;
import com.fieldapp.repo.DeviceRepo;
import com.fieldapp.repo.RoleRepo;
import com.fieldapp.repo.TrafficMetricRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataBootstrapTest {
    @Mock private RoleRepo roleRepo;
    @Mock private AppUserRepo appUserRepo;
    @Mock private DeviceRepo deviceRepo;
    @Mock private DeviceHealthRepo deviceHealthRepo;
    @Mock private DeviceEssentialDataRepo essentialDataRepo;
    @Mock private DevicePriceHistoryRepo priceHistoryRepo;
    @Mock private TrafficMetricRepo trafficMetricRepo;
    @Mock private PasswordEncoder passwordEncoder;

    private DataBootstrap dataBootstrap;

    @BeforeEach
    void setUp() {
        dataBootstrap = new DataBootstrap(
                roleRepo,
                appUserRepo,
                deviceRepo,
                deviceHealthRepo,
                essentialDataRepo,
                priceHistoryRepo,
                trafficMetricRepo,
                passwordEncoder
        );
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "enc:" + inv.getArgument(0, String.class));
    }

    @Test
    void runSeedsDevicesAndTrafficWhenCountsAreLow() {
        Map<RoleName, Role> roleStore = configureRoleStore();
        Map<String, AppUser> users = configureUserStore(new HashMap<>());
        long[] deviceIdSeq = {1L};
        when(deviceRepo.count()).thenReturn(0L);
        when(trafficMetricRepo.count()).thenReturn(0L);
        when(deviceRepo.save(any(Device.class))).thenAnswer(inv -> {
            Device d = inv.getArgument(0);
            if (d.getId() == null) {
                d.setId(deviceIdSeq[0]++);
            }
            return d;
        });
        when(trafficMetricRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        dataBootstrap.run();

        verify(deviceRepo, atLeastOnce()).save(any(Device.class));
        verify(trafficMetricRepo).saveAll(anyList());
        assertThat(roleStore).hasSize(RoleName.values().length);
        assertThat(users).containsKey("sadmin1");
    }

    @Test
    void runMigratesLegacyUserSkipsSeedAndUpdatesExistingDefaultUser() {
        configureRoleStore();
        AppUser legacy = AppUser.builder()
                .id(99L)
                .username("superadmin")
                .displayName("Legacy")
                .email("legacy@field.local")
                .passwordHash("old")
                .userType(UserType.EMPLOYEE)
                .build();
        AppUser admin1 = AppUser.builder()
                .id(5L)
                .username("admin1")
                .displayName("Old Admin")
                .email("old-admin@field.local")
                .passwordHash("old")
                .userType(UserType.EMPLOYEE)
                .build();
        Map<String, AppUser> users = new HashMap<>();
        users.put("superadmin", legacy);
        users.put("admin1", admin1);
        configureUserStore(users);
        when(deviceRepo.count()).thenReturn(10L);
        when(trafficMetricRepo.count()).thenReturn(2L);

        dataBootstrap.run();

        verify(deviceRepo, never()).save(any(Device.class));
        verify(trafficMetricRepo, never()).saveAll(anyList());
        assertThat(users).containsKey("sadmin");
        assertThat(users.get("admin1").getDisplayName()).isEqualTo("Admin User 1");
        assertThat(users.get("admin1").getPasswordHash()).isEqualTo("enc:admin123");
    }

    @Test
    void runDoesNotMigrateLegacyWhenSadminAlreadyExists() {
        configureRoleStore();
        AppUser legacy = AppUser.builder()
                .id(99L)
                .username("superadmin")
                .displayName("Legacy")
                .email("legacy@field.local")
                .passwordHash("old")
                .userType(UserType.EMPLOYEE)
                .build();
        AppUser current = AppUser.builder()
                .id(100L)
                .username("sadmin")
                .displayName("Current")
                .email("sadmin@field.local")
                .passwordHash("cur")
                .userType(UserType.EMPLOYEE)
                .build();
        Map<String, AppUser> users = new HashMap<>();
        users.put("superadmin", legacy);
        users.put("sadmin", current);
        configureUserStore(users);
        when(deviceRepo.count()).thenReturn(10L);
        when(trafficMetricRepo.count()).thenReturn(2L);

        dataBootstrap.run();

        assertThat(users).containsKey("superadmin");
        assertThat(users).containsKey("sadmin");
    }

    private Map<RoleName, Role> configureRoleStore() {
        Map<RoleName, Role> roleStore = new EnumMap<>(RoleName.class);
        when(roleRepo.findByName(any(RoleName.class))).thenAnswer(inv -> {
            RoleName name = inv.getArgument(0);
            return Optional.ofNullable(roleStore.get(name));
        });
        when(roleRepo.save(any(Role.class))).thenAnswer(inv -> {
            Role role = inv.getArgument(0);
            roleStore.put(role.getName(), role);
            return role;
        });
        return roleStore;
    }

    private Map<String, AppUser> configureUserStore(Map<String, AppUser> users) {
        when(appUserRepo.findByUsername(any(String.class))).thenAnswer(inv -> {
            String username = inv.getArgument(0);
            return Optional.ofNullable(users.get(username));
        });
        when(appUserRepo.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser user = inv.getArgument(0);
            users.values().removeIf(u -> u.getId() != null && u.getId().equals(user.getId()));
            users.put(user.getUsername(), user);
            return user;
        });
        return users;
    }
}
