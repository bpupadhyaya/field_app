package com.fieldapp.controller;

import com.fieldapp.dto.SnapshotCleanupRequest;
import com.fieldapp.model.SnapshotRecord;
import com.fieldapp.model.UserRuntimeSession;
import com.fieldapp.repo.AuditEventRepo;
import com.fieldapp.service.RuntimeService;
import com.fieldapp.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock private SnapshotService snapshotService;
    @Mock private RuntimeService runtimeService;
    @Mock private AuditEventRepo auditEventRepo;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(snapshotService, runtimeService, auditEventRepo);
    }

    @Test
    void createSnapshotUsesAuthenticatedName() {
        var auth = new UsernamePasswordAuthenticationToken("admin1", null, List.of());
        SnapshotRecord snapshot = SnapshotRecord.builder().id(1L).createdBy("admin1").payloadJson("{}").build();
        when(snapshotService.capture("admin1")).thenReturn(snapshot);

        Object out = adminController.createSnapshot(auth);

        assertThat(out).isEqualTo(snapshot);
    }

    @Test
    void restoreAndCleanupReturnExpectedMaps() {
        when(snapshotService.restore(12L)).thenReturn("ok");
        when(snapshotService.cleanup(5, 30)).thenReturn(3);

        Map<String, Object> restore = adminController.restore(12L);
        Map<String, Object> cleanup = adminController.cleanup(new SnapshotCleanupRequest(5, 30));

        assertThat(restore).containsEntry("message", "ok");
        assertThat(cleanup).containsEntry("deleted", 3);
    }

    @Test
    void listRuntimeTrafficAndAuditDelegate() {
        SnapshotRecord snapshot = SnapshotRecord.builder().id(1L).createdBy("a").payloadJson("{}").build();
        UserRuntimeSession session = UserRuntimeSession.builder().username("u").cloud("AWS").region("us-east-1").zone("z1").dataCenter("dc").build();
        Object traffic = java.util.Map.of("cloud", "AWS");
        when(snapshotService.list()).thenReturn(List.of(snapshot));
        when(runtimeService.sessions()).thenReturn(List.of(session));
        org.mockito.Mockito.doReturn(List.of(traffic)).when(runtimeService).traffic();
        when(auditEventRepo.findAll()).thenReturn(List.of());

        assertThat(adminController.listSnapshots()).isEqualTo(List.of(snapshot));
        assertThat(adminController.runtimeSessions()).isEqualTo(List.of(session));
        assertThat(adminController.traffic()).isEqualTo(List.of(traffic));
        assertThat(adminController.audit()).isEqualTo(List.of());

        verify(auditEventRepo).findAll();
    }
}
