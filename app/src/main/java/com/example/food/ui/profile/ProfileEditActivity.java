package com.example.food.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food.R;
import com.example.food.data.preferences.UserSessionPreferences;
import com.example.food.data.repository.UserRepository;
import com.example.food.db.entity.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class ProfileEditActivity extends AppCompatActivity {

    private TextInputEditText displayNameInput;
    private TextInputEditText heightInput;
    private TextInputEditText weightInput;
    private TextInputEditText birthDateInput;
    private TextInputEditText passwordInput;
    private TextInputEditText passwordConfirmInput;
    private RadioGroup genderGroup;
    private RadioGroup goalGroup;

    private UserRepository userRepository;
    private User currentUser;
    private int calculatedAge = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        userRepository = new UserRepository(this);
        initViews();
        bindEvents();
        loadCurrentUser();
    }

    private void initViews() {
        displayNameInput = findViewById(R.id.et_display_name);
        heightInput = findViewById(R.id.et_height);
        weightInput = findViewById(R.id.et_weight);
        birthDateInput = findViewById(R.id.et_birth_date);
        passwordInput = findViewById(R.id.et_password);
        passwordConfirmInput = findViewById(R.id.et_password_confirm);
        genderGroup = findViewById(R.id.toggle_gender);
        goalGroup = findViewById(R.id.toggle_goal);
    }

    private void bindEvents() {
        findViewById(R.id.btn_cancel_profile).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
        birthDateInput.setOnClickListener(v -> showBirthDatePicker());

        genderGroup.setOnCheckedChangeListener((group, checkedId) ->
                updateToggleStyle(group, checkedId, R.id.btn_gender_male, R.id.btn_gender_female));
        goalGroup.setOnCheckedChangeListener((group, checkedId) ->
                updateToggleStyle(group, checkedId, R.id.btn_goal_bulk, R.id.btn_goal_cut));
        updateToggleStyle(genderGroup, genderGroup.getCheckedRadioButtonId(), R.id.btn_gender_male, R.id.btn_gender_female);
        updateToggleStyle(goalGroup, goalGroup.getCheckedRadioButtonId(), R.id.btn_goal_bulk, R.id.btn_goal_cut);
    }

    private void loadCurrentUser() {
        int userId = new UserSessionPreferences(this).getUserId();
        if (userId <= 0) {
            finish();
            return;
        }

        userRepository.getUserById(userId, user -> {
            if (user == null) {
                finish();
                return;
            }
            currentUser = user;
            fillForm(user);
        });
    }

    private void fillForm(User user) {
        if (user.getDisplayName() != null) {
            displayNameInput.setText(user.getDisplayName());
        }
        if (user.getHeight() != null) {
            heightInput.setText(String.valueOf(user.getHeight()));
        }
        if (user.getWeight() != null) {
            weightInput.setText(String.valueOf(user.getWeight()));
        }
        if (user.getAge() != null) {
            calculatedAge = user.getAge();
            birthDateInput.setText("约" + user.getAge() + "岁");
        }
        if ("FEMALE".equals(user.getGender())) {
            genderGroup.check(R.id.btn_gender_female);
        } else {
            genderGroup.check(R.id.btn_gender_male);
        }
        if (user.getGoal() != null && user.getGoal() == 2) {
            goalGroup.check(R.id.btn_goal_cut);
        } else {
            goalGroup.check(R.id.btn_goal_bulk);
        }
    }

    private void showBirthDatePicker() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            birthDateInput.setText(String.format(Locale.CHINA, "%d-%02d-%02d", year, month + 1, dayOfMonth));
            int age = currentYear - year;
            if (month > currentMonth || (month == currentMonth && dayOfMonth > currentDay)) {
                age--;
            }
            calculatedAge = Math.max(age, 0);
        }, currentYear, currentMonth, currentDay).show();
    }

    private void saveProfile() {
        if (currentUser == null) {
            return;
        }

        String displayName = textOf(displayNameInput);
        String height = textOf(heightInput);
        String weight = textOf(weightInput);
        String password = textOf(passwordInput);
        String passwordConfirm = textOf(passwordConfirmInput);

        if (!TextUtils.isEmpty(password) || !TextUtils.isEmpty(passwordConfirm)) {
            if (!password.equals(passwordConfirm)) {
                Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!TextUtils.isEmpty(displayName)) {
            currentUser.setDisplayName(displayName);
        }
        if (!TextUtils.isEmpty(height)) {
            currentUser.setHeight(Double.parseDouble(height));
        }
        if (!TextUtils.isEmpty(weight)) {
            currentUser.setWeight(Double.parseDouble(weight));
        }
        if (calculatedAge >= 0) {
            currentUser.setAge(calculatedAge);
        }
        currentUser.setGender(genderGroup.getCheckedRadioButtonId() == R.id.btn_gender_female ? "FEMALE" : "MALE");
        currentUser.setGoal(goalGroup.getCheckedRadioButtonId() == R.id.btn_goal_cut ? 2 : 1);

        userRepository.updateProfile(currentUser, () -> {
            if (!TextUtils.isEmpty(password)) {
                userRepository.changePassword(currentUser.getId(), password, () -> {});
            }
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void updateToggleStyle(RadioGroup group, int checkedId, int firstId, int secondId) {
        RadioButton first = group.findViewById(firstId);
        RadioButton second = group.findViewById(secondId);
        styleToggle(first, checkedId == firstId);
        styleToggle(second, checkedId == secondId);
    }

    private void styleToggle(RadioButton button, boolean selected) {
        button.setTextColor(getColor(selected ? R.color.white : R.color.text_primary));
        button.setBackgroundResource(selected
                ? R.drawable.home_granularity_selected_bg
                : R.drawable.home_granularity_unselected_bg);
    }
}
