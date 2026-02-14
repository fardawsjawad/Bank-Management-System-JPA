package com.bankapp.service;

import com.bankapp.model.UserAddress;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserAddressService {

    Long createUserAddress(UserAddress userAddress);

    Optional<UserAddress> getAddressById(Long addressId);
    List<UserAddress> getAddressesByUserId(Long userId);
    List<UserAddress> getAllAddresses();
    List<Long> getAddressIdsByUserId(Long userId);

    boolean updateUserAddress(UserAddress userAddress);

    boolean deleteUserAddress(Long addressId);
    boolean deleteAddressesByUserId(Long userId);

    boolean addressExists(Long addressId);

}
