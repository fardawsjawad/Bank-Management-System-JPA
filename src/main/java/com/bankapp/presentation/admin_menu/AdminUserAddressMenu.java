package com.bankapp.presentation.admin_menu;

import com.bankapp.presentation.admin_menu.flow.AdminUserAddressFlow;
import com.bankapp.presentation.admin_menu.flow.AdminUserFlow;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.service.UserAddressService;
import com.bankapp.service.UserService;
import com.bankapp.util.LogoutUser;

public class AdminUserAddressMenu {

    private final AdminUserAddressFlow adminUserAddressFlow;

    public AdminUserAddressMenu(
            UserAddressService userAddressService,
            UserService userService
    ) {

        this.adminUserAddressFlow = new AdminUserAddressFlow(userAddressService, userService);
    }

    public void display() {
        while (true) {
            System.out.println("\n=== User Address Operations ===");
            System.out.println("1. Add New Address for a User");
            System.out.println("2. Fetch Address by ID");
            System.out.println("3. Fetch Addresses by User Id");
            System.out.println("4. View all Addresses in the System");
            System.out.println("5. Delete User Address");
            System.out.println("6. Delete All User Addresses by User ID");
            System.out.println("7. Return to the previous menu");
            System.out.println("8. Logout");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int userChoice = ConsoleReader.readInt();

            switch (userChoice) {
                case 1 -> adminUserAddressFlow.createAddress();
                case 2 -> adminUserAddressFlow.fetchAddressById();
                case 3 -> adminUserAddressFlow.fetchAddressesByUserId();
                case 4 ->  adminUserAddressFlow.viewAllAddresses();
                case 5 ->   adminUserAddressFlow.deleteUserAddress();
                case 6 -> adminUserAddressFlow.deleteAddressesByUserId();
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
