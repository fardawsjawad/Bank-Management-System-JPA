package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.dao.sql.AccountSQLQueries;
import com.bankapp.dao.sql.TransactionSQLQueries;
import com.bankapp.exception.DAO_exceptions.DepositException;
import com.bankapp.exception.DAO_exceptions.TransactionAccessException;
import com.bankapp.exception.DAO_exceptions.TransferException;
import com.bankapp.exception.DAO_exceptions.WithdrawException;
import com.bankapp.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAOImpl implements TransactionDAO {

    private static final Logger logger = Logger.getLogger(TransactionDAOImpl.class.getName());

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


        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             TransactionSQLQueries.CREATE_TRANSACTION,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (transaction.getFromAccountId() != null) {
                preparedStatement.setLong(1, transaction.getFromAccountId());
            } else {
                preparedStatement.setNull(1, Types.INTEGER);
            }

            if (transaction.getToAccountId() != null) {
                preparedStatement.setLong(2, transaction.getToAccountId());
            } else {
                preparedStatement.setNull(2, Types.INTEGER);
            }

            preparedStatement.setString(3, transaction.getTransactionType().toString().toUpperCase());
            preparedStatement.setBigDecimal(4, transaction.getAmount());
            preparedStatement.setBigDecimal(5, transaction.getAvailableBalanceAfter());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(transaction.getTransactionDate()));
            preparedStatement.setString(7, transaction.getTransactionStatus().toString().toUpperCase());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert transaction - no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long transactionId = generatedKeys.getLong(1);
                    transaction.setTransactionId(transactionId);
                    logger.info("Transaction created successfully with ID: " + transactionId);
                    return transactionId;
                } else {
                    logger.severe("Failed to fetch generated transaction ID");
                    throw new TransactionAccessException("Transaction ID generation failed", null);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating transaction", e);
            throw new TransactionAccessException("Database error while creating transaction", e);
        }
    }

    @Override
    public Long createTransaction(Transaction transaction, Object persistenceContext) {
        if (!(persistenceContext instanceof Connection)) {
            throw new IllegalArgumentException("Expected JDBC Connection");
        }

        Connection connection = (Connection) persistenceContext;

        if (transaction == null) {
            logger.warning("Cannot create transaction: Transaction is null");
            return null;
        }

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             TransactionSQLQueries.CREATE_TRANSACTION,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (transaction.getFromAccountId() != null) {
                preparedStatement.setLong(1, transaction.getFromAccountId());
            } else {
                preparedStatement.setNull(1, Types.INTEGER);
            }

            if (transaction.getToAccountId() != null) {
                preparedStatement.setLong(2, transaction.getToAccountId());
            } else {
                preparedStatement.setNull(2, Types.INTEGER);
            }

            preparedStatement.setString(3, transaction.getTransactionType().toString().toUpperCase());
            preparedStatement.setBigDecimal(4, transaction.getAmount());
            preparedStatement.setBigDecimal(5, transaction.getAvailableBalanceAfter());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(transaction.getTransactionDate()));
            preparedStatement.setString(7, transaction.getTransactionStatus().toString().toUpperCase());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert transaction - no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long transactionId = generatedKeys.getLong(1);
                    transaction.setTransactionId(transactionId);
                    logger.info("Transaction created successfully with ID: " + transactionId);
                    return transactionId;
                } else {
                    logger.severe("Failed to fetch generated transaction ID");
                    throw new TransactionAccessException("Transaction ID generation failed", null);
                }
            }
        } catch (SQLException e) {
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

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    TransactionSQLQueries.SELECT_TRANSACTION_BY_ID)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    Transaction transaction = mapResultSetToTransaction(resultSet);
                    return Optional.of(transaction);

                } else {
                    logger.info("No transaction found with ID: " + id);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching transaction by ID: " + id, e);
            throw new TransactionAccessException("Error fetching transaction by ID: " + id, e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        List<Transaction> transactions = new ArrayList<>();

        if (accountId == null) {
            logger.warning("Cannot retrieve transaction: accountId is null");
            return transactions;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     TransactionSQLQueries.SELECT_TRANSACTION_BY_ACCOUNT_ID)) {

            preparedStatement.setLong(1, accountId);
            preparedStatement.setLong(2, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));

                }

                if (transactions.isEmpty()) {
                    logger.info("No transaction found for account ID: " + accountId);
                } else {
                    logger.info("Successfully retrieved " + transactions.size() + " transactions from database");
                }

                return transactions;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching transactions by account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by account ID: " + accountId, e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(Long accountId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = new ArrayList<>();

        if (accountId == null || start == null || end == null) {
            logger.warning("Cannot retrieve transactions: accountId or start or end is null");
            return transactions;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(TransactionSQLQueries.SELECT_TRANSACTIONS_BY_DATE_RANGE)) {

            preparedStatement.setLong(1, accountId);
            preparedStatement.setLong(2, accountId);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(start.atStartOfDay()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }

                if (transactions.isEmpty()) {
                    logger.info("No transactions found for the date range: " + start + " - " + end +
                            " for account ID: " + accountId);
                } else {
                    logger.info("Successfully retrieved " + transactions.size() + " transactions from database");
                }

                return transactions;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching transactions by date range: "
                    + start + " - " + end + " for account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by date range: " +
                    start + " - " + end + " for account ID: " + accountId, e);
        }
    }

    @Override
    public List<Transaction> getRecentTransactions(Long accountId, int limit) {
        List<Transaction> transactions = new ArrayList<>();

        if (accountId == null || limit < 1) {
            logger.warning("Cannot retrieve transactions: accountId is null or limit is less than 1");
            return transactions;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(TransactionSQLQueries.SELECT_RECENT_TRANSACTIONS)) {

            preparedStatement.setLong(1, accountId);
            preparedStatement.setLong(2, accountId);
            preparedStatement.setInt(3, limit);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }

                if (transactions.isEmpty()) {
                    logger.info("No transactions found for account ID: " + accountId);
                } else {
                    logger.info("Successfully retrieved " + transactions.size() + " transactions from database");
                }

                return transactions;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching transactions by account ID: " + accountId, e);
            throw new TransactionAccessException("Error fetching transactions by account ID: " + accountId, e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        List<Transaction> transactions = new ArrayList<>();

        if (status == null) {
            logger.warning("Cannot retrieve transactions: status is null");
            return transactions;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(TransactionSQLQueries.SELECT_TRANSACTIONS_BY_STATUS)) {

            preparedStatement.setString(1, status.toString().toUpperCase());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(mapResultSetToTransaction(resultSet));
                }

                if (transactions.isEmpty()) {
                    logger.info("No transactions found with status: " + status);
                } else {
                    logger.info("Successfully retrieved " + transactions.size() + " transactions from database");
                }

                return transactions;
            }
        } catch (SQLException e) {
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

        TransactionStatus currentStatus = getTransactionById(transactionId)
                .map(Transaction::getTransactionStatus)
                .orElse(null);

        if (currentStatus == null) {
            logger.warning("Transaction not found with ID: " + transactionId);
            return false;
        }

        if (currentStatus == TransactionStatus.COMPLETED &&
                (newStatus == TransactionStatus.PENDING || newStatus == TransactionStatus.FAILED)) {
            logger.warning("Invalid status update from " + currentStatus + " to " + newStatus);
            return false;
        }

        if (currentStatus == newStatus) {
            logger.info("Status already " + newStatus + " — no update required");
            return true;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(TransactionSQLQueries.UPDATE_TRANSACTION_STATUS)) {

            preparedStatement.setString(1, newStatus.toString().toUpperCase());
            preparedStatement.setLong(2, transactionId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Successfully updated transaction status to " + newStatus +
                        " for transaction ID: " + transactionId);
                return true;
            }

            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating transaction status for ID: " + transactionId, e);
            throw new TransactionAccessException("Error updating transaction status for transaction ID: " + transactionId, e);
        }
    }

    @Override
    public boolean deposit(Long accountId, BigDecimal amount) {
        if (accountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot deposit money: accountId is null or amount is < 0");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement selectStmt =
                    connection.prepareStatement(AccountSQLQueries.SELECT_ACCOUNT_BALANCE_AND_STATUS)) {

            connection.setAutoCommit(false);

            selectStmt.setLong(1, accountId);

            BigDecimal currentBalance = null;
            BigDecimal newBalance = null;

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (!resultSet.next()) {
                    logger.warning("Account with ID: " + accountId + " not found");
                    connection.rollback();
                    return false;
                }

                AccountStatus status = AccountStatus.valueOf(resultSet.getString("status").toUpperCase());
                if (status == AccountStatus.DELETED || status == AccountStatus.CLOSED) {
                    logger.warning("Cannot deposit: Account is closed: " + accountId);
                    connection.rollback();
                    return false;
                }

                currentBalance = resultSet.getBigDecimal("account_balance");
                newBalance = currentBalance.add(amount);
            }

            try (PreparedStatement updateStmt =
                         connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_BALANCE)) {
                updateStmt.setBigDecimal(1, newBalance);
                updateStmt.setLong(2, accountId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    logger.warning("Failed to update balance for account: " + accountId);
                    connection.rollback();
                    return false;
                }
            }

            Transaction depositTransaction = new Transaction();
            depositTransaction.setFromAccountId(null);
            depositTransaction.setToAccountId(accountId);
            depositTransaction.setTransactionType(TransactionType.DEPOSIT);
            depositTransaction.setAmount(amount);
            depositTransaction.setAvailableBalanceAfter(newBalance);
            depositTransaction.setTransactionDate(LocalDateTime.now());
            depositTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            Long transactionId = createTransaction(depositTransaction, connection);

            if (transactionId == null) {
                logger.warning("Failed to log transaction for deposit!");
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while depositing: " + amount, e);
            throw new DepositException("Error while depositing money for account ID: " + accountId, e);
        }
    }

    @Override
    public boolean withdraw(Long accountId, BigDecimal amount) {
        if (accountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot withdraw money: accountId is null or amount is < 0");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement selectStmt =
                     connection.prepareStatement(AccountSQLQueries.SELECT_ACCOUNT_BALANCE_AND_STATUS)) {

            connection.setAutoCommit(false);

            selectStmt.setLong(1, accountId);

            BigDecimal currentBalance = null;
            BigDecimal newBalance = null;

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (!resultSet.next()) {
                    logger.warning("Account with ID: " + accountId + " not found");
                    connection.rollback();
                    return false;
                }

                AccountStatus status = AccountStatus.valueOf(resultSet.getString("status").toUpperCase());
                if (status == AccountStatus.DELETED || status == AccountStatus.CLOSED) {
                    logger.warning("Cannot deposit: Account is closed: " + accountId);
                    connection.rollback();
                    return false;
                }

                currentBalance = resultSet.getBigDecimal("account_balance");

                if (currentBalance.compareTo(amount) < 0) {
                    logger.warning("Insufficient balance for withdrawal. Account ID: " + accountId);
                    connection.rollback();
                    return false;
                }

                newBalance = currentBalance.subtract(amount);
            }

            try (PreparedStatement updateStmt =
                         connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_BALANCE)) {
                updateStmt.setBigDecimal(1, newBalance);
                updateStmt.setLong(2, accountId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    logger.warning("Failed to update balance for account: " + accountId);
                    connection.rollback();
                    return false;
                }
            }

            Transaction withdrawTransaction = new Transaction();
            withdrawTransaction.setFromAccountId(accountId);
            withdrawTransaction.setToAccountId(null);
            withdrawTransaction.setTransactionType(TransactionType.WITHDRAW);
            withdrawTransaction.setAmount(amount);
            withdrawTransaction.setAvailableBalanceAfter(newBalance);
            withdrawTransaction.setTransactionDate(LocalDateTime.now());
            withdrawTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            Long transactionId = createTransaction(withdrawTransaction, connection);

            if (transactionId == null) {
                logger.warning("Failed to log transaction for withdraw!");
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while withdrawing: " + amount, e);
            throw new WithdrawException("Error while withdrawing money for account ID: " + accountId, e);
        }
    }

    @Override
    public Long transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId == null || toAccountId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning("Cannot transfer money: accountId is null or amount is < 0");
            return null;
        }

        if (fromAccountId.equals(toAccountId)) {
            logger.warning("Transfer to the same account is not allowed");
            return null;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection()) {

            connection.setAutoCommit(false);

            BigDecimal currentFromAccountBalance;
            BigDecimal newFromAccountBalance;

            try (PreparedStatement selectFromAccountStmt =
                         connection.prepareStatement(AccountSQLQueries.SELECT_ACCOUNT_BALANCE_AND_STATUS)) {

                selectFromAccountStmt.setLong(1, fromAccountId);

                try (ResultSet resultSet = selectFromAccountStmt.executeQuery()) {
                    if (!resultSet.next()) {
                        logger.warning("Account with ID: " + fromAccountId + " not found");
                        connection.rollback();
                        return null;
                    }

                    AccountStatus status = AccountStatus.valueOf(resultSet.getString("status").toUpperCase());
                    if (status == AccountStatus.DELETED || status == AccountStatus.CLOSED) {
                        logger.warning("Cannot transfer amount: Account is closed: " + fromAccountId);
                        connection.rollback();
                        return null;
                    }

                    currentFromAccountBalance = resultSet.getBigDecimal("account_balance");

                    if (currentFromAccountBalance.compareTo(amount) < 0) {
                        logger.warning("Insufficient balance cannot transfer amount. Account ID: " + fromAccountId);
                        connection.rollback();
                        return null;
                    }

                    newFromAccountBalance = currentFromAccountBalance.subtract(amount);
                }

                try (PreparedStatement updateFromAccountStmt =
                             connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_BALANCE)) {
                    updateFromAccountStmt.setBigDecimal(1, newFromAccountBalance);
                    updateFromAccountStmt.setLong(2, fromAccountId);
                    int rowsAffected = updateFromAccountStmt.executeUpdate();

                    if (rowsAffected == 0) {
                        logger.warning("Failed to update balance for account: " + fromAccountId);
                        connection.rollback();
                        return null;
                    }
                }
            }

            try (PreparedStatement selectToAccountStmt =
                    connection.prepareStatement(AccountSQLQueries.SELECT_ACCOUNT_BALANCE_AND_STATUS)) {

                selectToAccountStmt.setLong(1, toAccountId);

                BigDecimal currentToAccountBalance = null;
                BigDecimal newToAccountBalance = null;

                try (ResultSet resultSet = selectToAccountStmt.executeQuery()) {
                    if (!resultSet.next()) {
                        logger.warning("Account with ID: " + toAccountId + " not found");
                        connection.rollback();
                        return null;
                    }

                    AccountStatus status = AccountStatus.valueOf(resultSet.getString("status").toUpperCase());
                    if (status == AccountStatus.DELETED || status == AccountStatus.CLOSED) {
                        logger.warning("Cannot transfer: Account is closed: " + toAccountId);
                        connection.rollback();
                        return null;
                    }

                    currentToAccountBalance = resultSet.getBigDecimal("account_balance");
                    newToAccountBalance = currentToAccountBalance.add(amount);
                }

                try (PreparedStatement updateToAccountStmt =
                             connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_BALANCE)) {
                    updateToAccountStmt.setBigDecimal(1, newToAccountBalance);
                    updateToAccountStmt.setLong(2, toAccountId);
                    int rowsAffected = updateToAccountStmt.executeUpdate();

                    if (rowsAffected == 0) {
                        logger.warning("Failed to update balance for account: " + toAccountId);
                        connection.rollback();
                        return null;
                    }
                }
            }

            Transaction transferTransaction = new Transaction();
            transferTransaction.setFromAccountId(fromAccountId);
            transferTransaction.setToAccountId(toAccountId);
            transferTransaction.setTransactionType(TransactionType.TRANSFER);
            transferTransaction.setAmount(amount);
            transferTransaction.setAvailableBalanceAfter(newFromAccountBalance);
            transferTransaction.setTransactionDate(LocalDateTime.now());
            transferTransaction.setTransactionStatus(TransactionStatus.COMPLETED);

            Long transactionId = createTransaction(transferTransaction, connection);

            if (transactionId == null) {
                logger.warning("Failed to log transaction for transfer!");
                connection.rollback();
                return null;
            }

            connection.commit();
            return transactionId;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while transferring " + amount + " from account ID: " +
                    fromAccountId + " to account ID: " + toAccountId, e);
            throw new TransferException("Error while transferring " + amount + " from account ID: " +
                    fromAccountId + " to account ID: " + toAccountId, e);
        }
    }

    private Transaction mapResultSetToTransaction (ResultSet resultSet) throws SQLException{
        TransactionType transactionType =
                TransactionType.valueOf(resultSet.getString("transaction_type").toUpperCase());
        TransactionStatus transactionStatus =
                TransactionStatus.valueOf(resultSet.getString("transaction_status").toUpperCase());
        LocalDateTime transactionDate =
                resultSet.getTimestamp("transaction_date").toLocalDateTime();

        return new Transaction(
                resultSet.getLong("transaction_id"),
                resultSet.getLong("from_account_id"),
                resultSet.getLong("to_account_id"),
                transactionType,
                resultSet.getBigDecimal("amount"),
                resultSet.getBigDecimal("available_balance_after"),
                transactionDate,
                transactionStatus
        );
    }

}
