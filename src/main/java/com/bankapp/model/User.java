package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {
        "userAddresses",
        "role",
        "employmentProfile",
        "userRole",
        "accounts",
        "passwordHash"
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private Long userId;

    @Column(name = "username", unique = true, nullable = false, length = 100,
            insertable = false, updatable = false)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "email_address", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "nationality", nullable = false, length = 100)
    private String nationality;

    @Column(name = "passport_number", unique = true, nullable = false, length = 50)
    private String passportNumber;

    @Column(name = "phone_number", unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch =  FetchType.LAZY
    )
    private List<UserAddress> userAddresses = new ArrayList<>();

    @Transient
    private Role role;

    @Setter(AccessLevel.NONE)
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private UserRole userRole;

    @Setter(AccessLevel.NONE)
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private EmploymentProfile employmentProfile;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Account> accounts = new HashSet<>();

    public User(Long userId, String username, String passwordHash, String firstName, String lastName,
                LocalDate dateOfBirth, Gender gender, String email, String nationality, String passportNumber,
                String phoneNumber, List<UserAddress> userAddresses, Role role, EmploymentProfile employmentProfile) {

        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
        this.userAddresses = userAddresses;
        this.role = role;
        this.employmentProfile = employmentProfile;
    }

    public User(Long userId, String username, String firstName, String lastName,
                LocalDate dateOfBirth, Gender gender, String email, String nationality,
                String passportNumber, String phoneNumber) {

        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
        this.userAddresses = new ArrayList<>();
        this.role = null;
    }

    public User(Long userId, String firstName, String lastName, LocalDate dateOfBirth,
                Gender gender, String email, String nationality, String passportNumber,
                String phoneNumber) {

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    public User (String firstName, String lastName, LocalDate dateOfBirth, Gender gender,
                 String email, String nationality, String passportNumber, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    public void setEmploymentProfile(EmploymentProfile profile) {
        this.employmentProfile = profile;

        if (profile != null) {
            profile.setUser(this);
        }
    }


    public void addAddress(UserAddress address) {
        userAddresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(UserAddress address) {
        userAddresses.remove(address);
        address.setUser(null);
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;

        if (userRole != null) {
            userRole.setUser(this);
            this.role = userRole.getUserRole();
        } else {
            this.role = null;
        }
    }

    public void setRole(Role role) {
        this.role = role;

        if (role != null && this.userRole != null) {
            this.userRole.setUserRole(role);
        }
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }

}
