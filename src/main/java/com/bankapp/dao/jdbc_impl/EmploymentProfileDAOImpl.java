package com.bankapp.dao.jdbc_impl;

import com.bankapp.config.DBConnectionPoolUtil;
import com.bankapp.dao.EmploymentProfileDAO;
import com.bankapp.dao.sql.EmploymentProfileSQLQueries;
import com.bankapp.exception.DAO_exceptions.EmploymentProfileAccessException;
import com.bankapp.model.AccountPurpose;
import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.SourceOfFunds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmploymentProfileDAOImpl implements EmploymentProfileDAO {

    private static final Logger logger = Logger.getLogger(EmploymentProfileDAOImpl.class.getName());


    @Override
    public Long createEmploymentProfile(EmploymentProfile employmentProfile) {
        try (Connection connection = DBConnectionPoolUtil.getConnection()) {
            return createEmploymentProfile(employmentProfile, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long createEmploymentProfile(EmploymentProfile employmentProfile, Object persistenceContext) {
        if (!(persistenceContext instanceof Connection)) {
            throw new IllegalArgumentException("Expected JDBC Connection");
        }

        Connection connection = (Connection) persistenceContext;

        if (employmentProfile == null) {
            logger.warning("Cannot create employment profile: EmploymentProfile is null");
            return null;
        }

        if (employmentProfile.getUserId() == null) {
            logger.warning("Cannot create employment profile: userId is null");
            return null;
        }

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(
                             EmploymentProfileSQLQueries.CREATE_EMPLOYMENT_PROFILE,
                             PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, employmentProfile.getUserId());
            preparedStatement.setString(2, employmentProfile.getOccupation());
            preparedStatement.setBigDecimal(3, employmentProfile.getAnnualIncome());
            preparedStatement.setString(4, employmentProfile.getSourceOfFunds().toString().toUpperCase());
            preparedStatement.setString(5, employmentProfile.getAccountPurpose().toString().toUpperCase());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.severe("Failed to insert employment profile - no rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys() ) {
                if (generatedKeys.next()) {
                    long employmentProfileId = generatedKeys.getLong(1);
                    employmentProfile.setEmploymentProfileId(employmentProfileId);
                    logger.info("Employment profile created successfully with ID: " + employmentProfileId);
                    return employmentProfileId;
                } else {
                    logger.severe("Failed to fetch generated employment profile ID");
                    throw new EmploymentProfileAccessException("Employment profile ID generation failed", null);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while creating employment profile", e);
            throw new EmploymentProfileAccessException("Database error while creating employment profile", e);
        }    }

    @Override
    public Optional<EmploymentProfile> getEmploymentProfileById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve employment profile: employmentProfileId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(EmploymentProfileSQLQueries.SELECT_EMPLOYMENT_PROFILE_BY_ID)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    EmploymentProfile employmentProfile = mapResultSetToEmploymentProfile(resultSet);
                    return Optional.of(employmentProfile);

                } else {
                    logger.info("No employment profile found with ID: " + id);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile by ID: " + id, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile by ID: " + id, e);
        }
    }

    @Override
    public Optional<EmploymentProfile> getEmploymentProfileByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve employment profile: userId is null");
            return Optional.empty();
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(EmploymentProfileSQLQueries.SELECT_EMPLOYMENT_PROFILE_BY_USER_ID)) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    logger.info("Successfully retrieved employment profile");
                    EmploymentProfile employmentProfile = mapResultSetToEmploymentProfile(resultSet);
                    return Optional.of(employmentProfile);
                } else {
                    logger.info("No employment profile found for user ID: " + userId);
                    return Optional.empty();
                }

            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile by user ID: " + userId, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile by user ID: " + userId, e);
        }

    }

    @Override
    public List<EmploymentProfile> getAllEmploymentProfiles() {
        List<EmploymentProfile>  employmentProfiles = new ArrayList<>();

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(EmploymentProfileSQLQueries.SELECT_ALL_EMPLOYMENT_PROFILES);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                employmentProfiles.add(mapResultSetToEmploymentProfile(resultSet));
            }

            if (employmentProfiles.isEmpty()) {
                logger.info("No employment profiles found");
            } else {
                logger.info("Successfully retrieved " + employmentProfiles.size() +
                        " employment profiles from database");
            }

            return employmentProfiles;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profiles", e);
            throw new EmploymentProfileAccessException("Error fetching employment profiles", e);
        }
    }

    @Override
    public Long getEmploymentProfileIdByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve employment profile: userId is null");
            return null;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    EmploymentProfileSQLQueries.SELECT_EMPLOYMENT_PROFILE_ID_BY_USER_ID
            )) {

            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("employment_profile_id");
                }
            }

            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile with userId: " + userId, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile for userId" + userId, e);
        }
    }

    @Override
    public boolean updateEmploymentProfile(EmploymentProfile employmentProfile) {
        if (employmentProfile == null || employmentProfile.getEmploymentProfileId() == null) {
            logger.warning("Cannot update employment profile: EmploymentProfile or employmentProfileId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement =
                connection.prepareStatement(EmploymentProfileSQLQueries.UPDATE_EMPLOYMENT_PROFILE)) {

            preparedStatement.setString(1, employmentProfile.getOccupation());
            preparedStatement.setBigDecimal(2, employmentProfile.getAnnualIncome());
            preparedStatement.setString(3, employmentProfile.getSourceOfFunds().toString().toUpperCase());
            preparedStatement.setString(4, employmentProfile.getAccountPurpose().toString().toUpperCase());
            preparedStatement.setLong(5, employmentProfile.getEmploymentProfileId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated Employment Profile for ID: " + employmentProfile.getEmploymentProfileId());
                return true;
            }

            logger.warning("Update failed: No employment profile found with ID "
                    + employmentProfile.getEmploymentProfileId());
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while updating employment profile: ID=" +
                    employmentProfile.getEmploymentProfileId(), e);
            throw new EmploymentProfileAccessException("Error while updating employment profile", e);
        }
    }

    @Override
    public boolean deleteEmploymentProfile(Long employmentProfileId) {
        if (employmentProfileId == null) {
            logger.warning("Cannot delete employment profile: employmentProfileId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(EmploymentProfileSQLQueries.DELETE_EMPLOYMENT_PROFILE)) {

            preparedStatement.setLong(1, employmentProfileId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Employment profile with ID: " + employmentProfileId + " deleted.");
                return true;
            }

            logger.warning("Deletion failed: No employment profile found with ID " + employmentProfileId);
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while deleting employment profile with ID: " +
                    employmentProfileId, e);
            throw new EmploymentProfileAccessException("Error deleting employment profile with ID: " +
                    employmentProfileId, e);
        }
    }

    @Override
    public boolean employmentProfileExists(Long employmentProfileId) {
        if (employmentProfileId == null) {
            logger.warning("Cannot retrieve employment profile: employmentProfileId is null");
            return false;
        }

        try (Connection connection = DBConnectionPoolUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    EmploymentProfileSQLQueries.CHECK_EMPLOYMENT_PROFILE_EXISTS
            )) {

            preparedStatement.setLong(1, employmentProfileId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile ID: " +
                    employmentProfileId, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile ID: " +
                    employmentProfileId, e);
        }
    }

    private EmploymentProfile mapResultSetToEmploymentProfile(ResultSet resultSet) throws SQLException {
        SourceOfFunds sourceOfFunds =
                SourceOfFunds.valueOf(resultSet.getString("source_of_funds").toUpperCase());
        AccountPurpose accountPurpose =
                AccountPurpose.valueOf(resultSet.getString("account_purpose").toUpperCase());

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
