package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.sql.AccountSQLQueries;
import com.bankapp.exception.DAO_exceptions.AccountAccessException;
import com.bankapp.model.Account;
import com.bankapp.model.AccountStatus;
import com.bankapp.model.AccountType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountDAOImpl implements AccountDAO {

    private static final Logger logger = Logger.getLogger(AccountDAOImpl.class.getName());

    @Override
    public Long createAccount(Account account) {
        if (account == null || account.getAccountOwnerId() == null) {
            logger.warning("Cannot create account: Account or AccountOwnerId is null");
            return null;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             AccountSQLQueries.INSERT_ACCOUNT,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, account.getAccountOwnerId());
            preparedStatement.setString(2, account.getAccountType().toString().toUpperCase());
            preparedStatement.setBigDecimal(3, account.getAccountBalance());
            preparedStatement.setString(4, account.getBicSwiftCode());
            preparedStatement.setBigDecimal(5, account.getOverdraftLimit());
            preparedStatement.setDate(6, Date.valueOf(account.getOpeningDate()));
            preparedStatement.setString(7, account.getAccountStatus().toString().toUpperCase());
            preparedStatement.setString(8, account.getTransactionPinHash());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert account - no rows affected.");
                throw new AccountAccessException("Account creation failed - No rows affected", null);
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long accountId = generatedKeys.getLong(1);
                    account.setAccountId(accountId);
                    logger.info("Account created successfully with ID: " + accountId);
                    return accountId;
                } else {
                    logger.severe("Failed to fetch generated account ID");
                    throw new AccountAccessException("Account ID generation failed", null);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating account", e);
            throw new AccountAccessException("Database error while creating account", e);
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account>  accounts = new ArrayList<>();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.SELECT_ALL_NON_DELETED_ACCOUNTS);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                accounts.add(mapResultSetToAccount(resultSet));
            }

            logger.info("Successfully retrieved " + accounts.size() + " accounts from database");
            return accounts;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while retrieving all accounts", e);
            throw new AccountAccessException("Error retrieving accounts", e);
        }
    }

    @Override
    public Optional<Account> getAccountById(Long accountId) {
        if (accountId == null) {
            logger.warning("Cannot retrieve account: accountId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.SELECT_NON_DELETED_ACCOUNT_BY_ID)) {

            preparedStatement.setLong(1, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    Account account = mapResultSetToAccount(resultSet);
                    return Optional.of(account);

                } else {
                    logger.info("No account found with ID: " + accountId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching account by ID: " + accountId, e);
            throw new AccountAccessException("Error fetching account by ID: " + accountId, e);
        }
    }

    @Override
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            logger.warning("Cannot retrieve account: accountNumber is null/empty");
            return Optional.empty();
        }

        accountNumber = accountNumber.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.SELECT_NON_DELETED_ACCOUNT_BY_ACCOUNT_NUMBER)) {

            preparedStatement.setString(1, accountNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToAccount(resultSet));
                } else {
                    logger.info("No account found with number: " + accountNumber);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching account by account number: " + accountNumber, e);
            throw new AccountAccessException("Error fetching account by account number: " + accountNumber, e);
        }
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        List<Account>  accounts = new ArrayList<>();

        if (userId == null) {
            logger.warning("Cannot retrieve account: userId is null");
            return accounts;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement(AccountSQLQueries.SELECT_NON_DELETED_ACCOUNTS_BY_USER_ID)) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    accounts.add(mapResultSetToAccount(resultSet));
                }

                if (accounts.isEmpty()) {
                    logger.info("No account found for user ID: " + userId);
                } else {
                    logger.info("Successfully retrieved " + accounts.size() + " accounts from database");
                }

                return accounts;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching account by user ID: " + userId, e);
            throw new AccountAccessException("Error fetching account by user ID: " + userId, e);
        }
    }

    @Override
    public boolean accountExistsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            logger.warning("Cannot check account existence: accountNumber is null/empty");
            return false;
        }

        accountNumber = accountNumber.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.CHECK_ACCOUNT_NUMBER_EXISTS)) {

            preparedStatement.setString(1, accountNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching account by account number: " + accountNumber, e);
            throw new AccountAccessException("Error fetching account by account number: " + accountNumber, e);
        }
    }

    @Override
    public boolean changeAccountType(Long accountId, AccountType newType, BigDecimal overdraftLimit) {
        if (accountId == null || newType == null) {
            logger.warning("Cannot update account type: accountId or newType is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_TYPE)) {

            preparedStatement.setString(1, newType.toString().toUpperCase());
            preparedStatement.setBigDecimal(2, overdraftLimit);
            preparedStatement.setLong(3, accountId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Successfully updated account type to " + newType + " for account ID: " + accountId);
                return true;
            }

            logger.warning("No account found with ID: " + accountId + " — update skipped");
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating account type for ID: " + accountId, e);
            throw new AccountAccessException("Error updating account type for account ID: " + accountId, e);
        }
    }

    @Override
    public boolean updateAccountBalance(Long accountId, BigDecimal newBalance) {
        if (accountId == null || newBalance == null) {
            logger.warning("Cannot update account balance: accountId or newBalance is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.UPDATE_ACCOUNT_BALANCE)) {

            preparedStatement.setBigDecimal(1, newBalance);
            preparedStatement.setLong(2, accountId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Successfully updated account balance to " + newBalance + " for account ID: " + accountId);
                return true;
            }

            logger.warning("No account found with ID: " + accountId + " — update skipped");
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating account balance for ID: " + accountId, e);
            throw new AccountAccessException("Error updating account balance for account ID: " + accountId, e);
        }
    }

    @Override
    public boolean updateAccountStatus(Long accountId, AccountStatus newStatus) {
        if (accountId == null || newStatus == null) {
            logger.warning("Cannot update account status: accountId or newStatus is null");
            return false;
        }

        String sql;

        if (newStatus.equals(AccountStatus.CLOSED)) {
            sql = AccountSQLQueries.UPDATE_ACCOUNT_STATUS_TO_CLOSED;
        } else {
            sql = AccountSQLQueries.UPDATE_ACCOUNT_STATUS;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(sql)) {

            preparedStatement.setString(1, newStatus.toString().toUpperCase());
            preparedStatement.setLong(2, accountId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Successfully updated account status to " + newStatus + " for account ID: " + accountId);
                return true;
            }

            logger.warning("Update skipped - Make sure account ID exists and account balance is 0");
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating account status for ID: " + accountId, e);
            throw new AccountAccessException("Error updating account status for account ID: " + accountId, e);
        }
    }

    @Override
    public boolean deleteAccount(Long accountId) {
        if (accountId == null) {
            logger.warning("Cannot delete account: accountId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(AccountSQLQueries.DELETE_ACCOUNT)) {

            preparedStatement.setLong(1, accountId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Soft-deleted account with ID: " + accountId);
                return true;
            }

            logger.warning("Deletion skipped - Make sure account ID exists and account balance is 0");
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE,
                    "Database error while soft deleting account for ID: " + accountId, e);
            throw new AccountAccessException(
                    "Error soft deleting account for account ID: " + accountId, e);
        }
    }

    private Account mapResultSetToAccount (ResultSet resultSet) throws SQLException{
        AccountType accountType = AccountType.valueOf(resultSet.getString("account_type"));
        AccountStatus accountStatus = AccountStatus.valueOf(resultSet.getString("status"));
        LocalDate closingDate = resultSet.getDate("closing_date") != null ?
                resultSet.getDate("closing_date").toLocalDate() : null;

        return new Account(
                        resultSet.getLong("account_id"),
                        resultSet.getString("account_number"),
                        resultSet.getString("transaction_pin_hash"),
                        resultSet.getLong("account_owner_id"),
                        accountType,
                        resultSet.getBigDecimal("account_balance"),
                        resultSet.getString("bic_swift_code"),
                        resultSet.getBigDecimal("overdraft_limit"),
                        resultSet.getDate("opening_date").toLocalDate(),
                        closingDate,
                        accountStatus,
                        resultSet.getString("status_reason")
        );
    }
}
