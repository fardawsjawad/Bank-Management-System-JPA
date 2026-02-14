package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.UserDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.exception.DAO_exceptions.DAOException;
import com.bankapp.exception.DAO_exceptions.DataAccessException;
import com.bankapp.model.*;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class UserJPADAOImpl implements UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createUser(User user) {
        if (user == null) {
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(user);

            transaction.commit();

            return user.getUserId();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while creating user", ex);
            throw new DataAccessException("Error inserting user into database", ex);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long createUser(User user, Object persistenceContext) {
        if (!(persistenceContext instanceof EntityManager)) {
            throw new IllegalArgumentException("Expected JPA EntityManager");
        }

        EntityManager entityManager = (EntityManager) persistenceContext;

        if (user == null) {
            return null;
        }

        try {
            entityManager.persist(user);
            return user.getUserId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while creating user", e);
            throw new DataAccessException("Error inserting user into database", e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve user: userId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<User> query =
                    entityManager.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.userAddresses " +
                                    "LEFT JOIN FETCH u.userRole " +
                                    "LEFT JOIN FETCH u.employmentProfile " +
                                    "WHERE u.userId = :id",
                            User.class
                    );

            query.setParameter("id", id);

            List<User> users = query.getResultList();
            if (users.isEmpty()) {
                logger.info("No user found with ID: " + id);
                return Optional.empty();
            }

            return Optional.of(users.get(0));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching user by ID: " + id, e);
            throw new DAOException("Error fetching user by ID: " + id, e);
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            logger.warning("Cannot retrieve user: username is null or blank");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<User> query =
                    entityManager.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.userAddresses " +
                                    "LEFT JOIN FETCH u.userRole " +
                                    "LEFT JOIN FETCH u.employmentProfile " +
                                    "WHERE u.username = :username",
                            User.class
                    );

            query.setParameter("username", username);

            List<User> users = query.getResultList();
            if (users.isEmpty()) {
                logger.info("No user found with username: " + username);
                return Optional.empty();
            }

            return Optional.of(users.get(0));

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching user by username: " + username, e);
            throw new DAOException("Error fetching user by username: " + username, e);

        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            logger.warning("Cannot retrieve user: email is null or blank");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<User> query =
                    entityManager.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.userAddresses " +
                                    "LEFT JOIN FETCH u.userRole " +
                                    "LEFT JOIN FETCH u.employmentProfile " +
                                    "WHERE u.email = :email",
                            User.class
                    );

            query.setParameter("email", email);

            List<User> users = query.getResultList();
            if (users.isEmpty()) {
                logger.info("No user found with email: " + email);
                return Optional.empty();
            }

            return Optional.of(users.get(0));

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching user by email: " + email, e);
            throw new DAOException("Error fetching user by email: " + email, e);

        }
    }

    @Override
    public Optional<User> getUserForAuthentication(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            logger.warning("Cannot check user existence: identifier is null");
            return Optional.empty();
        }

        identifier = identifier.trim();

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<User> query =
                    entityManager.createQuery(
                            "SELECT u " +
                                    "FROM User u " +
                                    "LEFT JOIN FETCH u.userAddresses " +
                                    "LEFT JOIN FETCH u.employmentProfile " +
                                    "LEFT JOIN FETCH u.userRole " +
                                    "WHERE u.username = :identifier OR u.email = :identifier",
                            User.class
                    );

            query.setParameter("identifier", identifier);

            List<User> results = query.getResultList();

            if (results.isEmpty()) {
                return Optional.empty();
            }

            if (results.size() > 1) {
                throw new DAOException("Multiple users found for identifier: " + identifier);
            }

            User user = results.get(0);
            user.setRole(user.getUserRole().getUserRole());

            return Optional.of(user);

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching user for authentication: " + identifier, e);
            throw new DAOException("Database error while fetching user for authentication: " + identifier, e);

        }
    }

    @Override
    public Optional<User> getUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            logger.warning("Cannot check user existence: identifier is null");
            return Optional.empty();
        }

        identifier = identifier.trim();

        try (EntityManager entityManager =  JPAUtil.getEntityManager()) {

            TypedQuery<User> query =
                    entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier",
                            User.class
                    );

            query.setParameter("identifier", identifier);

            List<User> results = query.getResultList();

            if (results.isEmpty()) {
                logger.info("No user found with identifier: " + identifier);
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching user by identifier: " + identifier, e);
            throw new DAOException("Database error while fetching user by identifier: " + identifier, e);
        }
    }

    @Override
    public boolean updateUser(User user) {


        if (user == null) {
            logger.warning("Cannot update user: user is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            entityManager.merge(user);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating user", e);
            throw new DataAccessException("Error updating user in database", e);

        } finally {
            entityManager.close();
        }

    }

    @Override
    public boolean updateUserUsername(Long userId, String newUsername) {
        if (userId == null || newUsername == null || newUsername.isEmpty()) {
            logger.warning("Cannot update username: userId or newUsername is null/empty");
            return false;
        }

        newUsername = newUsername.trim();

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            Query query =
                    entityManager.createQuery(
                            "UPDATE User u SET u.username = :newUsername WHERE u.userId = :userId"
                    );

            query.setParameter("newUsername", newUsername);
            query.setParameter("userId", userId);

            int rowsAffected = query.executeUpdate();

            transaction.commit();

            return rowsAffected > 0;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating username for user ID: " + userId, e);
            throw new DAOException("Error updating username for user ID: " + userId, e);

        } finally {
            entityManager.close();
        }

    }

    @Override
    public boolean updateUserPassword(Long userId, String newHashedPassword) {
        if (userId == null || newHashedPassword == null || newHashedPassword.isEmpty()) {
            logger.warning("Cannot update password: userId or newPassword is null/empty");
            return false;
        }

        newHashedPassword = newHashedPassword.trim();

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            Query query =
                    entityManager.createQuery(
                            "UPDATE User u SET u.passwordHash = :newHashedPassword WHERE u.userId = :userId"
                    );

            query.setParameter("newHashedPassword", newHashedPassword);
            query.setParameter("userId", userId);

            int rowsAffected = query.executeUpdate();

            transaction.commit();

            return rowsAffected > 0;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating password for user ID: " + userId, e);
            throw new DAOException("Error updating password for user ID: " + userId, e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            logger.warning("Cannot delete user: userId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            User user = entityManager.find(User.class, userId);
            if (user == null) return false;

            entityManager.remove(user);

            transaction.commit();
            return true;

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean userExistsByUsername(String username) {
        if (username == null || username.isBlank()) {
            logger.warning("Cannot retrieve user: username is  null or blank");
            return false;
        }

        username = username.trim();

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :username",
                            Long.class
                    );

            query.setParameter("username", username);

            Long count = query.getSingleResult();

            return count > 0;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while checking username existence: " + username, e);
            throw new DAOException("Error checking username existence: " + username, e);
        }
    }

    @Override
    public boolean userExistsByEmail(String email) {
        if (email == null || email.isBlank()) {
            logger.warning("Cannot retrieve user: email is  null or blank");
            return false;
        }

        email = email.trim();

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                            Long.class
                    );

            query.setParameter("email", email);

            Long count = query.getSingleResult();

            return count > 0;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while checking email existence: " + email, e);
            throw new DAOException("Error checking email existence: " + email, e);
        }
    }

    @Override
    public boolean userExistsByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve user: userId is  null");
            return false;
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.userId = :userId",
                            Long.class
                    );

            query.setParameter("userId", userId);

            Long count = query.getSingleResult();

            return count > 0;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while checking userId existence: " + userId, e);
            throw new DAOException("Error checking userId existence: " + userId, e);
        }
    }

}
