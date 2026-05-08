package com.example.food.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvUserNumber;
    private TextView tvDisplayName;
    private TextView tvUsername;
    private TextView tvPhone;
    private TextView tvGender;
    private TextView tvAge;
    private TextView tvHeight;
    private TextView tvWeight;
    private TextView tvMacroCarb;
    private TextView tvMacroProtein;
    private TextView tvMacroFat;
    private MaterialCardView cardMacro;
    private PieChart pieChart;
    private View rowProfileInfoHeader;
    private MaterialCardView cardLogout;

    private UserRepository userRepository;
    private UserSessionPreferences sessionPreferences;
    private UserGoalPreferences goalPreferences;

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
        tvMacroCarb = root.findViewById(R.id.tv_macro_carb);
        tvMacroProtein = root.findViewById(R.id.tv_macro_protein);
        tvMacroFat = root.findViewById(R.id.tv_macro_fat);
        cardMacro = root.findViewById(R.id.card_macro);
        pieChart = root.findViewById(R.id.pie_chart_macro);
        rowProfileInfoHeader = root.findViewById(R.id.row_profile_info_header);
        cardLogout = root.findViewById(R.id.card_logout);

        rowProfileInfoHeader.setOnClickListener(v -> openProfileEditor());
        cardLogout.setOnClickListener(v -> logout());

        setupPieChart();
        return root;
    }

    private void openProfileEditor() {
        startActivity(new Intent(requireContext(), ProfileEditActivity.class));
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
        pieChart.setHoleRadius(52f);
        pieChart.setTransparentCircleRadius(57f);
        pieChart.getLegend().setEnabled(false);
    }

    private void loadUserProfile() {
        int userId = sessionPreferences.getUserId();
        if (userId == -1) {
            return;
        }

        userRepository.getUserById(userId, user -> {
            if (user == null) {
                return;
            }
            tvUserNumber.setText(user.getUserNumber() != null ? user.getUserNumber() : "--");
            tvDisplayName.setText(resolveDisplayName(user));
            tvUsername.setText(user.getUsername());
            tvPhone.setText(formatPhone(user.getPhone()));
            tvGender.setText(formatGender(user.getGender()));
            tvAge.setText(user.getAge() != null ? user.getAge() + " 岁" : "未设置");
            tvHeight.setText(user.getHeight() != null
                    ? String.format(Locale.CHINA, "%.1f cm", user.getHeight()) : "未设置");
            tvWeight.setText(user.getWeight() != null
                    ? String.format(Locale.CHINA, "%.1f kg", user.getWeight()) : "未设置");

            updateDailyCalorie(user);
            syncCalorieGoalToHome(user);
        });
    }

    private String resolveDisplayName(User user) {
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName();
        }
        return user.getUsername() != null ? user.getUsername() : "AIFood 用户";
    }

    private void updateDailyCalorie(User user) {
        if (user.getHeight() == null || user.getWeight() == null
                || user.getAge() == null || user.getGender() == null) {
            cardMacro.setVisibility(View.GONE);
            return;
        }

        double dailyCalorie = calculateDailyCalorie(user);

        MacroRatio ratio = buildMacroRatio(user);
        double carbGrams = Math.round((dailyCalorie * ratio.carb) / 4);
        double proteinGrams = Math.round((dailyCalorie * ratio.protein) / 4);
        double fatGrams = Math.round((dailyCalorie * ratio.fat) / 9);

        tvMacroCarb.setText(String.format(Locale.CHINA, "碳水 %.0f g  %.0f%%", carbGrams, ratio.carb * 100));
        tvMacroProtein.setText(String.format(Locale.CHINA, "蛋白质 %.0f g  %.0f%%", proteinGrams, ratio.protein * 100));
        tvMacroFat.setText(String.format(Locale.CHINA, "脂肪 %.0f g  %.0f%%", fatGrams, ratio.fat * 100));
        cardMacro.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) ratio.carb, "碳水"));
        entries.add(new PieEntry((float) ratio.protein, "蛋白质"));
        entries.add(new PieEntry((float) ratio.fat, "脂肪"));

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
                || user.getAge() == null || user.getGender() == null) {
            return;
        }

        double dailyCalorie = calculateDailyCalorie(user);
        MacroRatio ratio = buildMacroRatio(user);

        UserGoal goal = goalPreferences.loadUserGoal();
        goal.setCaloriesGoal(dailyCalorie);
        goal.setCarbohydrateGoal(Math.round((dailyCalorie * ratio.carb) / 4));
        goal.setProteinGoal(Math.round((dailyCalorie * ratio.protein) / 4));
        goal.setFatGoal(Math.round((dailyCalorie * ratio.fat) / 9));
        goalPreferences.saveUserGoal(goal);
    }

    private double calculateDailyCalorie(User user) {
        double bmr;
        if ("MALE".equals(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        return Math.round(bmr * 1.2);
    }

    private MacroRatio buildMacroRatio(User user) {
        int goal = user.getGoal() != null ? user.getGoal() : 1;
        if (goal == 2) {
            return new MacroRatio(0.40, 0.30, 0.30);
        }
        return new MacroRatio(0.45, 0.30, 0.25);
    }

    private String formatGender(String gender) {
        if (gender == null) {
            return "未设置";
        }
        switch (gender) {
            case "MALE":
                return "男";
            case "FEMALE":
                return "女";
            default:
                return "未设置";
        }
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "--";
        }
        String normalized = phone.trim();
        if (normalized.length() < 7) {
            return normalized;
        }
        return normalized.substring(0, 3) + " **** " + normalized.substring(normalized.length() - 4);
    }

    private void logout() {
        sessionPreferences.clearSession();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private static class MacroRatio {
        final double carb;
        final double protein;
        final double fat;

        MacroRatio(double carb, double protein, double fat) {
            this.carb = carb;
            this.protein = protein;
            this.fat = fat;
        }
    }
}
