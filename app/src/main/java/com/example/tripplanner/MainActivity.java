package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.tripplanner.adapter.DistanceMatrixCallback;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.DistanceMatrixEntry;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.utils.PlacesClientProvider;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.HomeFragment;
import com.example.tripplanner.utils.RoutePlanner;
import com.example.tripplanner.utils.SensorDetector;
import com.example.tripplanner.utils.CaptureAct;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.tripplanner.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private GoogleMap mMap;
    private PlacesClient placesClient;

    private SensorDetector sensorDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        placesClient = Places.createClient(this);
        PlacesClientProvider.initialize(placesClient);

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
                            Button scanBtn = importPlanView.findViewById(R.id.scanBtn);

                            scanBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    scanCode();
                                }
                            });

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
        sensorDetector = new SensorDetector(this);

        // Detect weather and plan trip
        // weatherTripPlanner.detectWeatherAndPlanTrip();

        // Example usage of Route Planner
//        List<ActivityItem> activityItems = new ArrayList<>();
//        // Create some sample ActivityItems
//        ActivityItem item1 = new ActivityItem("Visit NYU");
//        item1.setLocation(new Location("New York University", 40.779437, -73.963244));
//
//        ActivityItem item2 = new ActivityItem("Lunch at Central Park");
//        item2.setLocation(new Location("Central Park", 40.785091, -73.968285));
//
//        ActivityItem item3 = new ActivityItem("Empire State Building Tour");
//        item3.setLocation(new Location("Empire State Building", 40.748817, -73.985428));
//
//        // Add items to the list
//        activityItems.add(item1);
//        activityItems.add(item2);
//        activityItems.add(item3);

//        // Fetch the distance matrix
//        RoutePlanner.fetchDistanceMatrix(activityItems, "driving", new DistanceMatrixCallback() {
//            @Override
//            public void onSuccess(List<DistanceMatrixEntry> distanceMatrix) {
//                // Handle the successful result
//                Log.d("RoutePlannerUtil","Distance Matrix fetched successfully!");
//                List<ActivityItem> bestRoute = RoutePlanner.calculateBestRoute(distanceMatrix, activityItems);
//                Log.d("RoutePlannerUtil", "Best Route: " + bestRoute);
//
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                // Handle the error
//                Log.d("RoutePlannerUtil", "Failed to fetch Distance Matrix: " + e.getMessage());
//            }
//
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listeners when the activity is resumed
        sensorDetector.registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listeners when the activity is paused
        sensorDetector.unregisterSensorListeners();
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

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Plan ID");
            String tripID = result.getContents();
            builder.setMessage(tripID);
            builder.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    FirestoreDB firestoreDB = FirestoreDB.getInstance();
                    // Ensure currentUser is not null
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();

                        // Add user to the trip
                        firestoreDB.addUserToTrip(tripID, userId,
                            (Void) -> {
                                Log.d("IMPORT PLAN", "Successfully added user to trip");
                                // Navigate to added trip details
                                Intent intent = new Intent(MainActivity.this, EditPlanActivity.class);
                                intent.putExtra("tripId", tripID);
                                startActivity(intent);
                            },
                            e -> Log.e("IMPORT PLAN", "Failed to add user to trip: " + e.getMessage())
                        );
                    }


                }
            }).show();
        }
    });


}