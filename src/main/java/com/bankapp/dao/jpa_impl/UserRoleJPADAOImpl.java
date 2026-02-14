package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.UserRoleDAO;
import com.bankapp.dao.jdbc_impl.UserRoleDAOImpl;
import com.bankapp.exception.DAO_exceptions.DAOException;
import com.bankapp.exception.DAO_exceptions.DataAccessException;
import com.bankapp.exception.DAO_exceptions.UserRoleAccessException;
import com.bankapp.model.User;
import com.bankapp.model.UserRole;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRoleJPADAOImpl implements UserRoleDAO {

    private static final Logger logger = Logger.getLogger(UserRoleDAOImpl.class.getName());

    @Override
    public Long createUserRole(UserRole userRole) {
        if (userRole == null) {
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(userRole);

            transaction.commit();

            return userRole.getRoleId();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while creating user role", ex);
            throw new DataAccessException("Error inserting user role into database", ex);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long createUserRole(UserRole userRole, Object persistenceContext) {
        if (!(persistenceContext instanceof EntityManager)) {
            throw new IllegalArgumentException("Expected JPA EntityManager");
        }

        EntityManager entityManager = (EntityManager) persistenceContext;

        if (userRole == null) {
            return null;
        }

        try {

            entityManager.persist(userRole);
            return userRole.getRoleId();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while creating user", e);
            throw new DataAccessException("Error inserting user into database", e);
        }
    }

    @Override
    public Optional<UserRole> getRoleById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve user role: roleId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            UserRole userRole = entityManager.find(UserRole.class, id);

            if (userRole != null) {
                return Optional.of(userRole);
            } else {
                logger.info("No user role found with ID: " + id);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching user role by ID: " + id, e);
            throw new DAOException("Error fetching user role by ID: " + id, e);
        }
    }

    @Override
    public Optional<UserRole> getRoleByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve role: userId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<UserRole> query =
                    entityManager.createQuery(
                            "SELECT ur FROM UserRole ur WHERE ur.userId = :userId",
                            UserRole.class
                    );

            query.setParameter("userId", userId);

            List<UserRole> results = query.getResultList();

            if (results.isEmpty()) {
                logger.info("No user role found for userId: " + userId);
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching user role by userId: " + userId, e);
            throw new DAOException("Error fetching user role by userId: " + userId, e);

        }
    }

    @Override
    public List<UserRole> getAllUserRoles() {
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<UserRole> query =
                    entityManager.createQuery(
                            "SELECT ur FROM UserRole ur",
                            UserRole.class
                    );

            return query.getResultList();
        } catch (Exception e) {
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

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            UserRole existingRole = entityManager.find(UserRole.class, userRole.getRoleId());

            if (existingRole == null) {
                logger.warning("Update failed: No user role found with ID "
                        + userRole.getRoleId());
                transaction.rollback();
                return false;
            }

            existingRole.setUserRole(userRole.getUserRole());
            existingRole.setUser(userRole.getUser());

            transaction.commit();

            logger.info("Updated User Role for ID: " + userRole.getRoleId());
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE,
                    "Database error while updating user role: ID=" + userRole.getRoleId(), e);

            throw new UserRoleAccessException("Error while updating user role", e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteRole(Long roleId) {
        if (roleId == null) {
            logger.warning("Cannot delete user role: roleId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            UserRole existingRole = entityManager.find(UserRole.class, roleId);

            if (existingRole == null) {
                logger.warning("Deletion failed: No user role found with ID "
                        + roleId);
                transaction.rollback();
                return false;
            }

            User user = existingRole.getUser();
            user.setUserRole(null);

            transaction.commit();

            logger.info("Deleted User Role for ID: " + roleId);
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE,
                    "Database error while deleting user role: ID=" + roleId, e);

            throw new UserRoleAccessException("Error while deleting user role", e);
        }
    }
}
