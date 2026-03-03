package com.fieldapp.service;

import com.fieldapp.model.UserRuntimeSession;
import com.fieldapp.repo.TrafficMetricRepo;
import com.fieldapp.repo.UserRuntimeSessionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuntimeServiceTest {
    @Mock private UserRuntimeSessionRepo userRuntimeSessionRepo;
    @Mock private TrafficMetricRepo trafficMetricRepo;

    private RuntimeService runtimeService;

    @BeforeEach
    void setUp() {
        runtimeService = new RuntimeService(userRuntimeSessionRepo, trafficMetricRepo);
    }

    @Test
    void markOnlineCreatesSessionWhenMissing() {
        when(userRuntimeSessionRepo.findByUsername("user1")).thenReturn(Optional.empty());
        when(userRuntimeSessionRepo.save(any(UserRuntimeSession.class))).thenAnswer(inv -> inv.getArgument(0));

        runtimeService.markOnline("user1");

        verify(userRuntimeSessionRepo).save(any(UserRuntimeSession.class));
    }

    @Test
    void markOnlineUpdatesExistingSession() {
        UserRuntimeSession s = UserRuntimeSession.builder().username("user1").online(false).build();
        when(userRuntimeSessionRepo.findByUsername("user1")).thenReturn(Optional.of(s));

        runtimeService.markOnline("user1");

        assertThat(s.isOnline()).isTrue();
        verify(userRuntimeSessionRepo).save(s);
    }

    @Test
    void sessionsAndTrafficDelegate() {
        when(userRuntimeSessionRepo.findAll()).thenReturn(List.of());
        when(trafficMetricRepo.findAll()).thenReturn(List.of());

        assertThat(runtimeService.sessions()).isEmpty();
        assertThat(runtimeService.traffic()).isEmpty();
    }
}
