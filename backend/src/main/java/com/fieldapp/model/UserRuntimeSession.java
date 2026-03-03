package com.fieldapp.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "user_runtime_sessions") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRuntimeSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private String dataCenter;
    @Column(nullable = false) private String cloud;
    @Column(nullable = false) private String region;
    @Column(nullable = false) private String zone;
    @Builder.Default private boolean online = true;
    @Builder.Default private Instant updatedAt = Instant.now();
}
