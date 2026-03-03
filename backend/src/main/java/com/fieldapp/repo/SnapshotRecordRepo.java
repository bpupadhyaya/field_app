package com.fieldapp.repo;
import com.fieldapp.model.SnapshotRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface SnapshotRecordRepo extends JpaRepository<SnapshotRecord, Long> {
    List<SnapshotRecord> findByCreatedAtBefore(Instant t);
}
