package com.example.tripplanner.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        if (days <= 1) {
            dayAndNight = "1 day";
        }
        else if (days == 2) {
            dayAndNight = "2 days and 1 night";
        } else {
            dayAndNight = days + " days" + " and " + (days - 1) + " nights";
        }
        Date endDateMinusOneDay = new Date(allPlans.get(position).getEndDate().toDate().getTime() - TimeUnit.DAYS.toMillis(1));
        Timestamp adjustedEndDate = new Timestamp(endDateMinusOneDay);
        dayAndNight = dayAndNight + "\n" + formatTimestamp(allPlans.get(position).getStartDate()) + " ~ " + formatTimestamp(adjustedEndDate);
        holder.duration.setText(dayAndNight);

        // get the number of activities of the trip
        Map<String, List<ActivityItem>> plans = allPlans.get(position).getPlans();
        int count = 0;
        for (List<ActivityItem> plan: plans.values()) {
            count += plan.size();
        }
        String countStr = count > 1 ? " activities" : " activity";
        holder.numActivity.setText(count + countStr);

        // get the cover image of the trip
        holder.img.setImageResource(allPlans.get(position).getCityDrawable());

        // set the background colour of the card view
        holder.plan.setCardBackgroundColor(holder.itemView.getResources().getColor(getRandomColor(sb.length()), null));

    }

    private int getRandomColor(int len) {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.orange);

        return colorCode.get(len % colorCode.size());
    }

    @Override
    public int getItemCount() {
        return allPlans.size();
    }

    public String formatTimestamp(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
        return sdf.format(date);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        TextView locations;
        private TextView duration;
        private TextView numActivity;
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
                            Log.d("DEBUG", "click on card: "+ pos);
                        }
                    } else {
                        Log.d("DEBUG", "interface is null");
                    }
                }
            });
        }
//        public void bind(Trip trip) {
//            if (trip == null){
//                Log.d("MEMORY", "Trip is null");
//                return;
//            }
//            List<Location> locationList = trip.getLocations();
//            StringBuilder sb = new StringBuilder();
//            for (Location location : locationList) {
//                sb.append(location.getName()).append(", ");
//            }
//            if (sb.length() > 0) {
//                sb.setLength(sb.length() - 2);
//            }
//            locations.setText(sb.toString());
//
//            // get duration of the trip
//            int days = trip.getLastingDays();
//            String dayAndNight;
//            if (days <= 1) {
//                dayAndNight = "1 day";
//            }
//            else if (days == 2) {
//                dayAndNight = "2 days and 1 night";
//            } else {
//                dayAndNight = days + " days" + " and " + (days - 1) + " nights";
//            }
//            duration.setText(dayAndNight);
//
//            // get the number of activities of the trip
//            Map<String, List<ActivityItem>> plans = trip.getPlans();
//            int count = 0;
//            for (List<ActivityItem> plan: plans.values()) {
//                count += plan.size();
//            }
//            String countStr = count > 1 ? " activities" : " activity";
//            numActivity.setText(countStr);
//
//
//            // titleTextView.setText(trip.getTitle());
//        }
    }
}
