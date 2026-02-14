package com.bankapp.presentation.admin_menu.flow;

import com.bankapp.exception.service_exceptions.account_service.AccountNotFoundException;
import com.bankapp.exception.service_exceptions.trasaction_service.TransactionNotFoundException;
import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.model.TransactionStatus;
import com.bankapp.model.TransactionType;
import com.bankapp.presentation.input.ConsoleReader;
import com.bankapp.presentation.input.GetUserInput;
import com.bankapp.service.TransactionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminUserTransactionFlow {

    private final TransactionService transactionService;

    public AdminUserTransactionFlow(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void fetchTransactionById() {
        System.out.print("\nEnter Transaction ID: ");
        Long transactionId = ConsoleReader.readLong();

        try {
            Optional<Transaction> transaction =  transactionService.getTransactionById(transactionId);
            if (transaction.isPresent()) {
                printTransactionInfo(transaction.get());
            } else {
                System.out.println("Transaction not found");
            }
        } catch (Exception e) {
            System.out.println("\nTransaction:");
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchTransactionsByAccountId() {
        System.out.print("\nEnter Account ID: ");
        Long accountId =  ConsoleReader.readLong();

        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
            if (transactions != null && !transactions.isEmpty()) {
                printTransactionList(transactions);
            } else {
                System.out.println("No transactions found");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void viewTransactionsByDate() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();

        System.out.print("Enter start date (yyyy-MM-dd): ");
        LocalDate startDate = ConsoleReader.readLocalDate();

        System.out.print("Enter end date (yyyy-MM-dd): ");
        LocalDate endDate = ConsoleReader.readLocalDate();

        try {
            List<Transaction> transactions =  transactionService.getTransactionsByDateRange(accountId, startDate, endDate);
            if ((transactions != null) && (!transactions.isEmpty())) {
                printTransactionList(transactions);
            } else {
                System.out.println("No transactions found");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }

    }

    public void fetchRecentTransactions() {
        System.out.print("\nEnter Account ID: ");
        Long accountId = ConsoleReader.readLong();
        System.out.print("Enter the number of transactions you want to retrieve: ");
        int numberOfTransactions = ConsoleReader.readInt();

        try {
            List<Transaction> transactions = transactionService.getRecentTransactions(accountId, numberOfTransactions);
            if (transactions != null && !transactions.isEmpty()) {
                printTransactionList(transactions);
            } else {
                System.out.println("No transactions found");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void fetchTransactionsByStatus() {

        TransactionStatus transactionStatus = GetUserInput.getTransactionStatus();
        if (transactionStatus == null) {
            return;
        }

        try {
            List<Transaction> transactions = transactionService.getTransactionsByStatus(transactionStatus);
            if (transactions != null && !transactions.isEmpty()) {
                printTransactionList(transactions);
            } else {
                System.out.println("No transactions found");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    public void updateTransactionStatus() {
        System.out.print("\nEnter Transaction ID: ");
        Long transactionId = ConsoleReader.readLong();

        TransactionStatus newStatus = GetUserInput.getTransactionStatus();
        if (newStatus == null) {
            return;
        }

        try {
            boolean updated = transactionService.updateTransactionStatus(transactionId, newStatus);
            if (updated) {
                System.out.println("Transaction status updated successfully");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n");
        }
    }

    private void printTransactionList(List<Transaction> transactions) {
        System.out.println("\nTransactions:");
        int index = 1;
        for (Transaction transaction : transactions) {
            System.out.println("\n[Transaction #" + (index++) + "] ");
            printTransactionInfo(transaction);
        }
    }

    private void printTransactionInfo(Transaction transaction) {
        System.out.println("Transaction ID           : " + transaction.getTransactionId());

        if (transaction.getTransactionType() == TransactionType.TRANSFER) {
            System.out.println("Sender's Account ID      : " + transaction.getFromAccountId());
            System.out.println("Beneficiary's Account ID : " + transaction.getToAccountId());
        } else if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            System.out.println("Deposited to Account ID  : " + transaction.getToAccountId());
        } else if (transaction.getTransactionType() == TransactionType.WITHDRAW) {
            System.out.println("Withdrawn From Account ID: "  + transaction.getFromAccountId());
        }

        System.out.println("Transaction Type         : " + transaction.getTransactionType());
        System.out.println("Amount                   : " + transaction.getAmount());
        System.out.println("Available Balance After  : " + transaction.getAvailableBalanceAfter());
        System.out.println("Transaction Date         : " + transaction.getTransactionDate());
        System.out.println("Transaction Status       : " + transaction.getTransactionStatus());
    }

}
