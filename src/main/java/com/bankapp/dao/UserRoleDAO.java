package com.bankapp.dao;

import com.bankapp.model.UserRole;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserRoleDAO {

    // CREATE
    Long createUserRole(UserRole userRole);
    Long createUserRole(UserRole userRole, Object persistenceContext);

    // RETRIEVE
    Optional<UserRole> getRoleById(Long id);
    Optional<UserRole> getRoleByUserId(Long userId);
    List<UserRole> getAllUserRoles();

    // UPDATE
    boolean updateRole(UserRole userRole);

    // DELETE
    boolean deleteRole(Long roleId);
}
