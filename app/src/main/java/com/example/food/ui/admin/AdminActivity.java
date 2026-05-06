package com.example.food.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food.R;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.UserRepository;
import com.example.food.ui.login.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 管理员后台主页
 * 包含两个 Tab：用户管理 + 食物库管理
 */
public class AdminActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        setSupportActionBar(findViewById(R.id.toolbar));

        // 设置 ViewPager 适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new UserManagementFragment();
                    case 1: return new FoodManagementFragment();
                    case 2: return new AdminSettingsFragment();
                    default: return new UserManagementFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        // 绑定 TabLayout 与 ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("用户管理"); break;
                case 1: tab.setText("食物库管理"); break;
                case 2: tab.setText("回收管理"); break;
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_change_pwd) {
            showChangePasswordDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        TextInputLayout tilCurrent = new TextInputLayout(this);
        tilCurrent.setHint("当前密码");
        tilCurrent.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etCurrent = new TextInputEditText(this);
        etCurrent.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilCurrent.addView(etCurrent);
        layout.addView(tilCurrent);

        TextInputLayout tilNew = new TextInputLayout(this);
        tilNew.setHint("新密码");
        tilNew.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etNew = new TextInputEditText(this);
        etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilNew.addView(etNew);
        layout.addView(tilNew);

        TextInputLayout tilConfirm = new TextInputLayout(this);
        tilConfirm.setHint("确认新密码");
        tilConfirm.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etConfirm = new TextInputEditText(this);
        etConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilConfirm.addView(etConfirm);
        layout.addView(tilConfirm);

        new MaterialAlertDialogBuilder(this)
                .setTitle("修改管理员密码")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认修改", (dialog, which) -> {
                    String current = etCurrent.getText() != null ? etCurrent.getText().toString().trim() : "";
                    String newPwd = etNew.getText() != null ? etNew.getText().toString().trim() : "";
                    String confirm = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(current) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirm)) {
                        Toast.makeText(this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPwd.equals(confirm)) {
                        Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPwd.length() < 6) {
                        Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserRepository userRepo = new UserRepository(this);
                    UserSessionPreferences session = new UserSessionPreferences(this);
                    userRepo.getUserById(session.getUserId(), admin -> {
                        if (admin != null && admin.getPassword().equals(current)) {
                            userRepo.changePassword(admin.getId(), newPwd, () ->
                                    Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "当前密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void logout() {
        UserSessionPreferences session = new UserSessionPreferences(this);
        session.clearSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
