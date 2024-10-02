package com.example.tripplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tripplanner.helperclass.ActivityItem;
import com.example.tripplanner.R;

import java.util.List;

public class ActivityItemAdapter extends ArrayAdapter<ActivityItem> {
    private Context context;
    private List<ActivityItem> activityItemList;

    public ActivityItemAdapter(Context context, List<ActivityItem> activityItemList) {
        super(context, 0, activityItemList);
        this.context = context;
        this.activityItemList = activityItemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ActivityItem activityItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_item, parent, false);
        }

        TextView activityName = convertView.findViewById(R.id.activityName);
        TextView activityDetails = convertView.findViewById(R.id.activityDetails);

        activityName.setText(activityItem.getName());

        String details = "";
        if (!activityItem.getStartTime().isEmpty()) {
            details += "Start Time: " + activityItem.getStartTime() + "\n";
        }
        if (!activityItem.getEndTime().isEmpty()) {
            details += "Start Time: " + activityItem.getEndTime() + "\n";
        }
        if (!activityItem.getLocation().isEmpty()) {
            details += "Location: " + activityItem.getLocation() + "\n";
        }
        if (!activityItem.getNotes().isEmpty()) {
            details += "Note: " + activityItem.getNotes();
        }

        activityDetails.setText(details);

        return convertView;
    }
}

