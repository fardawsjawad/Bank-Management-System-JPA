package com.bankapp.presentation.user_menu.flow;

import com.bankapp.model.*;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.EmploymentProfileService;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserRoleService;

import java.util.List;
import java.util.Optional;

public class UserFlow {

    private final EmploymentProfileService employmentProfileService;
    private final UserAddressService userAddressService;
    private final UserRoleService userRoleService;

    public UserFlow(EmploymentProfileService employmentProfileService,
                    UserAddressService userAddressService, UserRoleService userRoleService) {
        this.employmentProfileService = employmentProfileService;
        this.userAddressService = userAddressService;
        this.userRoleService = userRoleService;
    }

    public void viewProfile(User user) {
        printHeader("USER PROFILE");
        printUserBasicInfo(user);

        printSeparator("ADDRESS DETAILS");
        printUserAddresses(user.getUserId());

        printSeparator("EMPLOYMENT DETAILS");
        printEmploymentProfile(user.getUserId());
    }

    public void updateAddress(Long userId) {
        printHeader("UPDATE ADDRESS");

        UserAddress userAddress = getNewUserAddress();

        if (userAddress ==  null) return;

        userAddress.setUserId(userId);

        boolean updated = userAddressService.updateUserAddress(userAddress);

        if (updated) {
            System.out.println("Address updated successfully");
        } else  {
            System.out.println("Address could not be updated");
        }
    }

    public void updateEmploymentProfile(Long userId) {
        printHeader("UPDATE EMPLOYMENT PROFILE");

        EmploymentProfile employmentProfile = getNewEmploymentProfile();

        if (employmentProfile == null) return;

        employmentProfile.setUserId(userId);

        boolean updated = employmentProfileService.updateEmploymentProfile(employmentProfile);

        if (updated) {
            System.out.println("Employment profile updated successfully");
        } else {
            System.out.println("Employment profile could not be updated");
        }
    }

    // --------------------------------------------------
    private void printUserBasicInfo(User user) {
        System.out.println("User ID        : " + user.getUserId());
        System.out.println("Username       : " + user.getUsername());
        System.out.println("Name           : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("Date of Birth  : " + user.getDateOfBirth());
        System.out.println("Gender         : " + user.getGender());
        System.out.println("Email          : " + user.getEmail());
        System.out.println("Phone Number  : " + user.getPhoneNumber());
        System.out.println("Nationality   : " + user.getNationality());
        System.out.println("Passport No.  : " + user.getPassportNumber());

        Optional<UserRole> userRole = userRoleService.getRoleByUserId(user.getUserId());
        userRole.ifPresent(role -> user.setRole(role.getUserRole()));
        System.out.println("Role           : " + user.getRole());
    }

    // --------------------------------------------------
    private void printUserAddresses(Long userId) {
        List<UserAddress> addresses = userAddressService.getAddressesByUserId(userId);

        if (addresses == null || addresses.isEmpty()) {
            System.out.println("No address records found.");
            return;
        }

        int index = 1;
        for (UserAddress address : addresses) {
            System.out.println("\n[Address #" + index++ + "]");
            System.out.println("Address ID     : " + address.getAddressId());
            System.out.println("Type           : " + address.getAddressType());
            System.out.println("Address Line 1 : " + address.getAddressLine1());
            System.out.println("Address Line 2 : " +
                    (address.getAddressLine2() != null ? address.getAddressLine2() : "N/A"));
            System.out.println("Locality       : " + address.getLocality());
            System.out.println("City           : " + address.getCity());
            System.out.println("District       : " +
                    (address.getDistrict() != null ? address.getDistrict() : "N/A"));
            System.out.println("State          : " + address.getStateOrProvince());
            System.out.println("Postal Code    : " + address.getPostalCode());
            System.out.println("Country        : " + address.getCountry());
        }
    }

    // --------------------------------------------------
    private void printEmploymentProfile(Long userId) {
        Optional<EmploymentProfile> employmentProfileOptional =
                employmentProfileService.getEmploymentProfileByUserId(userId);

        if (!employmentProfileOptional.isPresent()) {
            System.out.println("No employment profile found.");
            return;
        }

        EmploymentProfile employmentProfile = employmentProfileOptional.get();

        System.out.println("\n[Employment Profile: ");
        System.out.println("User ID         : " +  employmentProfile.getUserId());
        System.out.println("Employment Profile ID    : " + employmentProfile.getEmploymentProfileId());
        System.out.println("Occupation      : " + employmentProfile.getOccupation());
        System.out.println("Annual Income   : " + employmentProfile.getAnnualIncome());
        System.out.println("Source of Funds : " + employmentProfile.getSourceOfFunds());
        System.out.println("Account Purpose : " + employmentProfile.getAccountPurpose());
    }

    // --------------------------------------------------
    private void printHeader(String title) {
        System.out.println("\n====================================================");
        System.out.printf("%20s%n", title);
        System.out.println("====================================================\n");
    }

    private void printSeparator(String title) {
        System.out.println("\n----------------------------------------------------");
        System.out.printf("%20s%n", title);
        System.out.println("----------------------------------------------------");
    }

    // --------------------------------------------------
    private UserAddress getNewUserAddress() {
        System.out.print("\nEnter Address ID: ");
        Long addressId = ConsoleReader.readLong();

        if (!userAddressService.addressExists(addressId)) {
            System.out.println("Address with ID: " +  addressId + " does not exist.");
            return null;
        }

        UserAddress userAddress = GetUserInput.getUserAddress();
        if (userAddress == null) {
            return null;
        }

        userAddress.setAddressId(addressId);
        return userAddress;
    }

    // --------------------------------------------------
    private EmploymentProfile getNewEmploymentProfile() {
        System.out.print("\nEnter EmploymentProfile ID: ");
        Long employmentProfileId = ConsoleReader.readLong();

        if (!employmentProfileService.employmentProfileExists(employmentProfileId)) {
            System.out.println("Employment profile with ID: " + employmentProfileId + " does not exist.");
            return null;
        }

        EmploymentProfile employmentProfile = GetUserInput.getEmploymentProfile();
        if (employmentProfile == null) {
            return null;
        }

        employmentProfile.setEmploymentProfileId(employmentProfileId);

        return employmentProfile;
    }

}
