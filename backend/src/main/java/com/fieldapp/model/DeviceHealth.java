package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "device_health") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceHealth {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long deviceId;
    private int engineTotalHours; private int engineUsedHours; private int batteryHealthPercent;
    private int wheelWearPercent; private int wheelPressurePsi; private int cabinTempC;
    private int chassisTempC; private int roboticArmsHealthPercent; private int connectivityHealthPercent;
    @Builder.Default private Instant capturedAt = Instant.now();
}
