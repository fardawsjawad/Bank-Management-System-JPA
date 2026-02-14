package com.bankapp.presentation.admin_menu;

import com.bankapp.model.User;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.service.*;
import com.bankapp.util.LogoutUser;

public class AdminMenu {

    private final AdminUserMenu adminUserMenu;
    private final AdminUserAddressMenu adminUserAddressMenu;
    private final AdminUserEmploymentProfileMenu adminUserEmploymentProfileMenu;
    private final AdminUserAccountMenu adminUserAccountMenu;
    private final AdminUserTransactionMenu adminUserTransactionMenu;

    public AdminMenu(
            UserService userService,
            UserRoleService userRoleService,
            UserAddressService userAddressService,
            EmploymentProfileService employmentProfileService,
            AccountService accountService,
            TransactionService transactionService
    ) {

        this.adminUserMenu = new AdminUserMenu(userService, userRoleService,  userAddressService, employmentProfileService);
        this.adminUserAddressMenu = new AdminUserAddressMenu(userAddressService, userService);
        this.adminUserEmploymentProfileMenu = new AdminUserEmploymentProfileMenu(employmentProfileService, userService);
        this.adminUserAccountMenu = new AdminUserAccountMenu(userService, accountService);
        this.adminUserTransactionMenu = new AdminUserTransactionMenu(transactionService);
    }

    public void start(User user) {
        System.out.println("Welcome " + user.getFirstName() + " " + user.getLastName() + "!\n");
        while (true) {
            System.out.println("=== Bank Management System ===");
            System.out.println("1. User Operations");
            System.out.println("2. User Address Operations");
            System.out.println("3. User Employment Profile Operations");
            System.out.println("4. User Account Operations");
            System.out.println("5. User Transaction Operations");
            System.out.println("6. Logout");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = ConsoleReader.readInt();

            switch (choice) {
                case 1 -> adminUserMenu.display();
                case 2 -> adminUserAddressMenu.display();
                case 3 -> adminUserEmploymentProfileMenu.display();
                case 4 -> adminUserAccountMenu.display();
                case 5 -> adminUserTransactionMenu.display();
                case 6 -> LogoutUser.logout();
                case 0 -> {
                    System.out.println("Exiting the program. Good bye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

}
