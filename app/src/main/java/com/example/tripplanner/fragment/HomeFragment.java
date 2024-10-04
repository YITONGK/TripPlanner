package com.example.tripplanner.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.R;
import com.example.tripplanner.adapter.AllPlanAdapter;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    public static int PLAN = R.layout.home_fragment_layout_plan;
    public static int LOCATION = R.layout.home_fragment_layout_location;
    public static String LAYOUT_TYPE = "type";

    private int layout = R.layout.home_fragment_layout_plan;
    private GoogleMap mMap;

    private ArrayList<Trip> allPlans;
    private AllPlanAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE);
        }

        View rootView = inflater.inflate(layout, container, false);

        if (this.layout == R.layout.home_fragment_layout_location) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
        else{
            rootView = inflater.inflate(R.layout.home_fragment_layout_plan, container, false);
            displayAllPlans(rootView);
        }

        return rootView;
    }

    private void displayAllPlans(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.allPlanRecyclerView);
        ArrayList<Trip> allPlans = new ArrayList<>();
        // bind the adapter
        adapter = new AllPlanAdapter(rootView.getContext(), allPlans);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        // get trip data from database
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
         if (currentUser != null) {
             String userId = currentUser.getUid();
            FirestoreDB firestoreDB = new FirestoreDB();
            firestoreDB.getTripsByUserId(userId, trips -> {
                // Handle the list of trips
                allPlans.clear();
                allPlans.addAll(trips);
                adapter.notifyDataSetChanged();
                for (Trip trip : trips) {
                    Log.d("PLAN", "Trip: " + trip.getName());
                    // allPlans.add(trip);
                }
                // Update the adapter with the fetched trips
                // adapter.updateTrips(trips);
            }, e -> {
                Log.d("PLAN", "Error getting trips: " + e.getMessage());
            });
         } else {
             Log.d("Debug", "No user is signed in.");
         }

        // create some sample trips
//        Trip newTrip = new Trip("Holiday in Paris", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
//        Location eiffelTower = new Location("Eiffel Tower", 48.8584, 2.2945);
//        newTrip.addLocation(eiffelTower);
//
//        Trip newTrip2 = new Trip("Holiday in Australia", LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 5));
//        Location melbourne = new Location("Melbourne", 48.8584, 2.2945);
//        Location sydney = new Location("Sydney", 30, 144);
//        newTrip2.addLocation(melbourne);
//        newTrip2.addLocation(sydney);


//        allPlans.add(newTrip);
//        allPlans.add(newTrip2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    // Recommended method to generate new LayoutDemoFragment
    // Instead of calling new LayoutDemoFragment() directly
    public static Fragment newInstance(int layout) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layout);
        fragment.setArguments(bundle);
        return fragment;
    }
}
