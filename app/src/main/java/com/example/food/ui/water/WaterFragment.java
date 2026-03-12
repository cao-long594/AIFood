package com.example.food.ui.water;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

import com.example.food.R;
import com.example.food.db.AppDatabase;
import com.example.food.db.dao.WaterRecordDao;
import com.example.food.db.entity.WaterRecord;
import com.example.food.utils.Constants;
import com.example.food.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 饮水记录Fragment
 * 显示饮水记录和管理功能
 */
public class WaterFragment extends Fragment {

    private WaterCircleProgressView waterProgressView;//圆形进度
    private RecyclerView waterRecyclerView;
    private EditText customAmountEditText;
    private EditText goalInputEditText;
    private Button addCustomButton;
    private Button btn150ml, btn300ml, btn500ml, btn1000ml;
    private Date selectedDate;
    private WaterAdapter waterAdapter;
    private AppDatabase database;
    private WaterRecordDao waterRecordDao;
    private double waterGoal;
    
    // 日历相关成员变量
    private TextView tvMonthYear;
    private Button btnPrevWeek, btnNextWeek;
    private GridView gvCalendar;
    private CalendarAdapter calendarAdapter;
    private Calendar currentCalendar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_water, container, false);
        initDatabase(); // 先初始化数据库
        initViews(root); // 然后再初始化视图
        loadWaterGoal();
        selectedDate = new Date();
        currentCalendar = Calendar.getInstance();
        initCalendar();
        loadWaterRecords();
        return root;
    }

    private void initViews(View root) {
        // 初始化主要视图
        waterRecyclerView = root.findViewById(R.id.rv_water);
        customAmountEditText = root.findViewById(R.id.et_custom_amount);
        goalInputEditText = root.findViewById(R.id.et_goal_input);
        addCustomButton = root.findViewById(R.id.btn_confirm);
        waterProgressView=root.findViewById(R.id.waterProgressView);
        // 初始化快速选择按钮
        btn150ml = root.findViewById(R.id.btn_150ml);
        btn300ml = root.findViewById(R.id.btn_300ml);
        btn500ml = root.findViewById(R.id.btn_500ml);
        btn1000ml = root.findViewById(R.id.btn_1000ml);
        
        // 初始化日历相关视图
        tvMonthYear = root.findViewById(R.id.tv_current_date);
        btnPrevWeek = root.findViewById(R.id.btn_prev_week);
        btnNextWeek = root.findViewById(R.id.btn_next_week);
        gvCalendar = root.findViewById(R.id.gv_calendar);

        // 设置RecyclerView
        waterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        waterAdapter = new WaterAdapter(getContext());
        // 确保waterRecordDao不为null后再设置
        if (waterRecordDao != null) {
            waterAdapter.setWaterRecordDao(waterRecordDao);
        }
        // 设置删除监听器，当记录被删除时重新加载数据更新进度
        waterAdapter.setOnItemDeleteListener(() -> {
            loadWaterRecords(); // 重新加载数据以更新进度
        });
        waterRecyclerView.setAdapter(waterAdapter);

        // 设置目标输入监听，当焦点失去时自动保存
        goalInputEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveWaterGoalFromInput();
            }
        });
        
        // 添加回车确认功能
        goalInputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // 失去焦点，触发保存逻辑
                goalInputEditText.clearFocus();
                // 隐藏软键盘
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                        getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(goalInputEditText.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        // 设置快速选择按钮点击事件
        btn150ml.setOnClickListener(v -> addWaterRecord(150));
        btn300ml.setOnClickListener(v -> addWaterRecord(300));
        btn500ml.setOnClickListener(v -> addWaterRecord(500));
        btn1000ml.setOnClickListener(v -> addWaterRecord(1000));

        // 设置自定义添加按钮点击事件
        addCustomButton.setOnClickListener(v -> {
            try {
                String amountText = customAmountEditText.getText().toString().trim();
                if (!amountText.isEmpty()) {
                    double amount = Double.parseDouble(amountText);
                    if (amount > 0) {
                        addWaterRecord(amount);
                        customAmountEditText.setText(""); // 清空输入框
                    } else {
                        Toast.makeText(getContext(), "请输入有效的饮水量", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "请输入饮水量", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(getContext());
        waterRecordDao = database.waterRecordDao();
    }

    private void loadWaterGoal() {
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.PREFS_NAME, 0);
        waterGoal = preferences.getFloat(Constants.PREF_WATER_GOAL, (float) Constants.DEFAULT_WATER_GOAL);
        // 如果GoalInputEditText已初始化，则设置其值
        if (goalInputEditText != null) {
            goalInputEditText.setText(String.format("%.0f", waterGoal));
        }
    }

    private void saveWaterGoal(double goal) {
        this.waterGoal = goal;
        SharedPreferences preferences = getContext().getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.PREF_WATER_GOAL, (float) goal);
        editor.apply();
    }

    /**
     * 从输入框中保存饮水目标
     */
    private void saveWaterGoalFromInput() {
        try {
            String goalText = goalInputEditText.getText().toString().trim();
            if (!goalText.isEmpty()) {
                double newGoal = Double.parseDouble(goalText);
                if (newGoal > 0) {
                    saveWaterGoal(newGoal);
                    loadWaterRecords(); // 重新加载数据以更新进度
                } else {
                    // 如果输入无效，恢复原值
                    goalInputEditText.setText(String.format("%.0f", waterGoal));
                    Toast.makeText(getContext(), "请输入有效的目标值", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException e) {
            // 如果输入无效，恢复原值
            goalInputEditText.setText(String.format("%.0f", waterGoal));
            Toast.makeText(getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadWaterRecords() {
        new Thread(() -> {
            // 确保使用当前日期进行查询，除非用户明确选择了其他日期
            Date queryDate = selectedDate != null ? selectedDate : new Date();
            Date startDate = DateUtils.getDateStart(queryDate);
            Date endDate = DateUtils.getDateEnd(queryDate);

            // 查询记录和总量
            List<WaterRecord> records = waterRecordDao.getRecordsByDate(startDate, endDate);
            double totalAmount = waterRecordDao.getTotalAmountByDate(startDate, endDate);
            
            // 在UI线程更新
            getActivity().runOnUiThread(() -> {
                // 确保adapter不为空
                if (waterAdapter != null) {
                    // 创建新的列表副本，避免引用问题
                    List<WaterRecord> recordsCopy = new ArrayList<>(records);
                    waterAdapter.setData(recordsCopy);
                }
                
                // 更新目标输入框的值
                if (goalInputEditText != null) {
                    goalInputEditText.setText(String.format("%.0f", waterGoal));
                }
                
                // 更新圆形进度视图
                if (waterProgressView != null) {
                    waterProgressView.setMaxProgress((float) waterGoal); // 设置目标值
                    waterProgressView.setCurrentProgress((float) totalAmount); // 设置当前值
                }
            });
        }).start();
    }



    /**
     * 添加饮水记录
     * @param amount 饮水量(ml)
     */
    private void addWaterRecord(double amount) {
        new Thread(() -> {
            // 确保使用新的Date实例，包含精确到毫秒的时间戳
            Date currentTime = new Date();
            WaterRecord record = new WaterRecord(amount, currentTime);
            // 插入记录并获取插入的ID
            long insertedId = waterRecordDao.insert(record);
            getActivity().runOnUiThread(() -> {
                // 立即重新加载所有记录，确保数据一致性
                loadWaterRecords();
                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 初始化日历功能
     */
    private void initCalendar() {
        // 初始化当前日历
        currentCalendar = Calendar.getInstance();
        if (selectedDate == null) {
            selectedDate = new Date();
        }
        currentCalendar.setTime(selectedDate);
        
        // 创建并设置日历适配器
        calendarAdapter = new CalendarAdapter(getContext(), selectedDate);
        // 设置日期选择监听器
        calendarAdapter.setOnDateSelectedListener(date -> {
            selectedDate = date;
            // 更新日历选中状态
            calendarAdapter.setSelectedDate(date);
            // 加载选中日期的饮水记录
            loadWaterRecords();
        });
        gvCalendar.setAdapter(calendarAdapter);
        
        // 更新月份年份显示
        updateMonthYearDisplay();
        
        // 设置周切换按钮监听
        btnPrevWeek.setOnClickListener(v -> {
            // 获取上一周的日期
            selectedDate = DateUtils.getPreviousWeek(selectedDate);
            updateMonthYearDisplay();
            // 使用新的updateWeek方法更新适配器
            calendarAdapter.updateWeek(selectedDate);
            loadWaterRecords();
        });
        
        btnNextWeek.setOnClickListener(v -> {
            // 获取下一周的日期
            selectedDate = DateUtils.getNextWeek(selectedDate);
            updateMonthYearDisplay();
            // 使用新的updateWeek方法更新适配器
            calendarAdapter.updateWeek(selectedDate);
            loadWaterRecords();
        });
    }
    
    /**
     * 更新月份年份显示
     */
    private void updateMonthYearDisplay() {
        if (tvMonthYear != null) {
            // 使用selectedDate确保显示正确的日期
            Date displayDate = selectedDate != null ? selectedDate : new Date();
            int year = DateUtils.getYear(displayDate);
            int month = DateUtils.getMonth(displayDate);
            String monthYear = DateUtils.formatYearMonth(year, month);
            tvMonthYear.setText(monthYear);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadWaterGoal();
        loadWaterRecords();
        // 刷新日历状态
        if (calendarAdapter != null) {
            calendarAdapter.notifyDataSetChanged();
        }
    }
}