package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.tripplanner.adapter.RecommentActivityAdapter;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.utils.GptApiClient;
import com.example.tripplanner.utils.PlacesClientProvider;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.HomeFragment;
import com.example.tripplanner.utils.SensorDetector;
import com.example.tripplanner.utils.CaptureAct;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tripplanner.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private PlacesClient placesClient;

    private SensorDetector sensorDetector;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
            loadUserProfile();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

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
        binding.navView.setSelectedItemId(R.id.navigation_plan);
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
                                        // Validate the trip ID
                                        FirestoreDB firestoreDB = FirestoreDB.getInstance();
                                        firestoreDB.getTripByTripId(tripID,
                                                new OnSuccessListener<Trip>() {
                                                    @Override
                                                    public void onSuccess(Trip trip) {
                                                        // Ensure currentUser is not null
                                                        FirebaseUser currentUser = FirebaseAuth.getInstance()
                                                                .getCurrentUser();
                                                        if (currentUser != null) {
                                                            String userId = currentUser.getUid();

                                                            // Add user to the trip
                                                            firestoreDB.addUserToTrip(tripID, userId,
                                                                    (Void) -> {
                                                                        importPlanBottomSheet.dismiss();
                                                                        // Navigate to added trip details
                                                                        Intent i = new Intent(MainActivity.this,
                                                                                EditPlanActivity.class);
                                                                        i.putExtra("tripId", tripID);
                                                                        i.putExtra("From", "Main");
                                                                        startActivity(i);
                                                                    },
                                                                    e -> {});
                                                        }
                                                    }
                                                },
                                                e -> {
                                                    // Notify user of invalid trip ID
                                                    Toast.makeText(MainActivity.this,
                                                            "Invalid Trip ID. Please try again.", Toast.LENGTH_SHORT)
                                                            .show();
                                                });
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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        placesClient = Places.createClient(this);

        // Initialize sensors
        sensorDetector = new SensorDetector(this);
        sensorDetector.setOnShakeListener(() -> {
            // Show confirmation dialog
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("One-Day Trip Plan")
                .setMessage("Do you want to generate a one-day trip plan?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Show loading dialog
                                ProgressDialog loadingDialog = new ProgressDialog(MainActivity.this);
                                loadingDialog.setMessage("Loading...");
                                loadingDialog.setCancelable(false);
                                loadingDialog.show();

                                // Get current location and search nearby places
                                Log.d("SENSOR", "Shake event detected");
                                searchNearbyPlaces(new android.location.Location("Melbourne"), places -> {

                                    // Access temperature and humidity
                                    float temperature = sensorDetector.getAmbientTemperature();
                                    float humidity = sensorDetector.getRelativeHumidity();
                                    String sensorData = "Temperature: " + temperature + ", Humidity: " + humidity;
                                    Log.d("SENSOR", sensorData);

                                    AtomicReference<String> userPreferences = new AtomicReference<>("Enjoy cafe and bakery");
                                    FirestoreDB.getInstance().getUserById(FirestoreDB.getCurrentUserId(), (user) -> {
                                        userPreferences.set(user.getPreference());
                                    }, e -> {
                                    });

                                    Log.d("SENSOR", "Places: " + places.get(0).getAddress());
                                    // Split the string by commas
                                    String country = "Australia";
                                    try {
                                        String[] parts = places.get(0).getAddress().split(",");
                                        country = parts[parts.length - 1].trim();
                                    } catch (Exception ex) {
                                        Log.d("SENSOR", "Error: " + ex);
                                    }

                                    String finalCountry = country;
                                    GptApiClient.generateOneDayTripPlan(sensorData, places, userPreferences.get(), new GptApiClient.GptApiCallback() {
                                        @Override
                                        public void onSuccess(String response) {
                                            // Handle the successful response here
                                            loadingDialog.dismiss();
                                            Log.d("SENSOR", "Trip plan recommended: " + response);

                                            String tripName = GptApiClient.getStringFromJsonResponse(response, "tripName");

                                            // Parse the JSON response into a list of ActivityItem objects
                                            GptApiClient.parseActivityItemsFromJson(finalCountry, response, placesClient, new GptApiClient.OnActivityItemsParsedListener() {
                                                @Override
                                                public void onActivityItemsParsed(List<ActivityItem> recommendedActivities) {
                                                    Log.d("SENSOR", "RecommendActivities: " + recommendedActivities);

                                                    // Show a popup window to let users select which activities to add to plan
                                                    ListView listView = new ListView(MainActivity.this);
                                                    RecommentActivityAdapter recommentActivityAdapter = new RecommentActivityAdapter(MainActivity.this, recommendedActivities);
                                                    listView.setAdapter(recommentActivityAdapter);

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    builder.setTitle("Recommendations");
                                                    builder.setView(listView);

                                                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            SensorDetector.setIsShaken(false);
                                                            // Get only the selected items
                                                            List<ActivityItem> selectedItems = recommentActivityAdapter.getSelectedItems();
                                                            List<Location> newloc = new ArrayList<>();
                                                            newloc.add(recommendedActivities.get(0).getLocation());
                                                            Trip trip = new Trip(tripName, Timestamp.now(), 1, newloc, FirestoreDB.getCurrentUserId());

                                                            Map<String, List<ActivityItem>> newPlans = new HashMap<>();
                                                            newPlans.put("0", selectedItems);
                                                            trip.setPlans(newPlans);


                                                            FirestoreDB db = FirestoreDB.getInstance();
                                                            db.createTrip(FirestoreDB.getCurrentUserId(), trip.convertTripToMap());

                                                            binding.navView.setSelectedItemId(R.id.navigation_plan);

                                                            Fragment planFragment = HomeFragment.newInstance(HomeFragment.PLAN);
                                                            getSupportFragmentManager()
                                                                    .beginTransaction()
                                                                    .replace(R.id.fragmentContainerView, planFragment)
                                                                    .commit();

                                                        }
                                                    });
                                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            SensorDetector.setIsShaken(false);
                                                            dialogInterface.dismiss();
                                                        }
                                                    });

                                                    builder.create().show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            // Handle the error here
                                            loadingDialog.dismiss();
//                                            Toast.makeText(MainActivity.this, "Failed to recommend trip plan", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                });
                            }
                        }
                )
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Reset isShaken flag
                            SensorDetector.setIsShaken(false);
                        }
                    })
                .show();

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listeners when the activity is resumed
        sensorDetector.registerSensorListeners();
        loadUserProfile();
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

    private void searchNearbyPlaces( android.location.Location location, OnSuccessListener<List<Place>> listener) {
        // Define the place fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES);

        // Create a request object
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    List<Place> places = new ArrayList<>();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        places.add(placeLikelihood.getPlace());
                    }
                    listener.onSuccess(places);
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("select_navigation_plan", false)) {
            binding.navView.setSelectedItemId(R.id.navigation_plan);
            Fragment planFragment = HomeFragment.newInstance(HomeFragment.PLAN);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, planFragment)
                    .commit();
        } else {
            binding.navView.setSelectedItemId(R.id.navigation_map);

            Fragment planFragment = HomeFragment.newInstance(HomeFragment.LOCATION);
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
                                // Navigate to added trip details
                                Intent intent = new Intent(MainActivity.this, EditPlanActivity.class);
                                intent.putExtra("tripId", tripID);
                                intent.putExtra("From", "Main");
                                startActivity(intent);
                            },
                            e -> {}
                        );
                    }


                }
            }).show();
        }
    });

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profilePictureUrl = documentSnapshot.getString("profilePicture");

                ImageView profileBtn = findViewById(R.id.profileBtn);

                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.woman)
                            .into(profileBtn);
                } else {
                    profileBtn.setImageResource(R.drawable.woman);
                }
            }
        }).addOnFailureListener(e -> {
        });
    }
}