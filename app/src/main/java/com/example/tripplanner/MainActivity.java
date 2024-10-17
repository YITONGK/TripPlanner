package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.HomeFragment;
import com.example.tripplanner.utils.WeatherTripPlanner;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.tripplanner.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private GoogleMap mMap;

    private WeatherTripPlanner weatherTripPlanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handleIntent(getIntent());

        Fragment plan_layout = HomeFragment.newInstance(HomeFragment.PLAN);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, plan_layout)
                .addToBackStack(null)
                .commit();

        // Navigate to profile page
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Setting for Navigation Bar
        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                // To show plan layout demonstration
                if (id == R.id.navigation_plan) {
                    Fragment plan_layout = HomeFragment.newInstance(HomeFragment.PLAN);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, plan_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                // To create new plan activity
                if (id == R.id.navigation_add) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_plan_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    CardView addNewPlan = view.findViewById(R.id.addNewPlan);
                    CardView importPlan = view.findViewById(R.id.addSharePlan);

                    addNewPlan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            startActivity(new Intent(MainActivity.this, CreateNewPlanActivity.class));
                        }
                    });
                    // import existing plan
                    importPlan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            BottomSheetDialog importPlanBottomSheet = new BottomSheetDialog(MainActivity.this);
                            View importPlanView = LayoutInflater.from(MainActivity.this)
                                    .inflate(R.layout.import_plan_bottom_sheet, null);
                            importPlanBottomSheet.setContentView(importPlanView);
                            importPlanBottomSheet.show();

                            EditText tripIDView = importPlanBottomSheet.findViewById(R.id.planID);
                            Button confirmButton = importPlanView.findViewById(R.id.confirmButton);

                            confirmButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String tripID = tripIDView.getText().toString().trim();
                                    if (!tripID.isEmpty()) {
                                        Log.d("SHARE", tripID);
                                        // Validate the trip ID
                                        FirestoreDB firestoreDB = FirestoreDB.getInstance();
                                        firestoreDB.getTripByTripId(tripID,
                                                new OnSuccessListener<Trip>() {
                                                    @Override
                                                    public void onSuccess(Trip trip) {
                                                        Log.d("IMPORT PLAN", "Trip ID Verified");
                                                        // Ensure currentUser is not null
                                                        FirebaseUser currentUser = FirebaseAuth.getInstance()
                                                                .getCurrentUser();
                                                        if (currentUser != null) {
                                                            String userId = currentUser.getUid();

                                                            // Add user to the trip
                                                            firestoreDB.addUserToTrip(tripID, userId,
                                                                    (Void) -> {
                                                                        Log.d("IMPORT PLAN",
                                                                                "Successfully added user to trip");
                                                                        importPlanBottomSheet.dismiss();
                                                                        // Navigate to added trip details
                                                                        Intent i = new Intent(MainActivity.this,
                                                                                EditPlanActivity.class);
                                                                        i.putExtra("tripId", tripID);
                                                                        startActivity(i);
                                                                    },
                                                                    e -> Log.e("IMPORT PLAN",
                                                                            "Failed to add user to trip: "
                                                                                    + e.getMessage()));
                                                        }
                                                    }
                                                },
                                                e -> {
                                                    Log.d("IMPORT PLAN", "Trip ID Invalid: " + e.getMessage());
                                                    // Notify user of invalid trip ID
                                                    Toast.makeText(MainActivity.this,
                                                            "Invalid Trip ID. Please try again.", Toast.LENGTH_SHORT)
                                                            .show();
                                                });
                                    } else {
                                        Log.e("SHARE", "Trip ID is empty");
                                    }
                                }
                            });

                        }
                    });

                    return true;
                }
                // To show Location layout demonstration
                else if (id == R.id.navigation_map) {
                    Fragment Location_layout = HomeFragment.newInstance(HomeFragment.LOCATION);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, Location_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Initialize WeatherTripPlanner
        weatherTripPlanner = new WeatherTripPlanner(this);

        String origin = "New York, NY";
        String destination = "Los Angeles, CA";
        String[] modes = { "driving", "walking", "transit" };
        String[] modes = { "driving", "walking", "transit" };

        for (String mode : modes) {
            try {
                String url = buildDirectionsUrl(origin, destination, mode);
                getDistanceMatrix(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // Detect weather and plan trip
        // weatherTripPlanner.detectWeatherAndPlanTrip();

        // manually add a trip object
        // String name = "My Awesome Trip";
        // Timestamp startDate = Timestamp.now();
        // int receivedDays = 5; // Duration of the trip in days
        //
        // // Create a list of Location objects
        // List<Location> locationList = new ArrayList<>();
        // locationList.add(new Location("1", "New York City", "City", 40.7128,
        // -74.0060));
        // locationList.add(new Location("2", "Los Angeles", "City", 34.0522,
        // -118.2437));
        // locationList.add(new Location("3", "Chicago", "City", 41.8781, -87.6298));
        //
        // FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        // String userId = currentUser.getUid();
        //
        // // Create Trip object
        // Trip trip = new Trip(name, startDate, receivedDays, locationList, userId);
        //
        // // Create FirestoreDB instance and add trip to Firestore
        // FirestoreDB firestore = new FirestoreDB();
        //
        // firestore.createTrip(userId, trip.convertTripToMap());

        // FirestoreDB firestoreDB = new FirestoreDB();
        // String tripId = "3Rt1mDAOhYzwLY4ouR7K";
        //
        // firestoreDB.deleteTripById(tripId, new OnSuccessListener<Void>() {
        // @Override
        // public void onSuccess(Void aVoid) {
        // // Handle successful deletion
        // Log.d("PLAN", "Trip successfully deleted.");
        // // Update UI or navigate back
        // }
        // }, new OnFailureListener() {
        // @Override
        // public void onFailure(@NonNull Exception e) {
        // // Handle deletion failure
        // Log.e("PLAN", "Error deleting trip", e);
        // }
        // });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listeners when the activity is resumed
        weatherTripPlanner.registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listeners when the activity is paused
        weatherTripPlanner.unregisterSensorListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("select_navigation_plan", false)) {
            binding.navView.setSelectedItemId(R.id.navigation_plan);

            Fragment planFragment = HomeFragment.newInstance(HomeFragment.PLAN);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, planFragment)
                    .commit();
        }
    }

    private String buildDirectionsUrl(String origin, String destination, String mode)
            throws UnsupportedEncodingException {
        String apiKey = "YOUR_API_KEY"; // Replace with your actual API key
        String urlOrigin = URLEncoder.encode(origin, "UTF-8");
        String urlDestination = URLEncoder.encode(destination, "UTF-8");
        String urlMode = URLEncoder.encode(mode, "UTF-8");

        return "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + urlOrigin
                + "&destination=" + urlDestination
                + "&mode=" + urlMode
                + "&key=" + apiKey;
    }

    // New method to get distance matrix from Google Maps API
    private void getDistanceMatrix(String url) {

        // Make a network request to the Google Maps API
        // You can use libraries like Retrofit or OkHttp for this
        // For simplicity, this example uses a basic approach
        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    // Handle the response (parse JSON, etc.)
                    Log.d("DistanceMatrix", response.toString());
                } else {
                    Log.e("DistanceMatrix", "Error: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("DistanceMatrix", "Exception: " + e.getMessage());
            }
        }).start();
    }

}