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

    public ReplanActivityAdapter(Context context, List<ActivityItem> items) {
        super(context, 0, items);
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

        // Return the completed view to render on screen
        return convertView;
    }

}

