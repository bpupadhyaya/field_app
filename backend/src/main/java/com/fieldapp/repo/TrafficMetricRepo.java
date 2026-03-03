package com.fieldapp.repo;
import com.fieldapp.model.TrafficMetric;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TrafficMetricRepo extends JpaRepository<TrafficMetric, Long> {}
