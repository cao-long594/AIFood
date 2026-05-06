package com.example.food.db;

import android.content.Context;
import android.util.Log;

import com.example.food.db.dao.UserDao;
import com.example.food.db.entity.User;

/**
 * 种子数据导入器
 * 确保默认管理员和测试用户始终存在
 */
public class UserSeedImporter {

    private static final String TAG = "UserSeedImporter";
    private static final Object LOCK = new Object();

    /**
     * 确保默认账号已存在（每次启动检查数据库，不依赖 SharedPreferences）
     */
    public static void ensureAdminSeeded(Context context, AppDatabase database) {
        synchronized (LOCK) {
            UserDao userDao = database.userDao();

            // 不管之前是否种子过，每次都检查 admin 是否存在
            User existingAdmin = userDao.getUserByUsername("admin");
            if (existingAdmin == null) {
                try {
                    User admin = new User("admin", "admin123", "ADMIN", true);
                    userDao.insert(admin);
                    Log.d(TAG, "默认管理员账号已创建 (admin/admin123)");
                } catch (Exception e) {
                    Log.e(TAG, "创建管理员账号失败", e);
                }
            } else if (!existingAdmin.isEnabled()) {
                // 如果管理员被意外禁用，恢复启用
                existingAdmin.setEnabled(true);
                userDao.update(existingAdmin);
                Log.d(TAG, "管理员账号已重新启用");
            }

            // 创建默认测试普通用户
            User existingUser = userDao.getUserByUsername("testuser");
            if (existingUser == null) {
                try {
                    User testUser = new User("testuser", "user123", "USER", true);
                    testUser.setUserNumber("202605001");
                    testUser.setPhone("13800000001");
                    testUser.setDisplayName("测试用户");
                    userDao.insert(testUser);
                    Log.d(TAG, "默认普通用户已创建 (testuser/user123)");
                } catch (Exception e) {
                    Log.e(TAG, "创建默认用户失败", e);
                }
            }
        }
    }
}
