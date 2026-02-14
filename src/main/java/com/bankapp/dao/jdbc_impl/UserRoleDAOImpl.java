package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.UserRoleDAO;
import com.bankapp.dao.sql.UserRoleSQLQueries;
import com.bankapp.exception.DAO_exceptions.UserAddressAccessException;
import com.bankapp.exception.DAO_exceptions.UserRoleAccessException;
import com.bankapp.model.Role;
import com.bankapp.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRoleDAOImpl implements UserRoleDAO {

    private static final Logger logger = Logger.getLogger(UserRoleDAOImpl.class.getName());

    @Override
    public Long createUserRole(UserRole userRole) {
        try (Connection connection = DBConnectionPoolUtil.getConnection()) {
            return createUserRole(userRole, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long createUserRole(UserRole userRole, Object persistenceContext) {

        if (!(persistenceContext instanceof Connection)) {
            throw new IllegalArgumentException("Expected JDBC Connection");
        }

        Connection connection = (Connection) persistenceContext;

        if (userRole == null) {
            logger.warning("Cannot create user role: UserRole is null");
            return null;
        }

        if (userRole.getUserId() == null) {
            logger.warning("Cannot create user role: userId is null");
            return null;
        }

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             UserRoleSQLQueries.CREATE_USER_ROLE,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, userRole.getUserId());
            preparedStatement.setString(2, userRole.getUserRole().toString().toUpperCase());


            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert user role - no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys() ) {
                if (generatedKeys.next()) {
                    long userRoleId = generatedKeys.getLong(1);
                    userRole.setRoleId(userRoleId);
                    logger.info("User role created successfully with ID: " + userRoleId);
                    return userRoleId;
                } else {
                    logger.severe("Failed to fetch generated user role ID");
                    throw new UserRoleAccessException("User role ID generation failed", null);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating user role", e);
            throw new UserAddressAccessException("Database error while creating user role", e);
        }    }

    @Override
    public Optional<UserRole> getRoleById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve user role: roleId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserRoleSQLQueries.SELECT_USER_ROLE_BY_ID)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    UserRole userRole = mapResultSetToUserRole(resultSet);
                    return Optional.of(userRole);

                } else {
                    logger.info("No role found with ID: " + id);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user role by ID: " + id, e);
            throw new UserRoleAccessException("Error fetching user role by ID: " + id, e);
        }
    }

    @Override
    public Optional<UserRole> getRoleByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve user role: userId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserRoleSQLQueries.SELECT_ROLE_BY_USER_ID)) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    UserRole userRole = mapResultSetToUserRole(resultSet);
                    return Optional.of(userRole);

                } else {
                    logger.info("No role found with user ID: " + userId);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user role by user ID: " + userId, e);
            throw new UserRoleAccessException("Error fetching user role by user ID: " + userId, e);
        }
    }

    @Override
    public List<UserRole> getAllUserRoles() {
        List<UserRole>  userRoles = new ArrayList<>();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserRoleSQLQueries.SELECT_ALL_USER_ROLES);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userRoles.add(mapResultSetToUserRole(resultSet));
            }

            if (userRoles.isEmpty()) {
                logger.info("No user role found");
            } else {
                logger.info("Successfully retrieved " + userRoles.size() +
                        " user roles from database");
            }

            return userRoles;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user roles", e);
            throw new UserRoleAccessException("Error fetching user roles", e);
        }
    }

    @Override
    public boolean updateRole(UserRole userRole) {
        if (userRole == null) {
            logger.warning("Cannot update user role: UserRole is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserRoleSQLQueries.UPDATE_USER_ROLE)) {

            preparedStatement.setString(1, userRole.getUserRole().toString().toUpperCase());
            preparedStatement.setLong(2, userRole.getRoleId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated User Role for ID: " + userRole.getRoleId());
                return true;
            }

            logger.warning("Update failed: No user role found with ID "
                    + userRole.getRoleId());
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating user role: ID=" +
                    userRole.getRoleId(), e);
            throw new UserRoleAccessException("Error while updating user role", e);
        }
    }

    @Override
    public boolean deleteRole(Long roleId) {
        if (roleId == null) {
            logger.warning("Cannot delete user role: roleId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserRoleSQLQueries.DELETE_USER_ROLE)) {

            preparedStatement.setLong(1, roleId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("User Role with ID: " + roleId + " deleted.");
                return true;
            }

            logger.warning("Deletion failed: No user role found with ID " + roleId);
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting user role with ID: " +
                    roleId, e);
            throw new UserRoleAccessException("Error deleting user role with ID: " +
                    roleId, e);
        }
    }

    private UserRole mapResultSetToUserRole(ResultSet resultSet) throws SQLException {
        Role role =
                Role.valueOf(resultSet.getString("user_role").toUpperCase());
        return new UserRole(
                resultSet.getLong("role_id"),
                resultSet.getLong("user_id"),
                role
        );
    }
}
