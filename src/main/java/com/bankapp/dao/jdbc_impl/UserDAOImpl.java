package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.UserDAO;
import com.bankapp.dao.sql.EmploymentProfileSQLQueries;
import com.bankapp.dao.sql.RoleSQLQueries;
import com.bankapp.dao.sql.UserAddressSQLQueries;
import com.bankapp.dao.sql.UserSQLQueries;
import com.bankapp.exception.DAO_exceptions.DAOException;
import com.bankapp.exception.DAO_exceptions.DataAccessException;
import com.bankapp.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOImpl implements UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createUser(User user) {
        try (Connection connection = DBConnectionPoolUtil.getConnection()) {
            return createUser(user, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long createUser(User user, Object persistenceContext) {

        if (!(persistenceContext instanceof Connection)) {
            throw new IllegalArgumentException("Expected JDBC Connection");
        }

        Connection connection = (Connection) persistenceContext;

        if (user == null) {
            logger.warning("Cannot create user: User is null");
            return null;
        }

        try (PreparedStatement userStmt =
                     connection.prepareStatement(UserSQLQueries.INSERT_USER,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            userStmt.setString(1, user.getPasswordHash());
            userStmt.setString(2, user.getFirstName());
            userStmt.setString(3, user.getLastName());
            userStmt.setDate(4, Date.valueOf(user.getDateOfBirth()));
            userStmt.setString(5, user.getGender().toString());
            userStmt.setString(6, user.getEmail());
            userStmt.setString(7, user.getNationality());
            userStmt.setString(8, user.getPassportNumber());
            userStmt.setString(9, user.getPhoneNumber());

            int userRowsAffected = userStmt.executeUpdate();
            if (userRowsAffected == 0) {
                connection.rollback();
                logger.severe("Failed to insert user - no rows affected.");
                throw new DataAccessException("User creation failed", null);
            }

            try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getLong(1));
                } else {
                    connection.rollback();
                    logger.severe("Failed to fetch generated user ID");
                    throw new DataAccessException("User ID generation failed", null);
                }
            }

            return user.getUserId();

        } catch (SQLException e) {
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

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement(UserSQLQueries.SELECT_USER_BY_ID)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    User user = mapResultSetToUser(resultSet);
                    return Optional.of(user);

                } else {
                    logger.info("No user found with ID: " + id);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user by ID: " + id, e);
            throw new DAOException("Error fetching user by ID: " + id, e);
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            logger.warning("Cannot retrieve user: username is null or empty");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.SELECT_USER_BY_USERNAME)) {

            preparedStatement.setString(1, username.trim());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    User user = mapResultSetToUser(resultSet);
                    return Optional.of(user);

                } else {
                    logger.info("No user found with username: " + username);
                    return Optional.empty();
                }
            }


        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user by username: " + username, e);
            throw new DAOException("Error fetching user by username: " + username, e);
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.warning("Cannot retrieve user: email is null or empty");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.SELECT_USER_BY_EMAIL)) {

            preparedStatement.setString(1, email.trim());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    User user = mapResultSetToUser(resultSet);
                    return Optional.of(user);

                } else {
                    logger.info("No user found with email: " + email);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user by email: " + email, e);
            throw new DAOException("Error fetching user by email: " + email, e);
        }
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            logger.warning("Cannot update user: User or userId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.UPDATE_USER)) {

            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setDate(3, Date.valueOf(user.getDateOfBirth()));
            preparedStatement.setString(4, user.getGender().toString().toUpperCase());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setString(6, user.getNationality());
            preparedStatement.setString(7, user.getPassportNumber());
            preparedStatement.setString(8, user.getPhoneNumber());
            preparedStatement.setLong(9, user.getUserId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated User profile for ID: " + user.getUserId());
                return true;
            }

            logger.warning("Update failed: No user found with ID " + user.getUserId());
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating user: ID=" + user.getUserId(), e);
            throw new DAOException("Error while updating user", e);
        }
    }

    @Override
    public boolean updateUserUsername(Long userId, String newUsername) {
        if (userId == null || newUsername == null || newUsername.isEmpty()) {
            logger.warning("Cannot update username: userId or newUsername is null/empty");
            return false;
        }

        newUsername = newUsername.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.UPDATE_USER_USERNAME)) {

            preparedStatement.setString(1, newUsername);
            preparedStatement.setLong(2, userId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated username for ID: " + userId);
                return true;
            }

            logger.warning("Update failed: No user found with ID " + userId);
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating username for user ID: " + userId, e);
            throw new DAOException("Error updating username for user ID: " + userId, e);
        }
    }

    @Override
    public boolean updateUserPassword(Long userId, String newHashedPassword) {
        if (userId == null || newHashedPassword == null || newHashedPassword.isEmpty()) {
            logger.warning("Cannot update password: userId or newHashedPassword is null/empty");
            return false;
        }

        newHashedPassword = newHashedPassword.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.UPDATE_USER_PASSWORD)) {

            preparedStatement.setString(1, newHashedPassword);
            preparedStatement.setLong(2, userId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated password for ID: " + userId);
                return true;
            }

            logger.warning("Update failed: No user found with ID " + userId);
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating password for user ID: " + userId, e);
            throw new DAOException("Error updating password for user ID: " + userId, e);
        }
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            logger.warning("Cannot delete user: userId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.DELETE_USER)) {

            preparedStatement.setLong(1, userId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("User with ID: " + userId + " deleted.");
                return true;
            }

            logger.warning("Deletion failed: No user found with ID " + userId);
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting user with ID: " + userId, e);
            throw new DAOException("Error deleting user with ID: " + userId, e);
        }
    }

    @Override
    public boolean userExistsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            logger.warning("Cannot retrieve user: username is  null or empty");
            return false;
        }

        username = username.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.CHECK_USERNAME_EXISTS)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while checking username existence: " + username, e);
            throw new DAOException("Error checking username existence: " + username, e);
        }
    }

    @Override
    public boolean userExistsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.warning("Cannot check user existence: email is null or empty");
            return false;
        }

        email = email.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.CHECK_EMAIL_EXISTS)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while checking email existence: " + email, e);
            throw new DAOException("Database error while checking email existence: " + email, e);
        }
    }

    @Override
    public boolean userExistsByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot check user existence: userId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserSQLQueries.CHECK_USER_ID_EXISTS)) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while checking user ID existence: " + userId, e);
            throw new DAOException("Database error while checking user ID existence: " + userId, e);
        }
    }

    @Override
    public Optional<User> getUserForAuthentication(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            logger.warning("Cannot check user existence: identifier is null");
            return Optional.empty();
        }

        identifier = identifier.trim();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement userStmt = connection.prepareStatement(
                     UserSQLQueries.GET_USER_BY_IDENTIFIER
             )) {

                userStmt.setString(1, identifier);
                userStmt.setString(2, identifier);

                try (ResultSet resultSet = userStmt.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    List<UserAddress> userAddresses = fetchUserAddresses(connection, resultSet.getLong("user_id"));
                    EmploymentProfile employmentProfile = fetchUserEmploymentProfile(connection, resultSet.getLong("user_id"));
                    Role role = fetchUserRole(connection, resultSet.getLong("user_id"));

                    return Optional.of(mapResultSetToUserWithCredentials(
                            resultSet,
                            userAddresses,
                            employmentProfile,
                            role
                    ));
                }

        } catch (SQLException e) {
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

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(UserSQLQueries.GET_USER_BY_IDENTIFIER)) {

            preparedStatement.setString(1, identifier);
            preparedStatement.setString(2, identifier);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if  (resultSet.next()) {
                    return Optional.of(
                            mapResultSetToUser(resultSet)
                    );
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user by identifier: " + identifier, e);
            throw new DAOException("Database error while fetching user by identifier: " + identifier, e);
        }
    }

    private List<UserAddress> fetchUserAddresses(Connection connection, Long userId) throws SQLException {
        List<UserAddress> userAddresses = new ArrayList<>();

        try (PreparedStatement addressStmt = connection.prepareStatement(
                UserAddressSQLQueries.SELECT_ADDRESS_BY_USER_ID
        )) {

            addressStmt.setLong(1, userId);

            try (ResultSet resultSet = addressStmt.executeQuery()) {
                while (resultSet.next()) {
                    userAddresses.add(
                            mapResultSetToUserAddress(resultSet)
                    );
                }
            }

            return userAddresses;
        }
    }

    private EmploymentProfile fetchUserEmploymentProfile(Connection connection, Long userId) throws SQLException {
        try (PreparedStatement employmentProfileStmt = connection.prepareStatement(
                EmploymentProfileSQLQueries.SELECT_EMPLOYMENT_PROFILE_BY_USER_ID
        )) {

            employmentProfileStmt.setLong(1, userId);

            try (ResultSet resultSet = employmentProfileStmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEmploymentProfile(resultSet);
                } else {
                    return null;
                }
            }
        }
    }

    private Role fetchUserRole(Connection connection, Long userId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                RoleSQLQueries.SELECT_USER_ROLE
        )) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Role.valueOf(resultSet.getString("user_role").toUpperCase());
                } else {
                    return null;
                }
            }
        }
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        Gender gender =
                Gender.valueOf(resultSet.getString("gender").toUpperCase());

        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("username"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getDate("date_of_birth").toLocalDate(),
                gender,
                resultSet.getString("email_address"),
                resultSet.getString("nationality"),
                resultSet.getString("passport_number"),
                resultSet.getString("phone_number")
        );
    }

    private User mapResultSetToUserWithCredentials(
            ResultSet resultSet,
            List<UserAddress> userAddresses,
            EmploymentProfile employmentProfile,
            Role role
    ) throws SQLException {

        Gender gender =
                Gender.valueOf(resultSet.getString("gender").toUpperCase());
        LocalDate dateOfBirth = resultSet.getDate("date_of_birth").toLocalDate();

        return new  User(
                resultSet.getLong("user_id"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                dateOfBirth,
                gender,
                resultSet.getString("email_address"),
                resultSet.getString("nationality"),
                resultSet.getString("passport_number"),
                resultSet.getString("phone_number"),
                userAddresses,
                role,
                employmentProfile
        );
    }

    private UserAddress mapResultSetToUserAddress(ResultSet resultSet) throws SQLException {
        AddressType addressType = AddressType.valueOf(resultSet.getString("address_type").toUpperCase());

        return new UserAddress(
                resultSet.getLong("address_id"),
                resultSet.getLong("user_id"),
                addressType,
                resultSet.getString("address_line_1"),
                resultSet.getString("address_line_2"),
                resultSet.getString("locality"),
                resultSet.getString("city"),
                resultSet.getString("district"),
                resultSet.getString("state_or_province"),
                resultSet.getString("postal_code"),
                resultSet.getString("country")
        );
    }

    private EmploymentProfile mapResultSetToEmploymentProfile(ResultSet resultSet) throws SQLException {
        SourceOfFunds sourceOfFunds = SourceOfFunds.valueOf(resultSet.getString("source_of_funds").toUpperCase());
        AccountPurpose accountPurpose = AccountPurpose.valueOf(resultSet.getString("account_purpose").toUpperCase());

        return new EmploymentProfile(
                resultSet.getLong("employment_profile_id"),
                resultSet.getLong("user_id"),
                resultSet.getString("occupation"),
                resultSet.getBigDecimal("annual_income"),
                sourceOfFunds,
                accountPurpose
        );
    }

}
