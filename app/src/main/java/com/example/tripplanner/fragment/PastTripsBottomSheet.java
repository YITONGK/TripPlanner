package com.example.tripplanner.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.EditPlanActivity;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.AllPlanAdapter;
import com.example.tripplanner.adapter.AllPlanInterface;
import com.example.tripplanner.adapter.SectionedPlanAdapter;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PastTripsBottomSheet extends BottomSheetDialogFragment implements AllPlanInterface {

    // private AllPlanAdapter adapter;
    private SectionedPlanAdapter adapter;
    private ArrayList<Trip> pastTrips = new ArrayList<>();

    public PastTripsBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_past_trips, container, false);
        fetchPastTrips(view);

        return view;
    }

    private void fetchPastTrips(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.pastTripsRecyclerView);
        // AllPlanAdapter adapter = new AllPlanAdapter(getContext(), pastTrips, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirestoreDB firestoreDB = FirestoreDB.getInstance();
            firestoreDB.getPastTripsByUserId(userId, new OnSuccessListener<List<Trip>>() {
                @Override
                public void onSuccess(List<Trip> trips) {
                    Map<String, List<Trip>> tripsByCountry = groupTripsByCountry(trips);
                    Log.d("DEBUG", "Trips by country: " + tripsByCountry.size());
                    adapter = new SectionedPlanAdapter(getContext(), tripsByCountry, PastTripsBottomSheet.this);
                    recyclerView.setAdapter(adapter);

                    // int totalLocations = trips.stream()
                    //         .mapToInt(trip -> trip.getLocations().size())
                    //         .sum();

                    if (adapter != null) { // Check if adapter is initialized
                        pastTrips.clear();
                        pastTrips.addAll(trips);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Adapter not initialized", Toast.LENGTH_SHORT).show();
                    }
                }
            }, e -> {
                Toast.makeText(getContext(), "Failed to load past trips", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "No user is signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, List<Trip>> groupTripsByCountry(List<Trip> trips) {
        Map<String, List<Trip>> tripsByCountry = new HashMap<>();
        for (Trip trip : trips) {
            for (Location location : trip.getLocations()) {
                 String country = location.getCountry() == null ? "Others" : location.getCountry();
                 Log.d("MEMORY", "Country: "+country);
//                String country = "Country";
                if (!tripsByCountry.containsKey(country)) {
                    tripsByCountry.put(country, new ArrayList<>());
                }
                tripsByCountry.get(country).add(trip);
            }
        }
        return tripsByCountry;
    }

    @Override
    public void onItemClick(int position) {
        // Navigate to specific plan detail page
        Log.d("MEMORY", "position: "+position);
        Log.d("MEMORY", "pastTrips: "+pastTrips);
        Trip clickedTrip = pastTrips.get(position-1);
        Intent i = new Intent(getActivity(), EditPlanActivity.class);
        i.putExtra("tripId", clickedTrip.getId());
        startActivity(i);
    }
}