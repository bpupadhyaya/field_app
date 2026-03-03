package com.fieldapp.controller;

import com.fieldapp.dto.ControlCommandRequest;
import com.fieldapp.dto.UpdatePriceRequest;
import com.fieldapp.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    public Object list(@RequestParam(required = false) String q) { return deviceService.list(q); }

    @GetMapping("/{id}/health")
    public Object health(@PathVariable Long id) { return deviceService.health(id); }

    @GetMapping("/{id}/essential")
    public Object essential(@PathVariable Long id) { return deviceService.essential(id); }

    @PostMapping("/{id}/control")
    public Map<String, String> control(@PathVariable Long id, @Valid @RequestBody ControlCommandRequest req, Authentication auth) {
        return Map.of("message", deviceService.control(id, req, auth));
    }

    @PatchMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MANAGER')")
    public Object updatePrice(@PathVariable Long id, @Valid @RequestBody UpdatePriceRequest req, Authentication auth) {
        return deviceService.updatePrice(id, req.price(), auth.getName());
    }

    @GetMapping("/{id}/price-history")
    public Object priceHistory(@PathVariable Long id, @RequestParam(defaultValue = "24h") String range) {
        return deviceService.history(id, range);
    }
}
