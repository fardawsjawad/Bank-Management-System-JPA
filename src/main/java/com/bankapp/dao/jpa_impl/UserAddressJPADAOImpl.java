package com.bankapp.dao.jpa_impl;

import com.bankapp.dao.UserAddressDAO;
import com.bankapp.dao.jdbc_impl.UserDAOImpl;
import com.bankapp.exception.DAO_exceptions.*;
import com.bankapp.exception.service_exceptions.user_address_service.UserAddressException;
import com.bankapp.model.User;
import com.bankapp.model.UserAddress;
import com.bankapp.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAddressJPADAOImpl implements UserAddressDAO{

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public Long createUserAddress(UserAddress userAddress) {
        if (userAddress == null) {
            return null;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(userAddress);

            transaction.commit();

            return userAddress.getAddressId();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while creating user address", ex);
            throw new DataAccessException("Error inserting user address into database", ex);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Long createUserAddress(UserAddress userAddress, Object persistenceContext) {
        if (!(persistenceContext instanceof EntityManager)) {
            throw new IllegalArgumentException("Expected JPA EntityManager");
        }

        EntityManager entityManager = (EntityManager) persistenceContext;

        if (userAddress == null) {
            return null;
        }

        try {

            entityManager.persist(userAddress);
            return userAddress.getAddressId();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while creating user address", e);
            throw new DataAccessException("Error inserting user address into database", e);
        }
    }

    @Override
    public Optional<UserAddress> getAddressById(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot retrieve user address: addressId is null");
            return Optional.empty();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            UserAddress userAddress = entityManager.find(UserAddress.class, addressId);
            if (userAddress == null) {
                logger.info("No address found with ID: " + addressId);
                return Optional.empty();
            }

            return Optional.of(userAddress);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching user address by ID: " + addressId, e);
            throw new UserAddressAccessException("Error fetching user address by ID: " + addressId, e);
        }

    }

    @Override
    public List<UserAddress> getAddressesByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve user address: userId is null");
            return new ArrayList<>();
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<UserAddress> query =
                    entityManager.createQuery(
                            "SELECT ua FROM UserAddress ua WHERE ua.user.userId = :userId",
                            UserAddress.class
                    );

            query.setParameter("userId", userId);

            List<UserAddress> results = query.getResultList();

            if (results.isEmpty()) {
                logger.info("No address found for user ID: " + userId);
            }

            logger.info("Successfully retrieved " + results.size() +
                    " addresses from database");
            return  results;

        }  catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching address by user ID: " + userId, e);
            throw new UserAddressAccessException("Error fetching address by user ID: " + userId, e);
        }
    }

    @Override
    public List<UserAddress> getAllAddresses() {
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<UserAddress> query =
                    entityManager.createQuery(
                            "SELECT ua FROM UserAddress ua",
                            UserAddress.class
                    );

            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching user addresses", e);
            throw new UserAddressException("Error fetching user addresses", e);
        }
    }

    @Override
    public List<Long> getAddressIdsByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot retrieve address ID: userId is null");
            return null;
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            TypedQuery<Long> query =
                    entityManager.createQuery(
                            "SELECT ua.addressId FROM UserAddress ua WHERE ua.user.userId = :userId",
                            Long.class
                    );

            query.setParameter("userId", userId);

            List<Long> results = query.getResultList();
            if (results.isEmpty()) {
                logger.info("No address found for user ID: " + userId);
                return null;
            }

            return results;

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Database error while fetching user address by userId: " + userId, e);
            throw new UserAddressException("Error fetching user address by userId: " + userId, e);

        }
    }

    @Override
    public boolean updateUserAddress(UserAddress userAddress) {
        if (userAddress == null || userAddress.getAddressId() == null) {
            logger.warning("Cannot update user address: userAddress or addressId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            transaction.begin();

            entityManager.merge(userAddress);

            transaction.commit();
            return true;

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            logger.log(Level.SEVERE, "Database error while updating user address ID: " + userAddress.getAddressId(), e);
            throw new DataAccessException("Error updating user address in database", e);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteUserAddress(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot delete user address: addressId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            UserAddress existingAddress = entityManager.find(UserAddress.class, addressId);
            if (existingAddress == null) {
                logger.info("No address found with ID: " + addressId);
                return false;
            }

            entityManager.remove(existingAddress);

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE, "Database error while deleting user address with ID: " +
                    addressId, e);
            throw new UserAddressAccessException("Error deleting user address with ID: " +
                    addressId, e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteAddressesByUserId(Long userId) {
        if (userId == null) {
            logger.warning("Cannot delete user addresses: userId is null");
            return false;
        }

        EntityManager entityManager = JPAUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {

            User user =  entityManager.find(User.class, userId);
            if (user == null) {
                logger.info("No address found for user ID: " + userId);
                return false;
            }

            transaction.begin();

            List<UserAddress> addresses = user.getUserAddresses();

            List<UserAddress> addressCopy = new ArrayList<>(addresses);

            for (UserAddress address :  addressCopy) {
                user.removeAddress(address);
            }

            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();

            logger.log(Level.SEVERE, "Database error while deleting addresses for userId: " +
                    userId, e);
            throw new UserAddressAccessException("Error deleting addresses for userId: " +
                    userId, e);
        } finally {
            entityManager.close();
        }

    }

    @Override
    public boolean addressExists(Long addressId) {
        if (addressId == null) {
            logger.warning("Cannot check for address existence. addressId is null");
            return false;
        }

        try (EntityManager entityManager = JPAUtil.getEntityManager()) {

            UserAddress address = entityManager.find(UserAddress.class, addressId);
            return address != null;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error while fetching address ID: " +
                    addressId, e);
            throw new UserAddressAccessException("Error fetching address ID: " +
                    addressId, e);
        }
    }
}
