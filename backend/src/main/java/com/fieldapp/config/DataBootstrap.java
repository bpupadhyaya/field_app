package com.fieldapp.config;

import com.fieldapp.model.*;
import com.fieldapp.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataBootstrap implements CommandLineRunner {
    private final RoleRepo roleRepo;
    private final AppUserRepo appUserRepo;
    private final DeviceRepo deviceRepo;
    private final DeviceHealthRepo deviceHealthRepo;
    private final DeviceEssentialDataRepo essentialDataRepo;
    private final DevicePriceHistoryRepo priceHistoryRepo;
    private final TrafficMetricRepo trafficMetricRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (RoleName rn : RoleName.values()) {
            roleRepo.findByName(rn).orElseGet(() -> roleRepo.save(Role.builder().name(rn).build()));
        }
        var allRoles = Arrays.stream(RoleName.values())
                .map(rn -> roleRepo.findByName(rn).orElseThrow())
                .collect(Collectors.toSet());
        var adminRole = roleRepo.findByName(RoleName.ROLE_ADMIN).orElseThrow();
        var managerRole = roleRepo.findByName(RoleName.ROLE_MANAGER).orElseThrow();
        var userRole = roleRepo.findByName(RoleName.ROLE_USER).orElseThrow();
        var dealerRole = roleRepo.findByName(RoleName.ROLE_DEALER).orElseThrow();

        migrateLegacySuperadminUsername();

        ensureDefaultUser("sadmin", "sadmin123", "Super Admin", "sadmin@field.local", UserType.EMPLOYEE, allRoles);
        ensureDefaultUser("sadmin1", "sadmin123", "Super Admin 1", "sadmin1@field.local", UserType.EMPLOYEE, allRoles);
        ensureDefaultUser("sadmin2", "sadmin123", "Super Admin 2", "sadmin2@field.local", UserType.EMPLOYEE, allRoles);
        ensureDefaultUser("sadmin3", "sadmin123", "Super Admin 3", "sadmin3@field.local", UserType.EMPLOYEE, allRoles);

        ensureDefaultUser("admin", "admin123", "Admin User", "admin@field.local", UserType.EMPLOYEE, Set.of(adminRole));
        ensureDefaultUser("admin1", "admin123", "Admin User 1", "admin1@field.local", UserType.EMPLOYEE, Set.of(adminRole));
        ensureDefaultUser("admin2", "admin123", "Admin User 2", "admin2@field.local", UserType.EMPLOYEE, Set.of(adminRole));
        ensureDefaultUser("admin3", "admin123", "Admin User 3", "admin3@field.local", UserType.EMPLOYEE, Set.of(adminRole));

        ensureDefaultUser("manager1", "manager123", "Manager One", "manager1@field.local", UserType.EMPLOYEE, Set.of(managerRole));
        ensureDefaultUser("manager2", "manager123", "Manager Two", "manager2@field.local", UserType.EMPLOYEE, Set.of(managerRole));
        ensureDefaultUser("manager3", "manager123", "Manager Three", "manager3@field.local", UserType.EMPLOYEE, Set.of(managerRole));

        ensureDefaultUser("user1", "user123", "User One", "user1@field.local", UserType.EMPLOYEE, Set.of(userRole));
        ensureDefaultUser("user2", "user123", "User Two", "user2@field.local", UserType.EMPLOYEE, Set.of(userRole));
        ensureDefaultUser("user3", "user123", "User Three", "user3@field.local", UserType.EMPLOYEE, Set.of(userRole));

        ensureDefaultUser("dealer1", "dealer123", "Dealer One", "dealer1@field.local", UserType.DEALER, Set.of(dealerRole));
        ensureDefaultUser("dealer2", "dealer123", "Dealer Two", "dealer2@field.local", UserType.DEALER, Set.of(dealerRole));
        ensureDefaultUser("dealer3", "dealer123", "Dealer Three", "dealer3@field.local", UserType.DEALER, Set.of(dealerRole));

        if (deviceRepo.count() < 10) {
            List<String[]> types = List.of(
                    new String[]{"Agriculture", "Compact Tractors"}, new String[]{"Agriculture", "Utility Tractors"}, new String[]{"Agriculture", "Specialty Tractors"}, new String[]{"Agriculture", "Row Crop Tractors"}, new String[]{"Agriculture", "4WD Tractors"},
                    new String[]{"Lawn & Garden", "Mowers"}, new String[]{"Construction", "Excavators"}, new String[]{"Construction", "Compact Construction"}, new String[]{"Utility", "Gator Utility Vehicles"}, new String[]{"Technology", "Precision Upgrades"},
                    new String[]{"Forestry", "Skidders"}, new String[]{"Grounds Care", "Commercial Mowers"}, new String[]{"Golf", "Fairway Mowers"}, new String[]{"Electric", "Electric Utility"}, new String[]{"Attachments", "Implements"}
            );
            int i = 0;
            for (String[] t : types) {
                i++;
                Device d = deviceRepo.save(Device.builder().deviceCode("DV-" + (1000 + i)).name(t[1] + " #" + i).category(t[0]).type(t[1]).locationName("Field Zone " + ((i % 4) + 1)).latitude(41.0 + i * 0.01).longitude(-93.0 - i * 0.01).zoneId("Z" + ((i % 3) + 1)).status(i % 2 == 0 ? DeviceStatus.OPERABLE : DeviceStatus.PARKED).currentPrice(BigDecimal.valueOf(50000 + i * 4000L)).build());
                deviceHealthRepo.save(DeviceHealth.builder().deviceId(d.getId()).engineTotalHours(15000).engineUsedHours(300 + i * 20).batteryHealthPercent(90 - i).wheelWearPercent(10 + i).wheelPressurePsi(32 + (i % 3)).cabinTempC(22 + (i % 4)).chassisTempC(40 + (i % 5)).roboticArmsHealthPercent(85 + (i % 10)).connectivityHealthPercent(80 + (i % 15)).build());
                essentialDataRepo.save(DeviceEssentialData.builder().deviceId(d.getId()).registrationInfo("Registered in IA").rentalInfo("Standard rental terms").paymentHistory("No overdue").licenseHistory("Compliant").rentalHistory("Seasonal usage").ownershipHistory("Owned by Farm Ops").fuelStatus("72% tank").repairHistory("No critical issues").build());
                for (int day = 90; day >= 0; day -= 3) {
                    priceHistoryRepo.save(DevicePriceHistory.builder().deviceId(d.getId()).price(d.getCurrentPrice().add(java.math.BigDecimal.valueOf((90 - day) * 15L - 450))).capturedAt(Instant.now().minusSeconds(day * 86400L)).build());
                }
            }
        }

        if (trafficMetricRepo.count() == 0) {
            trafficMetricRepo.saveAll(List.of(
                    TrafficMetric.builder().cloud("AWS").region("us-east-1").zone("use1-az1").activeUsers(1200).requestsPerSecond(1800).avgLatencyMs(110).build(),
                    TrafficMetric.builder().cloud("GCP").region("us-central1").zone("us-central1-a").activeUsers(900).requestsPerSecond(1400).avgLatencyMs(125).build(),
                    TrafficMetric.builder().cloud("Azure").region("eastus").zone("1").activeUsers(700).requestsPerSecond(1100).avgLatencyMs(140).build()
            ));
        }
    }

    private void ensureDefaultUser(String username, String password, String displayName, String email, UserType userType, Set<Role> roles) {
        appUserRepo.findByUsername(username).ifPresentOrElse(user -> {
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setUserType(userType);
            user.setRoles(roles);
            appUserRepo.save(user);
        }, () -> appUserRepo.save(AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(displayName)
                .email(email)
                .userType(userType)
                .roles(roles)
                .build()));
    }

    private void migrateLegacySuperadminUsername() {
        var legacy = appUserRepo.findByUsername("superadmin");
        var current = appUserRepo.findByUsername("sadmin");
        if (legacy.isPresent() && current.isEmpty()) {
            var user = legacy.get();
            user.setUsername("sadmin");
            user.setEmail("sadmin@field.local");
            appUserRepo.save(user);
        }
    }
}
