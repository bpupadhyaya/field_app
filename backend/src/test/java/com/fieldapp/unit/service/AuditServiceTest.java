package com.fieldapp.unit.service;

import com.fieldapp.repo.AuditEventRepo;
import com.fieldapp.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {
    @Mock private AuditEventRepo auditEventRepo;

    @Test
    void logSavesAuditEventWithPayload() {
        AuditService auditService = new AuditService(auditEventRepo);

        auditService.log("admin1", "PRICE_UPDATE", "DEVICE", "42", "updated to 200");

        var captor = ArgumentCaptor.forClass(com.fieldapp.model.AuditEvent.class);
        verify(auditEventRepo).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin1");
        assertThat(captor.getValue().getAction()).isEqualTo("PRICE_UPDATE");
        assertThat(captor.getValue().getResourceId()).isEqualTo("42");
    }
}
