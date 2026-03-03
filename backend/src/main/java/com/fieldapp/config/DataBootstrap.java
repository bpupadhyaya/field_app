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
        var superAdminRole = roleRepo.findByName(RoleName.ROLE_SUPER_ADMIN).orElseThrow();

        migrateLegacySuperadminUsername();

        if (appUserRepo.findByUsername("sadmin").isEmpty()) {
            appUserRepo.save(AppUser.builder().username("sadmin").passwordHash(passwordEncoder.encode("sadmin123")).displayName("Super Admin").email("sadmin@field.local").userType(UserType.EMPLOYEE).roles(Set.of(superAdminRole)).build());
            appUserRepo.save(AppUser.builder().username("admin").passwordHash(passwordEncoder.encode("Admin@123")).displayName("Admin User").email("admin@field.local").userType(UserType.EMPLOYEE).roles(Set.of(roleRepo.findByName(RoleName.ROLE_ADMIN).orElseThrow())).build());
            appUserRepo.save(AppUser.builder().username("manager1").passwordHash(passwordEncoder.encode("Manager@123")).displayName("Manager One").email("manager1@field.local").userType(UserType.EMPLOYEE).roles(Set.of(roleRepo.findByName(RoleName.ROLE_MANAGER).orElseThrow())).build());
            appUserRepo.save(AppUser.builder().username("user1").passwordHash(passwordEncoder.encode("User@123")).displayName("User One").email("user1@field.local").userType(UserType.EMPLOYEE).roles(Set.of(roleRepo.findByName(RoleName.ROLE_USER).orElseThrow())).build());
            appUserRepo.save(AppUser.builder().username("dealer1").passwordHash(passwordEncoder.encode("Dealer@123")).displayName("Dealer One").email("dealer1@field.local").userType(UserType.DEALER).roles(Set.of(roleRepo.findByName(RoleName.ROLE_DEALER).orElseThrow())).build());
        }
        appUserRepo.findByUsername("sadmin").ifPresent(sa -> {
            sa.setRoles(allRoles);
            appUserRepo.save(sa);
        });
        appUserRepo.findByUsername("manager1").ifPresent(manager -> {
            manager.setRoles(Set.of(roleRepo.findByName(RoleName.ROLE_MANAGER).orElseThrow()));
            appUserRepo.save(manager);
        });
        resetDefaultPassword("sadmin", "sadmin123");
        resetDefaultPassword("admin", "admin123");
        resetDefaultPassword("manager1", "manager123");
        resetDefaultPassword("user1", "user123");
        resetDefaultPassword("dealer1", "dealer123");

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

    private void resetDefaultPassword(String username, String rawPassword) {
        appUserRepo.findByUsername(username).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            appUserRepo.save(user);
        });
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
