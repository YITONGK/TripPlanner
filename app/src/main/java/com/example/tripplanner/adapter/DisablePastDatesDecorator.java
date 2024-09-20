package com.example.tripplanner.adapter;


import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Calendar;

public class DisablePastDatesDecorator implements DayViewDecorator {
    private Calendar today;

    public DisablePastDatesDecorator() {
        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.getDate().before(today.getTime());
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setDaysDisabled(true);
    }
}
