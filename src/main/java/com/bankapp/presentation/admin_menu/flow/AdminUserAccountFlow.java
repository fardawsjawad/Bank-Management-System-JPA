package com.bankapp.presentation.admin_menu.flow;

import com.bankapp.model.Account;
import com.bankapp.model.AccountStatus;
import com.bankapp.model.AccountType;
import com.bankapp.model.User;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.AccountService;
import com.bankapp.service.UserService;

import java.io.Console;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AdminUserAccountFlow {

    private final UserService userService;
    private final AccountService accountService;

    public AdminUserAccountFlow(
            UserService userService,
            AccountService accountService
    ) {

        this.userService = userService;
        this.accountService = accountService;
    }

    public void createAccount() {
        System.out.print("\nEnter User ID: ");
        Long userId = ConsoleReader.readLong();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("User not found");
            return;
        }

        Account account = GetUserInput.generateAccountForCreation();
        account.setUser(userOptional.get());

        try {
            Long accountId = accountService.createAccount(account);
            if (accountId != null) {
                System.out.println("Account created successfully");
            } else  {
                System.out.println("Account creation failed");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void viewAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found");
            return;
        }

        int index = 1;
        for(Account account: accounts) {
            System.out.println("\n[Account #" + (index++) + "]:");
            printAccountInfo(account);
        }
    }

    public void fetchAccountById() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        try {
            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (accountOptional.isPresent()) {
                System.out.println("\nAccount:");
                printAccountInfo(accountOptional.get());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchAccountByAccountNumber() {
        System.out.print("\nEnter Account Number: ");
        String accountNumber = ConsoleReader.readString();

        try {
            Optional<Account> accountOptional = accountService.getAccountByAccountNumber(accountNumber);
            if (accountOptional.isPresent()) {
                System.out.println("\nAccount:");
                printAccountInfo(accountOptional.get());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchAccountsByUserId() {
        System.out.print("\nEnter user ID: ");
        Long userId = ConsoleReader.readLong();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("User not found\n");
            return;
        }

        try {
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            if (accounts.isEmpty()) {
                System.out.println("No accounts found for user");
                return;
            }

            int index = 1;
            for(Account account: accounts) {
                System.out.println("\n[Account #" + (index++) + "]:");
                printAccountInfo(account);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void updateAccountType() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        System.out.println("Choose account type: ");
        System.out.println("1. Savings");
        System.out.println("2. Current");
        System.out.print("Enter your choice: ");
        int accountTypeChoice = ConsoleReader.readInt();

        AccountType accountType = null;

        switch (accountTypeChoice) {
            case 1 -> accountType = AccountType.SAVINGS;
            case 2 -> accountType = AccountType.CURRENT;
            default -> {
                System.out.println("Invalid choice");
                return;
            }
        }

        try {
            boolean updated = accountService.changeAccountType(accountId, accountType);
            if (updated) {
                System.out.println("Account Type Updated Successfully");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void updateAccountBalance() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        System.out.print("Enter new Balance: ");
        BigDecimal newBalance = ConsoleReader.readBigDecimal();

        try {
            boolean updated = accountService.updateAccountBalance(accountId, newBalance);
            if (updated) {
                System.out.println("Account Balance Updated Successfully");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void updateAccountStatus() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        try {
            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (!accountOptional.isPresent()) {
                System.out.println("Account not found");
                return;
            }

            System.out.println("Choose new status: ");
            System.out.println("1. Active");
            System.out.println("2. Frozen");
            System.out.println("3. Suspend");
            System.out.println("4. Dormant");
            System.out.println("5. Close");
            System.out.print("Enter your choice: ");
            int accountStatusChoice = ConsoleReader.readInt();

            AccountStatus accountStatus = null;

            switch (accountStatusChoice) {
                case 1 -> accountStatus = AccountStatus.ACTIVE;
                case 2 -> accountStatus = AccountStatus.FROZEN;
                case 3 -> accountStatus = AccountStatus.SUSPENDED;
                case 4 -> accountStatus = AccountStatus.DORMANT;
                case 5 -> accountStatus = AccountStatus.CLOSED;
                default -> {
                    System.out.println("Invalid choice");
                    return;
                }
            }

            boolean updated = accountService.updateAccountStatus(accountId, accountStatus);
            if (updated) {
                System.out.println("Account Status Updated Successfully");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void deleteAccount() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        try {
            boolean deleted = accountService.deleteAccount(accountId);
            if (deleted) {
                System.out.println("Account Deleted Successfully");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    private void printAccountInfo(Account account) {
        System.out.println("Account ID      : " + account.getAccountId());
        System.out.println("Account Owner ID: "  + account.getAccountOwnerId());
        System.out.println("Account Type    : " + account.getAccountType());
        System.out.println("Current Balance : " + account.getAccountBalance());
        System.out.println("Swift Code      : " + account.getBicSwiftCode());
        System.out.println("Overdraft Limit : " + account.getOverdraftLimit());
        System.out.println("Opening Date    : " + account.getOpeningDate());

        if (account.getClosingDate() != null) {
            System.out.println("Closing Date    : " + account.getClosingDate());
        }

        System.out.println("Account Status  : "  + account.getAccountStatus());

        if (account.getStatusReason() != null) {
            System.out.println("Status Reason   : "  + account.getStatusReason());
        }
    }

}
