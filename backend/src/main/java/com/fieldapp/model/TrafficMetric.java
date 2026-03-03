package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "traffic_metrics") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrafficMetric {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String cloud;
    @Column(nullable = false) private String region;
    @Column(nullable = false) private String zone;
    private int activeUsers; private int requestsPerSecond; private int avgLatencyMs;
    @Builder.Default private Instant sampledAt = Instant.now();
}
