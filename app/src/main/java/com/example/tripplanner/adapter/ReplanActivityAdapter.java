package com.example.tripplanner.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tripplanner.R;
import com.example.tripplanner.entity.ActivityItem;

import java.util.ArrayList;
import java.util.List;

public class ReplanActivityAdapter extends ArrayAdapter<ActivityItem> {
//    private List<ActivityItem> selectedItems;  // To track the selected state of each item

    public ReplanActivityAdapter(Context context, List<ActivityItem> items) {
        super(context, 0, items);
//        this.selectedItems = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the item for this position
        ActivityItem item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_replan_activity, parent, false);
        }

        // Lookup view for data population
        TextView nameTextView = convertView.findViewById(R.id.activityName);
        TextView locationTextView = convertView.findViewById(R.id.activityLocation);

        // Populate the data into the template view using the ActivityItem object
        nameTextView.setText(item.getName());
        locationTextView.setText(item.getLocation().getName());

        // Set the background color based on whether the item is selected
//        convertView.setBackgroundColor(selectedItems.contains(item) ? Color.LTGRAY : Color.TRANSPARENT);

        // Set a click listener for the row
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedItems.contains(item)) {
//                    selectedItems.remove(item);
//                    v.setBackgroundColor(Color.TRANSPARENT);
//                } else {
//                    selectedItems.add(item);
//                    v.setBackgroundColor(Color.LTGRAY);
//                }
//            }
//        });

        // Return the completed view to render on screen
        return convertView;
    }

    // Get the selected items
//    public List<ActivityItem> getSelectedItems() {
//        return selectedItems;
//    }
}

