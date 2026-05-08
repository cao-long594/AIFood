package com.example.food.data.repository;

import android.content.Context;

import com.example.food.core.concurrent.AppExecutors;
import com.example.food.db.AppDatabase;
import com.example.food.db.UserSeedImporter;
import com.example.food.db.dao.UserDao;
import com.example.food.db.entity.User;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 用户仓库
 * 提供用户相关操作的异步回调封装
 */
public class UserRepository {

    public interface Callback<T> {
        void onResult(T data);
    }

    private final Context appContext;
    private final AppDatabase database;
    private final UserDao userDao;

    public UserRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(appContext);
        this.userDao = database.userDao();
    }

    /**
     * 用户登录验证
     */
    public void authenticate(String username, String password, String role, Callback<User> callback) {
        AppExecutors.runOnIo(() -> {
            UserSeedImporter.ensureAdminSeeded(appContext, database);
            User user = userDao.authenticate(username, password, role);
            AppExecutors.runOnMain(() -> callback.onResult(user));
        });
    }

    /**
     * 获取所有用户列表
     */
    public void getAllUsers(Callback<List<User>> callback) {
        AppExecutors.runOnIo(() -> {
            List<User> users = userDao.getAllUsers();
            AppExecutors.runOnMain(() -> callback.onResult(users));
        });
    }

    /**
     * 添加新用户
     */
    public void addUser(User user, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.insert(user);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 更新用户信息
     */
    public void updateUser(User user, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.update(user);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 删除用户（硬删除）
     */
    public void deleteUser(int userId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.deleteById(userId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 软删除用户
     */
    public void softDeleteUser(int userId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.softDelete(userId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 恢复已软删除的用户
     */
    public void restoreUser(int userId, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.restoreUser(userId);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 切换用户启用/禁用状态
     */
    public void toggleUserEnabled(int userId, boolean enabled, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            User user = userDao.getUserById(userId);
            if (user != null) {
                user.setEnabled(enabled);
                userDao.update(user);
            }
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 获取管理员数量（用于防止删除最后一个管理员）
     */
    public void getAdminCount(Callback<Integer> callback) {
        AppExecutors.runOnIo(() -> {
            int count = userDao.getAdminCount();
            AppExecutors.runOnMain(() -> callback.onResult(count));
        });
    }

    /**
     * 根据ID查找用户
     */
    public void getUserById(int userId, Callback<User> callback) {
        AppExecutors.runOnIo(() -> {
            User user = userDao.getUserById(userId);
            AppExecutors.runOnMain(() -> callback.onResult(user));
        });
    }

    /**
     * 根据用户名查找用户
     */
    public void getUserByUsername(String username, Callback<User> callback) {
        AppExecutors.runOnIo(() -> {
            User user = userDao.getUserByUsername(username);
            AppExecutors.runOnMain(() -> callback.onResult(user));
        });
    }

    /**
     * 根据手机号查找用户
     */
    public void getUserByPhone(String phone, Callback<User> callback) {
        AppExecutors.runOnIo(() -> {
            User user = userDao.getUserByPhone(phone);
            AppExecutors.runOnMain(() -> callback.onResult(user));
        });
    }

    /**
     * 生成用户编号（年月+当月流水号）
     */
    public void generateUserNumber(Callback<String> callback) {
        AppExecutors.runOnIo(() -> {
            Calendar now = Calendar.getInstance(Locale.CHINA);
            String prefix = String.format(Locale.CHINA, "%d%02d",
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH) + 1);
            int count = userDao.getUserCountByMonth(prefix);
            String userNumber = prefix + String.format(Locale.CHINA, "%03d", count + 1);
            AppExecutors.runOnMain(() -> callback.onResult(userNumber));
        });
    }

    /**
     * 注册新用户（生成编号、验证唯一性由调用方负责）
     */
    public void registerUser(User user, Callback<Long> callback) {
        AppExecutors.runOnIo(() -> {
            long id = userDao.insert(user);
            AppExecutors.runOnMain(() -> callback.onResult(id));
        });
    }

    /**
     * 检查手机号是否已被注册
     */
    public void isPhoneTaken(String phone, Callback<Boolean> callback) {
        AppExecutors.runOnIo(() -> {
            User existing = userDao.getUserByPhone(phone);
            AppExecutors.runOnMain(() -> callback.onResult(existing != null));
        });
    }

    /**
     * 更新用户个人资料（不允许修改手机号、用户编号、角色）
     */
    public void updateProfile(User user, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            userDao.update(user);
            AppExecutors.runOnMain(onComplete);
        });
    }

    /**
     * 修改密码
     */
    public void changePassword(int userId, String newPassword, Runnable onComplete) {
        AppExecutors.runOnIo(() -> {
            User user = userDao.getUserById(userId);
            if (user != null) {
                user.setPassword(newPassword);
                userDao.update(user);
            }
            AppExecutors.runOnMain(onComplete);
        });
    }
}
