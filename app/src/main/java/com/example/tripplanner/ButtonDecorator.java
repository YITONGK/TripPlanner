package com.example.tripplanner;

import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ButtonDecorator {
    private ConstraintLayout constraintLayout;
    private int lastButtonId = 0;
    private ArrayList<Button> buttonArrayList = new ArrayList<>();;

    public ButtonDecorator(ConstraintLayout layout) {
        this.constraintLayout = layout;
    }

    public void addButtonsFromJson(JSONArray locations) {
        try {
            for (int i = 0; i < locations.length(); i++) {
                String location = locations.getString(i);
                Button button = createButton(location);
                constraintLayout.addView(button);
                setConstraints(button, i);
                setUpButtonListener(button, i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Button createButton(String location) {
        Button button = new Button(constraintLayout.getContext());
        button.setId(View.generateViewId());


        button.setLayoutParams(new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.WRAP_CONTENT, 120));
//        button.setBackground(ContextCompat.getDrawable(constraintLayout.getContext(), R.drawable.baseline_close_24));
        button.setBackgroundTintList(ContextCompat.getColorStateList(constraintLayout.getContext(), android.R.color.white));
        button.setTextColor(ContextCompat.getColor(constraintLayout.getContext(), android.R.color.black));
        button.setPadding(16, 16, 16, 16);
        button.setTextSize(14);
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_close_24, 0);
        button.setCompoundDrawablePadding(8);
        button.setText(location);

        buttonArrayList.add(button);

        return button;
    }

    private void setConstraints(Button button, int index) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if (index == 0) {
            constraintSet.connect(button.getId(), ConstraintSet.START, R.id.button_add_location, ConstraintSet.END, 8);
            constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.button_back, ConstraintSet.BOTTOM, 30);
        } else {
            constraintSet.connect(button.getId(), ConstraintSet.START, lastButtonId, ConstraintSet.END, 8);
            constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.button_back, ConstraintSet.BOTTOM, 30);
        }

        constraintSet.applyTo(constraintLayout);
        lastButtonId = button.getId();
    }

    public ArrayList<Button> getButtonArrayList() {
        return buttonArrayList;
    }

    private void setUpButtonListener(Button button, int index) {
        button.setOnClickListener(view -> {
            removeButtonAndLocation(index);
        });
    }

    private void removeButtonAndLocation(int index) {
        // Remove button from view
        Button button = buttonArrayList.get(index);
        constraintLayout.removeView(button);

        // Remove button and location from lists
        buttonArrayList.remove(index);

        // Update constraints for remaining buttons if needed
        lastButtonId = buttonArrayList.isEmpty() ? 0 : buttonArrayList.get(buttonArrayList.size() - 1).getId();
        for (int i = index; i < buttonArrayList.size(); i++) {
            setConstraints(buttonArrayList.get(i), i);
        }
    }

}
