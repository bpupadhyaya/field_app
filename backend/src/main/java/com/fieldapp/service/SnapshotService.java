package com.fieldapp.service;

import com.fieldapp.model.SnapshotRecord;
import com.fieldapp.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SnapshotService {
    private final SnapshotRecordRepo snapshotRecordRepo;
    private final DeviceRepo deviceRepo;
    private final AppUserRepo appUserRepo;

    @Value("${app.snapshots.retain-min-count:5}") private int retainMinCount;
    @Value("${app.snapshots.max-age-days:30}") private int maxAgeDays;

    public SnapshotRecord capture(String createdBy) {
        Map<String, Object> payload = Map.of(
                "users", appUserRepo.count(),
                "devices", deviceRepo.count(),
                "capturedAt", Instant.now().toString()
        );
        return snapshotRecordRepo.save(SnapshotRecord.builder().createdBy(createdBy).payloadJson(payload.toString()).build());
    }

    public String restore(Long snapshotId) {
        snapshotRecordRepo.findById(snapshotId).orElseThrow();
        return "Restore simulated for snapshot " + snapshotId;
    }

    public int cleanup(Integer keepMinCountOverride, Integer maxAgeDaysOverride) {
        int keep = keepMinCountOverride == null ? retainMinCount : keepMinCountOverride;
        int age = maxAgeDaysOverride == null ? maxAgeDays : maxAgeDaysOverride;
        var all = snapshotRecordRepo.findAll().stream().sorted(Comparator.comparing(SnapshotRecord::getCreatedAt).reversed()).toList();
        var protectedIds = all.stream().limit(keep).map(SnapshotRecord::getId).toList();
        Instant threshold = Instant.now().minus(age, ChronoUnit.DAYS);
        int deleted = 0;
        for (SnapshotRecord rec : snapshotRecordRepo.findByCreatedAtBefore(threshold)) {
            if (!protectedIds.contains(rec.getId())) {
                snapshotRecordRepo.delete(rec);
                deleted++;
            }
        }
        return deleted;
    }

    public List<SnapshotRecord> list() { return snapshotRecordRepo.findAll(); }
}
