package com.fieldapp.repo;
import com.fieldapp.model.DevicePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface DevicePriceHistoryRepo extends JpaRepository<DevicePriceHistory, Long> {
    List<DevicePriceHistory> findByDeviceIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(Long deviceId, Instant from);
}
