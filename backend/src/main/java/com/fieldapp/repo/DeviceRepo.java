package com.fieldapp.repo;
import com.fieldapp.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DeviceRepo extends JpaRepository<Device, Long> {
    List<Device> findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCaseOrTypeContainingIgnoreCase(String c, String n, String t);
}
