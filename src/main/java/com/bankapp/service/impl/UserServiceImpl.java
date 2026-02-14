package com.bankapp.service.impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.EmploymentProfileDAO;
import com.bankapp.dao.UserAddressDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.dao.UserRoleDAO;
import com.bankapp.exception.service_exceptions.user_service.*;
import com.bankapp.model.*;
import com.bankapp.security.PasswordHasher;
import com.bankapp.service.UserService;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public Long createUser(User user, UserAddress userAddress, EmploymentProfile employmentProfile) {

        if (user == null) {
            throw new InvalidUserDataException("User cannot be null");
        }

        if (userDAO.userExistsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + user.getEmail());
        }

        if (userDAO.userPassportNumberExists(user.getPassportNumber())) {
            throw new InvalidUserDataException("Passport number already exists: " + user.getPassportNumber());
        }

        if (userDAO.userPhoneNumberExists(user.getPhoneNumber())) {
            throw new InvalidUserDataException("Phone number already exists: " + user.getPhoneNumber());
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            user.setPasswordHash(
                    PasswordHasher.hashPassword(user.getPasswordHash())
            );

            if (userAddress != null) {
                user.addAddress(userAddress);
            }

            if  (employmentProfile != null) {
                user.setEmploymentProfile(employmentProfile);
            }

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setUserRole(Role.USER);
            user.setUserRole(userRole);

            userDAO.createUser(user, entityManager);

            transaction.commit();

            return user.getUserId();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new UserCreationException("Database error: " +  e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        if (userId == null) {
            throw new InvalidUserDataException("UserId cannot be null");
        }

        return userDAO.getUserById(userId);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new InvalidUserDataException("Username cannot be empty/null");
        }

        return userDAO.getUserByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new InvalidUserDataException("Email cannot be empty/null");
        }

        return userDAO.getUserByEmail(email);
    }

    @Override
    public User getUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidUserDataException("Identifier cannot be empty/null");
        }

        return userDAO.getUserByIdentifier(identifier)
                .orElseThrow(() ->
                            new EmailAddressNotFoundException("Identifier: " + identifier + " not found")
                        );
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null) {
            throw new InvalidUserDataException("User cannot be null");
        }

        if (user.getUserId() == null) {
            throw new  InvalidUserDataException("UserId cannot be null");
        }

        User user1 = userDAO.getUserById(user.getUserId())
                .orElseThrow(() ->
                            new UserNotFoundException("User not found")
                        );

        user.setPasswordHash(user1.getPasswordHash());
        boolean success = userDAO.updateUser(user);
        if (!success) {
            throw new UserException("Failed to update user");
        }

        return true;
    }

    @Override
    public boolean updateUserUsername(Long userId, String newUsername) {
        if (userId == null || newUsername == null || newUsername.isEmpty()) {
            throw new InvalidUserDataException("userId and username cannot be empty/null");
        }

        if (!userDAO.updateUserUsername(userId, newUsername)) {
            throw new UserNotFoundException("User not found");
        }

        return true;
    }

    @Override
    public boolean updateUserPassword(Long userId, String newHashedPassword) {
        if (userId == null || newHashedPassword.isEmpty()) {
            throw new InvalidUserDataException("userId and password cannot be empty/null");
        }

        String hashedPassword =  PasswordHasher.hashPassword(newHashedPassword);
        return userDAO.updateUserPassword(userId, hashedPassword);
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new InvalidUserDataException("UserId cannot be null");
        }

        if (!userDAO.deleteUser(userId)) {
            throw new UserNotFoundException("User not found");
        }

        return true;
    }

    @Override
    public boolean userExistsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new InvalidUserDataException("Username cannot be empty/null");
        }

        return userDAO.userExistsByUsername(username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new InvalidUserDataException("Email cannot be empty/null");
        }

        return userDAO.userExistsByEmail(email);
    }
}
