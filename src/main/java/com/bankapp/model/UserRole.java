package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "role_id")
    private Long roleId;

    @Column(
            name = "user_id",
            insertable = false,
            updatable = false
    )
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role userRole;

    @OneToOne
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    public UserRole(Long roleId, Long userId, Role userRole) {
        this.roleId = roleId;
        this.userId = userId;
        this.userRole = userRole;
    }

    public UserRole(Long userId, Role userRole) {
        this.userId = userId;
        this.userRole = userRole;
    }

    @PrePersist
    @PreUpdate
    private void syncUserId() {
        if (user != null) {
            this.userId = user.getUserId();
        }
    }

}
