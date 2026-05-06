package com.example.food.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.preferences.OperationLogManager;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.UserRepository;
import com.example.food.db.entity.User;

import org.json.JSONObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员 - 用户管理页面
 * 查看所有用户、添加/删除/启用/禁用用户
 */
public class UserManagementFragment extends Fragment {

    private RecyclerView rvUsers;
    private TextView tvEmpty;
    private FloatingActionButton fabAddUser;

    private Spinner spinnerFilter;
    private UserRepository userRepository;
    private UserSessionPreferences sessionPreferences;
    private OperationLogManager logManager;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();
    private String userFilter = "全部";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_users, container, false);
        rvUsers = root.findViewById(R.id.rv_users);
        tvEmpty = root.findViewById(R.id.tv_empty);
        fabAddUser = root.findViewById(R.id.fab_add_user);
        spinnerFilter = root.findViewById(R.id.spinner_user_filter);

        userRepository = new UserRepository(requireContext());
        sessionPreferences = new UserSessionPreferences(requireContext());
        logManager = new OperationLogManager(requireContext());

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter();
        rvUsers.setAdapter(userAdapter);

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        // 用户状态筛选
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"全部", "使用中", "已禁用"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                userFilter = filterAdapter.getItem(pos);
                applyUserFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        loadUsers();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        userRepository.getAllUsers(users -> {
            userList = users != null ? users : new ArrayList<>();
            applyUserFilter();
        });
    }

    private void applyUserFilter() {
        filteredUserList = new ArrayList<>();
        for (User u : userList) {
            if ("使用中".equals(userFilter) && !u.isEnabled()) continue;
            if ("已禁用".equals(userFilter) && u.isEnabled()) continue;
            filteredUserList.add(u);
        }
        userAdapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredUserList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddUserDialog() {
        TextInputLayout tilUsername = new TextInputLayout(requireContext());
        tilUsername.setHint("用户名");
        tilUsername.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etUsername = new TextInputEditText(requireContext());
        tilUsername.addView(etUsername);

        TextInputLayout tilPassword = new TextInputLayout(requireContext());
        tilPassword.setHint("密码");
        tilPassword.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etPassword = new TextInputEditText(requireContext());
        tilPassword.addView(etPassword);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        layout.addView(tilUsername);
        layout.addView(tilPassword);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("添加用户")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("添加", (dialog, which) -> {
                    String username = etUsername.getText() != null
                            ? etUsername.getText().toString().trim() : "";
                    String password = etPassword.getText() != null
                            ? etPassword.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                        Toast.makeText(getContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(username, password, "USER", true);
                    userRepository.addUser(newUser, () -> {
                        Toast.makeText(getContext(), "用户已添加", Toast.LENGTH_SHORT).show();
                        // 查一下刚插入的用户ID
                        userRepository.getUserByUsername(username, u -> {
                            try {
                                org.json.JSONObject undoData = new org.json.JSONObject();
                                undoData.put("userId", u != null ? u.getId() : -1);
                                undoData.put("username", username);
                                undoData.put("password", password);
                                logManager.addLog("user_add", "添加用户 " + username, undoData.toString());
                            } catch (Exception ignored) {}
                        });
                        loadUsers();
                    });
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText etDisplayName = dialogView.findViewById(R.id.et_display_name);
        TextInputEditText etHeight = dialogView.findViewById(R.id.et_height);
        TextInputEditText etWeight = dialogView.findViewById(R.id.et_weight);
        TextInputEditText etBirthDate = dialogView.findViewById(R.id.et_birth_date);
        RadioGroup toggleGender = dialogView.findViewById(R.id.toggle_gender);
        RadioGroup toggleGoal = dialogView.findViewById(R.id.toggle_goal);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);
        TextInputEditText etPasswordConfirm = dialogView.findViewById(R.id.et_password_confirm);

        // 性别选项卡 - 选中态样式
        if (user.getGender() != null) {
            toggleGender.check("MALE".equals(user.getGender()) ? R.id.btn_gender_male : R.id.btn_gender_female);
        }
        toggleGender.setOnCheckedChangeListener((g, id) -> updateRadioStyle(g, id, R.id.btn_gender_male, R.id.btn_gender_female));
        updateRadioStyle(toggleGender, toggleGender.getCheckedRadioButtonId(), R.id.btn_gender_male, R.id.btn_gender_female);

        // 目标选项卡 - 选中态样式
        if (user.getGoal() != null) {
            toggleGoal.check(user.getGoal() == 2 ? R.id.btn_goal_cut : R.id.btn_goal_bulk);
        }
        toggleGoal.setOnCheckedChangeListener((g, id) -> updateRadioStyle(g, id, R.id.btn_goal_bulk, R.id.btn_goal_cut));
        updateRadioStyle(toggleGoal, toggleGoal.getCheckedRadioButtonId(), R.id.btn_goal_bulk, R.id.btn_goal_cut);

        // 填充现有数据
        if (user.getDisplayName() != null) etDisplayName.setText(user.getDisplayName());
        if (user.getHeight() != null) etHeight.setText(String.valueOf(user.getHeight()));
        if (user.getWeight() != null) etWeight.setText(String.valueOf(user.getWeight()));
        if (user.getAge() != null) {
            etBirthDate.setText("约" + user.getAge() + "岁");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("编辑用户 - " + user.getUsername())
                .setView(dialogView)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    // 昵称
                    String displayName = etDisplayName.getText() != null
                            ? etDisplayName.getText().toString().trim() : "";
                    // 身高
                    String heightStr = etHeight.getText() != null
                            ? etHeight.getText().toString().trim() : "";
                    // 体重
                    String weightStr = etWeight.getText() != null
                            ? etWeight.getText().toString().trim() : "";
                    // 密码
                    String password = etPassword.getText() != null
                            ? etPassword.getText().toString().trim() : "";
                    String passwordConfirm = etPasswordConfirm.getText() != null
                            ? etPasswordConfirm.getText().toString().trim() : "";
                    int genderId = toggleGender.getCheckedRadioButtonId();
                    int goalId = toggleGoal.getCheckedRadioButtonId();

                    if (!TextUtils.isEmpty(password) || !TextUtils.isEmpty(passwordConfirm)) {
                        if (!password.equals(passwordConfirm)) {
                            Toast.makeText(getContext(), "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (password.length() < 6) {
                            Toast.makeText(getContext(), "密码长度至少6位", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(displayName)) user.setDisplayName(displayName);
                    if (!TextUtils.isEmpty(heightStr)) {
                        user.setHeight(Double.parseDouble(heightStr));
                    }
                    if (!TextUtils.isEmpty(weightStr)) {
                        user.setWeight(Double.parseDouble(weightStr));
                    }
                    user.setGender(genderId == R.id.btn_gender_male ? "MALE" : "FEMALE");
                    user.setGoal(goalId == R.id.btn_goal_cut ? 2 : 1);

                    userRepository.updateProfile(user, () -> {
                        if (!TextUtils.isEmpty(password)) {
                            userRepository.changePassword(user.getId(), password, () -> {});
                        }
                        Toast.makeText(getContext(), "用户资料已更新", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    });
                })
                .show();
    }

    /**
     * 用户列表适配器
     */
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = filteredUserList.get(position);
            holder.tvUsername.setText(user.getUsername());
            holder.tvRole.setText("ADMIN".equals(user.getRole()) ? "管理员" : "普通用户");
            holder.tvStatus.setText(user.isEnabled() ? "正常" : "已禁用");
            holder.tvStatus.setTextColor(user.isEnabled()
                    ? getResources().getColor(R.color.mint_primary)
                    : getResources().getColor(R.color.token_fat));
            holder.switchEnabled.setChecked(user.isEnabled());

            // 不能禁用/删除/编辑自己
            boolean isSelf = user.getId() == sessionPreferences.getUserId();
            holder.switchEnabled.setEnabled(!isSelf);
            holder.btnDelete.setVisibility(isSelf ? View.GONE : View.VISIBLE);
            holder.btnEdit.setVisibility(isSelf ? View.GONE : View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> showEditUserDialog(user));

            holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean finalIsChecked = isChecked;
                userRepository.toggleUserEnabled(user.getId(), isChecked, () -> {
                    user.setEnabled(finalIsChecked);
                    Toast.makeText(getContext(),
                            finalIsChecked ? "用户已启用" : "用户已禁用", Toast.LENGTH_SHORT).show();
                    // 记录操作日志
                    try {
                        JSONObject undoData = new JSONObject();
                        undoData.put("userId", user.getId());
                        undoData.put("wasEnabled", !finalIsChecked);
                        logManager.addLog("user_toggle",
                                (finalIsChecked ? "启用" : "禁用") + "用户 " + user.getUsername(),
                                undoData.toString());
                    } catch (Exception ignored) {}
                });
            });

            holder.btnDelete.setOnClickListener(v -> {
                // 检查是否最后一个管理员
                if ("ADMIN".equals(user.getRole())) {
                    userRepository.getAdminCount(count -> {
                        if (count <= 1) {
                            Toast.makeText(getContext(), "至少保留一个管理员", Toast.LENGTH_SHORT).show();
                        } else {
                            confirmDeleteUser(user);
                        }
                    });
                } else {
                    confirmDeleteUser(user);
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredUserList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvRole, tvStatus;
            SwitchCompat switchEnabled;
            MaterialButton btnEdit, btnDelete;

            UserViewHolder(View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tv_username);
                tvRole = itemView.findViewById(R.id.tv_role);
                tvStatus = itemView.findViewById(R.id.tv_status);
                switchEnabled = itemView.findViewById(R.id.switch_enabled);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }

    private void confirmDeleteUser(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除用户 " + user.getUsername() + " 吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    String deletedUsername = user.getUsername();
                    int deletedId = user.getId();
                    userRepository.softDeleteUser(deletedId, () -> {
                        Toast.makeText(getContext(), "用户已删除", Toast.LENGTH_SHORT).show();
                        try {
                            org.json.JSONObject undoData = new org.json.JSONObject();
                            undoData.put("userId", deletedId);
                            undoData.put("username", deletedUsername);
                            logManager.addLog("user_delete", "删除用户 " + deletedUsername, undoData.toString());
                        } catch (Exception ignored) {}
                        loadUsers();
                    });
                })
                .show();
    }

    private void updateRadioStyle(RadioGroup group, int checkedId, int btn1Id, int btn2Id) {
        android.widget.RadioButton btn1 = group.findViewById(btn1Id);
        android.widget.RadioButton btn2 = group.findViewById(btn2Id);
        int selected = getResources().getColor(R.color.token_primary);
        int normal = getResources().getColor(R.color.text_primary);
        btn1.setTextColor(checkedId == btn1Id ? selected : normal);
        btn2.setTextColor(checkedId == btn2Id ? selected : normal);
    }
}
