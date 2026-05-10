package com.example.taskmanager.repository;

import com.example.taskmanager.model.UserPrevious;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest                // Only for repository testing! Test JPQL and Native queries. We use an in memory database (H2).
@ActiveProfiles("test")
public class UserRepositoryPreviousTest {

    /**
     * DataJpaTest loads entities, repositories, JPA configuration, H2 database, and the transaction manager
     * It does not load the Controller, Service, Security, DTO, and the full application context.
     */

    @Autowired      // Injects the repository. Obs, not mocked
    private  UserRepositoryPrevious userRepositoryPrev;

    @Test
    void saveUser_shouldPersistUserInDatabase () {
        UserPrevious user = new UserPrevious();
        user.setName("Jack");
        user.setEmail("jack@test.com");
        user.setAge(30);

        UserPrevious savedUser = userRepositoryPrev.save(user);

        assertNotNull(savedUser.getId());       // Because it is not a mock we need to use the real object and not any(), which is used only for mocking.
        assertEquals("Jack", savedUser.getName());
    }

    @Test
    void existUserByEmail_shouldReturnTrue_whenEmailExists() {
        UserPrevious user = new UserPrevious();
        user.setName("John");
        user.setEmail("john@test.com");
        user.setAge(25);

        userRepositoryPrev.save(user);

        boolean exists = userRepositoryPrev.existsUserByEmail("john@test.com");
        List<UserPrevious> usersByEmail = userRepositoryPrev.getUserByEmail("john@test.com");

        assertTrue(exists);
        assertFalse(usersByEmail.isEmpty());
    }

    @Test
    void updateUserAgeByEmail_shouldUpdateAgeCorrectly() {
        // Create a User
        UserPrevious user = new UserPrevious();
        user.setName("david");
        user.setEmail("david@test.com");
        user.setAge(20);

        // Save the user to the database (H2)
        userRepositoryPrev.save(user);

        // Update user's age by its email
        userRepositoryPrev.updateUserAgeByEmail("david@test.com", 35);

        // Retrieve the updated User
        UserPrevious updatedUser = userRepositoryPrev.getUserByEmail("david@test.com").get(0);

        // Assert result
        assertEquals(35, updatedUser.getAge());
    }

    @Test
    void updateUserAgeById_shouldUpdateAgeCorrectly() {
        // Create a User
        UserPrevious user = new UserPrevious();
        user.setName("david");
        user.setEmail("david@test.com");
        user.setAge(20);

        // Save the user to the database (H2)
        userRepositoryPrev.save(user);

        // Update user's age
        user.setAge(35);
        userRepositoryPrev.save(user);

        // Retrieve the updated User
        UserPrevious updatedUser = userRepositoryPrev.findById(1).get();

        // Assert result
        assertEquals(35, updatedUser.getAge());
    }

    @Test
    void saveUser_shouldFail_whenEmailIsNull() {
        // Prepare data
        UserPrevious user = new UserPrevious();
        user.setName("Invalid User");
        user.setAge(25);

        // Assert that it fails when saving
        assertThrows(Exception.class, () -> userRepositoryPrev.save(user));
    }
}
