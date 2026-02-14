package com.bankapp.presentation.admin_menu.flow;

import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.User;
import com.bankapp.model.UserAddress;
import com.bankapp.model.UserRole;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.EmploymentProfileService;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserRoleService;
import com.bankapp.service.UserService;
import com.bankapp.validation.UserValidator;

import java.util.List;
import java.util.Optional;

public class AdminUserFlow {

    private final UserService userService;
    private final UserRoleService  userRoleService;
    private final UserAddressService  userAddressService;
    private final EmploymentProfileService employmentProfileService;

    public AdminUserFlow(
            UserService userService,
            UserRoleService userRoleService,
            UserAddressService userAddressService,
            EmploymentProfileService employmentProfileService
    ) {

        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userAddressService = userAddressService;
        this.employmentProfileService = employmentProfileService;
    }

    public void createUser() {
        User user = GetUserInput.getUser(false);
        UserAddress userAddress = GetUserInput.getUserAddress();
        EmploymentProfile employmentProfile = GetUserInput.getEmploymentProfile();

        try {
            Long userId = userService.createUser(user, userAddress, employmentProfile);
            if (userId != null) {
                System.out.println("User created successfully");
            } else {
                System.out.println("User could not be created");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void displayUserById() {
        System.out.print("Enter User ID: ");
        Long userId = ConsoleReader.readLong();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("User not found");
        } else {

            User user = userOptional.get();

            printHeader("USER PROFILE");
            printUserBasicInfo(user);

            printSeparator("ADDRESS DETAILS");
            printUserAddresses(user.getUserId());

            printSeparator("EMPLOYMENT DETAILS");
            printEmploymentProfile(user.getUserId());
        }

    }

    public void displayUserByUsername() {
        System.out.print("Enter username: ");
        String username = ConsoleReader.readString();

        Optional<User> userOptional = userService.getUserByUsername(username);
        if (!userOptional.isPresent()) {
            System.out.println("User not found");
        } else {

            User user = userOptional.get();

            printHeader("USER PROFILE");
            printUserBasicInfo(user);

            printSeparator("ADDRESS DETAILS");
            printUserAddresses(user.getUserId());

            printSeparator("EMPLOYMENT DETAILS");
            printEmploymentProfile(user.getUserId());
        }

    }

    public void displayUserByEmail() {
        System.out.print("Enter email address: ");
        String email = ConsoleReader.readString();
        if (!UserValidator.isValidEmail(email)) {
            System.out.println("Invalid email address");
            return;
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        if (!userOptional.isPresent()) {
            System.out.println("User not found");
        } else {

            User user = userOptional.get();

            printHeader("USER PROFILE");
            printUserBasicInfo(user);

            printSeparator("ADDRESS DETAILS");
            printUserAddresses(user.getUserId());

            printSeparator("EMPLOYMENT DETAILS");
            printEmploymentProfile(user.getUserId());
        }

    }

    public void updateUser() {

        System.out.print("\nEnter user ID: ");
        Long userId = ConsoleReader.readLong();

        User user = GetUserInput.getUser(true);
        if (user == null) {
            return;
        }
        user.setUserId(userId);

        try {
            boolean updated = userService.updateUser(user);
            if (updated) {
                System.out.println("User updated successfully");
            } else  {
                System.out.println("User could not be updated");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }

    }

    public void updateUsername() {
        System.out.print("\nEnter user ID: ");
        Long userId = ConsoleReader.readLong();

        System.out.print("Enter new username: ");
        String newUsername = ConsoleReader.readString();

        try {
            boolean updated = userService.updateUserUsername(userId, newUsername);
            if (updated) {
                System.out.println("Username updated successfully");
            } else   {
                System.out.println("Username could not be updated");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void deleteUser() {
        System.out.print("\nEnter user ID: ");
        Long userId = ConsoleReader.readLong();

        System.out.print("Are you sure you want to delete this user? (Y/N): ");
        boolean answer = ConsoleReader.readYesNo();
        if (!answer) {
            return;
        }

        try {
            boolean deleted =  userService.deleteUser(userId);
            if (deleted) {
                System.out.println("User deleted successfully");
            } else {
                System.out.println("User could not be deleted");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
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

}
