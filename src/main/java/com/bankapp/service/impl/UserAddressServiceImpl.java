package com.bankapp.service.impl;

import com.bankapp.dao.UserAddressDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.exception.service_exceptions.user_address_service.AddressNotFoundException;
import com.bankapp.exception.service_exceptions.user_address_service.InvalidUserAddressDataException;
import com.bankapp.exception.service_exceptions.user_address_service.UserAddressCreationException;
import com.bankapp.exception.service_exceptions.user_address_service.UserAddressException;
import com.bankapp.exception.service_exceptions.user_service.UserNotFoundException;
import com.bankapp.model.User;
import com.bankapp.model.UserAddress;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressDAO userAddressDAO;
    private final UserDAO userDAO;

    public UserAddressServiceImpl(UserAddressDAO userAddressDAO,  UserDAO userDAO) {
        this.userAddressDAO = userAddressDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Long createUserAddress(UserAddress userAddress) {
        if (userAddress == null) {
            throw new InvalidUserAddressDataException("userAddress must not be null");
        }

        if (userAddress.getUser() == null ||
            userAddress.getUser().getUserId() == null) {
            throw new InvalidUserAddressDataException("userId must not be null");
        }

        Long userId = userAddress.getUser().getUserId();

        if (!userDAO.userExistsByUserId(userId)) {
            throw new UserNotFoundException(
                    "User with ID: " + userId + " does not exist"
            );
        }

        if (userAddress.getAddressType() == null) {
            throw new InvalidUserAddressDataException(
                    "addressType must not be null"
            );
        }

        Long id = userAddressDAO.createUserAddress(userAddress);

        if (id == null) {
            throw new UserAddressCreationException(
                    "Failed to create user address"
            );
        }

        return id;
    }

    @Override
    public Optional<UserAddress> getAddressById(Long addressId) {
        if (addressId == null) {
            throw new InvalidUserAddressDataException("addressId must not be null");
        }

        return userAddressDAO.getAddressById(addressId);
    }

    @Override
    public List<UserAddress> getAddressesByUserId(Long userId) {
        if (userId  == null) {
            throw new  InvalidUserAddressDataException("userId must not be null");
        }

        if (!userDAO.userExistsByUserId(userId)) {
            throw new  UserNotFoundException("User with ID: " +  userId + " does not exist");
        }

        return userAddressDAO.getAddressesByUserId(userId);
    }

    @Override
    public List<UserAddress> getAllAddresses() {
        return userAddressDAO.getAllAddresses();
    }

    @Override
    public List<Long> getAddressIdsByUserId(Long userId) {
        if (userId == null) {
            throw new InvalidUserAddressDataException("UserId must not be null");
        }

        return userAddressDAO.getAddressIdsByUserId(userId);
    }

    @Override
    public boolean updateUserAddress(UserAddress userAddress) {
        if (userAddress ==  null) {
            throw new  InvalidUserAddressDataException("userAddress must not be null");
        }

        if (userAddress.getAddressId() == null) {
            throw new InvalidUserAddressDataException("addressId must not be null");
        }

        userAddressDAO.getAddressById(userAddress.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        boolean success = userAddressDAO.updateUserAddress(userAddress);

        if (!success) {
            throw new UserAddressException("Failed to update user address");
        }

        return true;
    }

    @Override
    public boolean deleteUserAddress(Long addressId) {
        if (addressId == null) {
            throw new  InvalidUserAddressDataException("addressId must not be null");
        }

        boolean success = userAddressDAO.deleteUserAddress(addressId);

        if (!success) {
            throw new AddressNotFoundException("Address with ID: " +  addressId + " does not exist");
        }

        return true;
    }

    @Override
    public boolean deleteAddressesByUserId(Long userId) {
        if (userId  == null) {
            throw new InvalidUserAddressDataException("userId must not be null");
        }

        userDAO.getUserById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        boolean success = userAddressDAO.deleteAddressesByUserId(userId);

        if (!success) {
            throw new AddressNotFoundException("No address found for user with ID: " + userId);
        }

        return true;
    }

    @Override
    public boolean addressExists(Long addressId) {
        if (addressId == null) {
            throw new  InvalidUserAddressDataException("addressId must not be null");
        }

        return userAddressDAO.addressExists(addressId);
    }
}
