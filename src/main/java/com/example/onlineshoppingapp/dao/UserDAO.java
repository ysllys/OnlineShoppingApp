package com.example.onlineshoppingapp.dao;

import com.example.onlineshoppingapp.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persists a new User entity to the database (handles registration).
     * @param user The User object to save.
     * @return The persisted User entity, including its auto-generated ID.
     */
    @Transactional
    public User save(User user) {
        // entityManager.persist() inserts the new entity.
        // The object passed (user) is updated with the generated ID after commit.
        entityManager.persist(user);
        return user;
    }
    /**
     * Finds a User by the primary key ID.
     * @param id The ID of the user.
     * @return An Optional containing the User, or empty if not found.
     */
    public Optional<User> findById(Integer id) {
        // entityManager.find() is the standard way to retrieve by primary key
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    /**
     * Finds a User by username to check for registration uniqueness.
     * @param username The username to search for.
     * @return An Optional containing the User, or empty if not found.
     */
    public Optional<User> findByUsername(String username) {
        // HQL references the Java entity and attribute names
        String hql = "SELECT u FROM User u WHERE u.username = :username";

        try {
            User user = entityManager.createQuery(hql, User.class)
                    .setParameter("username", username)
                    // getSingleResult() throws NoResultException if no user is found
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds a User by email to check for registration uniqueness.
     * @param email The email to search for.
     * @return An Optional containing the User, or empty if not found.
     */
    public Optional<User> findByEmail(String email) {
        // HQL references the Java entity and attribute names
        String hql = "SELECT u FROM User u WHERE u.email = :email";

        try {
            User user = entityManager.createQuery(hql, User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}