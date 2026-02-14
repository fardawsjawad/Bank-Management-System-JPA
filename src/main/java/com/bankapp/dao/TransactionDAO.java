package com.bankapp.dao;

import com.bankapp.model.Transaction;
import com.bankapp.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {

    // CREATE
    Long createTransaction(Transaction transaction);
    Long createTransaction(Transaction transaction, Object persistenceContext);

    // RETRIEVE
    Optional<Transaction> getTransactionById(Long id);
    List<Transaction> getTransactionsByAccountId(Long accountId);
    List<Transaction> getTransactionsByDateRange(Long accountId, LocalDate start, LocalDate end);
    List<Transaction> getRecentTransactions(Long accountId, int limit);
    List<Transaction> getTransactionsByStatus(TransactionStatus status);

    // UPDATE
    boolean updateTransactionStatus(Long transactionId, TransactionStatus newStatus);

    // TRANSACTIONS
    boolean deposit(Long accountId, BigDecimal amount);
    boolean withdraw(Long accountId, BigDecimal amount);
    Long transfer(Long fromAccountId, Long toAccountId, BigDecimal amount);

}
