package com.fieldapp.controller;

import com.fieldapp.dto.CreateUserRequest;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.RoleName;
import com.fieldapp.model.UserType;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.RoleRepo;
import com.fieldapp.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AppUserRepo appUserRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MANAGER')")
    public Object list(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) return appUserRepo.findAll();
        return appUserRepo.findAll().stream().filter(u -> u.getUsername().contains(q) || u.getDisplayName().toLowerCase().contains(q.toLowerCase())).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public AppUser create(@Valid @RequestBody CreateUserRequest req) {
        if (appUserRepo.findByUsername(req.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        var roles = new HashSet<>(req.roles().stream()
                .map(this::parseRoleName)
                .map(role -> roleRepo.findByName(role).orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role)))
                .collect(Collectors.toSet()));
        var user = AppUser.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName())
                .email(req.email())
                .userType(req.userType() == null ? UserType.EMPLOYEE : req.userType())
                .managerId(req.managerId())
                .roles(roles)
                .build();
        var saved = appUserRepo.save(user);
        auditService.log("system", "USER_CREATE", "USER", saved.getId().toString(), saved.getUsername());
        return saved;
    }

    private RoleName parseRoleName(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("Role cannot be blank");
        }
        String normalized = rawRole.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        try {
            return RoleName.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + rawRole);
        }
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(org.springframework.security.core.Authentication auth) {
        var user = appUserRepo.findByUsername(auth.getName()).orElseThrow();
        return Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "roles", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        );
    }
}
