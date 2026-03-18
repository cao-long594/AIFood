package com.example.food.ui.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.food.utils.DateUtils;

import java.util.Date;

public class SelectedDateViewModel extends ViewModel {

    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>(DateUtils.getTodayStart());

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date date) {
        Date normalizedDate = DateUtils.getDateStart(date == null ? new Date() : date);
        Date currentDate = selectedDate.getValue();
        if (DateUtils.isSameDay(currentDate, normalizedDate)) {
            return;
        }
        selectedDate.setValue(normalizedDate);
    }
}

