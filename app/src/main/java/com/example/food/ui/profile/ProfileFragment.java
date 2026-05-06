package com.example.food.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food.R;
import com.example.food.data.preferences.UserGoalPreferences;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.UserRepository;
import com.example.food.db.entity.User;
import com.example.food.model.UserGoal;
import com.example.food.ui.login.LoginActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 个人中心页面
 * 展示用户信息、建议每日摄入热量、三大营养素分配及扇形图
 */
public class ProfileFragment extends Fragment {

    private TextView tvUserNumber, tvDisplayName, tvUsername, tvPhone;
    private TextView tvGender, tvAge, tvHeight, tvWeight, tvDailyCalorie;
    private TextView tvMacroCarb, tvMacroProtein, tvMacroFat;
    private MaterialCardView cardMacro;
    private PieChart pieChart;
    private MaterialButton btnEditProfile, btnLogout;

    private UserRepository userRepository;
    private UserSessionPreferences sessionPreferences;
    private UserGoalPreferences goalPreferences;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        userRepository = new UserRepository(requireContext());
        sessionPreferences = new UserSessionPreferences(requireContext());
        goalPreferences = new UserGoalPreferences(requireContext(), sessionPreferences.getUserId());

        tvUserNumber = root.findViewById(R.id.tv_user_number);
        tvDisplayName = root.findViewById(R.id.tv_display_name);
        tvUsername = root.findViewById(R.id.tv_username);
        tvPhone = root.findViewById(R.id.tv_phone);
        tvGender = root.findViewById(R.id.tv_gender);
        tvAge = root.findViewById(R.id.tv_age);
        tvHeight = root.findViewById(R.id.tv_height);
        tvWeight = root.findViewById(R.id.tv_weight);
        tvDailyCalorie = root.findViewById(R.id.tv_daily_calorie);
        tvMacroCarb = root.findViewById(R.id.tv_macro_carb);
        tvMacroProtein = root.findViewById(R.id.tv_macro_protein);
        tvMacroFat = root.findViewById(R.id.tv_macro_fat);
        cardMacro = root.findViewById(R.id.card_macro);
        pieChart = root.findViewById(R.id.pie_chart_macro);
        btnEditProfile = root.findViewById(R.id.btn_edit_profile);
        btnLogout = root.findViewById(R.id.btn_logout);

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> logout());

        setupPieChart();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setCenterTextSize(11f);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.getLegend().setEnabled(false);
    }

    private void loadUserProfile() {
        int userId = sessionPreferences.getUserId();
        if (userId == -1) return;

        userRepository.getUserById(userId, user -> {
            if (user == null) return;
            currentUser = user;

            tvUserNumber.setText(user.getUserNumber() != null ? user.getUserNumber() : "--");
            tvDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
            tvUsername.setText(user.getUsername());
            tvPhone.setText(user.getPhone() != null ? user.getPhone() : "--");
            tvGender.setText(formatGender(user.getGender()));
            tvAge.setText(user.getAge() != null ? String.valueOf(user.getAge()) + " 岁" : "未设置");
            tvHeight.setText(user.getHeight() != null ? String.format(Locale.CHINA, "%.1f cm", user.getHeight()) : "未设置");
            tvWeight.setText(user.getWeight() != null ? String.format(Locale.CHINA, "%.1f kg", user.getWeight()) : "未设置");

            updateDailyCalorie(user);
            syncCalorieGoalToHome(user);
        });
    }

    private void updateDailyCalorie(User user) {
        if (user.getHeight() == null || user.getWeight() == null
                || user.getAge() == null || user.getGender() == null) {
            tvDailyCalorie.setText("完善信息后自动计算");
            cardMacro.setVisibility(View.GONE);
            return;
        }

        double bmr;
        if ("MALE".equals(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        double dailyCalorie = Math.round(bmr * 1.2);
        tvDailyCalorie.setText(String.format(Locale.CHINA, "%.0f", dailyCalorie));

        // 计算营养素分配
        int goal = user.getGoal() != null ? user.getGoal() : 1;
        double carbRatio, proteinRatio, fatRatio;
        if (goal == 2) {
            // 减脂：碳水40% 蛋白30% 脂肪30%
            carbRatio = 0.40; proteinRatio = 0.30; fatRatio = 0.30;
        } else {
            // 增肌：碳水45% 蛋白30% 脂肪25%
            carbRatio = 0.45; proteinRatio = 0.30; fatRatio = 0.25;
        }

        double carbGrams = Math.round((dailyCalorie * carbRatio) / 4);
        double proteinGrams = Math.round((dailyCalorie * proteinRatio) / 4);
        double fatGrams = Math.round((dailyCalorie * fatRatio) / 9);

        tvMacroCarb.setText(String.format(Locale.CHINA, "碳水：%.0f g (%.0f%%)", carbGrams, carbRatio * 100));
        tvMacroProtein.setText(String.format(Locale.CHINA, "蛋白质：%.0f g (%.0f%%)", proteinGrams, proteinRatio * 100));
        tvMacroFat.setText(String.format(Locale.CHINA, "脂肪：%.0f g (%.0f%%)", fatGrams, fatRatio * 100));
        cardMacro.setVisibility(View.VISIBLE);

        // 扇形图
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) carbRatio, "碳水"));
        entries.add(new PieEntry((float) proteinRatio, "蛋白质"));
        entries.add(new PieEntry((float) fatRatio, "脂肪"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                getResources().getColor(R.color.token_carb),
                getResources().getColor(R.color.token_protein),
                getResources().getColor(R.color.token_fat)
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter());
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void syncCalorieGoalToHome(User user) {
        if (user.getHeight() == null || user.getWeight() == null
                || user.getAge() == null || user.getGender() == null) return;

        double bmr;
        if ("MALE".equals(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        double dailyCalorie = Math.round(bmr * 1.2);

        UserGoal goal = goalPreferences.loadUserGoal();
        goal.setCaloriesGoal(dailyCalorie);

        // 计算推荐营养值
        int g = user.getGoal() != null ? user.getGoal() : 1;
        double carbR = (g == 2) ? 0.40 : 0.45;
        double proteinR = 0.30;
        double fatR = (g == 2) ? 0.30 : 0.25;
        double recCarb = Math.round((dailyCalorie * carbR) / 4);
        double recProtein = Math.round((dailyCalorie * proteinR) / 4);
        double recFat = Math.round((dailyCalorie * fatR) / 9);

        goal.setCarbohydrateGoal(recCarb);
        goal.setProteinGoal(recProtein);
        goal.setFatGoal(recFat);
        goalPreferences.saveUserGoal(goal);
    }

    private String formatGender(String gender) {
        if (gender == null) return "未设置";
        switch (gender) {
            case "MALE": return "男";
            case "FEMALE": return "女";
            default: return "未设置";
        }
    }

    private void logout() {
        sessionPreferences.clearSession();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

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

        // 性别选项卡 - 初始化选中态样式 + 切换监听
        setupRadioGroupToggle(toggleGender, R.id.btn_gender_male, R.id.btn_gender_female);
        if (currentUser.getGender() != null) {
            toggleGender.check("MALE".equals(currentUser.getGender()) ? R.id.btn_gender_male : R.id.btn_gender_female);
        }
        toggleGender.setOnCheckedChangeListener((group, checkedId) -> {
            updateRadioButtonStyle(group, checkedId, R.id.btn_gender_male, R.id.btn_gender_female);
        });

        // 目标选项卡 - 初始化选中态样式 + 切换监听
        setupRadioGroupToggle(toggleGoal, R.id.btn_goal_bulk, R.id.btn_goal_cut);
        if (currentUser.getGoal() != null) {
            toggleGoal.check(currentUser.getGoal() == 2 ? R.id.btn_goal_cut : R.id.btn_goal_bulk);
        }
        toggleGoal.setOnCheckedChangeListener((group, checkedId) -> {
            updateRadioButtonStyle(group, checkedId, R.id.btn_goal_bulk, R.id.btn_goal_cut);
        });

        // 出生日期 → 点击弹出DatePicker
        // 初始化：两个按钮默认同为未选中样式
        updateRadioButtonStyle(toggleGender, toggleGender.getCheckedRadioButtonId(), R.id.btn_gender_male, R.id.btn_gender_female);
        updateRadioButtonStyle(toggleGoal, toggleGoal.getCheckedRadioButtonId(), R.id.btn_goal_bulk, R.id.btn_goal_cut);

        etBirthDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int year = cal.get(java.util.Calendar.YEAR);
            int month = cal.get(java.util.Calendar.MONTH);
            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
            new android.app.DatePickerDialog(requireContext(), (view, y, m, d) -> {
                etBirthDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", y, m + 1, d));
                // 自动计算年龄
                int age = year - y;
                if (m + 1 > month + 1 || (m + 1 == month + 1 && d > day)) age--;
                currentUser.setAge(age > 0 ? age : 0);
            }, year, month, day).show();
        });
        // 如果已有年龄，推算大致出生年份显示
        if (currentUser.getAge() != null) {
            etBirthDate.setText("约" + currentUser.getAge() + "岁");
        }

        // 填充现有数据
        if (currentUser.getDisplayName() != null) etDisplayName.setText(currentUser.getDisplayName());
        if (currentUser.getHeight() != null) etHeight.setText(String.valueOf(currentUser.getHeight()));
        if (currentUser.getWeight() != null) etWeight.setText(String.valueOf(currentUser.getWeight()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("编辑个人资料")
                .setView(dialogView)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String displayName = etDisplayName.getText() != null
                            ? etDisplayName.getText().toString().trim() : "";
                    String heightStr = etHeight.getText() != null
                            ? etHeight.getText().toString().trim() : "";
                    String weightStr = etWeight.getText() != null
                            ? etWeight.getText().toString().trim() : "";
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

                    if (!TextUtils.isEmpty(displayName)) currentUser.setDisplayName(displayName);
                    if (!TextUtils.isEmpty(heightStr)) currentUser.setHeight(Double.parseDouble(heightStr));
                    if (!TextUtils.isEmpty(weightStr)) currentUser.setWeight(Double.parseDouble(weightStr));
                    currentUser.setGender(genderId == R.id.btn_gender_male ? "MALE" : "FEMALE");
                    currentUser.setGoal(goalId == R.id.btn_goal_cut ? 2 : 1);

                    userRepository.updateProfile(currentUser, () -> {
                        if (!TextUtils.isEmpty(password)) {
                            userRepository.changePassword(currentUser.getId(), password, () -> {});
                        }
                        Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        loadUserProfile();
                    });
                })
                .show();
    }

    private void setupRadioGroupToggle(RadioGroup group, int btn1Id, int btn2Id) {
        group.post(() -> updateRadioButtonStyle(group, group.getCheckedRadioButtonId(), btn1Id, btn2Id));
    }

    private void updateRadioButtonStyle(RadioGroup group, int checkedId, int btn1Id, int btn2Id) {
        android.widget.RadioButton btn1 = group.findViewById(btn1Id);
        android.widget.RadioButton btn2 = group.findViewById(btn2Id);
        int selectedColor = getResources().getColor(R.color.token_primary);
        int normalColor = getResources().getColor(R.color.text_primary);
        btn1.setTextColor(checkedId == btn1Id ? selectedColor : normalColor);
        btn2.setTextColor(checkedId == btn2Id ? selectedColor : normalColor);
    }
}
