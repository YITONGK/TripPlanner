package com.example.tripplanner.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.R;
import com.example.tripplanner.entity.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SectionedPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final Context context;
    private final Map<String, List<Trip>> tripsByCity;
    private final List<Object> items;

    public SectionedPlanAdapter(Context context, Map<String, List<Trip>> tripsByCity) {
        this.context = context;
        this.tripsByCity = tripsByCity;
        this.items = new ArrayList<>();
        prepareItems();
    }

    private void prepareItems() {
        for (Map.Entry<String, List<Trip>> entry : tripsByCity.entrySet()) {
            String city = entry.getKey();
            List<Trip> trips = entry.getValue();
            items.add(city); // Add city as a section header
            items.addAll(trips); // Add trips under the city
        }
        Log.d("DEBUG", "Items prepared: " + items.toString()); // Log the items list
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? VIEW_TYPE_SECTION : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_SECTION) {
            View view = inflater.inflate(R.layout.section_header, parent, false);
            return new SectionViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.all_plan_row, parent, false);
            return new AllPlanAdapter.ViewHolder(view, null); // Pass null or a valid interface
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SECTION) {
            Log.d("DEBUG", "[SectionedPlanAdaptor] Bind item to SectionViewHolder");
            ((SectionViewHolder) holder).bind((String) items.get(position));
        } else {
            Log.d("DEBUG", "[SectionedPlanAdaptor] Bind trip to all plan");
            Trip trip = (Trip) items.get(position);
            ((AllPlanAdapter.ViewHolder) holder).bind(trip);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView sectionTitle;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
        }

        public void bind(String city) {
            sectionTitle.setText(city);
        }
    }
}
