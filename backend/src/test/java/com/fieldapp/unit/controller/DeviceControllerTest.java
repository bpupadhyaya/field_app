package com.fieldapp.unit.controller;

import com.fieldapp.controller.DeviceController;
import com.fieldapp.dto.ControlCommandRequest;
import com.fieldapp.dto.UpdatePriceRequest;
import com.fieldapp.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {
    @Mock private DeviceService deviceService;
    private DeviceController deviceController;

    @BeforeEach
    void setUp() {
        deviceController = new DeviceController(deviceService);
    }

    @Test
    void delegatesListHealthEssentialAndHistory() {
        deviceController.list("tractor");
        deviceController.health(1L);
        deviceController.essential(1L);
        deviceController.priceHistory(1L, "7d");

        verify(deviceService).list("tractor");
        verify(deviceService).health(1L);
        verify(deviceService).essential(1L);
        verify(deviceService).history(1L, "7d");
    }

    @Test
    void controlReturnsMessageMap() {
        var auth = new UsernamePasswordAuthenticationToken("user1", null, List.of());
        when(deviceService.control(2L, new ControlCommandRequest("move", "Z1"), auth)).thenReturn("Command accepted: move");

        Map<String, String> result = deviceController.control(2L, new ControlCommandRequest("move", "Z1"), auth);

        assertThat(result).containsEntry("message", "Command accepted: move");
    }

    @Test
    void updatePricePassesAuthenticatedUser() {
        var auth = new UsernamePasswordAuthenticationToken("admin1", null, List.of());
        UpdatePriceRequest req = new UpdatePriceRequest(BigDecimal.TEN);

        deviceController.updatePrice(3L, req, auth);

        verify(deviceService).updatePrice(3L, BigDecimal.TEN, "admin1");
    }
}
