package com.example.food.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food.db.entity.User;
import com.example.food.model.UserRole;

/**
 * 用户会话管理
 * 使用SharedPreferences持久化登录状态
 */
public class UserSessionPreferences {

    private static final String PREFS_NAME = "user_session_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences preferences;

    public UserSessionPreferences(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存登录会话
     */
    public void saveSession(User user) {
        preferences.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_ROLE, user.getRole())
                .apply();
    }

    /**
     * 清除登录会话（退出登录）
     */
    public void clearSession() {
        preferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    public UserRole getRole() {
        String role = preferences.getString(KEY_ROLE, "USER");
        return "ADMIN".equals(role) ? UserRole.ADMIN : UserRole.USER;
    }

    public boolean isAdmin() {
        return getRole() == UserRole.ADMIN;
    }
}
