package com.example.food.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.example.food.data.repository.UserRepository;
import com.example.food.db.entity.User;
import com.example.food.ui.login.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 用户注册页面
 * 普通用户自行注册，自动生成用户编号，注册后返回登录页
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etPassword, etPasswordConfirm;
    private TextView tvError, tvToLogin;
    private MaterialButton btnRegister;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);

        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        tvError = findViewById(R.id.tv_register_error);
        btnRegister = findViewById(R.id.btn_register);
        tvToLogin = findViewById(R.id.tv_to_login);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String passwordConfirm = etPasswordConfirm.getText() != null ? etPasswordConfirm.getText().toString().trim() : "";

        // 基本验证
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm)) {
            showError("请填写所有字段");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            showError("两次密码输入不一致");
            return;
        }

        if (password.length() < 6) {
            showError("密码长度至少6位");
            return;
        }

        tvError.setVisibility(View.GONE);
        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        // 检查用户名是否已存在
        userRepository.getUserByUsername(username, existingUser -> {
            if (existingUser != null) {
                showError("用户名已被使用");
                btnRegister.setEnabled(true);
                btnRegister.setText("注册");
                return;
            }

            // 检查手机号是否已注册
            userRepository.isPhoneTaken(phone, isTaken -> {
                if (isTaken) {
                    showError("该手机号已被注册");
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");
                    return;
                }

                // 生成用户编号并注册
                userRepository.generateUserNumber(userNumber -> {
                    User newUser = new User(username, password, "USER", true, userNumber, phone);
                    userRepository.registerUser(newUser, id -> {
                        Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                });
            });
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
