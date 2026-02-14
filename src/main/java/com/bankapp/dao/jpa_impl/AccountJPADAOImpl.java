package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.AccountDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.exception.DAO_exceptions.AccountAccessException;
import com.bankapp.model.*;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountJPADAOImpl implements AccountDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createAccount(Account account) {
        if (account == null) {
            logger.warning("Cannot create account: Account is null");
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            entityManager.persist(account);

            transaction.commit();
            return account.getAccountId();

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE, "Database error while creating account", e);
            throw new AccountAccessException("Database error while creating account", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Account> query =
                    entityManager.createQuery(
                            "SELECT a FROM Account a",
                            Account.class
                    );

            return query.getResultList();

        } catch (Exception e) {
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

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.info("No account found with ID: " + accountId);
                return Optional.empty();
            }

            return Optional.of(account);

        } catch (Exception e) {
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

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Account> query =
                    entityManager.createQuery(
                            "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber",
                            Account.class
                    );

            query.setParameter("accountNumber", accountNumber);

            List<Account> results = query.getResultList();
            if (results.isEmpty()) {
                logger.info("No account found for account number: " + accountNumber);
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching account by account number: " + accountNumber, e);
            throw new AccountAccessException("Error fetching account by account number: " + accountNumber, e);
        }
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve account: userId is null");
            return Collections.emptyList();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Account> query =
                    entityManager.createQuery(
                            "SELECT a FROM Account a WHERE a.user.userId = :userId",
                            Account.class
                    );

            query.setParameter("userId", userId);

            return query.getResultList();

        } catch (Exception e) {
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

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(a) FROM Account a WHERE a.accountNumber = :accountNumber",
                            Long.class
                    );

            query.setParameter("accountNumber", accountNumber);

            Long count =  query.getSingleResult();

            return count > 0;

        } catch (Exception e) {
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

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Query query =
                    entityManager.createQuery(
                                    "UPDATE Account a " +
                                    "SET a.accountType = :newType, a.overdraftLimit = :overdraftLimit " +
                                    "WHERE a.accountId = :accountId " +
                                    "AND a.accountStatus <> com.bankapp.model.AccountStatus.DELETED"
                    );

            query.setParameter("newType", newType);
            query.setParameter("overdraftLimit", overdraftLimit);
            query.setParameter("accountId", accountId);

            int rowsAffected = query.executeUpdate();

            transaction.commit();
            return rowsAffected > 0;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating account type for ID: " + accountId, e);
            throw new AccountAccessException("Error updating account type for account ID: " + accountId, e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean updateAccountBalance(Long accountId, BigDecimal newBalance) {
        if (accountId == null || newBalance == null) {
            logger.warning("Cannot update account balance: accountId or newBalance is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.info("Account with ID: " + accountId + " does not exist");
                transaction.rollback();
                return false;
            }

            account.setAccountBalance(newBalance);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating account balance for ID: " + accountId, e);
            throw new AccountAccessException("Error updating account balance for account ID: " + accountId, e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean updateAccountStatus(Long accountId, AccountStatus newStatus) {
        if (accountId == null || newStatus == null) {
            logger.warning("Cannot update account status: accountId or newStatus is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.info("Account with ID: " + accountId + " does not exist");
                transaction.rollback();
                return false;
            }

            if (account.getAccountStatus() == AccountStatus.DELETED) {
                logger.info("Account with ID: " + accountId + " is DELETED");
                transaction.rollback();
                return false;
            }

            account.setAccountStatus(newStatus);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating account status for ID: " + accountId, e);
            e.printStackTrace();
            throw new AccountAccessException("Error updating account status for account ID: " + accountId, e);

        } finally {
            entityManager.close();
        }

    }

    @Override
    public boolean deleteAccount(Long accountId) {
        if (accountId == null) {
            logger.warning("Cannot delete account: accountId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            if (account == null) {
                logger.info("Account with ID: " + accountId + " does not exist");
                transaction.rollback();
                return false;
            }

            if (account.getAccountStatus() == AccountStatus.DELETED) {
                logger.info("Account with ID: " + accountId + " is already deleted");
                transaction.rollback();
                return false;
            }

            if (account.getAccountBalance().compareTo(BigDecimal.ZERO) > 0) {
                logger.info("Cannot deleted account, account balance is positive");
                transaction.rollback();
                return false;
            }

            account.setAccountStatus(AccountStatus.DELETED);
            account.setClosingDate(LocalDate.now());

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE,
                    "Database error while soft deleting account for ID: " + accountId, e);
            throw new AccountAccessException(
                    "Error soft deleting account for account ID: " + accountId, e);
        }
        finally {
            entityManager.close();
        }
    }
}
