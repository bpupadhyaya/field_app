package com.fieldapp.unit.controller;

import com.fieldapp.controller.AuthController;
import com.fieldapp.dto.LoginRequest;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.Role;
import com.fieldapp.model.RoleName;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.security.JwtService;
import com.fieldapp.service.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private AppUserRepo appUserRepo;
    @Mock private JwtService jwtService;
    @Mock private RuntimeService runtimeService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authenticationManager, appUserRepo, jwtService, runtimeService);
    }

    @Test
    void rootAndHealthReturnExpectedPayloads() {
        var root = authController.root();
        var health = authController.health();

        assertThat(root).containsEntry("message", "Field App backend is running");
        assertThat(root).containsEntry("health", "/api/health");
        assertThat(health).containsEntry("status", "ok");
        assertThat(health).containsEntry("service", "field-app-backend");
        assertThat(health.get("time")).isInstanceOf(String.class);
    }

    @Test
    void loginAuthenticatesBuildsTokenAndMarksUserOnline() {
        Role role = Role.builder().name(RoleName.ROLE_ADMIN).build();
        AppUser user = AppUser.builder()
                .username("admin1")
                .displayName("Admin One")
                .roles(Set.of(role))
                .build();
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin1", null));
        when(appUserRepo.findByUsername("admin1")).thenReturn(Optional.of(user));
        when(jwtService.generate("admin1", java.util.List.of("ROLE_ADMIN"))).thenReturn("jwt-token");

        var response = authController.login(new LoginRequest("admin1", "admin123"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("jwt-token");
        assertThat(response.getBody().username()).isEqualTo("admin1");
        assertThat(response.getBody().displayName()).isEqualTo("Admin One");
        assertThat(response.getBody().roles()).containsExactly("ROLE_ADMIN");
        verify(runtimeService).markOnline("admin1");
    }
}
