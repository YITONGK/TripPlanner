package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tripplanner.databinding.ActivityEditPlanBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.PlanFragment;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditPlanActivity extends AppCompatActivity {

    private String selectedPlace;
    private int days;
    private String tripName;
    private ActivityEditPlanBinding binding;
    private final ArrayList<Fragment> fragments = new ArrayList<>();
    private FragmentManager fragmentManager;
    private Trip trip;

    private ActivityResultLauncher<Intent> planSettingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets();

        // Get intent extras
        String tripId = getIntent().getStringExtra("tripId");
        String jsonString = getIntent().getStringExtra("planDetails");

        if (tripId != null && !tripId.isEmpty()) {
            Log.d("TAG", "Trip ID: " + tripId);
            fetchTripData(tripId);
        } else if (jsonString != null && !jsonString.isEmpty()) {
            Log.d("TAG", "Plan Details JSON: " + jsonString);
            parsePlanDetails(jsonString);

            // Update the UI
            runOnUiThread(() -> {
                setupTripInfo();
                initializeFragmentsAndTabs();
                setupTabSelectedListener();
                loadFragment(fragments.get(0));
            });
        } else {
            // Handle the case where neither tripId nor planDetails are provided
            Log.d("TAG", "No trip ID or plan details provided.");
            // Optionally, finish the activity or show an error message
            finish();
        }

        setupCloseButton();
        setupPlanSettingsLauncher();
        setupSettingsButton();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchTripData(String tripId) {
        FirestoreDB firestoreDB = new FirestoreDB();
        firestoreDB.getTripByTripId(tripId, this::onTripDataFetched, e -> {
            Log.d("PLAN", "Error getting trip by trip ID: " + e.getMessage());
            // Handle the error appropriately (e.g., show an error message to the user)
        });
    }

    private void onTripDataFetched(Trip trip) {
        Log.d("PLAN", "Trip data fetched: " + trip.toString());
        this.trip = trip;
        extractDetailsFromTrip(trip);

        runOnUiThread(() -> {
            setupTripInfo();
            initializeFragmentsAndTabs();
            setupTabSelectedListener();
            loadFragment(fragments.get(0));
        });
    }

    private void parsePlanDetails(String jsonString) {
        try {
            JSONObject tripPlan = new JSONObject(jsonString);
            extractDetailsFromPlan(tripPlan);
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing error
            Log.d("TAG", "Error parsing plan details: " + e.getMessage());
        }
    }

    private void extractDetailsFromTrip(Trip trip) {
        // Get locations and duration of the plan
        List<Location> locations = trip.getLocations();

        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append(location.getName()).append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        selectedPlace = sb.toString();
        days = trip.getNumDays();

        // TODO: get activities of the plan
    }

    private void extractDetailsFromPlan(JSONObject tripPlan) {
        try {
            JSONArray placeArray = tripPlan.getJSONArray("location");
            ArrayList<String> placeList = new ArrayList<>();
            for (int i = 0; i < placeArray.length(); i++) {
                placeList.add(placeArray.getString(i));
            }
            StringBuilder sb = new StringBuilder();
            for (String place : placeList) {
                sb.append(place).append(", ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2);
            }
            selectedPlace = sb.toString();
            days = tripPlan.getInt("days");

            // TODO: get activities of the plan
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("TAG", "Error extracting details from plan: " + e.getMessage());
        }
    }


    private void setupTripInfo() {
        tripName = days + (days > 1 ? " days" : " day") + " trip to " + selectedPlace;
        TextView tripTo = findViewById(R.id.textViewSelectedPlace);
        tripTo.setText(tripName);
        updateDayAndNightText();
    }

    private void setupCloseButton() {
        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view -> {
            Intent intent = new Intent(EditPlanActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("select_navigation_plan", true);
            startActivity(intent);
        });
    }

    private void setupPlanSettingsLauncher() {
        planSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handlePlanSettingsResult(result.getData());
                    }
                }
        );
    }

    private void handlePlanSettingsResult(Intent data) {
        if (data.hasExtra("tripName")) {
            String newTripName = data.getStringExtra("tripName");
            TextView tripTo = findViewById(R.id.textViewSelectedPlace);
            tripTo.setText(newTripName != null ? newTripName : tripName);

            int newDays = data.getIntExtra("days", days);
            if (days != newDays) {
                adjustFragmentsForNewDays(newDays);
                days = newDays;
                updateDayAndNightText();
                refreshTabsAndFragments();
                loadFragment(fragments.get(0));
            }
        }
    }

    private void adjustFragmentsForNewDays(int newDays) {
        if (days < newDays) {
            addTabsAndFragments(days, newDays);
        } else {
            removeTabsAndFragments(days, newDays);
        }
    }

    private void setupSettingsButton() {
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(EditPlanActivity.this, PlanSettingActivity.class);
            intent.putExtra("tripName", tripName);
            intent.putExtra("days", days);
            planSettingsLauncher.launch(intent);
        });
    }

    private void initializeFragmentsAndTabs() {
        fragmentManager = getSupportFragmentManager();
        TabLayout tabLayout = binding.tabLayout;
        fragments.clear();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        addOverviewFragment(tabLayout, transaction);
        addDayFragments(tabLayout, transaction);

        transaction.commitNow();
    }

    private void addOverviewFragment(TabLayout tabLayout, FragmentTransaction transaction) {
        tabLayout.addTab(tabLayout.newTab().setText("Overview"));
        Fragment overviewFragment = PlanFragment.newInstance(PlanFragment.OVERVIEW, -1);
        transaction.add(R.id.fragmentContainerView, overviewFragment, "fragment_overview");
        fragments.add(overviewFragment);
    }

    private void addDayFragments(TabLayout tabLayout, FragmentTransaction transaction) {
        for (int i = 0; i < days; i++) {
            String tabTitle = "Day " + (i + 1);
            tabLayout.addTab(tabLayout.newTab().setText(tabTitle));
            Fragment dayFragment = PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY, i);
            transaction.add(R.id.fragmentContainerView, dayFragment, "fragment_day_" + i);
            transaction.hide(dayFragment);
            fragments.add(dayFragment);
        }
    }

    private void setupTabSelectedListener() {
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadFragment(fragments.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: Handle unselected tab
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Handle reselected tab
            }
        });
    }

    private void addTabsAndFragments(int oldDays, int newDays) {
        TabLayout tabLayout = binding.tabLayout;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = oldDays; i < newDays; i++) {
            String tabTitle = "Day " + (i + 1);
            tabLayout.addTab(tabLayout.newTab().setText(tabTitle));
            Fragment dayFragment = PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY, i);
            transaction.add(R.id.fragmentContainerView, dayFragment, "fragment_day_" + i);
            transaction.hide(dayFragment);
            fragments.add(dayFragment);
        }
        transaction.commitNow();
    }

    private void removeTabsAndFragments(int oldDays, int newDays) {
        TabLayout tabLayout = binding.tabLayout;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = oldDays - 1; i >= newDays; i--) {
            tabLayout.removeTabAt(i + 1);
            Fragment fragment = fragments.remove(i + 1);
            transaction.remove(fragment);
        }
        transaction.commitNow();
    }

    private void refreshTabsAndFragments() {
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.removeAllTabs();
        for (int i = 0; i < fragments.size(); i++) {
            String tabTitle = (i == 0) ? "Overview" : "Day " + i;
            tabLayout.addTab(tabLayout.newTab().setText(tabTitle));
        }
    }

    private void updateDayAndNightText() {
        String dayAndNightText;
        if (days == 1) {
            dayAndNightText = "1 day";
        } else {
            dayAndNightText = days + " days and " + (days - 1) + " nights";
        }
        TextView daysAndNight = findViewById(R.id.textViewDaysAndNights);
        daysAndNight.setText(dayAndNightText);
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
}
