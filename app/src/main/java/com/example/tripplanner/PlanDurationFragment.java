package com.example.tripplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.List;

public class PlanDurationFragment extends Fragment {

    static final int DAYS = R.layout.plan_duration_fragment_days;
    static final int CALENDAR = R.layout.plan_duration_fragment_calendar;
    static final String LAYOUT_TYPE = "type";

    private int layout;
    private MaterialCalendarView materialCalendarView;
    private HighlightRangeDecorator rangeDecorator;

    private CalendarDay startDate = null;
    private CalendarDay endDate = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = getArguments() != null ? getArguments().getInt(LAYOUT_TYPE, DAYS) : DAYS;
        View view = inflater.inflate(layout, container, false);

        if (layout == DAYS) {
            NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
            setupNumberPicker(numberPicker);
        } else if (layout == CALENDAR) {
            materialCalendarView = view.findViewById(R.id.materialCalendarView);
            setupCalendarView();
        }
        return view;
    }

    private void setupCalendarView() {
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);

        DisablePastDatesDecorator disablePastDatesDecorator = new DisablePastDatesDecorator();
        materialCalendarView.addDecorator(disablePastDatesDecorator);

        rangeDecorator = new HighlightRangeDecorator(requireContext());
        materialCalendarView.addDecorator(rangeDecorator);


//        CalendarDay today = CalendarDay.today();
//        materialCalendarView.setSelectedDate(today);
//
//        rangeDecorator.setDateRange(Collections.singletonList(today));
//        materialCalendarView.invalidateDecorators();

        // display the start date and end date on the calendar
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                materialCalendarView.clearSelection(); // 清除默认选择
                if (startDate == null) {
                    // choose start date
                    startDate = date;
                    rangeDecorator.setDateRange(Collections.singletonList(startDate));
                    endDate = null;
                } else if (endDate == null) {
                    // choose end date
                    if (date.isBefore(startDate)) {
                        // if end date earlier than start date, replace start date to end date
                        startDate = date;
                        rangeDecorator.setDateRange(Collections.singletonList(startDate));
                    } else {
                        endDate = date;
                        // set the date rage
                        List<CalendarDay> datesInRange = new ArrayList<>();
                        Calendar current = Calendar.getInstance();
                        current.setTime(startDate.getDate());

                        while (!current.getTime().after(endDate.getDate())) {
                            datesInRange.add(CalendarDay.from(current));
                            current.add(Calendar.DAY_OF_MONTH, 1); // add one day from start date
                        }

                        rangeDecorator.setDateRange(datesInRange);
                    }
                } else {
                    // choose again
                    startDate = date;
                    endDate = null;
                    rangeDecorator.setDateRange(Collections.singletonList(startDate));
                }
                widget.invalidateDecorators(); // refresh the date
            }
        });
    }

    private void setupNumberPicker(NumberPicker numberPicker) {
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                System.out.println("Selected: " + newVal);
            }
        });
    }

    static Fragment newInstance(int layout) {
        Fragment fragment = new PlanDurationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layout);
        fragment.setArguments(bundle);
        return fragment;
    }
}
