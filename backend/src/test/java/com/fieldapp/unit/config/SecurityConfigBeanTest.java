package com.fieldapp.unit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigBeanTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void securityBeansAreConfigured() {
        assertThat(securityFilterChain).isNotNull();
        assertThat(authenticationProvider).isNotNull();
        assertThat(authenticationManager).isNotNull();
        assertThat(passwordEncoder.matches("admin123", passwordEncoder.encode("admin123"))).isTrue();
    }
}
