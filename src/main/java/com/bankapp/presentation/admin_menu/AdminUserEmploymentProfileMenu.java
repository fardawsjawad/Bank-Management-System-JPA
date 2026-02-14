package com.bankapp.presentation.admin_menu;

import com.bankapp.presentation.admin_menu.flow.AdminUserEmploymentProfileFlow;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.service.EmploymentProfileService;
import com.bankapp.service.UserService;
import com.bankapp.util.LogoutUser;

public class AdminUserEmploymentProfileMenu {

    private final AdminUserEmploymentProfileFlow adminUserEmploymentProfileFlow;

    public AdminUserEmploymentProfileMenu(
            EmploymentProfileService employmentProfileService,
            UserService userService
    ) {

        this.adminUserEmploymentProfileFlow = new AdminUserEmploymentProfileFlow(employmentProfileService, userService);
    }

    public void display() {
        while (true) {
            System.out.println("\n=== Employment Profile Operations ===");
            System.out.println("1. Create Employment Profile for a User");
            System.out.println("2. Fetch Employment Profile by ID");
            System.out.println("3. Fetch Employment Profile by User ID");
            System.out.println("4. Fetch All Employment Profiles in the System");
            System.out.println("5. Update a User's Employment Profile");
            System.out.println("6. Delete an Employment Profile");
            System.out.println("7. Return to the previous menu");
            System.out.println("8. Logout");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int userChoice = ConsoleReader.readInt();

            switch (userChoice) {
                case 1 -> adminUserEmploymentProfileFlow.createEmploymentProfile();
                case 2 -> adminUserEmploymentProfileFlow.fetchEmploymentProfileById();
                case 3 -> adminUserEmploymentProfileFlow.fetchEmploymentProfileByUserId();
                case 4 -> adminUserEmploymentProfileFlow.fetchAllEmploymentProfiles();
                case 5 -> adminUserEmploymentProfileFlow.updateEmploymentProfile();
                case 6 -> adminUserEmploymentProfileFlow.deleteEmploymentProfile();
                case 7 -> {
                    return;
                }
                case 8 -> LogoutUser.logout();
                case 0 -> {
                    System.out.println("Exiting the program. Good Bye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again!");
            }
        }
    }

}
