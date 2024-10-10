package com.example.tripplanner.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.tripplanner.R;
import com.example.tripplanner.entity.Location;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class ButtonDecorator {

    public interface OnButtonClickListener {
        void onButtonClicked(int index, Button button);
    }

    private LinearLayout linearLayout;
    private OnButtonClickListener listener;

    public ButtonDecorator(LinearLayout layout, OnButtonClickListener listener) {
        this.linearLayout = layout;
        this.listener = listener;
    }

    public void addButtonsFromList(List<Location> locations) {
        try {
            for (int i = 0; i < locations.size(); i++) {
                String location = locations.get(i).getName();
                addSingleButton(location, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllButtons() {
        linearLayout.removeAllViews();
    }

    public void addSingleButton(String location, int index) {
        Button button = createButton(location, index);
        linearLayout.addView(button);
    }

    private Button createButton(String location, int index) {
        Button button = new Button(linearLayout.getContext());
        button.setId(View.generateViewId());
        button.setTag(index);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                120
        );
        layoutParams.setMargins(8, 8, 8, 8);
        button.setLayoutParams(layoutParams);

        button.setBackgroundTintList(ContextCompat.getColorStateList(linearLayout.getContext(), android.R.color.white));
        button.setTextColor(ContextCompat.getColor(linearLayout.getContext(), android.R.color.black));
        button.setPadding(16, 16, 16, 16);
        button.setTextSize(14);
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.location_close_24, 0);
        button.setCompoundDrawablePadding(8);
        button.setText(location);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) button.getTag();
                if (listener != null) {
                    listener.onButtonClicked(position, button);
                }
            }
        });

        return button;
    }
}