package com.example.tripplanner.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter<AutocompletePrediction> {
    public AutocompleteAdapter(Context context, List<AutocompletePrediction> predictions) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1, predictions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        AutocompletePrediction item = getItem(position);

        TextView textView1 = row.findViewById(android.R.id.text1);
        TextView textView2 = row.findViewById(android.R.id.text2);
        textView1.setText(item.getPrimaryText(null));
        textView2.setText(item.getSecondaryText(null));

        return row;
    }
}
