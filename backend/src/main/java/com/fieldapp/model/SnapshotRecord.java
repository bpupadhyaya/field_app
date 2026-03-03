package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "snapshot_records") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SnapshotRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String createdBy;
    @Column(nullable = false, length = 20000) private String payloadJson;
    @Builder.Default private Instant createdAt = Instant.now();
}
