package com.bankapp.service.impl;

import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.exception.DAO_exceptions.DepositException;
import com.bankapp.exception.DAO_exceptions.TransferException;
import com.bankapp.exception.DAO_exceptions.WithdrawException;
import com.bankapp.exception.service_exceptions.account_service.AccountBalanceException;
import com.bankapp.exception.service_exceptions.account_service.AccountNotFoundException;
import com.bankapp.exception.service_exceptions.account_service.InvalidAccountStateException;
import com.bankapp.exception.service_exceptions.trasaction_service.InsufficientBalanceException;
import com.bankapp.exception.service_exceptions.trasaction_service.InvalidTransactionDataException;
import com.bankapp.exception.service_exceptions.trasaction_service.TransactionCreationException;
import com.bankapp.exception.service_exceptions.trasaction_service.TransactionNotFoundException;
import com.bankapp.model.Account;
import com.bankapp.model.AccountStatus;
import com.bankapp.model.Transaction;
import com.bankapp.model.TransactionStatus;
import com.bankapp.security.PasswordHasher;
import com.bankapp.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TransactionServiceImpl implements TransactionService {

    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public TransactionServiceImpl(TransactionDAO transactionDAO, AccountDAO accountDAO) {
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public Long createTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionDataException("Transaction cannot be null");
        }

        if (transaction.getTransactionType() == null) {
            throw new InvalidTransactionDataException("Transaction type cannot be null");
        }

        if (transaction.getAmount() == null ||
                transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionDataException("Transaction amount must be greater than zero");
        }

        if (transaction.getAvailableBalanceAfter() == null) {
            throw new InvalidTransactionDataException("Available balance after transaction cannot be null");
        }

        switch (transaction.getTransactionType()) {
            case DEPOSIT:
                if (transaction.getToAccountId() == null) {
                    throw new InvalidTransactionDataException("Deposit requires toAccountId");
                }
                break;

            case WITHDRAW:
                if (transaction.getFromAccountId() == null) {
                    throw new InvalidTransactionDataException("Withdraw requires fromAccountId");
                }
                break;

            case TRANSFER:
                if (transaction.getFromAccountId() == null ||
                        transaction.getToAccountId() == null) {
                    throw new InvalidTransactionDataException("Transfer requires both account IDs");
                }
                if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
                    throw new InvalidTransactionDataException("Cannot transfer to the same account");
                }
                break;

            default:
                throw new InvalidTransactionDataException("Unsupported transaction type");
        }

        if (transaction.getTransactionStatus() == null) {
            transaction.setTransactionStatus(TransactionStatus.PENDING);
        }

        Long transactionId = transactionDAO.createTransaction(transaction);

        if (transactionId == null) {
            throw new TransactionCreationException("Failed to create transaction");
        }

        return transactionId;
    }


    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        if (id == null) {
            throw new InvalidTransactionDataException("Transaction ID must not be null");
        }

        transactionDAO.getTransactionById(id)
                .orElseThrow(() ->
                            new TransactionNotFoundException("No transactions exist with ID: " + id)
                        );

        return transactionDAO.getTransactionById(id);
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        if (accountId == null) {
            throw new InvalidTransactionDataException("accountId must not be null");
        }

        accountDAO.getAccountById(accountId)
                .orElseThrow(() ->
                            new AccountNotFoundException("Account with ID: " + accountId + " not found")
                        );

        List<Transaction> transactions = transactionDAO.getTransactionsByAccountId(accountId);
        if (transactions == null ||
                transactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions exist for account with ID: " + accountId);
        }
        Collections.sort(transactions, (t1, t2) ->
                t2.getTransactionDate().compareTo(t1.getTransactionDate()));

        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(Long accountId, LocalDate start, LocalDate end) {
        if (accountId == null || start == null || end == null) {
            throw new InvalidTransactionDataException("accountId, start and end cannot be null");
        }

        if (start.isAfter(end)) {
            throw new InvalidTransactionDataException(
                    "Start date cannot be after end date"
            );
        }

        if (end.isAfter(LocalDate.now())) {
            throw new InvalidTransactionDataException(
                    "End date cannot be greater than current date"
            );
        }

        accountDAO.getAccountById(accountId)
                .orElseThrow(
                        () -> new AccountNotFoundException("Account with ID: " + accountId + " not found")
                );

        return transactionDAO.getTransactionsByDateRange(accountId, start, end);
    }

    @Override
    public List<Transaction> getRecentTransactions(Long accountId, int limit) {
        if (accountId == null) {
            throw new InvalidTransactionDataException("accountId must not be null");
        }

        if (limit < 1) {
            throw new InvalidTransactionDataException("limit must be greater than zero");
        }

        if (limit > 100) {
            throw new InvalidTransactionDataException("limit cannot exceed 100");
        }

        accountDAO.getAccountById(accountId)
                .orElseThrow(() ->
                            new  AccountNotFoundException("Account with ID: " + accountId + " not found")
                        );

        return transactionDAO.getRecentTransactions(accountId, limit);
    }

    @Override
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        if (status == null) {
            throw new InvalidTransactionDataException("status must not be null");
        }

        return transactionDAO.getTransactionsByStatus(status);
    }

    @Override
    public boolean updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        if (transactionId == null || newStatus == null) {
            throw new InvalidTransactionDataException("transactionId and newStatus must not be null");
        }

        Transaction existingTransaction =
                transactionDAO.getTransactionById(transactionId)
                        .orElseThrow(() ->
                                    new TransactionNotFoundException("Transaction not found")
                                );

        TransactionStatus currentStatus = existingTransaction.getTransactionStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidTransactionDataException(
                    "Invalid status transition from " +  currentStatus + " to " + newStatus
            );
        }

        return transactionDAO.updateTransactionStatus(transactionId, newStatus);
    }

    @Override
    public boolean deposit(Long accountId, String pinCodeHash, BigDecimal amount) {
        if (accountId == null) {
            throw new InvalidTransactionDataException("accountId must not be null");
        }

        Account account = accountDAO.getAccountById(accountId)
                .orElseThrow(() ->
                            new AccountNotFoundException("Account not found")
                        );

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Cannot deposit amount. The account is currently in-active");
        }


        if (!PasswordHasher.checkPin(pinCodeHash, account.getTransactionPinHash())) {
            throw new InvalidTransactionDataException("Incorrect PIN entered");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionDataException("Deposit amount must be greater than zero");
        }

        boolean success = transactionDAO.deposit(accountId, amount);

        if (!success) {
            throw new DepositException(
                    "Deposit failed for accountId: " + accountId + ", amount: " + amount
            );
        }

        return true;
    }

    @Override
    public boolean withdraw(Long accountId, String pinCodeHash, BigDecimal amount) {

        if (accountId == null) {
            throw new InvalidTransactionDataException("accountId must not be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionDataException("Withdrawal amount must be greater than zero");
        }

        Account account = accountDAO.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Account is in-active");
        }

        if (!PasswordHasher.checkPin(pinCodeHash, account.getTransactionPinHash())) {
            throw new InvalidTransactionDataException("Incorrect PIN entered");
        }

        if (account.getAccountBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        boolean success = transactionDAO.withdraw(accountId, amount);

        if (!success) {
            throw new WithdrawException(
                    "Withdrawal failed for accountId: " + accountId + ", amount: " + amount
            );
        }

        return true;
    }


    @Override
    public Long transfer(Long fromAccountId, Long toAccountId, String fromAccountPinCodeHash, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null) {
            throw new InvalidTransactionDataException("fromAccountId and toAccountId must not be null");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidTransactionDataException("Cannot transfer to the same account");
        }

        Account fromAccount = accountDAO.getAccountById(fromAccountId)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account with ID: " + fromAccountId + " not found")
                );

        Account toAccount = accountDAO.getAccountById(toAccountId)
                .orElseThrow(() ->
                            new AccountNotFoundException("Account with ID: " + toAccountId + " not found")
                        );

        if (fromAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Account with ID: " + fromAccountId + " is in-active");
        }

        if (toAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException("Account with ID: " + toAccountId + " is in-active");
        }

        if (!PasswordHasher.checkPin(fromAccountPinCodeHash, fromAccount.getTransactionPinHash())) {
            throw new InvalidTransactionDataException("Incorrect PIN entered");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionDataException("Transfer amount must be greater than zero");
        }

        if (fromAccount.getAccountBalance().compareTo(amount) < 0) {
            throw new AccountBalanceException("Insufficient balance. Cannot transfer amount.");
        }

        Long transactionId = transactionDAO.transfer(fromAccountId, toAccountId, amount);

        if (transactionId == null) {
            throw new TransferException(
                    "Transfer from account ID: " + fromAccountId +
                            " to account ID: " + toAccountId +
                            " failed"
            );
        }

        return transactionId;
    }

    private boolean isValidStatusTransition(TransactionStatus current, TransactionStatus next) {

        if (current == TransactionStatus.PENDING) {
            return next == TransactionStatus.SUCCESS || next == TransactionStatus.FAILED;
        }

        return false;
    }
}
