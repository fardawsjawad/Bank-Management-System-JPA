package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.EmploymentProfileDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.exception.DAO_exceptions.DataAccessException;
import com.bankapp.exception.DAO_exceptions.EmploymentProfileAccessException;
import com.bankapp.model.EmploymentProfile;
import com.bankapp.model.User;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmploymentProfileJPADAOImpl implements EmploymentProfileDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createEmploymentProfile(EmploymentProfile employmentProfile) {
        if (employmentProfile == null) {
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(employmentProfile);

            transaction.commit();
            return employmentProfile.getEmploymentProfileId();

        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while creating employment profile", ex);
            throw new DataAccessException("Error inserting employment profile into database", ex);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long createEmploymentProfile(EmploymentProfile employmentProfile, Object persistenceContext) {
        if (!(persistenceContext instanceof EntityManager)) {
            throw new IllegalArgumentException("Expected JPA EntityManager");
        }

        EntityManager entityManager = (EntityManager) persistenceContext;

        if (employmentProfile == null) {
            return null;
        }

        try {

            entityManager.persist(employmentProfile);
            return employmentProfile.getEmploymentProfileId();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while creating employment profile", e);
            throw new DataAccessException("Error inserting employment profile into database", e);
        }
    }

    @Override
    public Optional<EmploymentProfile> getEmploymentProfileById(Long id) {
        if (id == null) {
            logger.warning("Cannot retrieve employment profile: employmentProfileId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            EmploymentProfile employmentProfile = entityManager.find(EmploymentProfile.class, id);
            if (employmentProfile == null) {
                logger.info("No employment profile found with ID: " + id);
                return Optional.empty();
            }

            return Optional.of(employmentProfile);

        } catch (Exception e) {
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

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            User user = entityManager.find(User.class, userId);
            if (user == null) {
                logger.info("No user exists with ID: " + userId);
                return Optional.empty();
            }

            if (user.getEmploymentProfile() == null) {
                logger.info("No employment profile exists for user with ID: " + userId);
                return Optional.empty();
            }

            return Optional.of(user.getEmploymentProfile());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile by user ID: " + userId, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile by user ID: " + userId, e);
        }
    }

    @Override
    public List<EmploymentProfile> getAllEmploymentProfiles() {
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<EmploymentProfile> query =
                    entityManager.createQuery(
                            "SELECT ep FROM EmploymentProfile ep",
                            EmploymentProfile.class
                    );

            return query.getResultList();

        } catch (Exception e) {
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

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT ep.employmentProfileId FROM EmploymentProfile ep WHERE ep.user.userId = :userId",
                            Long.class
                    );

            query.setParameter("userId", userId);

            List<Long> results = query.getResultList();
            if (results.isEmpty()) {
                logger.info("No employment profile found for user ID: " + userId);
                return null;
            }

            return results.get(0);

        } catch (Exception e) {

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

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            entityManager.merge(employmentProfile);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating employment profile: ID=" +
                    employmentProfile.getEmploymentProfileId(), e);
            e.printStackTrace();
            throw new EmploymentProfileAccessException("Error while updating employment profile", e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteEmploymentProfile(Long employmentProfileId) {
        if (employmentProfileId == null) {
            logger.warning("Cannot delete employment profile: employmentProfileId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            EmploymentProfile existingEmploymentProfile =
                    entityManager.find(EmploymentProfile.class, employmentProfileId);

            if (existingEmploymentProfile == null) {
                logger.info("No employment profile found with ID: " + employmentProfileId);
                transaction.rollback();
                return false;
            }

            entityManager.remove(existingEmploymentProfile);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE, "Database error while deleting employment profile with ID: " +
                    employmentProfileId, e);
            throw new EmploymentProfileAccessException("Error deleting employment profile with ID: " +
                    employmentProfileId, e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean employmentProfileExists(Long employmentProfileId) {
        if (employmentProfileId == null) {
            logger.warning("Cannot retrieve employment profile: employmentProfileId is null");
            return false;
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            EmploymentProfile employmentProfile = entityManager.find(EmploymentProfile.class, employmentProfileId);
            return employmentProfile != null;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching employment profile ID: " +
                    employmentProfileId, e);
            throw new EmploymentProfileAccessException("Error fetching employment profile ID: " +
                    employmentProfileId, e);
        }
    }
}
