package com.example.food.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food.R;
import com.example.food.data.preferences.OperationLogManager;
import com.example.food.data.repository.FoodRepository;
import com.example.food.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 回收管理页面
 * 用户操作 + 食物操作分类展示，折叠展开，每项支持撤销/恢复
 */
public class AdminSettingsFragment extends Fragment {

    private View layoutUserSection, layoutFoodSection;
    private TextView tvSectionUser, tvSectionFood, tvEmptyUser, tvEmptyFood;
    private RecyclerView rvUserLogs, rvFoodLogs;
    private boolean userExpanded = true, foodExpanded = true;

    private UserRepository userRepository;
    private FoodRepository foodRepository;
    private OperationLogManager logManager;
    private LogAdapter userLogAdapter, foodLogAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_settings, container, false);

        userRepository = new UserRepository(requireContext());
        foodRepository = new FoodRepository(requireContext());
        logManager = new OperationLogManager(requireContext());

        layoutUserSection = root.findViewById(R.id.layout_user_section);
        layoutFoodSection = root.findViewById(R.id.layout_food_section);
        tvSectionUser = root.findViewById(R.id.tv_section_user);
        tvSectionFood = root.findViewById(R.id.tv_section_food);
        rvUserLogs = root.findViewById(R.id.rv_user_logs);
        rvFoodLogs = root.findViewById(R.id.rv_food_logs);
        tvEmptyUser = root.findViewById(R.id.tv_empty_user);
        tvEmptyFood = root.findViewById(R.id.tv_empty_food);

        rvUserLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoodLogs.setLayoutManager(new LinearLayoutManager(getContext()));

        // 折叠展开
        tvSectionUser.setOnClickListener(v -> {
            userExpanded = !userExpanded;
            layoutUserSection.setVisibility(userExpanded ? View.VISIBLE : View.GONE);
        });
        tvSectionFood.setOnClickListener(v -> {
            foodExpanded = !foodExpanded;
            layoutFoodSection.setVisibility(foodExpanded ? View.VISIBLE : View.GONE);
        });

        loadLogs();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogs();
    }

    private void loadLogs() {
        List<OperationLogManager.LogEntry> allLogs = logManager.getLogs();
        List<OperationLogManager.LogEntry> userLogs = new ArrayList<>();
        List<OperationLogManager.LogEntry> foodLogs = new ArrayList<>();

        for (OperationLogManager.LogEntry entry : allLogs) {
            if ("user_add".equals(entry.action) || "user_delete".equals(entry.action)) {
                userLogs.add(entry);
            } else if ("food_delete".equals(entry.action) || "food_restore".equals(entry.action)) {
                foodLogs.add(entry);
            }
        }

        userLogAdapter = new LogAdapter(userLogs, true);
        rvUserLogs.setAdapter(userLogAdapter);
        tvEmptyUser.setVisibility(userLogs.isEmpty() ? View.VISIBLE : View.GONE);

        foodLogAdapter = new LogAdapter(foodLogs, false);
        rvFoodLogs.setAdapter(foodLogAdapter);
        tvEmptyFood.setVisibility(foodLogs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ===== 撤销/恢复操作 =====

    private void undoAction(OperationLogManager.LogEntry entry) {
        try {
            org.json.JSONObject data = new org.json.JSONObject(entry.undoJson);
            switch (entry.action) {
                case "user_add":
                    int uid = data.getInt("userId");
                    userRepository.softDeleteUser(uid, () ->
                            Toast.makeText(getContext(), "已撤销：软删除用户", Toast.LENGTH_SHORT).show());
                    break;
                case "user_delete":
                    int uid2 = data.getInt("userId");
                    userRepository.restoreUser(uid2, () ->
                            Toast.makeText(getContext(), "已撤销：恢复用户", Toast.LENGTH_SHORT).show());
                    break;
                case "food_delete":
                    int fid = data.getInt("foodId");
                    foodRepository.restoreFood(fid, () ->
                            Toast.makeText(getContext(), "已撤销：恢复食物", Toast.LENGTH_SHORT).show());
                    break;
                case "food_restore":
                    int fid2 = data.getInt("foodId");
                    foodRepository.softDelete(fid2, () ->
                            Toast.makeText(getContext(), "已撤销：删除食物", Toast.LENGTH_SHORT).show());
                    break;
            }
            loadLogs();
        } catch (Exception e) {
            Toast.makeText(getContext(), "操作失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoAction(OperationLogManager.LogEntry entry) {
        try {
            org.json.JSONObject data = new org.json.JSONObject(entry.undoJson);
            switch (entry.action) {
                case "user_add":
                    int uid3 = data.getInt("userId");
                    userRepository.restoreUser(uid3, () ->
                            Toast.makeText(getContext(), "已恢复：恢复用户", Toast.LENGTH_SHORT).show());
                    break;
                case "user_delete":
                    int uid4 = data.getInt("userId");
                    userRepository.softDeleteUser(uid4, () ->
                            Toast.makeText(getContext(), "已恢复：软删除用户", Toast.LENGTH_SHORT).show());
                    break;
                case "food_delete":
                    int fid = data.getInt("foodId");
                    foodRepository.softDelete(fid, () ->
                            Toast.makeText(getContext(), "已恢复：重新删除食物", Toast.LENGTH_SHORT).show());
                    break;
                case "food_restore":
                    int fid2 = data.getInt("foodId");
                    foodRepository.restoreFood(fid2, () ->
                            Toast.makeText(getContext(), "已恢复：恢复食物", Toast.LENGTH_SHORT).show());
                    break;
            }
            loadLogs();
        } catch (Exception e) {
            Toast.makeText(getContext(), "操作失败", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== 日志适配器 =====

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
        private final List<OperationLogManager.LogEntry> logs;
        private final boolean isUserLog;

        LogAdapter(List<OperationLogManager.LogEntry> logs, boolean isUserLog) {
            this.logs = logs;
            this.isUserLog = isUserLog;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_log, parent, false);
            return new LogViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            OperationLogManager.LogEntry entry = logs.get(position);
            holder.tvDesc.setText(entry.description);
            holder.tvTime.setText(logManager.formatTime(entry.timestamp));

            // 所有操作都支持撤销/恢复
            holder.btnUndo.setVisibility(View.VISIBLE);
            holder.btnRedo.setVisibility(View.VISIBLE);

            holder.btnUndo.setOnClickListener(v -> undoAction(entry));
            holder.btnRedo.setOnClickListener(v -> redoAction(entry));
        }

        @Override
        public int getItemCount() { return logs.size(); }

        class LogViewHolder extends RecyclerView.ViewHolder {
            TextView tvDesc, tvTime, btnUndo, btnRedo;
            LogViewHolder(View v) {
                super(v);
                tvDesc = v.findViewById(R.id.tv_log_desc);
                tvTime = v.findViewById(R.id.tv_log_time);
                btnUndo = v.findViewById(R.id.btn_undo);
                btnRedo = v.findViewById(R.id.btn_redo);
            }
        }
    }
}
