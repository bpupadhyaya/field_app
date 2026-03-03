package com.fieldapp.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generateAndExtractUsernameRoundTrip() {
        JwtService jwtService = new JwtService("test_secret_test_secret_test_secret_test_secret_123456", 60);

        String token = jwtService.generate("admin1", List.of("ROLE_ADMIN"));
        String username = jwtService.extractUsername(token);

        assertThat(token).isNotBlank();
        assertThat(username).isEqualTo("admin1");
    }
}
