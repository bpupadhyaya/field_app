package com.fieldapp.controller;

import com.fieldapp.dto.CreateUserRequest;
import com.fieldapp.model.AppUser;
import com.fieldapp.model.Role;
import com.fieldapp.model.RoleName;
import com.fieldapp.model.UserType;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.RoleRepo;
import com.fieldapp.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock private AppUserRepo appUserRepo;
    @Mock private RoleRepo roleRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(appUserRepo, roleRepo, passwordEncoder, auditService);
    }

    @Test
    void listReturnsAllWhenQueryBlank() {
        when(appUserRepo.findAll()).thenReturn(List.of());
        assertThat(userController.list(" ")).isEqualTo(List.of());
    }

    @Test
    void listReturnsAllWhenQueryNull() {
        when(appUserRepo.findAll()).thenReturn(List.of());
        assertThat(userController.list(null)).isEqualTo(List.of());
    }

    @Test
    void listFiltersByUsernameOrDisplayName() {
        AppUser u1 = AppUser.builder().username("user1").displayName("User One").build();
        AppUser u2 = AppUser.builder().username("admin1").displayName("Admin One").build();
        when(appUserRepo.findAll()).thenReturn(List.of(u1, u2));

        Object out = userController.list("admin");

        assertThat((List<?>) out).hasSize(1);
    }

    @Test
    void listFiltersByDisplayNameCaseInsensitive() {
        AppUser u1 = AppUser.builder().username("user1").displayName("User One").build();
        AppUser u2 = AppUser.builder().username("alpha").displayName("Manager One").build();
        when(appUserRepo.findAll()).thenReturn(List.of(u1, u2));

        Object out = userController.list("manager");

        assertThat((List<?>) out).hasSize(1);
    }

    @Test
    void listReturnsEmptyWhenNoUsersMatchQuery() {
        AppUser u1 = AppUser.builder().username("user1").displayName("User One").build();
        AppUser u2 = AppUser.builder().username("alpha").displayName("Manager One").build();
        when(appUserRepo.findAll()).thenReturn(List.of(u1, u2));

        Object out = userController.list("zzz");

        assertThat((List<?>) out).isEmpty();
    }

    @Test
    void createBuildsAndSavesUser() {
        Role adminRole = Role.builder().name(RoleName.ROLE_ADMIN).build();
        when(appUserRepo.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(roleRepo.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(appUserRepo.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });

        AppUser out = userController.create(new CreateUserRequest(
                "newadmin",
                "pw",
                "New Admin",
                "newadmin@x.local",
                UserType.EMPLOYEE,
                Set.of("admin"),
                null
        ));

        assertThat(out.getUsername()).isEqualTo("newadmin");
        verify(auditService).log("system", "USER_CREATE", "USER", "9", "newadmin");
    }

    @Test
    void createRejectsDuplicateUsername() {
        when(appUserRepo.findByUsername("admin1")).thenReturn(Optional.of(AppUser.builder().username("admin1").build()));

        assertThatThrownBy(() -> userController.create(new CreateUserRequest(
                "admin1", "pw", "Admin", "a@x", UserType.EMPLOYEE, Set.of("admin"), null
        ))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
    }

    @Test
    void createRejectsInvalidRole() {
        when(appUserRepo.findByUsername("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.create(new CreateUserRequest(
                "x", "pw", "X", "x@x.local", UserType.EMPLOYEE, Set.of("notarole"), null
        ))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid role");
    }

    @Test
    void createAcceptsPrefixedRoleNames() {
        Role adminRole = Role.builder().name(RoleName.ROLE_ADMIN).build();
        when(appUserRepo.findByUsername("x2")).thenReturn(Optional.empty());
        when(roleRepo.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(appUserRepo.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        AppUser out = userController.create(new CreateUserRequest(
                "x2", "pw", "X2", "x2@x.local", UserType.EMPLOYEE, Set.of("ROLE_ADMIN"), null
        ));

        assertThat(out.getUsername()).isEqualTo("x2");
    }

    @Test
    void createRejectsBlankRole() {
        when(appUserRepo.findByUsername("x3")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.create(new CreateUserRequest(
                "x3", "pw", "X3", "x3@x.local", UserType.EMPLOYEE, Set.of(" "), null
        ))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Role cannot be blank");
    }

    @Test
    void createRejectsNullRole() {
        when(appUserRepo.findByUsername("x5")).thenReturn(Optional.empty());
        Set<String> roles = new LinkedHashSet<>();
        roles.add(null);

        assertThatThrownBy(() -> userController.create(new CreateUserRequest(
                "x5", "pw", "X5", "x5@x.local", UserType.EMPLOYEE, roles, null
        ))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Role cannot be blank");
    }

    @Test
    void createDefaultsUserTypeToEmployeeWhenNull() {
        Role userRole = Role.builder().name(RoleName.ROLE_USER).build();
        when(appUserRepo.findByUsername("x4")).thenReturn(Optional.empty());
        when(roleRepo.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(appUserRepo.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser u = inv.getArgument(0);
            u.setId(11L);
            return u;
        });

        AppUser out = userController.create(new CreateUserRequest(
                "x4", "pw", "X4", "x4@x.local", null, Set.of("user"), null
        ));

        assertThat(out.getUserType()).isEqualTo(UserType.EMPLOYEE);
    }

    @Test
    void whoamiReturnsPrincipalInfo() {
        AppUser u = AppUser.builder().username("user1").displayName("User One").build();
        when(appUserRepo.findByUsername("user1")).thenReturn(Optional.of(u));
        var auth = new UsernamePasswordAuthenticationToken(
                "user1",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        var out = userController.whoami(auth);

        assertThat(out).containsEntry("username", "user1");
        assertThat(out).containsEntry("displayName", "User One");
    }
}
