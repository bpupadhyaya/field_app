package com.fieldapp.repo;
import com.fieldapp.model.DeviceHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DeviceHealthRepo extends JpaRepository<DeviceHealth, Long> { Optional<DeviceHealth> findTopByDeviceIdOrderByCapturedAtDesc(Long deviceId); }
