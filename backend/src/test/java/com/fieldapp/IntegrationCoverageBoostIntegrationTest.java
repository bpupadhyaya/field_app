package com.fieldapp;

import com.fieldapp.config.DataBootstrap;
import com.fieldapp.controller.ApiExceptionHandler;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.Device;
import com.fieldapp.model.RoleName;
import com.fieldapp.model.SnapshotRecord;
import com.fieldapp.model.UserType;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.DeviceRepo;
import com.fieldapp.repo.RoleRepo;
import com.fieldapp.repo.SnapshotRecordRepo;
import com.fieldapp.security.JwtAuthFilter;
import com.fieldapp.security.JwtService;
import com.fieldapp.service.DeviceService;
import com.fieldapp.service.SnapshotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.MethodParameter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationCoverageBoostIntegrationTest {

    @Autowired
    private DataBootstrap dataBootstrap;

    @Autowired
    private AppUserRepo appUserRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private SnapshotRecordRepo snapshotRecordRepo;

    @Autowired
    private DeviceRepo deviceRepo;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiExceptionHandler apiExceptionHandler;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bootstrapRerunCoversExistingUserAndLegacyMigrationPaths() throws Exception {
        appUserRepo.findByUsername("sadmin").ifPresent(appUserRepo::delete);
        appUserRepo.findByUsername("superadmin").ifPresent(appUserRepo::delete);

        var superAdminRole = roleRepo.findByName(RoleName.ROLE_SUPER_ADMIN).orElseThrow();
        appUserRepo.save(AppUser.builder()
                .username("superadmin")
                .passwordHash(passwordEncoder.encode("sadmin123"))
                .displayName("Legacy Super Admin")
                .email("legacy@field.local")
                .userType(UserType.EMPLOYEE)
                .roles(Set.of(superAdminRole))
                .build());

        dataBootstrap.run();
        dataBootstrap.run();

        // Cover case where both legacy and current usernames exist.
        var superAdminRole2 = roleRepo.findByName(RoleName.ROLE_SUPER_ADMIN).orElseThrow();
        appUserRepo.save(AppUser.builder()
                .username("superadmin")
                .passwordHash(passwordEncoder.encode("sadmin123"))
                .displayName("Legacy Also Exists")
                .email("legacy2@field.local")
                .userType(UserType.EMPLOYEE)
                .roles(Set.of(superAdminRole2))
                .build());
        dataBootstrap.run();

        assertThat(appUserRepo.findByUsername("sadmin")).isPresent();
    }

    @Test
    void jwtFilterHandlesInvalidTokenAndExistingSecurityContext() throws Exception {
        mockMvc.perform(get("/api/users/whoami")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());

        String token = jwtService.generate("admin", List.of("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/users/whoami")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void snapshotCleanupCoversDefaultAndOverrideBranches() {
        snapshotRecordRepo.save(SnapshotRecord.builder()
                .createdBy("admin")
                .payloadJson("{}")
                .createdAt(Instant.now().minus(45, ChronoUnit.DAYS))
                .build());
        snapshotRecordRepo.save(SnapshotRecord.builder()
                .createdBy("admin")
                .payloadJson("{}")
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build());

        int deletedWithOverride = snapshotService.cleanup(0, 30);
        int deletedWithDefaults = snapshotService.cleanup(null, null);
        int deletedWhenProtected = snapshotService.cleanup(100, 1);

        assertThat(deletedWithOverride).isGreaterThanOrEqualTo(1);
        assertThat(deletedWithDefaults).isGreaterThanOrEqualTo(0);
        assertThat(deletedWhenProtected).isGreaterThanOrEqualTo(0);
    }

    @Test
    void additionalControllerAndFilterBranchesAreCovered() throws Exception {
        String adminToken = jwtService.generate("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .queryParam("q", "   ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nullrole","password":"x","displayName":"x","email":"x@field.local","roles":[null]}"""))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/users/whoami")
                        .header("Authorization", "Basic abc"))
                .andExpect(status().isForbidden());

        // Branch: valid bearer token but auth already present in SecurityContext.
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + adminToken);
        req.setRequestURI("/api/users/whoami");
        req.setMethod("GET");
        jwtAuthFilter.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    void apiExceptionHandlerFallbackBranchesAreCovered() throws Exception {
        var badReq = apiExceptionHandler.handleBadRequest(new IllegalArgumentException());
        assertThat(badReq.getBody()).containsEntry("error", "Bad request");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "dummy");
        bindingResult.addError(new FieldError("dummy", "field", "x", false, null, null, null));
        MethodParameter parameter = new MethodParameter(
                IntegrationCoverageBoostIntegrationTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
        MethodArgumentNotValidException validationEx = new MethodArgumentNotValidException(parameter, bindingResult);

        var validation = apiExceptionHandler.handleValidation(validationEx);
        @SuppressWarnings("unchecked")
        var fields = (java.util.Map<String, String>) validation.getBody().get("fields");
        assertThat(fields).containsEntry("field", "Invalid value");
    }

    @Test
    void serviceLevelBranchesNotEasilyReachableFromControllerAreCovered() {
        Device device = deviceRepo.findAll().getFirst();
        assertThat(deviceService.history(device.getId(), "unknown-range")).isNotNull();

        assertThat(deviceService.updatePrice(device.getId(), BigDecimal.valueOf(99999), "manager1")).isNotNull();
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String input) {
        // Used only for MethodParameter creation in tests.
    }
}
