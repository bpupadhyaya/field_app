package com.fieldapp.service;

import com.fieldapp.dto.ControlCommandRequest;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.Device;
import com.fieldapp.model.DevicePriceHistory;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.DeviceEssentialDataRepo;
import com.fieldapp.repo.DeviceHealthRepo;
import com.fieldapp.repo.DevicePriceHistoryRepo;
import com.fieldapp.repo.DeviceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock private DeviceRepo deviceRepo;
    @Mock private DeviceHealthRepo deviceHealthRepo;
    @Mock private DeviceEssentialDataRepo deviceEssentialDataRepo;
    @Mock private DevicePriceHistoryRepo priceHistoryRepo;
    @Mock private AppUserRepo appUserRepo;
    @Mock private AuditService auditService;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(
                deviceRepo,
                deviceHealthRepo,
                deviceEssentialDataRepo,
                priceHistoryRepo,
                appUserRepo,
                auditService
        );
    }

    @Test
    void listWithoutQueryReturnsAll() {
        when(deviceRepo.findAll()).thenReturn(List.of());

        deviceService.list(" ");

        verify(deviceRepo).findAll();
    }

    @Test
    void updatePricePersistsDeviceAndHistoryAndAudit() {
        Device device = Device.builder()
                .id(42L)
                .zoneId("Z1")
                .currentPrice(BigDecimal.valueOf(100))
                .updatedAt(Instant.EPOCH)
                .build();
        when(deviceRepo.findById(42L)).thenReturn(Optional.of(device));

        Device updated = deviceService.updatePrice(42L, BigDecimal.valueOf(250), "admin1");

        assertThat(updated.getCurrentPrice()).isEqualByComparingTo("250");
        verify(deviceRepo).save(device);
        verify(priceHistoryRepo).save(any(DevicePriceHistory.class));
        verify(auditService).log("admin1", "PRICE_UPDATE", "DEVICE", "42", "updated to 250");
    }

    @Test
    void controlRejectsRegularUserWhenZoneDoesNotMatch() {
        Device device = Device.builder().id(5L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("user1").build();
        when(deviceRepo.findById(5L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("user1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThatThrownBy(() -> deviceService.control(5L, new ControlCommandRequest("move_forward", "Z2"), auth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("restricted to assigned zone");
    }

    @Test
    void controlRejectsRegularUserForSteerOutsideZoneCommand() {
        Device device = Device.builder().id(5L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("user1").build();
        when(deviceRepo.findById(5L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("user1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThatThrownBy(() -> deviceService.control(5L, new ControlCommandRequest("steer_outside_zone_left", "Z1"), auth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot steer");
    }

    @Test
    void controlAcceptsManagerCommandAndWritesAudit() {
        Device device = Device.builder().id(9L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("manager1").build();
        when(deviceRepo.findById(9L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("manager1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("manager1", null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        String result = deviceService.control(9L, new ControlCommandRequest("lane_navigation", "Z9"), auth);

        assertThat(result).isEqualTo("Command accepted: lane_navigation");
        verify(auditService).log("manager1", "DEVICE_CONTROL", "DEVICE", "9", "lane_navigation");
    }

    @Test
    void historyDefaultsToLast24Hours() {
        when(priceHistoryRepo.findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(any(), any()))
                .thenReturn(List.of());

        deviceService.history(1L, null);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(priceHistoryRepo).findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(any(), fromCaptor.capture());
        assertThat(fromCaptor.getValue()).isAfter(Instant.now().minusSeconds(26 * 3600));
    }
}
