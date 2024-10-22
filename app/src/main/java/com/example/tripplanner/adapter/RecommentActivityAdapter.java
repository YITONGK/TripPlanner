package com.example.tripplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.tripplanner.R;
import com.example.tripplanner.entity.ActivityItem;

import java.util.ArrayList;
import java.util.List;

public class RecommentActivityAdapter extends ArrayAdapter<ActivityItem> {
    private Context context;
    private List<ActivityItem> activityItems;
    private List<ActivityItem> selectedItems;  // To track the checked state of each item

    public RecommentActivityAdapter(Context context, List<ActivityItem> items) {
        super(context, 0, items);
        this.context = context;
        this.activityItems = items;
        this.selectedItems = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the item for this position
        ActivityItem item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_recommend_activity, parent, false);
        }

        // Lookup view for data population
        TextView nameTextView = convertView.findViewById(R.id.activityName);
        TextView locationTextView = convertView.findViewById(R.id.activityLocation);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        // Set the checkbox state based on whether the item is selected
        checkBox.setChecked(selectedItems.contains(item));

        // Set a listener for the checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Add or remove item from selectedItems list based on the checkbox state
                if (isChecked) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
            }
        });

        // Populate the data into the template view using the ActivityItem object
        nameTextView.setText(item.getName());
        locationTextView.setText(item.getLocation().getName());

        // Return the completed view to render on screen
        return convertView;
    }

    // Get the selected items
    public List<ActivityItem> getSelectedItems() {
        return selectedItems;
    }
}

