package com.bankapp.dao;

import com.bankapp.model.EmploymentProfile;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface EmploymentProfileDAO {

    // CREATE
    Long createEmploymentProfile(EmploymentProfile employmentProfile);
    Long createEmploymentProfile(EmploymentProfile employmentProfile, Object persistenceContext);

    // RETRIEVE
    Optional<EmploymentProfile> getEmploymentProfileById(Long id);
    Optional<EmploymentProfile> getEmploymentProfileByUserId(Long userId);
    List<EmploymentProfile> getAllEmploymentProfiles();
    Long getEmploymentProfileIdByUserId(Long userId);

    // UPDATE
    boolean updateEmploymentProfile(EmploymentProfile employmentProfile);

    // DELETE
    boolean deleteEmploymentProfile(Long employmentProfileId);

    // CHECK
    boolean employmentProfileExists(Long employmentProfileId);

}
