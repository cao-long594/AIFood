package com.example.food.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.MainActivity;
import com.example.food.R;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.UserRepository;
import com.example.food.db.entity.User;
import com.example.food.model.UserRole;
import com.example.food.ui.admin.AdminActivity;
import com.example.food.ui.register.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 登录页面
 * 支持账号密码登录 + 身份选择（普通用户/管理员）
 * 登录成功后根据角色跳转不同首页
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Spinner spRole;
    private TextView tvLoginError, tvToRegister;
    private MaterialButton btnLogin;

    private UserRepository userRepository;
    private UserSessionPreferences sessionPreferences;

    private String selectedRole = "USER";  // 默认普通用户

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionPreferences = new UserSessionPreferences(this);

        // 如果已登录，直接跳转
        if (sessionPreferences.isLoggedIn()) {
            routeToHome();
            return;
        }

        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);
        initViews();
        setupRoleSpinner();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        spRole = findViewById(R.id.sp_role);
        tvLoginError = findViewById(R.id.tv_login_error);
        tvToRegister = findViewById(R.id.tv_to_register);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void setupRoleSpinner() {
        String[] roles = {"普通用户", "管理员"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);
        spRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = position == 1 ? "ADMIN" : "USER";
                tvLoginError.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "USER";
            }
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";

        // 基本验证
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showError("请输入账号和密码");
            return;
        }

        tvLoginError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");

        userRepository.authenticate(username, password, selectedRole, user -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("登录");

            if (user != null) {
                // 登录成功，保存会话
                sessionPreferences.saveSession(user);
                routeToHome();
            } else {
                showError("账号或密码错误");
            }
        });
    }

    private void routeToHome() {
        Intent intent;
        if (sessionPreferences.isAdmin()) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
    }
}
