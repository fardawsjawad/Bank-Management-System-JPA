package com.bankapp.presentation.landing_page.flow;

import com.bankapp.exception.service_exceptions.user_service.EmailAddressNotFoundException;
import com.bankapp.exception.service_exceptions.user_service.InvalidUserDataException;
import com.bankapp.model.User;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.service.UserService;
import com.bankapp.util.EmailSender;
import com.bankapp.validation.UserValidator;

import java.util.Random;

public class ForgotPasswordFlow {

    private final UserService  userService;

    public ForgotPasswordFlow(UserService userService) {
        this.userService = userService;
    }

    public void resetPassword() {
        System.out.print("\nEnter username or email address: ");
        String identifier = ConsoleReader.readString();

        try {
            User user = userService.getUserByIdentifier(identifier);

            String generatedCode = generateCode();
            EmailSender.sendEmail(user.getEmail(), generatedCode);

            System.out.print("Enter OTP sent to your email: ");
            String verificationCode = ConsoleReader.readString();

            if (!verificationCode.trim().equals(generatedCode.trim())) {
                System.out.println("Incorrect OTP entered, try again.\n");
                return;
            }

            System.out.println("OTP successfully verified.");

            System.out.print("Enter new password: ");
            String newPassword = ConsoleReader.readString();
            if (!UserValidator.isValidPassword(newPassword)) {
                System.out.println("Invalid password format. " +
                        "Password length must be greater than 8  characters and " +
                        "password must contain letters and digits.\n");
                return;
            }


            boolean updated = userService.updateUserPassword(
                    user.getUserId(),
                    newPassword
            );

            if (updated) {
                System.out.println("Password successfully updated.\n");
            }

        } catch (EmailAddressNotFoundException | InvalidUserDataException e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    private String generateCode() {
        Random random = new Random();

        // Generate a random number between 100000 and 999999 (inclusive)
        int sixDigit = 100000 + random.nextInt(900000);

        // Convert the number to a string and return it
        return Integer.toString(sixDigit);
    }

}
