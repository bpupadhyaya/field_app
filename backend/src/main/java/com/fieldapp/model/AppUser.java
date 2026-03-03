package com.fieldapp.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
@Entity @Table(name = "app_users") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String displayName;
    @Column(nullable = false) private String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserType userType;
    private Long managerId;
    @Builder.Default private boolean enabled = true;
    @Builder.Default private Instant createdAt = Instant.now();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default private Set<Role> roles = new HashSet<>();
}
