package com.bankapp.service;

import com.bankapp.dao.UserDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.dao.jpa_impl.UserJPADAOImpl;
import com.bankapp.exception.service_exceptions.authentication_service.AuthenticationFailedException;
import com.bankapp.model.User;
import com.bankapp.security.PasswordHasher;

import java.util.Optional;

public class AuthenticationService {

    private final UserDAO  userDAO;

    public AuthenticationService() {
        this.userDAO = new UserJPADAOImpl();
    }

    public User authenticate(String identifier, String password) {
        Optional<User> userOptional = userDAO.getUserForAuthentication(identifier);

        if (!userOptional.isPresent()) {
            System.out.println("User not found with identifier");
            throw new AuthenticationFailedException("Invalid username/email or password");
        }

        User user = userOptional.get();

        if (!PasswordHasher.checkPassword(password, user.getPasswordHash())) {
            System.out.println("Password authentication failed");
            throw new AuthenticationFailedException("Invalid username/email or password");
        }

        return user;
    }

}
