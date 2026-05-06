package com.example.food.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 用户实体
 * role: "USER" = 普通用户, "ADMIN" = 管理员
 */
@Entity(tableName = "users", indices = {
        @Index(value = "username", unique = true),
        @Index(value = "phone", unique = true),
        @Index(value = "userNumber", unique = true)
})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String password;
    private String role;       // "USER" 或 "ADMIN"
    private boolean enabled;   // 是否启用

    // 用户扩展信息
    private String userNumber;  // 系统生成的唯一用户编号（如 202605001）
    private String phone;       // 手机号（唯一，不可修改）
    private String displayName; // 显示名称（可修改）
    private Double height;      // 身高（cm）
    private Double weight;      // 体重（kg）
    private Integer age;        // 年龄
    private String gender;      // "MALE" 或 "FEMALE"

    private Integer existStatus = 1; // 1=存在, 2=软删除

    private Integer goal; // 1=增肌, 2=减脂

    // 基础构造函数（Room 使用 + 管理员创建）
    public User(String username, String password, String role, boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    @Ignore
    public User(int id, String username, String password, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    // 注册用完整构造函数
    @Ignore
    public User(String username, String password, String role, boolean enabled,
                String userNumber, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.userNumber = userNumber;
        this.phone = phone;
    }

    // ===== getters and setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUserNumber() { return userNumber; }
    public void setUserNumber(String userNumber) { this.userNumber = userNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getExistStatus() { return existStatus != null ? existStatus : 1; }
    public void setExistStatus(Integer existStatus) { this.existStatus = existStatus; }

    public Integer getGoal() { return goal; }
    public void setGoal(Integer goal) { this.goal = goal; }
}
