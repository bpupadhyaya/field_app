package com.fieldapp.controller;

import com.fieldapp.dto.SnapshotCleanupRequest;
import com.fieldapp.repo.AuditEventRepo;
import com.fieldapp.service.RuntimeService;
import com.fieldapp.service.SnapshotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminController {
    private final SnapshotService snapshotService;
    private final RuntimeService runtimeService;
    private final AuditEventRepo auditEventRepo;

    @PostMapping("/snapshots")
    public Object createSnapshot(org.springframework.security.core.Authentication auth) { return snapshotService.capture(auth.getName()); }

    @GetMapping("/snapshots")
    public Object listSnapshots() { return snapshotService.list(); }

    @PostMapping("/snapshots/{id}/restore")
    public Map<String, Object> restore(@PathVariable Long id) { return Map.of("message", snapshotService.restore(id)); }

    @PostMapping("/snapshots/cleanup")
    public Map<String, Object> cleanup(@Valid @RequestBody SnapshotCleanupRequest req) {
        return Map.of("deleted", snapshotService.cleanup(req.keepMinCount(), req.maxAgeDays()));
    }

    @GetMapping("/runtime")
    public Object runtimeSessions() { return runtimeService.sessions(); }

    @GetMapping("/traffic")
    public Object traffic() { return runtimeService.traffic(); }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Object audit() { return auditEventRepo.findAll(); }
}
