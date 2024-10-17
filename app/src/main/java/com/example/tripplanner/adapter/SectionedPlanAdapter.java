package com.example.tripplanner.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.EditPlanActivity;
import com.example.tripplanner.R;
import com.example.tripplanner.entity.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SectionedPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SECTION = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final Context context;
    private final List<Object> items;
    private final AllPlanInterface allPlanInterface;

    public SectionedPlanAdapter(Context context,  Map<String, List<Trip>> tripsByCountry, AllPlanInterface allPlanInterface) {
        this.context = context;
        this.items = new ArrayList<>();
        this.allPlanInterface = allPlanInterface;
        prepareItems(tripsByCountry);
    }

    private void prepareItems(Map<String, List<Trip>> tripsByCountry) {
        for (Map.Entry<String, List<Trip>> entry : tripsByCountry.entrySet()) {
            String country = entry.getKey();
            List<Trip> trips = entry.getValue();
            items.add(country); // Add country as a section header
            items.addAll(trips); // Add trips under the country
        }
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
            return new AllPlanAdapter.ViewHolder(view, allPlanInterface); 
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionViewHolder) {
            Log.d("DEBUG", "[SectionedPlanAdaptor] Bind item to SectionViewHolder");
            ((SectionViewHolder) holder).sectionTitle.setText((String) items.get(position));
        } else if (holder instanceof AllPlanAdapter.ViewHolder) {
            Log.d("DEBUG", "[SectionedPlanAdaptor] Bind trip to all plan");
            Trip trip = (Trip) items.get(position);
            ((AllPlanAdapter.ViewHolder) holder).bind(trip); // Ensure bind method is used
        }
    }
//     public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//         // if (getItemViewType(position) == VIEW_TYPE_SECTION) {
//         //     Log.d("DEBUG", "[SectionedPlanAdaptor] Bind item to SectionViewHolder");
//         //     ((SectionViewHolder) holder).bind((String) items.get(position));
//         // } else {
//         //     Log.d("DEBUG", "[SectionedPlanAdaptor] Bind trip to all plan");
//         //     Trip trip = (Trip) items.get(position);
//         //     ((AllPlanAdapter.ViewHolder) holder).bind(trip);
//         // }
//         if (holder instanceof SectionViewHolder) {
//             Log.d("DEBUG", "[SectionedPlanAdaptor] Bind item to SectionViewHolder");
//            ((SectionViewHolder) holder).sectionTitle.setText((String) items.get(position));
//        } else if (holder instanceof AllPlanAdapter.ViewHolder) {
//             Log.d("DEBUG", "[SectionedPlanAdaptor] Bind trip to all plan");
//           Trip trip = (Trip) items.get(position);
//           allPlanAdapter.onBindViewHolder((AllPlanAdapter.ViewHolder) holder, position);
// //           ((AllPlanAdapter.ViewHolder) holder).locations.setText(trip.getName());

//        }
//     }

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
