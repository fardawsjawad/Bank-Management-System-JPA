package com.bankapp.dao;

import com.bankapp.model.UserAddress;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserAddressDAO {

    // CREATE
    Long createUserAddress(UserAddress userAddress);
    Long  createUserAddress(UserAddress userAddress, Object persistenceContext);

    // READ
    Optional<UserAddress> getAddressById(Long addressId);
    List<UserAddress> getAddressesByUserId(Long userId);
    List<UserAddress> getAllAddresses();
    List<Long> getAddressIdsByUserId(Long userId);

    // UPDATE
    boolean updateUserAddress(UserAddress userAddress);

    // DELETE
    boolean deleteUserAddress(Long addressId);
    boolean deleteAddressesByUserId(Long userId);

    // CHECK
    boolean addressExists(Long addressId);

}
