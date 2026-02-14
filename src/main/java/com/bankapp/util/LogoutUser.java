package com.bankapp.util;

import com.bankapp.dao.*;
import com.bankapp.dao.jdbc_impl.*;
import com.bankapp.presentation.landing_page.LoginMenu;
import com.bankapp.service.*;
import com.bankapp.service.impl.*;

public class LogoutUser {

    // DAO Dependencies
    private final static UserDAO userDAO = new UserDAOImpl();
    private final static UserAddressDAO userAddressDAO = new UserAddressDAOImpl();
    private final static EmploymentProfileDAO employmentProfileDAO = new EmploymentProfileDAOImpl();
    private final static UserRoleDAO userRoleDAO = new UserRoleDAOImpl();
    private final static UserService userService = new UserServiceImpl(userDAO);
    private final static AccountDAO accountDAO = new AccountDAOImpl();
    private final static TransactionDAO transactionDAO = new TransactionDAOImpl();

    // Service Dependencies
    private final static UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDAO, userDAO);
    private final static UserAddressService userAddressService = new UserAddressServiceImpl(userAddressDAO, userDAO);
    private final static EmploymentProfileService employmentProfileService = new EmploymentProfileServiceImpl(employmentProfileDAO, userDAO);
    private final static AccountService accountService = new AccountServiceImpl(accountDAO, userDAO);
    private final static TransactionService transactionService = new TransactionServiceImpl(transactionDAO, accountDAO);
    private final static AuthenticationService authenticationService = new AuthenticationService();

    private LogoutUser() {}

    public static void logout() {

        System.out.println("Logging out....\n");
        LoginMenu loginMenu = new LoginMenu(
                authenticationService,
                userService,
                userRoleService,
                userAddressService,
                employmentProfileService,
                accountService,
                transactionService
        );

        loginMenu.displayMenu();
    }

}
