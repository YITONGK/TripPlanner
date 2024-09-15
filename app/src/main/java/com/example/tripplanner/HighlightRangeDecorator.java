package com.example.tripplanner;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.HashSet;
import java.util.List;

public class HighlightRangeDecorator implements DayViewDecorator {
    private HashSet<CalendarDay> dateRange = new HashSet<>();
    private Context context;

    public HighlightRangeDecorator(Context context) {
        this.context = context;
    }

    public void setDateRange(List<CalendarDay> dates) {
        dateRange.clear();
        dateRange.addAll(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dateRange.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Apply the gray background to the selected dates
        view.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.selected_date_bg));
    }
}
