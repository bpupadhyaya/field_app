package com.fieldapp.service;

import com.fieldapp.dto.ControlCommandRequest;
import com.fieldapp.model.*;
import com.fieldapp.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepo deviceRepo;
    private final DeviceHealthRepo deviceHealthRepo;
    private final DeviceEssentialDataRepo essentialDataRepo;
    private final DevicePriceHistoryRepo priceHistoryRepo;
    private final AppUserRepo appUserRepo;
    private final AuditService auditService;

    public List<Device> list(String q) {
        if (q == null || q.isBlank()) return deviceRepo.findAll();
        return deviceRepo.findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCaseOrTypeContainingIgnoreCase(q, q, q);
    }

    public Device updatePrice(Long id, BigDecimal price, String byUser) {
        Device d = deviceRepo.findById(id).orElseThrow();
        d.setCurrentPrice(price);
        d.setUpdatedAt(Instant.now());
        deviceRepo.save(d);
        priceHistoryRepo.save(DevicePriceHistory.builder().deviceId(id).price(price).build());
        auditService.log(byUser, "PRICE_UPDATE", "DEVICE", id.toString(), "updated to " + price);
        return d;
    }

    public List<DevicePriceHistory> history(Long id, String range) {
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        if ("7d".equalsIgnoreCase(range)) from = Instant.now().minus(7, ChronoUnit.DAYS);
        if ("30d".equalsIgnoreCase(range)) from = Instant.now().minus(30, ChronoUnit.DAYS);
        if ("90d".equalsIgnoreCase(range)) from = Instant.now().minus(90, ChronoUnit.DAYS);
        return priceHistoryRepo.findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(id, from);
    }

    public String control(Long id, ControlCommandRequest request, Authentication auth) {
        Device device = deviceRepo.findById(id).orElseThrow();
        var user = appUserRepo.findByUsername(auth.getName()).orElseThrow();
        boolean isAdminOrManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdminOrManager) {
            if (request.zoneId() == null || !request.zoneId().equals(device.getZoneId())) {
                throw new IllegalArgumentException("User control restricted to assigned zone");
            }
            if (request.command().toLowerCase().contains("steer_outside_zone")) {
                throw new IllegalArgumentException("User cannot steer device outside zone");
            }
        }
        auditService.log(user.getUsername(), "DEVICE_CONTROL", "DEVICE", id.toString(), request.command());
        return "Command accepted: " + request.command();
    }

    public DeviceHealth health(Long deviceId) { return deviceHealthRepo.findTopByDeviceIdOrderByCapturedAtDesc(deviceId).orElse(null); }
    public DeviceEssentialData essential(Long deviceId) { return essentialDataRepo.findByDeviceId(deviceId).orElse(null); }
}
