package com.bankapp.presentation.user_menu.flow;

import com.bankapp.model.*;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.AccountService;
import com.bankapp.validation.AccountValidator;

import java.math.BigDecimal;
import java.util.Optional;

public class AccountFlow {

    private AccountService accountService;

    public AccountFlow(AccountService accountService) {
        this.accountService = accountService;
    }

    public void openAccount(User user) {
        Account account = GetUserInput.generateAccountForCreation();
        if (account == null) {
            return;
        }

        account.setUser(user);

        Long accountId = accountService.createAccount(account);
        if (accountId != null) {
            System.out.println("Account successfully created");
        } else {
            System.out.println("Account creation failed");
        }
    }

    public void closeAccount(Long userId) {
        System.out.print("\nEnter your account ID: ");
        Long accountId = ConsoleReader.readLong();

        try {
            Optional<Account> accountOptional = accountService.getAccountById(accountId);

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                if (account.getAccountOwnerId().equals(userId)) {

                    AccountStatus accountStatus = account.getAccountStatus();
                    if (!accountStatus.equals(AccountStatus.ACTIVE)) {
                        System.out.println("Cannot close account. Account is not active. " +
                                "Please contact management for further instructions.");
                        return;
                    }

                    if (account.getAccountBalance().compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("Cannot close account. Account balance is greater than zero");
                        return;
                    }

                    System.out.print("Are you sure you want to close your account? Y/N: ");
                    boolean answer = ConsoleReader.readYesNo();

                    if (answer) {
                        try {
                            boolean closed = accountService.updateAccountStatus(accountId, AccountStatus.CLOSED);
                            if (closed) {
                                System.out.println("Account successfully closed");
                            } else {
                                System.out.println("Account could not be closed");
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Account with ID: " + accountId + " could not be found");
                }
            } else {
                System.out.println("Account not found");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void viewAccountDetails(Long userId) {
        System.out.print("\nEnter your account number: ");
        String  accountNumber = ConsoleReader.readString();
        if (!AccountValidator.isValidAccountNumber(accountNumber)) {
            System.out.println("Invalid account number");
            return;
        }

        Optional<Account> accountOptional = accountService.getAccountByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            if (account.getAccountStatus() == AccountStatus.DELETED
                    || !account.getAccountOwnerId().equals(userId)) {
                System.out.println("Account not found.");
                return;
            }

            printAccountInfo(account);

        } else {
            System.out.println("Account not found");
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
