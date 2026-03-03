package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name = "device_price_history") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DevicePriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long deviceId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Builder.Default private Instant capturedAt = Instant.now();
}
