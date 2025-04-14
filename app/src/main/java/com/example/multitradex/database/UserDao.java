package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.multitradex.models.User;

import java.util.List;

@Dao
public interface UserDao {

    // ------------------- INSERT -------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMultipleUsers(List<User> users);

    // ------------------- UPDATE -------------------
    @Update
    void updateUser(User user);

    // ------------------- DELETE -------------------
    @Delete
    void deleteUser(User user);

    @Delete
    void deleteMultipleUsers(List<User> users);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    // ------------------- QUERY -------------------
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User getUserById(String userId);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    LiveData<User> getUserByIdLive(String userId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsersLive();

    @Query("SELECT * FROM users WHERE name LIKE '%' || :name || '%'")
    LiveData<List<User>> getUsersByName(String name);

    @Query("SELECT * FROM users WHERE role = :role")
    LiveData<List<User>> getUsersByRole(String role);

    @Query("SELECT * FROM users WHERE email = :email")
    LiveData<List<User>> getUsersByEmail(String email);
}
