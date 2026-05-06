package com.example.food.ui.water;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.food.R;
import com.example.food.core.concurrent.AppExecutors;
import com.example.food.data.preferences.UserGoalPreferences;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.Locale;

/**
 * 饮水记录页面
 */
public class WaterFragment extends Fragment {

    private TextView tvWaterAmount, tvWaterGoal;
    private WaterCircleView waterCircle;

    private int userId;
    private UserGoalPreferences goalPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_water, container, false);

        userId = new UserSessionPreferences(requireContext()).getUserId();
        goalPrefs = new UserGoalPreferences(requireContext(), userId);

        tvWaterAmount = root.findViewById(R.id.tv_water_amount);
        tvWaterGoal = root.findViewById(R.id.tv_water_goal);
        waterCircle = root.findViewById(R.id.water_circle);

        setupButton(root, R.id.btn_water_100, 100);
        setupButton(root, R.id.btn_water_200, 200);
        setupButton(root, R.id.btn_water_300, 300);
        setupButton(root, R.id.btn_water_500, 500);
        root.findViewById(R.id.btn_custom_water).setOnClickListener(v -> showCustomDialog());

        // 点击环形区域弹出调整窗格
        root.findViewById(R.id.water_circle_container).setOnClickListener(v -> showAdjustDialog());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayWater();
    }

    private void setupButton(View root, int id, double amount) {
        root.findViewById(id).setOnClickListener(v -> addWater(amount));
    }

    private void loadTodayWater() {
        if (userId <= 0) return;
        dailyGoal = goalPrefs.loadWaterGoal();
        tvWaterGoal.setText(String.format(Locale.CHINA, "%.0fml", dailyGoal));

        AppExecutors.runOnIo(() -> {
            long start = DateUtils.getDateStart(new Date()).getTime();
            long end = DateUtils.getDateEnd(new Date()).getTime();
            double total = 0;
            SQLiteDatabase db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    requireContext().getDatabasePath("food_app_database").getPath(), null, 0);
            Cursor c = db.rawQuery("SELECT COALESCE(SUM(amount),0) FROM water_records WHERE userId=? AND date>=? AND date<?",
                    new String[]{String.valueOf(userId), String.valueOf(start), String.valueOf(end)});
            if (c.moveToFirst()) total = c.getDouble(0);
            c.close();
            final double ft = total;
            AppExecutors.runOnMain(() -> {
                tvWaterAmount.setText(String.format(Locale.CHINA, "%.0f", ft));
                float pct = dailyGoal > 0 ? (float) (ft / dailyGoal) : 0f;
                waterCircle.setProgress(Math.min(pct, 1f));
            });
        });
    }

    private double dailyGoal = 2000;

    private void addWater(double amount) {
        if (userId <= 0) return;
        long now = System.currentTimeMillis();
        AppExecutors.runOnIo(() -> {
            SQLiteDatabase db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    requireContext().getDatabasePath("food_app_database").getPath(), null, 0);
            db.execSQL("INSERT INTO water_records (userId, amount, date, created_at) VALUES (?,?,?,?)",
                    new Object[]{userId, amount, now, now});
            AppExecutors.runOnMain(() -> {
                Toast.makeText(getContext(), "+" + (int) amount + "ml", Toast.LENGTH_SHORT).show();
                loadTodayWater();
            });
        });
    }

    private void showAdjustDialog() {
        if (userId <= 0) return;
        double currentGoal = goalPrefs.loadWaterGoal();
        // 获取当前已饮水量
        SQLiteDatabase dbr = android.database.sqlite.SQLiteDatabase.openDatabase(
                requireContext().getDatabasePath("food_app_database").getPath(), null, 0);
        long start = DateUtils.getDateStart(new Date()).getTime();
        long end = DateUtils.getDateEnd(new Date()).getTime();
        Cursor c = dbr.rawQuery("SELECT COALESCE(SUM(amount),0) FROM water_records WHERE userId=? AND date>=? AND date<?",
                new String[]{String.valueOf(userId), String.valueOf(start), String.valueOf(end)});
        double currentAmount = 0;
        if (c.moveToFirst()) currentAmount = c.getDouble(0);
        c.close();

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        TextInputLayout tilAmount = new TextInputLayout(requireContext());
        tilAmount.setHint("当前饮水量（ml）");
        tilAmount.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        EditText etAmount = new EditText(requireContext());
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setText(String.format(Locale.CHINA, "%.0f", currentAmount));
        tilAmount.addView(etAmount);
        layout.addView(tilAmount);

        TextInputLayout tilGoal = new TextInputLayout(requireContext());
        tilGoal.setHint("每日目标（ml）");
        tilGoal.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilGoal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        EditText etGoal = new EditText(requireContext());
        etGoal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etGoal.setText(String.format(Locale.CHINA, "%.0f", currentGoal));
        tilGoal.addView(etGoal);
        layout.addView(tilGoal);

        new AlertDialog.Builder(requireContext())
                .setTitle("调整饮水数据")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                    String goalStr = etGoal.getText() != null ? etGoal.getText().toString().trim() : "";
                    if (amountStr.isEmpty() || goalStr.isEmpty()) return;

                    double newAmount = Double.parseDouble(amountStr);
                    double newGoal = Double.parseDouble(goalStr);

                    // 保存目标
                    com.example.food.model.UserGoal ug = goalPrefs.loadUserGoal();
                    ug.setWaterGoal(newGoal);
                    goalPrefs.saveUserGoal(ug);

                    // 更新今日饮水记录：删除今日所有记录，插入一条汇总
                    AppExecutors.runOnIo(() -> {
                        SQLiteDatabase dbw = android.database.sqlite.SQLiteDatabase.openDatabase(
                                requireContext().getDatabasePath("food_app_database").getPath(), null, 0);
                        dbw.execSQL("DELETE FROM water_records WHERE userId=? AND date>=? AND date<?",
                                new Object[]{userId, start, end});
                        if (newAmount > 0) {
                            dbw.execSQL("INSERT INTO water_records (userId, amount, date, created_at) VALUES (?,?,?,?)",
                                    new Object[]{userId, newAmount, System.currentTimeMillis(), System.currentTimeMillis()});
                        }
                        AppExecutors.runOnMain(() -> {
                            Toast.makeText(getContext(), "已更新", Toast.LENGTH_SHORT).show();
                            loadTodayWater();
                        });
                    });
                })
                .show();
    }

    private void showCustomDialog() {
        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        inputLayout.setHint("输入饮水量（ml）");
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputLayout.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("自定义饮水")
                .setView(inputLayout)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (d, w) -> {
                    String val = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!val.isEmpty()) {
                        addWater(Double.parseDouble(val));
                    }
                })
                .show();
    }
}
