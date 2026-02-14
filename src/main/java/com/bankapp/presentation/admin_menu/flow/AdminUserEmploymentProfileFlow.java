package com.bankapp.presentation.admin_menu.flow;

import com.bankapp.exception.service_exceptions.employment_profile.EmploymentProfileNotFoundException;
import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.User;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.EmploymentProfileService;
import com.bankapp.service.UserService;

import java.util.List;
import java.util.Optional;

public class AdminUserEmploymentProfileFlow {

    private final EmploymentProfileService employmentProfileService;
    private final UserService userService;

    public AdminUserEmploymentProfileFlow(
            EmploymentProfileService employmentProfileService,
            UserService userService
    ) {

        this.employmentProfileService = employmentProfileService;
        this.userService = userService;
    }

    public void createEmploymentProfile() {

        System.out.print("\nEnter User ID: ");
        Long userId = ConsoleReader.readLong();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("User with ID: " + userId + " not found\n");
            return;
        }

        User user = userOptional.get();

        EmploymentProfile employmentProfile = GetUserInput.getEmploymentProfile();
        employmentProfile.setUser(user);

        try {
            Long employmentProfileId = employmentProfileService.createEmploymentProfile(employmentProfile);
            if (employmentProfileId != null) {
                System.out.println("Employment Profile created successfully");
            } else {
                System.out.println("Employment Profile creation failed");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchEmploymentProfileById() {
        System.out.print("\nEnter Employment Profile ID: ");
        Long employmentProfileId = ConsoleReader.readLong();

        try {
            Optional<EmploymentProfile> employmentProfileOptional = employmentProfileService.getEmploymentProfileById(employmentProfileId);
            employmentProfileOptional.ifPresent(this::printEmploymentProfile);
        } catch (EmploymentProfileNotFoundException e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchEmploymentProfileByUserId() {
        System.out.print("\nEnter User ID: ");
        Long userId = ConsoleReader.readLong();

        try {
            Optional<EmploymentProfile> employmentProfileOptional = employmentProfileService.getEmploymentProfileByUserId(userId);
            employmentProfileOptional.ifPresent(this::printEmploymentProfile);
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchAllEmploymentProfiles() {
        List<EmploymentProfile>  employmentProfiles = employmentProfileService.getAllEmploymentProfiles();
        employmentProfiles.forEach(this::printEmploymentProfile);
    }

    public void updateEmploymentProfile() {
        System.out.print("\nEnter Employment Profile ID: ");
        Long employmentProfileId = ConsoleReader.readLong();

        try {
            Optional<EmploymentProfile> optional =
                    employmentProfileService.getEmploymentProfileById(employmentProfileId);

            if (!optional.isPresent()) {
                System.out.println("Employment Profile not found\n");
                return;
            }

            EmploymentProfile existing = optional.get();

            EmploymentProfile input = GetUserInput.getEmploymentProfile();

            if (input != null) {
                existing.setOccupation(input.getOccupation());
                existing.setAnnualIncome(input.getAnnualIncome());
                existing.setSourceOfFunds(input.getSourceOfFunds());
                existing.setAccountPurpose(input.getAccountPurpose());
            }

            boolean updated = employmentProfileService.updateEmploymentProfile(existing);
            if (updated) {
                System.out.println("Employment Profile updated successfully");
            } else  {
                System.out.println("Employment Profile could not be updated");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void deleteEmploymentProfile() {
        System.out.print("\nEnter Employment Profile ID: ");
        Long employmentProfileId = ConsoleReader.readLong();

        try {
            boolean deleted = employmentProfileService.deleteEmploymentProfile(employmentProfileId);
            if (deleted) {
                System.out.println("Employment Profile deleted successfully");
            } else {
                System.out.println("Employment Profile could not be deleted");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    private void printEmploymentProfile(EmploymentProfile employmentProfile) {

        if (employmentProfile == null) {
            System.out.println("No employment profile found.");
            return;
        }

        System.out.println("\nEmployment Profile:");
        System.out.println("Employment Profile ID    : " + employmentProfile.getEmploymentProfileId());
        System.out.println("User ID         : " + employmentProfile.getUserId());
        System.out.println("Occupation      : " + employmentProfile.getOccupation());
        System.out.println("Annual Income   : " + employmentProfile.getAnnualIncome());
        System.out.println("Source of Funds : " + employmentProfile.getSourceOfFunds());
        System.out.println("Account Purpose : " + employmentProfile.getAccountPurpose());

    }


}
