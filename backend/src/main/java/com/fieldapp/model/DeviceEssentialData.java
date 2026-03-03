package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "device_essential_data") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceEssentialData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long deviceId;
    private String registrationInfo; private String rentalInfo; private String paymentHistory;
    private String licenseHistory; private String rentalHistory; private String ownershipHistory;
    private String fuelStatus; private String repairHistory;
    @Builder.Default private Instant updatedAt = Instant.now();
}
