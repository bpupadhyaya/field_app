package com.fieldapp.controller;

import com.fieldapp.dto.LoginRequest;
import com.fieldapp.dto.LoginResponse;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.security.JwtService;
import com.fieldapp.service.RuntimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AppUserRepo appUserRepo;
    private final JwtService jwtService;
    private final RuntimeService runtimeService;

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of("message", "Field App backend is running", "health", "/api/health");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "field-app-backend", "time", java.time.Instant.now().toString());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = appUserRepo.findByUsername(request.username()).orElseThrow();
        var roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        String token = jwtService.generate(user.getUsername(), roles.stream().toList());
        runtimeService.markOnline(user.getUsername());
        return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getDisplayName(), roles));
    }
}
