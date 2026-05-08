package com.example.food.ui.water;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.core.concurrent.AppExecutors;
import com.example.food.data.preferences.UserGoalPreferences;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.model.UserGoal;
import com.example.food.utils.DateUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterFragment extends Fragment {

    private TextView tvWaterAmount;
    private TextView tvWaterGoal;
    private TextView tvWaterProgress;
    private TextView tvWaterStatus;
    private TextView tvWaterDate;
    private TextView tvWaterHint;
    private TextView tvWaterCups;
    private TextView tvWaterAverage;
    private TextView tvWaterFrequency;
    private TextView tvWaterRate;
    private TextView tvEmptyRecords;
    private ProgressBar waterProgressBar;
    private WaterRecordAdapter recordAdapter;

    private int userId;
    private double dailyGoal = 2000;
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
        tvWaterProgress = root.findViewById(R.id.tv_water_progress);
        tvWaterStatus = root.findViewById(R.id.tv_water_status);
        tvWaterDate = root.findViewById(R.id.tv_water_date);
        tvWaterHint = root.findViewById(R.id.tv_water_hint);
        tvWaterCups = root.findViewById(R.id.tv_water_cups);
        tvWaterAverage = root.findViewById(R.id.tv_water_average);
        tvWaterFrequency = root.findViewById(R.id.tv_water_frequency);
        tvWaterRate = root.findViewById(R.id.tv_water_rate);
        tvEmptyRecords = root.findViewById(R.id.tv_empty_records);
        waterProgressBar = root.findViewById(R.id.progress_water);

        RecyclerView recordsView = root.findViewById(R.id.rv_water_records);
        recordAdapter = new WaterRecordAdapter(record -> deleteRecord(record.id));
        recordsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recordsView.setAdapter(recordAdapter);

        setupButton(root, R.id.btn_water_100, 100);
        setupButton(root, R.id.btn_water_200, 200);
        setupButton(root, R.id.btn_water_300, 300);
        setupButton(root, R.id.btn_water_500, 500);
        root.findViewById(R.id.btn_custom_water).setOnClickListener(v -> showCustomDialog());
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
        if (userId <= 0) {
            return;
        }
        dailyGoal = goalPrefs.loadWaterGoal();
        String databasePath = requireContext().getDatabasePath("food_app_database").getPath();

        AppExecutors.runOnIo(() -> {
            long start = DateUtils.getDateStart(new Date()).getTime();
            long end = DateUtils.getDateEnd(new Date()).getTime();
            List<WaterRecord> records = new ArrayList<>();
            double total = 0;

            SQLiteDatabase db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            Cursor c = db.rawQuery(
                    "SELECT id, amount, date FROM water_records WHERE userId=? AND date>=? AND date<? ORDER BY date DESC",
                    new String[]{String.valueOf(userId), String.valueOf(start), String.valueOf(end)});
            try {
                while (c.moveToNext()) {
                    WaterRecord record = new WaterRecord(c.getLong(0), c.getDouble(1), c.getLong(2));
                    records.add(record);
                    total += record.amount;
                }
            } finally {
                c.close();
                db.close();
            }

            double finalTotal = total;
            AppExecutors.runOnMain(() -> renderWaterState(finalTotal, records));
        });
    }

    private void renderWaterState(double total, List<WaterRecord> records) {
        tvWaterAmount.setText(String.format(Locale.CHINA, "%.0f", total));
        tvWaterGoal.setText(String.format(Locale.CHINA, "/ %,.0f ml", dailyGoal));
        tvWaterDate.setText(new SimpleDateFormat("yyyy年M月d日  E", Locale.CHINA).format(new Date()));

        int progress = dailyGoal > 0 ? Math.min(100, (int) Math.round((total / dailyGoal) * 100)) : 0;
        waterProgressBar.setProgress(progress);
        tvWaterProgress.setText(String.format(Locale.CHINA, "%d%%", progress));
        tvWaterStatus.setText(progress >= 100 ? "今日目标已完成" : "今日饮水");
        double remaining = Math.max(0, dailyGoal - total);
        tvWaterHint.setText(progress >= 100
                ? "已达成目标，继续保持身体水分平衡"
                : String.format(Locale.CHINA, "继续保持，距离目标还差 %.0f ml", remaining));

        int count = records.size();
        double average = count > 0 ? Math.round(total / count) : 0;
        tvWaterCups.setText(String.format(Locale.CHINA, "%d 杯", count));
        tvWaterAverage.setText(String.format(Locale.CHINA, "%.0f ml", average));
        tvWaterFrequency.setText(formatFrequency(records));
        tvWaterRate.setText(String.format(Locale.CHINA, "%d%%", progress));

        recordAdapter.submit(records);
        tvEmptyRecords.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String formatFrequency(List<WaterRecord> records) {
        if (records.size() < 2) {
            return "--";
        }
        long newest = records.get(0).date;
        long oldest = records.get(records.size() - 1).date;
        long intervalMillis = Math.abs(newest - oldest) / (records.size() - 1);
        long hours = Math.max(1, Math.round(intervalMillis / 3600000.0));
        return String.format(Locale.CHINA, "%dh/次", hours);
    }

    private void addWater(double amount) {
        if (userId <= 0) {
            return;
        }
        String databasePath = requireContext().getDatabasePath("food_app_database").getPath();
        long now = System.currentTimeMillis();

        AppExecutors.runOnIo(() -> {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            try {
                db.execSQL("INSERT INTO water_records (userId, amount, date, created_at) VALUES (?,?,?,?)",
                        new Object[]{userId, amount, now, now});
            } finally {
                db.close();
            }
            AppExecutors.runOnMain(() -> {
                Toast.makeText(getContext(), "+" + (int) amount + "ml", Toast.LENGTH_SHORT).show();
                loadTodayWater();
            });
        });
    }

    private void deleteRecord(long recordId) {
        String databasePath = requireContext().getDatabasePath("food_app_database").getPath();
        AppExecutors.runOnIo(() -> {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            try {
                db.execSQL("DELETE FROM water_records WHERE id=? AND userId=?",
                        new Object[]{recordId, userId});
            } finally {
                db.close();
            }
            AppExecutors.runOnMain(() -> {
                Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                loadTodayWater();
            });
        });
    }

    private void showCustomDialog() {
        TextInputLayout inputLayout = buildNumberInput("输入饮水量（ml）");
        TextInputEditText input = (TextInputEditText) inputLayout.getEditText();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("自定义饮水")
                .setView(inputLayout)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (d, w) -> {
                    String value = input != null && input.getText() != null
                            ? input.getText().toString().trim() : "";
                    if (!value.isEmpty()) {
                        addWater(Double.parseDouble(value));
                    }
                })
                .show();
    }

    private void showAdjustDialog() {
        if (userId <= 0) {
            return;
        }

        TextInputLayout amountLayout = buildNumberInput("今日总饮水量（ml）");
        TextInputEditText amountInput = (TextInputEditText) amountLayout.getEditText();
        if (amountInput != null) {
            amountInput.setText(tvWaterAmount.getText());
        }

        TextInputLayout goalLayout = buildNumberInput("每日目标（ml）");
        TextInputEditText goalInput = (TextInputEditText) goalLayout.getEditText();
        if (goalInput != null) {
            goalInput.setText(String.format(Locale.CHINA, "%.0f", dailyGoal));
        }

        ViewGroup form = new android.widget.LinearLayout(requireContext());
        form.setPadding(32, 12, 32, 0);
        ((android.widget.LinearLayout) form).setOrientation(android.widget.LinearLayout.VERTICAL);
        form.addView(amountLayout);
        form.addView(goalLayout);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("调整饮水数据")
                .setMessage("保存后会用一条汇总记录替换今天的明细。")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String amountValue = amountInput != null && amountInput.getText() != null
                            ? amountInput.getText().toString().trim() : "";
                    String goalValue = goalInput != null && goalInput.getText() != null
                            ? goalInput.getText().toString().trim() : "";
                    if (amountValue.isEmpty() || goalValue.isEmpty()) {
                        return;
                    }
                    replaceTodayTotal(Double.parseDouble(amountValue), Double.parseDouble(goalValue));
                })
                .show();
    }

    private TextInputLayout buildNumberInput(String hint) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setPadding(0, 0, 0, 14);

        TextInputEditText input = new TextInputEditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setSingleLine(true);
        layout.addView(input);
        return layout;
    }

    private void replaceTodayTotal(double amount, double goal) {
        String databasePath = requireContext().getDatabasePath("food_app_database").getPath();
        long start = DateUtils.getDateStart(new Date()).getTime();
        long end = DateUtils.getDateEnd(new Date()).getTime();
        long now = System.currentTimeMillis();

        UserGoal userGoal = goalPrefs.loadUserGoal();
        userGoal.setWaterGoal(goal);
        goalPrefs.saveUserGoal(userGoal);

        AppExecutors.runOnIo(() -> {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            try {
                db.execSQL("DELETE FROM water_records WHERE userId=? AND date>=? AND date<?",
                        new Object[]{userId, start, end});
                if (amount > 0) {
                    db.execSQL("INSERT INTO water_records (userId, amount, date, created_at) VALUES (?,?,?,?)",
                            new Object[]{userId, amount, now, now});
                }
            } finally {
                db.close();
            }
            AppExecutors.runOnMain(() -> {
                Toast.makeText(getContext(), "已更新", Toast.LENGTH_SHORT).show();
                loadTodayWater();
            });
        });
    }

    private static class WaterRecord {
        final long id;
        final double amount;
        final long date;

        WaterRecord(long id, double amount, long date) {
            this.id = id;
            this.amount = amount;
            this.date = date;
        }
    }

    private static class WaterRecordAdapter extends RecyclerView.Adapter<WaterRecordAdapter.RecordHolder> {
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        private final List<WaterRecord> records = new ArrayList<>();
        private final OnRecordDeleteListener deleteListener;

        WaterRecordAdapter(OnRecordDeleteListener deleteListener) {
            this.deleteListener = deleteListener;
        }

        void submit(List<WaterRecord> newRecords) {
            records.clear();
            records.addAll(newRecords);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_water_record, parent, false);
            return new RecordHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordHolder holder, int position) {
            WaterRecord record = records.get(position);
            holder.amountText.setText(String.format(Locale.CHINA, "%.0f ml", record.amount));
            holder.timeText.setText(timeFormat.format(new Date(record.date)));
            holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(record));
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        static class RecordHolder extends RecyclerView.ViewHolder {
            final TextView amountText;
            final TextView timeText;
            final View deleteButton;

            RecordHolder(@NonNull View itemView) {
                super(itemView);
                amountText = itemView.findViewById(R.id.tv_record_amount);
                timeText = itemView.findViewById(R.id.tv_record_time);
                deleteButton = itemView.findViewById(R.id.btn_delete_record);
            }
        }
    }

    private interface OnRecordDeleteListener {
        void onDelete(WaterRecord record);
    }
}
