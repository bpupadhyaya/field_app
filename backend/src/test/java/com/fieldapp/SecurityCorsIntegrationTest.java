package com.fieldapp;

import com.fieldapp.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:5174,, http://example.com, ")
@ActiveProfiles("test")
class SecurityCorsIntegrationTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Test
    void corsConfigurationFiltersBlankOrigins() {
        var source = securityConfig.corsConfigurationSource();
        var config = source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:5174", "http://example.com");
        assertThat(config.getAllowedOrigins()).doesNotContain("");
    }

}
