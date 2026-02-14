package com.bankapp.dao;

import com.bankapp.exception.DAO_exceptions.DataAccessException;
import com.bankapp.model.User;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.sql.Connection;
import java.util.Optional;
import java.util.logging.Level;

public interface UserDAO {

    Long createUser(User user);
    Long createUser(User user, Object persistenceContext);

    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserForAuthentication(String identifier);
    Optional<User> getUserByIdentifier(String identifier);

    boolean updateUser(User user);
    boolean updateUserUsername(Long userId, String newUsername);
    boolean updateUserPassword(Long userId, String newHashedPassword);

    boolean deleteUser(Long userId);

    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
    boolean userExistsByUserId(Long userId);

    default boolean userPassportNumberExists(String userPassportNumber) {
        EntityManager entityManager = JPAUtil.getEntityManager();

        try {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(u) from User u " +
                                    "WHERE u.passportNumber = :passportNumber",
                            Long.class
                    );

            query.setParameter("passportNumber", userPassportNumber);

            Long count = query.getSingleResult();
            return count > 0;

        } catch (Exception ex) {
            throw new DataAccessException("Error reading passport number from database", ex);
        } finally {
            entityManager.close();
        }
    }

    default boolean userPhoneNumberExists(String userPhoneNumber) {
        EntityManager entityManager = JPAUtil.getEntityManager();

        try {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT COUNT(u) from User u " +
                                    "WHERE u.phoneNumber = :phoneNumber",
                            Long.class
                    );

            query.setParameter("phoneNumber", userPhoneNumber);

            Long count = query.getSingleResult();
            return count > 0;

        } catch (Exception ex) {
            throw new DataAccessException("Error reading passport number from database", ex);
        } finally {
            entityManager.close();
        }
    }

}
