package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "audit_events") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String resourceType;
    private String resourceId; private String details;
    @Builder.Default private Instant createdAt = Instant.now();
}
