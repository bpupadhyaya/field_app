package com.fieldapp.config;

import com.fieldapp.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsConfigurationSourceFiltersBlankOrigins() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthFilter.class), mock(UserDetailsService.class));
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", " http://localhost:5173, ,https://field.app ");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/site/navigation"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:5173", "https://field.app");
    }

    @Test
    void corsConfigurationSourceCanProduceEmptyAllowedOriginsList() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthFilter.class), mock(UserDetailsService.class));
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", " , ");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/health"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).isEqualTo(List.of());
    }
}
