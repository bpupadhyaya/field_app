package com.fieldapp.security;

import com.fieldapp.model.AppUser;
import com.fieldapp.model.Role;
import com.fieldapp.model.RoleName;
import com.fieldapp.repo.AppUserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private AppUserRepo appUserRepo;

    @Test
    void loadUserByUsernameBuildsSpringSecurityUser() {
        Role role = Role.builder().name(RoleName.ROLE_MANAGER).build();
        AppUser user = AppUser.builder()
                .username("manager1")
                .passwordHash("hash")
                .enabled(true)
                .roles(Set.of(role))
                .build();
        when(appUserRepo.findByUsername("manager1")).thenReturn(Optional.of(user));

        CustomUserDetailsService service = new CustomUserDetailsService(appUserRepo);
        var out = service.loadUserByUsername("manager1");

        assertThat(out.getUsername()).isEqualTo("manager1");
        assertThat(out.getAuthorities()).extracting("authority").containsExactly("ROLE_MANAGER");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(appUserRepo.findByUsername("missing")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(appUserRepo);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
