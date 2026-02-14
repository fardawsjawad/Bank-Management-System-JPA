package com.bankapp.service.impl;

import com.bankapp.dao.EmploymentProfileDAO;
import com.bankapp.dao.UserDAO;
import com.bankapp.exception.service_exceptions.employment_profile.EmploymentProfileCreationException;
import com.bankapp.exception.service_exceptions.employment_profile.EmploymentProfileException;
import com.bankapp.exception.service_exceptions.employment_profile.EmploymentProfileNotFoundException;
import com.bankapp.exception.service_exceptions.employment_profile.InvalidEmploymentProfileDataException;
import com.bankapp.exception.service_exceptions.user_service.UserNotFoundException;
import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.User;
import com.bankapp.service.EmploymentProfileService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class EmploymentProfileServiceImpl implements EmploymentProfileService {

    private final EmploymentProfileDAO employmentProfileDAO;
    private final UserDAO userDAO;

    public EmploymentProfileServiceImpl(EmploymentProfileDAO employmentProfileDAO, UserDAO userDAO) {
        this.employmentProfileDAO = employmentProfileDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Long createEmploymentProfile(EmploymentProfile employmentProfile) {

        if (employmentProfile == null) {
            throw new InvalidEmploymentProfileDataException(
                    "EmploymentProfile must not be null"
            );
        }

        if (employmentProfile.getUser() == null ||
                employmentProfile.getUser().getUserId() == null) {
            throw new InvalidEmploymentProfileDataException(
                    "EmploymentProfile userId must not be null"
            );
        }

        if (employmentProfile.getSourceOfFunds() == null ||
                employmentProfile.getAccountPurpose() == null) {
            throw new InvalidEmploymentProfileDataException(
                    "sourceOfFunds and accountPurpose must not be null"
            );
        }

        if (employmentProfile.getAnnualIncome() == null ||
                employmentProfile.getAnnualIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEmploymentProfileDataException(
                    "annualIncome must be greater than zero"
            );
        }

        Long userId = employmentProfile.getUser().getUserId();

        userDAO.getUserById(userId)
                .orElseThrow(() ->
                            new UserNotFoundException("User not found")
                        );

        Optional<EmploymentProfile> existingProfile  =
                employmentProfileDAO.getEmploymentProfileByUserId(userId);

        if (existingProfile.isPresent()) {
            throw new EmploymentProfileCreationException(
                    "User already has employment profile"
            );
        }

        Long employmentProfileId =
                employmentProfileDAO.createEmploymentProfile(employmentProfile);

        if (employmentProfileId == null) {
            throw new EmploymentProfileCreationException(
                    "Failed to create employment profile for userId: "
                            + userId
            );
        }

        return employmentProfileId;
    }


    @Override
    public Optional<EmploymentProfile> getEmploymentProfileById(Long id) {
        if (id == null) {
            throw new InvalidEmploymentProfileDataException("id must not be null");
        }

        Optional<EmploymentProfile> employmentProfileOptional =
                employmentProfileDAO.getEmploymentProfileById(id);

        if (!employmentProfileOptional.isPresent()) {
            throw new EmploymentProfileNotFoundException("Employment Profile not found");
        }

        return employmentProfileOptional;
    }

    @Override
    public Optional<EmploymentProfile> getEmploymentProfileByUserId(Long userId) {
        if (userId == null) {
            throw new InvalidEmploymentProfileDataException("userId must not be null");
        }

        userDAO.getUserById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        return employmentProfileDAO.getEmploymentProfileByUserId(userId);
    }

    @Override
    public List<EmploymentProfile> getAllEmploymentProfiles() {
        return employmentProfileDAO.getAllEmploymentProfiles();
    }

    @Override
    public boolean updateEmploymentProfile(EmploymentProfile employmentProfile) {
        if (employmentProfile == null ||
            employmentProfile.getEmploymentProfileId() == null) {
            throw new InvalidEmploymentProfileDataException(
                    "Cannot update employment profile: EmploymentProfile or employmentProfileId is null"
            );
        }

        employmentProfileDAO.getEmploymentProfileById(employmentProfile.getEmploymentProfileId())
                        .orElseThrow(() ->
                                    new EmploymentProfileNotFoundException("Employment Profile not found")
                                );

        boolean success = employmentProfileDAO.updateEmploymentProfile(employmentProfile);

        if (!success) {
            throw new EmploymentProfileException("Failed to update employment profile");
        }

        return true;
    }

    @Override
    public boolean deleteEmploymentProfile(Long employmentProfileId) {
        if (employmentProfileId == null) {
            throw new  InvalidEmploymentProfileDataException(
                    "employmentProfileId must not be null"
            );
        }

        employmentProfileDAO.getEmploymentProfileById(employmentProfileId)
                        .orElseThrow(() ->
                                    new EmploymentProfileNotFoundException(
                                            "Employment profile not found for id: " + employmentProfileId
                                    )
                                );

        boolean success = employmentProfileDAO.deleteEmploymentProfile(employmentProfileId);

        if (!success) {
            throw new EmploymentProfileException(
                    "Failed to delete employment profile"
            );
        }

        return true;
    }

    @Override
    public boolean employmentProfileExists(Long employmentProfileId) {
        if (employmentProfileId == null) {
            throw new InvalidEmploymentProfileDataException("employmentProfileId must not be null");
        }

        return employmentProfileDAO.employmentProfileExists(employmentProfileId);
    }
}
