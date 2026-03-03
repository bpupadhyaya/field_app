package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name = "devices") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String deviceCode;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String category;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String locationName;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private String zoneId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DeviceStatus status;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal currentPrice;
    private Long managerUserId;
    @Builder.Default private Instant updatedAt = Instant.now();
}
