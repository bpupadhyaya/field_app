package com.fieldapp.service;

import com.fieldapp.model.AuditEvent;
import com.fieldapp.repo.AuditEventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditEventRepo auditEventRepo;

    public void log(String username, String action, String resourceType, String resourceId, String details) {
        auditEventRepo.save(AuditEvent.builder().username(username).action(action).resourceType(resourceType).resourceId(resourceId).details(details).build());
    }
}
