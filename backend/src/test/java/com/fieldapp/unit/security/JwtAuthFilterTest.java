package com.fieldapp.unit.security;

import com.fieldapp.security.JwtAuthFilter;
import com.fieldapp.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private ExposedJwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new ExposedJwtAuthFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsWhenAuthorizationHeaderMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.invoke(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void skipsWhenAuthorizationHeaderIsNotBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.invoke(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void skipsWhenTokenParsingFails() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtService.extractUsername("bad-token")).thenThrow(new RuntimeException("bad"));

        filter.invoke(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void authenticatesWhenTokenIsValidAndContextIsEmpty() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer good-token");
        when(jwtService.extractUsername("good-token")).thenReturn("admin1");
        var userDetails = User.withUsername("admin1").password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        when(userDetailsService.loadUserByUsername("admin1")).thenReturn(userDetails);

        filter.invoke(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername("admin1");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotReauthenticateWhenContextAlreadySet() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
        when(request.getHeader("Authorization")).thenReturn("Bearer good-token");
        when(jwtService.extractUsername("good-token")).thenReturn("admin1");

        filter.invoke(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername("admin1");
        verify(filterChain).doFilter(request, response);
    }

    private static final class ExposedJwtAuthFilter extends JwtAuthFilter {
        private ExposedJwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
            super(jwtService, userDetailsService);
        }

        private void invoke(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            doFilterInternal(request, response, filterChain);
        }
    }
}
