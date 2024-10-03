package com.example.tripplanner.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.shawnlin.numberpicker.NumberPicker;

import android.widget.Button;

import com.example.tripplanner.R;

public class NumberPickerFragment extends Fragment {

    private NumberPicker numberPicker;
    private OnNumberSelectedListener callback;
    private int currentDays = 1; // Default value

    public interface OnNumberSelectedListener {
        void onNumberSelected(int days);
    }

    public NumberPickerFragment(int days) {
        this.currentDays = days;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNumberSelectedListener) {
            callback = (OnNumberSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNumberSelectedListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.number_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setValue(currentDays);

        Button doneButton = view.findViewById(R.id.button_done);
        doneButton.setOnClickListener(v -> {
            int selectedDays = numberPicker.getValue();
            if (callback != null) {
                callback.onNumberSelected(selectedDays);
            }
            // Remove the fragment after selection
            requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });
    }
}
