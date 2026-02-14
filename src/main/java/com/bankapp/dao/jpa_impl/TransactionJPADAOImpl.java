package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.TransactionDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.exception.DAO_exceptions.DepositException;
import com.bankapp.exception.DAO_exceptions.TransactionAccessException;
import com.bankapp.exception.DAO_exceptions.TransferException;
import com.bankapp.exception.DAO_exceptions.WithdrawException;
import com.bankapp.model.*;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionJPADAOImpl implements TransactionDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createTransaction(Transaction transaction) {
        if (transaction == null) {
            logger.warning("Cannot create transaction: Transaction is null");
            return null;
        }

        if (transaction.getTransactionDate() == null) {
            logger.warning("Transaction date is null. Setting current time.");
            transaction.setTransactionDate(LocalDateTime.now());
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            entityTransaction.begin();

            entityManager.persist(transaction);

            entityTransaction.commit();
            return transaction.getTransactionId();

        } catch (Exception e) {

            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while creating transaction", e);
            throw new TransactionAccessException("Database error while creating transaction", e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long createTransaction(Transaction transaction, Object persistenceContext) {
        if (!(persistenceContext instanceof EntityManager)) {
            throw new IllegalArgumentException("Expected JPA EntityManager");
        }

        if (transaction == null) {
            logger.warning("Cannot create transaction: Transaction is null");
            return null;
        }

        EntityManager entityManager = (EntityManager) persistenceContext;

        try {

            entityManager.persist(transaction);
            return transaction.getTransactionId();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while creating transaction", e);
            throw new TransactionAccessException("Database error while creating transaction", e);
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve transaction: transactionId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            Transaction transaction = entityManager.find(Transaction.class, id);
            if (transaction == null) {
                logger.info("No transaction found with ID: " + id);
                return Optional.empty();
            }

            return Optional.of(transaction);
        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching transaction by ID: " + id, e);
            throw new TransactionAccessException("Error fetching transaction by ID: " + id, e);

        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        if (accountId == null) {
            logger.warning("Cannot retrieve transaction: accountId is null");
            return Collections.emptyList();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Transaction> query =
                    entityManager.createQuery(
                            "SELECT t FROM Transaction t " +
                                    "WHERE t.fromAccountId = :accountId " +
                                    "OR t.toAccountId = :accountId " +
                                    "ORDER BY t.transactionDate DESC",
                            Transaction.class
                    );

            query.setParameter("accountId", accountId);

            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching transactions by account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by account ID: " + accountId, e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(Long accountId, LocalDate start, LocalDate end) {
        if (accountId == null || start == null || end == null) {
            logger.warning("Cannot retrieve transactions: accountId or start or end is null");
            return Collections.emptyList();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Transaction> query =
                    entityManager.createQuery(
                            "SELECT t FROM Transaction t " +
                                    "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId)" +
                                    "AND t.transactionDate BETWEEN :start AND :end " +
                                    "ORDER BY t.transactionDate DESC",
                            Transaction.class
                    );

            query.setParameter("accountId", accountId);
            query.setParameter("start", start.atStartOfDay());
            query.setParameter("end", end.atTime(LocalTime.MAX));

            return query.getResultList();

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching transactions by date range: "
                    + start + " - " + end + " for account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by date range: " +
                    start + " - " + end + " for account ID: " + accountId, e);

        }
    }

    @Override
    public List<Transaction> getRecentTransactions(Long accountId, int limit) {
        if (accountId == null || limit < 1) {
            logger.warning("Cannot retrieve transactions: accountId is null or limit is less than 1");
            return Collections.emptyList();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Transaction> query =
                    entityManager.createQuery(
                            "SELECT t FROM Transaction t " +
                                    "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
                                    "ORDER BY t.transactionDate DESC",
                            Transaction.class
                    );

            query.setParameter("accountId", accountId);
            query.setMaxResults(limit);

            return query.getResultList();

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching transactions by account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by account ID: " + accountId, e);

        }
    }

    @Override
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        if (status == null) {
            logger.warning("Cannot retrieve transactions: status is null");
            return Collections.emptyList();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Transaction> query =
                    entityManager.createQuery(
                            "SELECT t FROM Transaction t " +
                                    "WHERE t.transactionStatus = :status",
                            Transaction.class
                    );

            query.setParameter("status", status);

            return query.getResultList();
        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching transactions by status: " + status, e);
            throw new TransactionAccessException("Error fetching transactions by status: " + status, e);

        }
    }

    @Override
    public boolean updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        if (transactionId == null || newStatus == null) {
            logger.warning("Cannot update transaction status: transactionId or newStatus is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Transaction currentTransaction = entityManager.find(Transaction.class, transactionId);
            if (currentTransaction == null) {
                logger.warning("Transaction not found with ID: " + transactionId);
                transaction.rollback();
                return false;
            }

            TransactionStatus currentStatus = currentTransaction.getTransactionStatus();

            if (currentStatus == TransactionStatus.COMPLETED &&
                    (newStatus == TransactionStatus.PENDING || newStatus == TransactionStatus.FAILED)) {
                logger.warning("Invalid status update from " + currentStatus + " to " + newStatus);
                transaction.rollback();
                return false;
            }

            if (currentStatus == newStatus) {
                logger.info("Status already " + newStatus + " — no update required");
                transaction.rollback();
                return true;
            }

            currentTransaction.setTransactionStatus(newStatus);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating transaction status for ID: " + transactionId, e);
            throw new TransactionAccessException("Error updating transaction status for transaction ID: " + transactionId, e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deposit(Long accountId, BigDecimal amount) {
        if (accountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot deposit money: accountId is null or amount is <= 0");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.warning("Account not found with ID: " + accountId);
                transaction.rollback();
                return false;
            }

            AccountStatus accountStatus = account.getAccountStatus();
            if (accountStatus == AccountStatus.DELETED || accountStatus == AccountStatus.CLOSED) {
                logger.warning("Cannot deposit: Account is closed or deleted: " + accountId);
                transaction.rollback();
                return false;
            }

            BigDecimal currentBalance = account.getAccountBalance() == null
                    ? BigDecimal.ZERO
                    : account.getAccountBalance();

            account.setAccountBalance(currentBalance.add(amount));

            Transaction depositTransaction = new Transaction();
            depositTransaction.setFromAccountId(null);
            depositTransaction.setToAccountId(accountId);
            depositTransaction.setTransactionType(TransactionType.DEPOSIT);
            depositTransaction.setAmount(amount);
            depositTransaction.setAvailableBalanceAfter(account.getAccountBalance());
            depositTransaction.setTransactionDate(LocalDateTime.now());
            depositTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            entityManager.persist(depositTransaction);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while depositing: " + amount, e);
            throw new DepositException("Error while depositing money for account ID: " + accountId, e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean withdraw(Long accountId, BigDecimal amount) {
        if (accountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot withdraw money: accountId is null or amount is < 0");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.warning("Account not found with ID: " + accountId);
                transaction.rollback();
                return false;
            }

            AccountStatus accountStatus = account.getAccountStatus();
            if (accountStatus == AccountStatus.DELETED || accountStatus == AccountStatus.CLOSED) {
                logger.warning("Cannot withdraw: Account is closed or deleted" + accountId);
                transaction.rollback();
                return false;
            }

            BigDecimal currentBalance = account.getAccountBalance() == null
                    ? BigDecimal.ZERO
                    : account.getAccountBalance();


            if (currentBalance.compareTo(amount) < 0) {
                logger.warning("Insufficient balance for withdrawal. Account ID: " + accountId);
                transaction.rollback();
                return false;
            }

            account.setAccountBalance(currentBalance.subtract(amount));

            Transaction withdrawTransaction = new Transaction();
            withdrawTransaction.setFromAccountId(accountId);
            withdrawTransaction.setToAccountId(null);
            withdrawTransaction.setTransactionType(TransactionType.WITHDRAW);
            withdrawTransaction.setAmount(amount);
            withdrawTransaction.setAvailableBalanceAfter(account.getAccountBalance());
            withdrawTransaction.setTransactionDate(LocalDateTime.now());
            withdrawTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            entityManager.persist(withdrawTransaction);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while withdrawing: " + amount, e);
            throw new WithdrawException("Error while withdrawing money for account ID: " + accountId, e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot transfer money: accountId is null or amount is <= 0");
            return null;
        }

        if (fromAccountId.equals(toAccountId)) {
            logger.warning("Transfer to the same account is not allowed");
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account fromAccount = entityManager.find(
                    Account.class,
                    fromAccountId,
                    LockModeType.PESSIMISTIC_WRITE
            );
            if (fromAccount == null) {
                logger.warning("Account not found with ID: " + fromAccountId);
                transaction.rollback();
                return null;
            }


            AccountStatus fromAccountStatus = fromAccount.getAccountStatus();
            if (fromAccountStatus == AccountStatus.DELETED || fromAccountStatus == AccountStatus.CLOSED) {
                logger.warning("Cannot transfer amount: Account is closed: " + fromAccountId);
                transaction.rollback();
                return null;
            }

            BigDecimal currentFromAccountBalance = fromAccount.getAccountBalance() == null
                    ? BigDecimal.ZERO
                    : fromAccount.getAccountBalance();


            if (currentFromAccountBalance.compareTo(amount) < 0) {
                logger.warning("Insufficient balance cannot transfer amount. Account ID: " + fromAccountId);
                transaction.rollback();
                return null;
            }

            fromAccount.setAccountBalance(currentFromAccountBalance.subtract(amount));

            Account toAccount = entityManager.find(
                    Account.class,
                    toAccountId,
                    LockModeType.PESSIMISTIC_WRITE
            );
            if (toAccount == null) {
                logger.warning("Account not found with ID: " + toAccountId);
                transaction.rollback();
                return null;
            }

            AccountStatus toAccountStatus = toAccount.getAccountStatus();
            if (toAccountStatus == AccountStatus.DELETED || toAccountStatus == AccountStatus.CLOSED) {
                logger.warning("Cannot transfer amount: Account is closed: " + fromAccountId);
                transaction.rollback();
                return null;
            }

            BigDecimal currentToAccountBalance = toAccount.getAccountBalance() == null
                    ? BigDecimal.ZERO
                    : toAccount.getAccountBalance();

            toAccount.setAccountBalance(currentToAccountBalance.add(amount));

            Transaction transferTransaction = new Transaction();
            transferTransaction.setFromAccountId(fromAccountId);
            transferTransaction.setToAccountId(toAccountId);
            transferTransaction.setTransactionType(TransactionType.TRANSFER);
            transferTransaction.setAmount(amount);
            transferTransaction.setAvailableBalanceAfter(fromAccount.getAccountBalance());
            transferTransaction.setTransactionDate(LocalDateTime.now());
            transferTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            entityManager.persist(transferTransaction);

            transaction.commit();
            return transferTransaction.getTransactionId();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while transferring " + amount + " from account ID: " +
                    fromAccountId + " to account ID: " + toAccountId, e);
            throw new TransferException("Error while transferring " + amount + " from account ID: " +
                    fromAccountId + " to account ID: " + toAccountId, e);
        } finally {
            entityManager.close();
        }
    }
}
