package com.bankapp.presentation.admin_menu.flow;

import com.bankapp.model.User;
import com.bankapp.model.UserAddress;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserService;

import java.util.List;
import java.util.Optional;

public class AdminUserAddressFlow {

    private final UserAddressService userAddressService;
    private final UserService userService;


    public AdminUserAddressFlow(
            UserAddressService userAddressService,
            UserService userService
    ) {

        this.userAddressService = userAddressService;
        this.userService = userService;
    }

    public void createAddress() {

        System.out.print("Enter User ID: ");
        Long userId = ConsoleReader.readLong();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("User with ID: " + userId + " not found");
            return;
        }

        User user = userOptional.get();

        UserAddress newAddress = GetUserInput.getUserAddress();
        newAddress.setUser(user);

        try {
            Long newAddressId = userAddressService.createUserAddress(newAddress);
            if (newAddressId != null) {
                System.out.println("Address added successfully");
            } else {
                System.out.println("Address could not be created");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    public void fetchAddressById() {
        System.out.print("\nEnter Address ID: ");
        Long addressId = ConsoleReader.readLong();

        try {
            Optional<UserAddress> addressOptional = userAddressService.getAddressById(addressId);
            if (!addressOptional.isPresent()) {
                System.out.println("No user address with ID " + addressId + " found");
                return;
            }

            System.out.println("\nAddress:");
            printUserAddress(addressOptional.get());
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    public void fetchAddressesByUserId() {
        System.out.print("\nEnter User ID: ");
        Long userId = ConsoleReader.readLong();

        try {
            List<UserAddress>  addresses = userAddressService.getAddressesByUserId(userId);
            if (addresses == null || addresses.isEmpty()) {
                System.out.println("No address records found.");
                return;
            }

            int index = 1;
            for(UserAddress address : addresses) {
                System.out.println("\n[Address #" + index++ + "]");
                printUserAddress(address);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    public void viewAllAddresses() {
        List<UserAddress> addresses = userAddressService.getAllAddresses();
        if (addresses == null || addresses.isEmpty()) {
            System.out.println("No address records found.");
        }

        int index = 1;
        for (UserAddress address : addresses) {
            System.out.println("\nAddress #" + index++);
            printUserAddress(address);
        }
    }

    public void deleteUserAddress() {
        System.out.print("\nEnter Address ID: ");
        Long addressId = ConsoleReader.readLong();

        try {
            boolean deleted = userAddressService.deleteUserAddress(addressId);
            if (deleted) {
                System.out.println("Address deleted successfully");
            } else {
                System.out.println("Address could not be deleted");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    public void deleteAddressesByUserId() {
        System.out.print("\nEnter User ID: ");
        Long userId = ConsoleReader.readLong();

        try {
            boolean deleted = userAddressService.deleteAddressesByUserId(userId);
            if (deleted) {
                System.out.println("All user addresses deleted successfully");
            } else  {
                System.out.println("Address could not be deleted");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void printUserAddress(UserAddress userAddress) {

        System.out.println("Address ID     : " + userAddress.getAddressId());
        System.out.println("Type           : " + userAddress.getAddressType());
        System.out.println("Address Line 1 : " + userAddress.getAddressLine1());
        System.out.println("Address Line 2 : " +
                (userAddress.getAddressLine2() != null ? userAddress.getAddressLine2() : "N/A"));
        System.out.println("Locality       : " + userAddress.getLocality());
        System.out.println("City           : " + userAddress.getCity());
        System.out.println("District       : " +
                (userAddress.getDistrict() != null ? userAddress.getDistrict() : "N/A"));
        System.out.println("State          : " + userAddress.getStateOrProvince());
        System.out.println("Postal Code    : " + userAddress.getPostalCode());
        System.out.println("Country        : " + userAddress.getCountry());

    }

}
