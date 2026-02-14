package com.bankapp.service.impl;

import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.exception.service_exceptions.account_service.*;
import com.bankapp.exception.service_exceptions.user_service.UserNotFoundException;
import com.bankapp.model.Account;
import com.bankapp.model.AccountStatus;
import com.bankapp.model.AccountType;
import com.bankapp.model.User;
import com.bankapp.security.PasswordHasher;
import com.bankapp.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;
    private final UserDAO userDAO;

    public AccountServiceImpl(AccountDAO accountDAO, UserDAO userDAO) {
        this.accountDAO = accountDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Long createAccount(Account account) {
        if (account == null) {
            throw new InvalidAccountDataException("Account cannot be null");
        }

        if (account.getUser() == null ||
                account.getUser().getUserId() == null) {
            throw new InvalidAccountDataException("UserId must not be null");
        }


        Long userId = account.getUser().getUserId();

        User user = userDAO.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with ID: " + userId + " does not exist"
                ));

        account.setUser(user);

        if (account.getAccountType() == null) {
            throw new InvalidAccountDataException("Account type must be provided");
        }

        account.setAccountBalance(BigDecimal.ZERO);
        account.setOverdraftLimit(BigDecimal.ZERO);
        account.setOpeningDate(LocalDate.now());
        account.setClosingDate(null);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setStatusReason(null);

        account.setTransactionPinHash(
                PasswordHasher.hashPassword(account.getTransactionPinHash())
        );

        Long accountId = accountDAO.createAccount(account);
        if (accountId == null) {
            throw new AccountCreationException("Failed to create account");
        }

        return accountId;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account>  accounts = accountDAO.getAllAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(accounts, (account1, account2) -> account1.getOpeningDate().compareTo(account2.getOpeningDate()));

        return accounts;
    }

    @Override
    public Optional<Account> getAccountById(Long accountId) {
        if (accountId == null) {
            throw new InvalidAccountDataException("Account ID must not be null");
        }

        Optional<Account> accountOptional = accountDAO.getAccountById(accountId);

        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found");
        }

        return accountOptional;
    }

    @Override
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new  InvalidAccountDataException("Account number must not be null or empty");
        }

        Optional<Account> accountOptional = accountDAO.getAccountByAccountNumber(accountNumber);

        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found");
        }

        return accountOptional;
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        if (userId == null) {
            throw new InvalidAccountDataException("UserId must not be null");
        }

        List<Account> userAccounts = accountDAO.getAccountsByUserId(userId);
        if (userAccounts == null ||
            userAccounts.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(userAccounts, (account1, account2) ->
                account1.getOpeningDate().compareTo(account2.getOpeningDate()));

        return userAccounts;
    }

    @Override
    public boolean accountExistsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new   InvalidAccountDataException("Account number must not be null or empty");
        }

        return accountDAO.accountExistsByAccountNumber(accountNumber);
    }

    @Override
    public boolean changeAccountType(Long accountId, AccountType newAccountType) {
        if (accountId == null ||  newAccountType == null) {
            throw new   InvalidAccountDataException("accountId and newAccountType must not be null");
        }

        Optional<Account> accountOptional = accountDAO.getAccountById(accountId);
        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = accountOptional.get();

        AccountStatus accountStatus = account.getAccountStatus();
        if (accountStatus == AccountStatus.FROZEN ||
        accountStatus == AccountStatus.SUSPENDED ||
        accountStatus == AccountStatus.DORMANT ||
        accountStatus == AccountStatus.CLOSED ||
        accountStatus == AccountStatus.DELETED) {
            throw new InvalidAccountStateException(
                    "Cannot change account type because the account is not active"
            );
        }

        if (account.getAccountType() == newAccountType) {
            throw new InvalidAccountDataException(
                    "New account type is the same as the current one"
            );
        }

        if (newAccountType == AccountType.CURRENT &&
            account.getAccountBalance().longValue() < 5000) {
            throw new AccountBalanceException("Minimum $5000 required for Current Account");
        }

        if (newAccountType == AccountType.CURRENT) {
            account.setOverdraftLimit(BigDecimal.ZERO);
        } else if (newAccountType == AccountType.SAVINGS) {
            account.setOverdraftLimit(null);
        }

        return accountDAO.changeAccountType(accountId,  newAccountType, account.getOverdraftLimit());
    }

    @Override
    public boolean updateAccountBalance(Long accountId, BigDecimal newBalance) {
        if (accountId == null || newBalance == null) {
            throw new InvalidAccountDataException("Account ID and newBalance must not be null");
        }

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new  InvalidAccountDataException("New balance must not be negative");
        }

        Optional<Account> accountOptional = accountDAO.getAccountById(accountId);
        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = accountOptional.get();
        AccountStatus accountStatus = account.getAccountStatus();

        if (accountStatus == AccountStatus.FROZEN ||
                accountStatus == AccountStatus.SUSPENDED ||
                accountStatus == AccountStatus.DORMANT ||
                accountStatus == AccountStatus.CLOSED ||
                accountStatus == AccountStatus.DELETED) {
            throw new InvalidAccountStateException("Cannot update balance of a non-active account");
        }

        return accountDAO.updateAccountBalance(accountId, newBalance);
    }

    @Override
    public boolean updateAccountStatus(Long accountId, AccountStatus newStatus) {
        if (accountId == null || newStatus == null) {
            throw new InvalidAccountDataException("Account ID and newStatus must not be null");
        }

        Optional<Account> accountOptional = accountDAO.getAccountById(accountId);
        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = accountOptional.get();
        AccountStatus accountStatus = account.getAccountStatus();

        if (newStatus == accountStatus) {
            throw new InvalidAccountDataException(
                    "New account status is the same as the current one"
            );
        }

        if (accountStatus == AccountStatus.DELETED) {
            throw new InvalidAccountStateException(
                    "Cannot change account status because the account is deleted"
            );
        }

        if (accountStatus == AccountStatus.CLOSED && newStatus != AccountStatus.DELETED) {
            throw new InvalidAccountStateException(
                    "Cannot change the status of closed account. Only deletion is allowed for closed accounts"
            );
        }

        if (newStatus == AccountStatus.DELETED && accountStatus != AccountStatus.CLOSED) {
            throw new InvalidAccountStateException(
                    "Only closed accounts can be marked as deleted"
            );
        }

        if ((newStatus == AccountStatus.CLOSED || newStatus == AccountStatus.DELETED) &&
                account.getAccountBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountBalanceException(
                    "Account balance is greater than zero. Make sure that the balance is 0."
            );
        }

        return  accountDAO.updateAccountStatus(accountId,  newStatus);

    }

    @Override
    public boolean deleteAccount(Long accountId) {
        if (accountId == null) {
            throw new InvalidAccountDataException("Account ID must not be null");
        }

        Optional<Account> accountOptional = accountDAO.getAccountById(accountId);
        if (!accountOptional.isPresent()) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account =  accountOptional.get();
        AccountStatus accountStatus = account.getAccountStatus();

        if (accountStatus == AccountStatus.DELETED) {
            throw new InvalidAccountStateException(
                    "Cannot delete account because the account is already marked as deleted"
            );
        }

        if (accountStatus != AccountStatus.CLOSED) {
            throw new InvalidAccountStateException(
                    "Only closed accounts can be marked as deleted"
            );
        }

        if (account.getAccountBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidAccountStateException(
                    "Cannot delete account. The account balance is positive"
            );
        }

        return accountDAO.deleteAccount(accountId);
    }
}
