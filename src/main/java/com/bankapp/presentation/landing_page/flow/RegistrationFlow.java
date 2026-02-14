package com.bankapp.presentation.landing_page.flow;

import com.bankapp.exception.service_exceptions.user_service.InvalidUserDataException;
import com.bankapp.exception.service_exceptions.user_service.UserAlreadyExistsException;
import com.bankapp.exception.service_exceptions.user_service.UserCreationException;
import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.User;
import com.bankapp.model.UserAddress;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.EmploymentProfileService;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserService;

import java.util.Optional;

public class RegistrationFlow {

    private final UserService userService;
    private final UserAddressService userAddressService;
    private final EmploymentProfileService employmentProfileService;

    public RegistrationFlow(
            UserService userService,
            UserAddressService userAddressService,
            EmploymentProfileService employmentProfileService
    ) {

        this.userService = userService;
        this.userAddressService = userAddressService;
        this.employmentProfileService = employmentProfileService;
    }

    public void registerUser() {

        User user = GetUserInput.getUser(false);
        UserAddress userAddress = GetUserInput.getUserAddress();
        EmploymentProfile employmentProfile = GetUserInput.getEmploymentProfile();

        try {
            Long userId = userService.createUser(
                    user,
                    userAddress,
                    employmentProfile
            );

            if (userId != null) {
                Optional<User> userOptional = userService.getUserById(userId);
                if (userOptional.isPresent()) {
                    System.out.println("Registration successful.");
                    System.out.println("Please note down your username: " +  userOptional.get().getUsername() + "\n");
                }
            }
        } catch (
                InvalidUserDataException |
                UserAlreadyExistsException |
                UserCreationException exception
        ) {
            exception.printStackTrace();
            System.out.println(exception.getMessage() + "\n");
        }

    }

}
