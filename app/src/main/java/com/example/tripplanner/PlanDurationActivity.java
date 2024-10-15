package com.example.tripplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tripplanner.adapter.AutocompleteAdapter;
import com.example.tripplanner.adapter.ButtonDecorator;
import com.example.tripplanner.databinding.PlanDurationBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.PlacesClientProvider;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.PlanDurationFragment;
import com.example.tripplanner.utils.OnFragmentInteractionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PlanDurationActivity extends AppCompatActivity
        implements OnFragmentInteractionListener, ButtonDecorator.OnButtonClickListener {
    private PlanDurationBinding binding;
    private List<Location> locationList = new ArrayList<>();
    private ButtonDecorator buttonDecorator;
    private int receivedDays;
    private String receivedStartDate;
    private String receivedEndDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private PlacesClient placesClient;
    private String receivedCalenderStartDate;
    private String receivedCalenderEndDate;
    private int revivedCalendarDays;
    private int currentTabPosition = 0;
    private String tripId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PlanDurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //2. add data into the JSONArray
        Location location = (Location) getIntent().getSerializableExtra("selectedPlace");
        Log.d("passed location", location.toString());
        locationList.add(location);

        // Remove Button (Dynamic add based on the JSON Objects)
        // 3. add the button based on the JSONARRAY
        LinearLayout linearLayout = findViewById(R.id.linear_layout_buttons);
        buttonDecorator = new ButtonDecorator(linearLayout, this);
        buttonDecorator.addButtonsFromList(locationList);

        // Back Button Functions
        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish PlanDuration Activity
                finish();
            }
        });

        placesClient = PlacesClientProvider.getPlacesClient();

        // Button Add Location Functions
        // New location will be add into the JSONArray
        Button addLocationButton = findViewById(R.id.button_add_location);
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set BottomSheetDialog and get the xml view
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PlanDurationActivity.this);
                View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
                bottomSheetDialog.setContentView(bottomSheetView);

                bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        FrameLayout bottomSheet = bottomSheetDialog
                                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            // Set BottomSheet Height as 90%
                            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
                            bottomSheet.setLayoutParams(layoutParams);

                            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            behavior.setSkipCollapsed(true);
                        }
                    }
                });

                SearchView searchViewLocation = bottomSheetView.findViewById(R.id.searchViewLocation);
                ListView listViewAutocomplete = bottomSheetView.findViewById(R.id.listViewAutocomplete);

                AutocompleteAdapter adapter = new AutocompleteAdapter(PlanDurationActivity.this, new ArrayList<>());
                listViewAutocomplete.setAdapter(adapter);

                searchViewLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (!query.isEmpty()) {
                            bottomSheetDialog.dismiss();
                            String selectedPlace = query;
                        } else {
                            Toast.makeText(PlanDurationActivity.this, "Please enter a location", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.length() > 0) {
                            performAutocomplete(newText, placesClient, adapter);
                            listViewAutocomplete.setVisibility(View.VISIBLE);
                        } else {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            listViewAutocomplete.setVisibility(View.GONE);
                        }
                        return true;
                    }
                });

                listViewAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                        AutocompletePrediction prediction = adapter.getItem(position);
                        String fullText = prediction.getFullText(null).toString();
                        searchViewLocation.setQuery(fullText, false);
                        bottomSheetDialog.dismiss();
                        String placeId = prediction.getPlaceId();
                        List<Place.Field> placeFields = Arrays.asList(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS_COMPONENTS,
                                Place.Field.TYPES,
                                Place.Field.LAT_LNG
                        );

                        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                                .build();

                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                            Place place = response.getPlace();
                            String country = null;
                            if (place.getAddressComponents() != null) {
                                for (AddressComponent component : place.getAddressComponents().asList()) {
                                    if (component.getTypes().contains("country")) {
                                        country = component.getName();
                                        break;
                                    }
                                }
                            }
                            Location loc = new Location(
                                    place.getId(),
                                    place.getName(),
                                    place.getPlaceTypes().get(0),
                                    place.getLatLng().latitude,
                                    place.getLatLng().longitude,
                                    country
                            );
                            locationList.add(loc);
                            buttonDecorator.addSingleButton(loc.getName(),locationList.size() - 1);
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                            }
                        });
                    }
                });

                // Display BottomSheetDialog
                bottomSheetDialog.show();
            }
        });

        // TabLayout Functions
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addTab(tabLayout.newTab().setText("Days"));
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"));
        loadFragment(PlanDurationFragment.newInstance(PlanDurationFragment.DAYS));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                Fragment selectedFragment;
                if (tab.getPosition() == 0) {
                    selectedFragment = PlanDurationFragment.newInstance(PlanDurationFragment.DAYS);
                } else {
                    selectedFragment = PlanDurationFragment.newInstance(PlanDurationFragment.CALENDAR);
                }
                loadFragment(selectedFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Button Done
        Button doneButton = findViewById(R.id.button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date parsedDate = new Date();
                        int tripDays = 0;
                        try {
                            if (currentTabPosition == 0) { // Days
                                parsedDate = dateFormat.parse(receivedStartDate);
                                tripDays = receivedDays;
                            } else if (currentTabPosition == 1) { // Calendar
                                parsedDate = dateFormat.parse(receivedCalenderStartDate);
                                tripDays = revivedCalendarDays;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return;
                        }
                        Timestamp startDate = new Timestamp(parsedDate);

                        // Create Trip object
                        Trip trip = new Trip("New Trip", startDate, tripDays, locationList, userId);
                        Log.d("trip_info", trip.toString());

                        // Create FirestoreDB instance and add trip to Firestore
                        FirestoreDB firestore = new FirestoreDB();
                        // firestore.createTrip(userId, trip);
                        firestore.createTrip(userId, trip, new OnSuccessListener<Trip>() {
                            @Override
                            public void onSuccess(Trip updatedTrip) {
                                // Update the original trip with the returned one
                                trip.setId(updatedTrip.getId());
                                tripId = updatedTrip.getId();
                                Intent intent = new Intent(PlanDurationActivity.this, EditPlanActivity.class);
                                intent.putExtra("tripId", tripId);
                                startActivity(intent);
                                // You can perform additional actions with the updated trip if needed
                                Log.d("PLAN", "Trip created with ID: " + trip.getId());
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("PLAN", "Error creating trip", e);
                            }
                        });
                    } else {
                        Log.d("PLAN", "[PlanDurationActivity] No user is signed in.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void DaysInteraction(String data) {
        receivedDays = Integer.parseInt(data);

        Calendar calendar = Calendar.getInstance();
        receivedStartDate = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, receivedDays - 1);
        receivedEndDate = dateFormat.format(calendar.getTime());
    }

    @Override
    public void DatesInteraction(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            receivedCalenderStartDate = null;
            receivedCalenderEndDate = null;
            revivedCalendarDays = 0;
            return;
        }

        receivedCalenderStartDate = startDate;
        receivedCalenderEndDate = endDate;

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.setTime(start);
            endCal.setTime(end);

            int daysDifference = 0;
            while (!startCal.after(endCal)) {
                startCal.add(Calendar.DAY_OF_MONTH, 1);
                daysDifference++;
            }
            revivedCalendarDays = daysDifference;

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void performAutocomplete(String query, PlacesClient placesClient, AutocompleteAdapter adapter) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            adapter.clear();
            adapter.addAll(response.getAutocompletePredictions());
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Toast.makeText(PlanDurationActivity.this,
                        "Error fetching autocomplete predictions: " + apiException.getStatusCode(), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    @Override
    public void onButtonClicked(int index, Button button) {
        if (locationList.size() <= 1) {
            Toast.makeText(this, "At least one location being selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        locationList.remove(index);
        LinearLayout linearLayout = findViewById(R.id.linear_layout_buttons);
        linearLayout.removeView(button);

        updateButtonTags();
    }

    private void updateButtonTags() {
        LinearLayout linearLayout = findViewById(R.id.linear_layout_buttons);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof Button) {
                view.setTag(i);
            }
        }
    }
}
