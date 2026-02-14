package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.UserAddressDAO;
import com.bankapp.dao.sql.UserAddressSQLQueries;
import com.bankapp.exception.DAO_exceptions.EmploymentProfileAccessException;
import com.bankapp.exception.DAO_exceptions.UserAddressAccessException;
import com.bankapp.model.AddressType;
import com.bankapp.model.UserAddress;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAddressDAOImpl implements UserAddressDAO {

    private static final Logger logger = Logger.getLogger(UserAddressDAOImpl.class.getName());

    @Override
    public Long createUserAddress(UserAddress userAddress) {
        try (Connection connection = DBConnectionPoolUtil.getConnection()) {
            return createUserAddress(userAddress, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long createUserAddress(UserAddress userAddress, Object persistenceContext) {
        if (!(persistenceContext instanceof Connection)) {
            throw new IllegalArgumentException("Expected JDBC Connection");
        }

        Connection connection = (Connection) persistenceContext;

        if (userAddress == null) {
            logger.warning("Cannot create user address: UserAddress is null");
            return null;
        }

        if (userAddress.getUserId() == null) {
            logger.warning("Cannot create user address: userId is null");
            return null;
        }

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             UserAddressSQLQueries.CREATE_USER_ADDRESS,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, userAddress.getUserId());
            preparedStatement.setString(2, userAddress.getAddressType().toString().toUpperCase());
            preparedStatement.setString(3, userAddress.getAddressLine1());
            preparedStatement.setString(4, userAddress.getAddressLine2());
            preparedStatement.setString(5, userAddress.getLocality());
            preparedStatement.setString(6, userAddress.getCity());
            preparedStatement.setString(7, userAddress.getDistrict());
            preparedStatement.setString(8, userAddress.getStateOrProvince());
            preparedStatement.setString(9, userAddress.getPostalCode());
            preparedStatement.setString(10, userAddress.getCountry());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert user address - no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys() ) {
                if (generatedKeys.next()) {
                    long userAddressId = generatedKeys.getLong(1);
                    userAddress.setAddressId(userAddressId);
                    logger.info("User address created successfully with ID: " + userAddressId);
                    return userAddressId;
                } else {
                    logger.severe("Failed to fetch generated user address ID");
                    throw new UserAddressAccessException("User address ID generation failed", null);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating user address", e);
            throw new UserAddressAccessException("Database error while creating user address", e);
        }    }

    @Override
    public Optional<UserAddress> getAddressById(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot retrieve user address: addressId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.SELECT_ADDRESS_BY_ID)) {

            preparedStatement.setLong(1, addressId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    UserAddress userAddress = mapResultSetToUserAddress(resultSet);
                    return Optional.of(userAddress);

                } else {
                    logger.info("No address found with ID: " + addressId);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user address by ID: " + addressId, e);
            throw new UserAddressAccessException("Error fetching user address by ID: " + addressId, e);
        }
    }

    @Override
    public List<UserAddress> getAddressesByUserId(Long userId) {
        List<UserAddress> userAddresses = new ArrayList<>();

        if (userId == null) {
            logger.warning("Cannot retrieve user address: userId is null");
            return userAddresses;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.SELECT_ADDRESS_BY_USER_ID)) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UserAddress userAddress = mapResultSetToUserAddress(resultSet);
                    userAddresses.add(userAddress);
                }

                if (userAddresses.isEmpty()) {
                    logger.info("No address found for user ID: " + userId);
                } else {
                    logger.info("Successfully retrieved " + userAddresses.size() +
                            " addresses from database");
                }

                return userAddresses;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching address by user ID: " + userId, e);
            throw new UserAddressAccessException("Error fetching address by user ID: " + userId, e);
        }
    }

    @Override
    public List<UserAddress> getAllAddresses() {
        List<UserAddress>  userAddresses= new ArrayList<>();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.SELECT_ALL_ADDRESSES);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userAddresses.add(mapResultSetToUserAddress(resultSet));
            }

            if (userAddresses.isEmpty()) {
                logger.info("No user address found");
            } else {
                logger.info("Successfully retrieved " + userAddresses.size() +
                        " user addresses from database");
            }

            return userAddresses;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching user addresses", e);
            throw new UserAddressAccessException("Error fetching user addresses", e);
        }
    }

    @Override
    public List<Long> getAddressIdsByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve user address: userId is null");
            return null;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    UserAddressSQLQueries.SELECT_ADDRESS_ID_BY_USER_ID
            )) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return List.of(resultSet.getLong("address_id"));
                }
            }

            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching address ID by user ID: " +
                    userId, e);
            throw new UserAddressAccessException("Error while fetching address ID ", e);        }
    }

    @Override
    public boolean updateUserAddress(UserAddress userAddress) {
        if (userAddress == null || userAddress.getAddressId() == null) {
            logger.warning("Cannot update user address: UserAddress or userAddressId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.UPDATE_USER_ADDRESS)) {

            preparedStatement.setString(1, userAddress.getAddressType().toString().toUpperCase());
            preparedStatement.setString(2, userAddress.getAddressLine1());
            preparedStatement.setString(3, userAddress.getAddressLine2());
            preparedStatement.setString(4, userAddress.getLocality());
            preparedStatement.setString(5, userAddress.getCity());

            if (userAddress.getDistrict() != null) {
                preparedStatement.setString(6, userAddress.getDistrict());
            } else {
                preparedStatement.setNull(6, Types.VARCHAR);
            }

            preparedStatement.setString(7, userAddress.getStateOrProvince());
            preparedStatement.setString(8, userAddress.getPostalCode());
            preparedStatement.setString(9, userAddress.getCountry());
            preparedStatement.setLong(10, userAddress.getAddressId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated User Address for ID: " + userAddress.getAddressId());
                return true;
            }

            logger.warning("Update failed: No user address found with ID "
                    + userAddress.getAddressId());
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating user address: ID=" +
                    userAddress.getAddressId(), e);
            throw new UserAddressAccessException("Error while updating user address", e);
        }
    }

    @Override
    public boolean deleteUserAddress(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot delete user address: addressId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.DELETE_USER_ADDRESS)) {

            preparedStatement.setLong(1, addressId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("User Address with ID: " + addressId + " deleted.");
                return true;
            }

            logger.warning("Deletion failed: No user address found with ID " + addressId);
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting user address with ID: " +
                    addressId, e);
            throw new UserAddressAccessException("Error deleting user address with ID: " +
                    addressId, e);
        }
    }

    @Override
    public boolean deleteAddressesByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot delete user address: userId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UserAddressSQLQueries.DELETE_ADDRESSES_BY_USER_ID)) {

            preparedStatement.setLong(1, userId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Successfully deleted " + rowsAffected + " user addresses from database");
                return true;
            }

            logger.warning("Deletion failed: No user address found with user ID " + userId);
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting user addresses with user ID: " +
                    userId, e);
            throw new EmploymentProfileAccessException("Error deleting user addresses with user ID: " +
                    userId, e);
        }
    }

    @Override
    public boolean addressExists(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot check for address existence. addressId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    UserAddressSQLQueries.CHECK_ADDRESS_EXISTS
            )) {

            preparedStatement.setLong(1, addressId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching address ID: " +
                    addressId, e);
            throw new UserAddressAccessException("Error fetching address ID: " +
                    addressId, e);
        }
    }

    private UserAddress mapResultSetToUserAddress(ResultSet resultSet) throws SQLException {
        AddressType addressType =
                AddressType.valueOf(resultSet.getString("address_type"));

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
}
