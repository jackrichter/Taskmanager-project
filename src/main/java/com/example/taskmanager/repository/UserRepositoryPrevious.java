package com.example.taskmanager.repository;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserPrevious;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepositoryPrevious extends JpaRepository<UserPrevious, Integer> {
    List<User> findUserByName(String name);
    List<User> findUserByEmail(String email);
    List<User> findUserByAgeGreaterThan(int age);
    List<User> findUserByAgeGreaterThanOrderByAgeDesc(int age);
    boolean existsUserByEmail(String email);

    List<User> findUserByNameAndEmail(String name, String email);

    @Query("select u from UserPrevious u where u.email = ?1")
    List<User> getUserByEmail(String email);

    // Better alternative JPQL!!!
    @Query("select u from UserPrevious u where u.email = :email")
    List<User> getUserByEmail2(@Param("email") String email);

    @Query("select u from UserPrevious u where u.name = :name and u.age > :age")
    List<User> findByNameAndOlderThan(@Param("name") String name, @Param("age") Integer age);

    // Native Query (Dependent on the underlying SQL flavor: PostgreSQL, MySQL, etc., so JPQL is to be preferred!)
    @Query(value = "SELECT * FROM users2 WHERE email LIKE '%:?1%'", nativeQuery = true)
    List<User> searchByEmailFragment(String email);
//    @Query("SELECT u FROM User u WHERE u.email LIKE %?1%")
//    List<User> searchByEmailFragment(String email);

    // Updating
    @Modifying
    @Query("UPDATE UserPrevious u SET u.age = :age WHERE u.email = :email")
    void updateUserAgeByEmail(@Param("email") String email, @Param("age") Integer age);
}
