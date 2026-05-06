package com.example.food.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.food.db.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE users SET existStatus = 2 WHERE id = :id")
    void softDelete(int id);

    @Query("UPDATE users SET existStatus = 1 WHERE id = :id")
    void restoreUser(int id);

    @Query("SELECT * FROM users WHERE existStatus = 1 ORDER BY username ASC")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND role = :role AND enabled = 1 AND existStatus = 1")
    User authenticate(String username, String password, String role);

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND existStatus = 1")
    int getAdminCount();

    @Query("SELECT * FROM users WHERE phone = :phone")
    User getUserByPhone(String phone);

    @Query("SELECT * FROM users WHERE userNumber = :userNumber")
    User getUserByUserNumber(String userNumber);

    @Query("SELECT COUNT(*) FROM users WHERE userNumber LIKE :prefix || '%'")
    int getUserCountByMonth(String prefix);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
}
