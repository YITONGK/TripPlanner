package com.example.tripplanner.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.BuildConfig;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.ActivityItemAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.example.tripplanner.adapter.AutocompleteAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;


public class PlanFragment extends Fragment implements OnMapReadyCallback {

    public static final int OVERVIEW = 0;
    public static final int PLAN_SPECIFIC_DAY = 1;
    static final String LAYOUT_TYPE = "type";
    private int layout = OVERVIEW;
    private int dayIndex = -1; // To identify the day
    private GoogleMap mMap;

    private PlacesClient placesClient;
    private AutocompleteAdapter autocompleteAdapter;
    final String apiKey = BuildConfig.PLACES_API_KEY;

    private PlanViewModel viewModel;

    // For specific day plan
    private TextView addActivityLocation;
    private ListView activityLocationList;
    private ArrayList<ActivityItem> activityItemArray;
    private ActivityItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), apiKey);
        }
        placesClient = Places.createClient(requireContext());

        // Get ViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(PlanViewModel.class);

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE, OVERVIEW);
            if (this.layout == PLAN_SPECIFIC_DAY) {
                this.dayIndex = getArguments().getInt("dayIndex", -1);
            }
        }
    }

    public static PlanFragment newInstance(int layoutType, int dayIndex) {
        PlanFragment fragment = new PlanFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layoutType);
        bundle.putInt("dayIndex", dayIndex);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        if (this.layout == PLAN_SPECIFIC_DAY) {
            rootView = inflater.inflate(R.layout.plan_specific_day, container, false);

            addActivityLocation = rootView.findViewById(R.id.addActivityLocation);
            activityLocationList = rootView.findViewById(R.id.activityLocationList);

            // Get the activity items list for this day from the ViewModel
            activityItemArray = viewModel.getActivityItemArray(dayIndex);
            adapter = new ActivityItemAdapter(getContext(), activityItemArray);
            activityLocationList.setAdapter(adapter);

            addActivityLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddActivityDialog();
                }
            });

            activityLocationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showEditActivityDialog(position);
                }
            });

        } else {
            rootView = inflater.inflate(R.layout.plan_overview, container, false);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    private void showAddActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add activity");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String activityName = input.getText().toString();
                if (!activityName.isEmpty()) {
                    ActivityItem activityItem = new ActivityItem(activityName);
                    activityItemArray.add(activityItem);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Please enter something", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showEditActivityDialog(int position) {
        ActivityItem activityItem = activityItemArray.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit activity");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_activity, null);
        builder.setView(dialogView);

        EditText startTime = dialogView.findViewById(R.id.startTime);
        EditText endTime = dialogView.findViewById(R.id.endTime);
        EditText inputLocation = dialogView.findViewById(R.id.inputLocation);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);
        ListView autocompleteListView = dialogView.findViewById(R.id.autocompleteListView);

        autocompleteAdapter = new AutocompleteAdapter(getContext(), new ArrayList<>());
        autocompleteListView.setAdapter(autocompleteAdapter);

        startTime.setText(activityItem.getStartTimeString());
        endTime.setText(activityItem.getEndTimeString());
//        inputLocation.setText(activityItem.getLocation().toString());
        inputNotes.setText(activityItem.getNotes());

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });
        inputLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    performAutocomplete(s.toString());
                    autocompleteListView.setVisibility(View.VISIBLE);
                } else {
                    autocompleteAdapter.clear();
                    autocompleteAdapter.notifyDataSetChanged();
                    autocompleteListView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        autocompleteListView.setOnItemClickListener((parent, view, pos, id) -> {
            AutocompletePrediction item = autocompleteAdapter.getItem(pos);
            String placeId = item.getPlaceId();
            fetchPlaceFromPlaceId(placeId, inputLocation, autocompleteListView);
        });


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                activityItem.setStartTime(startTime.getText().toString());
                activityItem.setEndTime(endTime.getText().toString());
                activityItem.setLocation(inputLocation.getText().toString());
                activityItem.setNotes(inputNotes.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Show a confirmation dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Activity")
                        .setMessage("Are you sure you want to delete this activity?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface confirmDialog, int whichButton) {
                                // Remove the activity item and notify the adapter
                                activityItemArray.remove(position);
                                adapter.notifyDataSetChanged();
                                confirmDialog.dismiss();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        builder.show();
    }

    private void performAutocomplete(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            autocompleteAdapter.clear();
            autocompleteAdapter.addAll(response.getAutocompletePredictions());
            autocompleteAdapter.notifyDataSetChanged();
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Toast.makeText(getContext(), "Error fetching autocomplete predictions: " + apiException.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPlaceFromPlaceId(String placeId, EditText inputLocation, ListView autocompleteListView) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            inputLocation.setText(place.getName());
            autocompleteListView.setVisibility(View.GONE);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Toast.makeText(getContext(), "Place not found: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
