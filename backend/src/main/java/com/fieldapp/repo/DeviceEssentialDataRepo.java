package com.fieldapp.repo;
import com.fieldapp.model.DeviceEssentialData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DeviceEssentialDataRepo extends JpaRepository<DeviceEssentialData, Long> { Optional<DeviceEssentialData> findByDeviceId(Long deviceId); }
