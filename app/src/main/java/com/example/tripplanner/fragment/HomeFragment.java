package com.example.tripplanner.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplanner.EditPlanActivity;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.AllPlanAdapter;
import com.example.tripplanner.adapter.AllPlanInterface;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback, AllPlanInterface {

    public static int PLAN = R.layout.home_fragment_layout_plan;
    public static int LOCATION = R.layout.home_fragment_layout_location;
    public static String LAYOUT_TYPE = "type";

    private int layout = R.layout.home_fragment_layout_plan;
    private GoogleMap mMap;

    private ArrayList<Trip> allPlans = new ArrayList<>();;
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
        } else {
            rootView = inflater.inflate(R.layout.home_fragment_layout_plan, container, false);
            displayAllPlans(rootView);
        }

        return rootView;
    }

    private void displayAllPlans(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.allPlanRecyclerView);
        // bind the adapter
        adapter = new AllPlanAdapter(rootView.getContext(), allPlans, this);
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
                Log.d("PLAN", "getting trips: " + trips.size());
                allPlans.clear();
                allPlans.addAll(trips);
                adapter.notifyDataSetChanged();
                for (Trip trip : trips) {
                    Log.d("PLAN", "Trip: " + trip.toString());
                }
            }, e -> {
                Log.d("PLAN", "Error getting trips: " + e.getMessage());
            });
        } else {
            Log.d("Debug", "No user is signed in.");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);

        // Get the current location
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15)); // Zoom level can be adjusted
        } else {
            // Fallback to a default location if current location is not available
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in
        // Sydney"));
        TextView textView = new TextView(this.getContext());
        textView.setText("Hello!!");
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.YELLOW);

        Marker marker = mMap.addMarker(
                new AdvancedMarkerOptions()
                        .position(sydney)
                        .iconView(textView));
        marker.setTag(0);

        // getCurrentLocation();

        // LatLng PERTH = new LatLng(-31.952854, 115.857342);
        // Marker markerPerth = mMap.addMarker(new MarkerOptions()
        // .position(PERTH)
        // .title("Perth"));
        // markerPerth.setTag(0);

        // Show the bottom sheet when the map is ready
        showPastTripsBottomSheet();
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

    @Override
    public void onItemClick(int position) {
        // Navigate to specific plan detail page
        Log.d("TAG", "click on card");
        Log.d("TAG", getActivity().toString());
        Intent i = new Intent(getActivity(), EditPlanActivity.class);
        i.putExtra("tripId", allPlans.get(position).getId());
        startActivity(i);
    }

    private android.location.Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    1);
            return null;
        }
        // Location location =
        // locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // if (location != null) {
        // return location;
        // }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d("SENSOR", "location: " + location);
        return location;
    }

    private void showPastTripsBottomSheet() {
        PastTripsBottomSheet bottomSheet = new PastTripsBottomSheet();
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }


}
