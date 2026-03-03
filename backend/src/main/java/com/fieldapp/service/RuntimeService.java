package com.fieldapp.service;

import com.fieldapp.model.UserRuntimeSession;
import com.fieldapp.repo.TrafficMetricRepo;
import com.fieldapp.repo.UserRuntimeSessionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuntimeService {
    private final UserRuntimeSessionRepo userRuntimeSessionRepo;
    private final TrafficMetricRepo trafficMetricRepo;

    public void markOnline(String username) {
        var session = userRuntimeSessionRepo.findByUsername(username).orElse(UserRuntimeSession.builder()
                .username(username).cloud("AWS").region("us-east-1").zone("use1-az1").dataCenter("iad-1").online(true).build());
        session.setOnline(true);
        session.setUpdatedAt(Instant.now());
        userRuntimeSessionRepo.save(session);
    }

    public List<UserRuntimeSession> sessions() { return userRuntimeSessionRepo.findAll(); }
    public List<?> traffic() { return trafficMetricRepo.findAll(); }
}
