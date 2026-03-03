package com.fieldapp.unit.service;

import com.fieldapp.model.SnapshotRecord;
import com.fieldapp.repo.AppUserRepo;
import com.fieldapp.repo.DeviceRepo;
import com.fieldapp.repo.SnapshotRecordRepo;
import com.fieldapp.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {
    @Mock private SnapshotRecordRepo snapshotRecordRepo;
    @Mock private DeviceRepo deviceRepo;
    @Mock private AppUserRepo appUserRepo;

    private SnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new SnapshotService(snapshotRecordRepo, deviceRepo, appUserRepo);
        ReflectionTestUtils.setField(snapshotService, "retainMinCount", 2);
        ReflectionTestUtils.setField(snapshotService, "maxAgeDays", 30);
    }

    @Test
    void captureBuildsPayloadAndSavesRecord() {
        when(appUserRepo.count()).thenReturn(5L);
        when(deviceRepo.count()).thenReturn(12L);
        when(snapshotRecordRepo.save(any(SnapshotRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        SnapshotRecord out = snapshotService.capture("admin1");

        assertThat(out.getCreatedBy()).isEqualTo("admin1");
        assertThat(out.getPayloadJson()).contains("users=5");
        assertThat(out.getPayloadJson()).contains("devices=12");
    }

    @Test
    void restoreThrowsWhenSnapshotMissing() {
        when(snapshotRecordRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> snapshotService.restore(99L)).isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void restoreReturnsMessageWhenSnapshotExists() {
        when(snapshotRecordRepo.findById(5L)).thenReturn(Optional.of(SnapshotRecord.builder().id(5L).build()));

        String out = snapshotService.restore(5L);

        assertThat(out).isEqualTo("Restore simulated for snapshot 5");
    }

    @Test
    void cleanupDeletesOnlyOldAndNotProtectedSnapshots() {
        Instant now = Instant.now();
        SnapshotRecord latest = SnapshotRecord.builder().id(3L).createdAt(now.minus(1, ChronoUnit.DAYS)).build();
        SnapshotRecord secondLatest = SnapshotRecord.builder().id(2L).createdAt(now.minus(2, ChronoUnit.DAYS)).build();
        SnapshotRecord oldProtected = SnapshotRecord.builder().id(2L).createdAt(now.minus(40, ChronoUnit.DAYS)).build();
        SnapshotRecord oldDeletable = SnapshotRecord.builder().id(1L).createdAt(now.minus(40, ChronoUnit.DAYS)).build();

        when(snapshotRecordRepo.findAll()).thenReturn(List.of(oldDeletable, secondLatest, latest));
        when(snapshotRecordRepo.findByCreatedAtBefore(any())).thenReturn(List.of(oldProtected, oldDeletable));

        int deleted = snapshotService.cleanup(null, null);

        assertThat(deleted).isEqualTo(1);
        verify(snapshotRecordRepo).delete(oldDeletable);
    }

    @Test
    void listDelegatesToRepository() {
        when(snapshotRecordRepo.findAll()).thenReturn(List.of());
        assertThat(snapshotService.list()).isEmpty();
    }

    @Test
    void cleanupSupportsExplicitOverrideValues() {
        Instant now = Instant.now();
        SnapshotRecord latest = SnapshotRecord.builder().id(10L).createdAt(now.minus(1, ChronoUnit.DAYS)).build();
        SnapshotRecord oldDeletable = SnapshotRecord.builder().id(11L).createdAt(now.minus(10, ChronoUnit.DAYS)).build();

        when(snapshotRecordRepo.findAll()).thenReturn(List.of(oldDeletable, latest));
        when(snapshotRecordRepo.findByCreatedAtBefore(any())).thenReturn(List.of(oldDeletable));

        int deleted = snapshotService.cleanup(1, 1);

        assertThat(deleted).isEqualTo(1);
        verify(snapshotRecordRepo).delete(oldDeletable);
    }
}
