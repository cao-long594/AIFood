package com.example.food.data.repository;

import com.example.food.ui.history.HistoryViewModel;

import java.util.Date;

public class HistoryRepository {

    private final HistoryViewModel historyViewModel = new HistoryViewModel();

    public Date resolveAnchorDate(long anchorDateMillis) {
        return historyViewModel.resolveAnchorDate(anchorDateMillis);
    }

    public HistoryViewModel.HistoryViewType resolveViewType(String value) {
        return historyViewModel.resolveViewType(value);
    }

    public String buildSelectionSummary(Date anchorDate, HistoryViewModel.HistoryViewType viewType) {
        return historyViewModel.buildSelectionSummary(anchorDate, viewType);
    }

    public HistoryViewModel.MonthSectionsResult buildMonthSections(Date selectedDate,
                                                                   Date today,
                                                                   HistoryViewModel.HistoryViewType viewType,
                                                                   int monthsBefore,
                                                                   int monthsAfter) {
        return historyViewModel.buildMonthSections(selectedDate, today, viewType, monthsBefore, monthsAfter);
    }

    public HistoryViewModel.YearSectionsResult buildYearSections(Date selectedDate,
                                                                 Date today,
                                                                 int yearsBefore,
                                                                 int yearsAfter) {
        return historyViewModel.buildYearSections(selectedDate, today, yearsBefore, yearsAfter);
    }
}
