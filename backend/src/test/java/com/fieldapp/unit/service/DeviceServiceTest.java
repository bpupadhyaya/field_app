package com.fieldapp.unit.service;

import com.fieldapp.dto.ControlCommandRequest;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.Device;
import com.fieldapp.model.DeviceEssentialData;
import com.fieldapp.model.DeviceHealth;
import com.fieldapp.model.DevicePriceHistory;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.DeviceEssentialDataRepo;
import com.fieldapp.repo.DeviceHealthRepo;
import com.fieldapp.repo.DevicePriceHistoryRepo;
import com.fieldapp.repo.DeviceRepo;
import com.fieldapp.service.AuditService;
import com.fieldapp.service.DeviceService;
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
import static org.mockito.Mockito.times;
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
    void listWithNullQueryReturnsAll() {
        when(deviceRepo.findAll()).thenReturn(List.of());

        deviceService.list(null);

        verify(deviceRepo).findAll();
    }

    @Test
    void listWithQueryUsesSearchRepositoryMethod() {
        when(deviceRepo.findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCaseOrTypeContainingIgnoreCase("tractor", "tractor", "tractor"))
                .thenReturn(List.of());

        deviceService.list("tractor");

        verify(deviceRepo).findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCaseOrTypeContainingIgnoreCase("tractor", "tractor", "tractor");
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
    void controlAcceptsAdminCommandAndWritesAudit() {
        Device device = Device.builder().id(10L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("admin1").build();
        when(deviceRepo.findById(10L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("admin1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("admin1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String result = deviceService.control(10L, new ControlCommandRequest("stop", null), auth);

        assertThat(result).isEqualTo("Command accepted: stop");
        verify(auditService).log("admin1", "DEVICE_CONTROL", "DEVICE", "10", "stop");
    }

    @Test
    void controlAcceptsSuperAdminCommandAndWritesAudit() {
        Device device = Device.builder().id(11L).zoneId("Z2").build();
        AppUser user = AppUser.builder().username("sadmin").build();
        when(deviceRepo.findById(11L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("sadmin")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("sadmin", null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));

        String result = deviceService.control(11L, new ControlCommandRequest("resume", null), auth);

        assertThat(result).isEqualTo("Command accepted: resume");
        verify(auditService).log("sadmin", "DEVICE_CONTROL", "DEVICE", "11", "resume");
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

    @Test
    void historySupportsSevenThirtyAndNinetyDayRanges() {
        when(priceHistoryRepo.findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(any(), any()))
                .thenReturn(List.of());

        deviceService.history(1L, "7d");
        deviceService.history(1L, "30d");
        deviceService.history(1L, "90d");

        verify(priceHistoryRepo, times(3)).findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(any(), any());
    }

    @Test
    void controlRejectsRegularUserWhenZoneIsMissing() {
        Device device = Device.builder().id(6L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("user1").build();
        when(deviceRepo.findById(6L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("user1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThatThrownBy(() -> deviceService.control(6L, new ControlCommandRequest("move", null), auth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("restricted to assigned zone");
    }

    @Test
    void controlAllowsRegularUserWithMatchingZoneAndSafeCommand() {
        Device device = Device.builder().id(7L).zoneId("Z1").build();
        AppUser user = AppUser.builder().username("user1").build();
        when(deviceRepo.findById(7L)).thenReturn(Optional.of(device));
        when(appUserRepo.findByUsername("user1")).thenReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String result = deviceService.control(7L, new ControlCommandRequest("move_forward", "Z1"), auth);

        assertThat(result).isEqualTo("Command accepted: move_forward");
        verify(auditService).log("user1", "DEVICE_CONTROL", "DEVICE", "7", "move_forward");
    }

    @Test
    void healthReturnsLatestOrNull() {
        DeviceHealth rec = DeviceHealth.builder().deviceId(1L).build();
        when(deviceHealthRepo.findTopByDeviceIdOrderByCapturedAtDesc(1L)).thenReturn(Optional.of(rec));
        when(deviceHealthRepo.findTopByDeviceIdOrderByCapturedAtDesc(2L)).thenReturn(Optional.empty());

        assertThat(deviceService.health(1L)).isEqualTo(rec);
        assertThat(deviceService.health(2L)).isNull();
    }

    @Test
    void essentialReturnsRecordOrNull() {
        DeviceEssentialData rec = DeviceEssentialData.builder().deviceId(1L).build();
        when(deviceEssentialDataRepo.findByDeviceId(1L)).thenReturn(Optional.of(rec));
        when(deviceEssentialDataRepo.findByDeviceId(2L)).thenReturn(Optional.empty());

        assertThat(deviceService.essential(1L)).isEqualTo(rec);
        assertThat(deviceService.essential(2L)).isNull();
    }
}
