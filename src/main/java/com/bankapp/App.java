package com.bankapp;

import com.bankapp.dao.*;
import com.bankapp.dao.jpa_impl.*;
import com.bankapp.model.*;
import com.bankapp.presentation.landing_page.LoginMenu;
import com.bankapp.service.*;
import com.bankapp.service.impl.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class App
{
    public static void main(String[] args )  {

        UserDAO userDAO = new UserJPADAOImpl();
        UserRoleDAO userRoleDAO = new UserRoleJPADAOImpl();
        UserAddressDAO userAddressDAO = new UserAddressJPADAOImpl();
        EmploymentProfileDAO employmentProfileDAO = new EmploymentProfileJPADAOImpl();
        AccountDAO accountDAO = new AccountJPADAOImpl();
        TransactionDAO transactionDAO = new TransactionJPADAOImpl();

        AuthenticationService authenticationService = new AuthenticationService();
        UserService  userService = new UserServiceImpl(userDAO);
        UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDAO, userDAO);
        UserAddressService userAddressService = new UserAddressServiceImpl(userAddressDAO, userDAO);
        EmploymentProfileService employmentProfileService = new EmploymentProfileServiceImpl(employmentProfileDAO, userDAO);
        AccountService accountService = new AccountServiceImpl(accountDAO, userDAO);
        TransactionService transactionService = new TransactionServiceImpl(transactionDAO, accountDAO);

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