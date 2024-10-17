package com.example.tripplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.R;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllPlanAdapter extends RecyclerView.Adapter<AllPlanAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Trip> allPlans;
    private final AllPlanInterface allPlanInterface;

    public AllPlanAdapter(Context context, ArrayList<Trip> allPlans, AllPlanInterface allPlanInterface) {
        this.context = context;
        this.allPlans = allPlans;
        this.allPlanInterface = allPlanInterface;
    }

    @NonNull
    @Override
    public AllPlanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.all_plan_row, parent, false);
        return new AllPlanAdapter.ViewHolder(view, allPlanInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull AllPlanAdapter.ViewHolder holder, int position) {
        // get destinations of the trip
        List<Location> locationList = allPlans.get(position).getLocations();
        StringBuilder sb = new StringBuilder();
        for (Location location : locationList) {
            sb.append(location.getName()).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        holder.locations.setText(sb.toString());

        // get duration of the trip
        int days = allPlans.get(position).getNumDays();
        String dayAndNight;
        if (days == 1) {
            dayAndNight = "1 day";
        }
        else if (days == 2) {
            dayAndNight = "2 days and 1 night";
        } else {
            dayAndNight = days + " days" + " and " + (days - 1) + " nights";
        }
        holder.duration.setText(dayAndNight);

        // get the number of activities of the trip
        Map<String, List<ActivityItem>> plans = allPlans.get(position).getPlans();
        int count = 0;
        for (List<ActivityItem> plan: plans.values()) {
            count += plan.size();
        }
        String countStr = count > 1 ? " activities" : " activity";
        holder.numActivity.setText(count + countStr);

        //TODO: get the cover image of the trip
//        holder.img.setImageResource(allPlans.get(position).getImage());

    }

    @Override
    public int getItemCount() {
        return allPlans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView locations, duration, numActivity;
        private CardView plan;

        public ViewHolder(@NonNull View itemView, AllPlanInterface allPlanInterface) {
            super(itemView);
            img = itemView.findViewById(R.id.image);
            locations = itemView.findViewById(R.id.locations);
            duration = itemView.findViewById(R.id.duration);
            numActivity = itemView.findViewById(R.id.numActivity);
            plan = itemView.findViewById(R.id.plan);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (allPlanInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            allPlanInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
        public void bind(Trip trip) {
            // titleTextView.setText(trip.getTitle());
        }
    }
}
