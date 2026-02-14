package com.bankapp.service.impl;

import com.bankapp.dao.UserDAO;
import com.bankapp.dao.UserRoleDAO;
import com.bankapp.exception.service_exceptions.user_role.InvalidUserRoleDataException;
import com.bankapp.exception.service_exceptions.user_role.UserRoleCreationException;
import com.bankapp.exception.service_exceptions.user_role.UserRoleException;
import com.bankapp.exception.service_exceptions.user_role.UserRoleNotFoundException;
import com.bankapp.exception.service_exceptions.user_service.UserNotFoundException;
import com.bankapp.model.Role;
import com.bankapp.model.User;
import com.bankapp.model.UserRole;
import com.bankapp.service.UserRoleService;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;


import java.util.List;
import java.util.Optional;

public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleDAO userRoleDAO;
    private final UserDAO  userDAO;

    public UserRoleServiceImpl(UserRoleDAO userRoleDAO, UserDAO userDAO) {
        this.userRoleDAO = userRoleDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Long createUserRole(UserRole userRole) {

        if (userRole == null || userRole.getUser() == null) {
            throw new InvalidUserRoleDataException("UserRole or User cannot be null");
        }

        Optional<User> userOptional = userDAO.getUserById(
                userRole.getUser().getUserId()
        );

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found");
        }

        User user = userOptional.get();

        userRole.setUserRole(Role.USER);
        userRole.setUser(user);

        return userRoleDAO.createUserRole(userRole);
    }

    @Override
    public Optional<UserRole> getRoleById(Long id) {
        if (id == null) {
            throw new InvalidUserRoleDataException("id must not be null");
        }

        return userRoleDAO.getRoleById(id);
    }

    @Override
    public Optional<UserRole> getRoleByUserId(Long userId) {
        if (userId == null) {
            throw new  InvalidUserRoleDataException("userId must not be null");
        }

        return userRoleDAO.getRoleByUserId(userId);
    }

    @Override
    public List<UserRole> getAllUserRoles() {
        return userRoleDAO.getAllUserRoles();
    }

    @Override
    public boolean updateRole(UserRole updatedUserRole) {
        if (updatedUserRole == null) {
            throw new InvalidUserRoleDataException("userRole must not be null");
        }

        if (updatedUserRole.getRoleId() == null) {
            throw new InvalidUserRoleDataException("roleId must not be null");
        }

        if (updatedUserRole.getUser() == null || updatedUserRole.getUser().getUserId() == null) {
            throw new InvalidUserRoleDataException("userId must not be null");
        }

        if (updatedUserRole.getUserRole() == null) {
            throw new InvalidUserRoleDataException("Role must not be null");
        }

        Long userId = updatedUserRole.getUser().getUserId();

        if (!userDAO.userExistsByUserId(userId)) {
            throw new UserNotFoundException("User with ID: " + userId + " does not exist");
        }

        UserRole currentRole = userRoleDAO.getRoleById(updatedUserRole.getRoleId())
                .orElseThrow(() ->
                        new UserRoleNotFoundException(
                                "Role with ID: " + updatedUserRole.getRoleId() + " does not exist"
                        )
                );

        if (currentRole.getUserRole().equals(updatedUserRole.getUserRole())) {
            throw new UserRoleException(
                    "Current user role is already " + updatedUserRole.getUserRole()
            );
        }

        boolean updated = userRoleDAO.updateRole(updatedUserRole);
        if (!updated) {
            throw new UserRoleException(
                    "Failed to update user role"
            );
        }

        return true;
    }

    @Override
    public boolean deleteRole(Long roleId) {

        if (roleId == null) {
            throw new InvalidUserRoleDataException("roleId must not be null");
        }


        Optional<UserRole> currentRoleOptional = userRoleDAO.getRoleById(roleId);

        if (!currentRoleOptional.isPresent()) {
                throw new UserRoleNotFoundException("Role with ID: " + roleId + " does not exist");
            }

        if (currentRoleOptional.get().getUserRole().equals(Role.ADMIN)) {
            throw new UserRoleException("Cannot delete admin role");
        }

        return userRoleDAO.deleteRole(roleId);
    }
}
