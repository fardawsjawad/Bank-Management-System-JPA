package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "useraddress")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    @EqualsAndHashCode.Include
    private Long addressId;

    @Column(name = "user_id",  nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressType  addressType;

    @Column(name = "address_line_1",  nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "locality")
    private String locality;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "state_or_province", nullable = false)
    private String stateOrProvince;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserAddress(Long addressId, Long userId, AddressType addressType, String addressLine1,
                       String addressLine2, String locality, String city, String district,
                       String stateOrProvince, String postalCode, String country) {

        this.addressId = addressId;
        this.userId = userId;
        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.district = district;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UserAddress(Long addressId, AddressType addressType, String addressLine1, String addressLine2,
                       String locality, String city, String district, String stateOrProvince,
                       String postalCode, String country) {

        this.addressId = addressId;
        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.district = district;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UserAddress(Long userId, AddressType addressType, String addressLine1, String addressLine2,
                       String locality, String city, String stateOrProvince,
                       String postalCode, String country) {

        this.userId = userId;
        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UserAddress(AddressType addressType, String addressLine1, String addressLine2,
                       String locality, String city, String stateOrProvince,
                       String postalCode, String country) {

        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    public UserAddress(AddressType addressType, String addressLine1, String addressLine2,
                       String locality, String city, String district, String stateOrProvince,
                       String postalCode, String country) {

        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.city = city;
        this.district = district;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

}
