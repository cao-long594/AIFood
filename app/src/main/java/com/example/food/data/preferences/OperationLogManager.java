package com.example.food.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 管理员操作日志管理
 * 记录管理员在用户管理和食物库管理中的操作，支持撤销
 */
public class OperationLogManager {

    private static final String PREFS_NAME = "admin_operation_log";
    private static final String KEY_LOGS = "logs";
    private static final String KEY_COUNTER = "counter";

    private final SharedPreferences preferences;
    private final SimpleDateFormat dateFormat;

    public OperationLogManager(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
    }

    /**
     * 操作日志条目
     */
    public static class LogEntry {
        public int id;
        public String action;      // user_toggle, user_add, user_delete, food_promote, food_demote, food_delete
        public String description; // 操作描述
        public String undoJson;    // 用于撤销的数据(JSON)
        public long timestamp;

        public LogEntry(int id, String action, String description, String undoJson, long timestamp) {
            this.id = id;
            this.action = action;
            this.description = description;
            this.undoJson = undoJson;
            this.timestamp = timestamp;
        }
    }

    /**
     * 添加操作日志
     * @param action     操作类型
     * @param description 操作描述
     * @param undoJson   撤销所需数据(可为空)
     */
    public void addLog(String action, String description, String undoJson) {
        int id = preferences.getInt(KEY_COUNTER, 0) + 1;
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("action", action);
            obj.put("description", description);
            obj.put("undoJson", undoJson != null ? undoJson : "");
            obj.put("timestamp", System.currentTimeMillis());

            JSONArray arr = new JSONArray(preferences.getString(KEY_LOGS, "[]"));
            arr.put(obj);
            // 最多保留50条
            while (arr.length() > 50) {
                arr.remove(0);
            }
            preferences.edit()
                    .putString(KEY_LOGS, arr.toString())
                    .putInt(KEY_COUNTER, id)
                    .apply();
        } catch (Exception ignored) {}
    }

    /**
     * 获取所有日志
     */
    public List<LogEntry> getLogs() {
        List<LogEntry> logs = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(preferences.getString(KEY_LOGS, "[]"));
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject obj = arr.getJSONObject(i);
                logs.add(new LogEntry(
                        obj.getInt("id"),
                        obj.getString("action"),
                        obj.getString("description"),
                        obj.optString("undoJson", ""),
                        obj.getLong("timestamp")
                ));
            }
        } catch (Exception ignored) {}
        return logs;
    }

    /**
     * 获取日志的时间格式化字符串
     */
    public String formatTime(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * 判断操作是否可撤销
     */
    public static boolean isUndoable(String action) {
        return "user_toggle".equals(action)
                || "food_promote".equals(action)
                || "food_demote".equals(action);
    }
}
