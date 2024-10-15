package com.example.tripplanner.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tripplanner.adapter.WeatherAdapter;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.BuildConfig;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.ActivityItemAdapter;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.PlacesClientProvider;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.example.tripplanner.adapter.AutocompleteAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.tripplanner.entity.Weather;
import com.example.tripplanner.utils.WeatherAPIClient;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class PlanFragment extends Fragment implements OnMapReadyCallback {

    public static final int OVERVIEW = 0;
    public static final int PLAN_SPECIFIC_DAY = 1;
    static final String LAYOUT_TYPE = "type";
    private int layout = OVERVIEW;
    private Timestamp startDate;
    private int dayIndex = -1;
    private GoogleMap mMap;

    public static List<Location> locationList;
    private String startDay;
    private int lastingDays;

    private PlacesClient placesClient;
    private AutocompleteAdapter autocompleteAdapter;
    final String apiKey = BuildConfig.PLACES_API_KEY;

    private PlanViewModel viewModel;
    private Trip trip;

    // For specific day plan
    private TextView addActivityLocation;
    private ListView activityLocationList;
    private ArrayList<ActivityItem> activityItemArray;
    private ActivityItemAdapter adapter;
    private AtomicReference<Location> activityLocation = new AtomicReference<>();


    // For weather forecast
    private ArrayList<Map<Integer, Weather>> allWeatherData = new ArrayList<>();
    private WeatherAdapter weatherAdapter;
//    private LinearLayout weatherForecastContainer;
    private WeatherAPIClient weatherAPIClient;

    public interface OnPlaceFetchedListener {
        void onPlaceFetched(Location location);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placesClient = PlacesClientProvider.getPlacesClient();

        // Get ViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(PlanViewModel.class);

        // Observe tripLiveData
        viewModel.getTripLiveData().observe(this, new Observer<Trip>() {
            @Override
            public void onChanged(Trip trip) {
                if (trip != null) {
                    PlanFragment.this.trip = trip;
                    PlanFragment.this.locationList = trip.getLocations();
                }
            }
        });

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE, OVERVIEW);
            if (this.layout == PLAN_SPECIFIC_DAY) {
                this.dayIndex = getArguments().getInt("dayIndex", -1);
                long startDateMillis = getArguments().getLong("startDateMillis");
                this.startDate = new Timestamp(new Date(startDateMillis));
            }
        }
    }

    public static PlanFragment newInstance(int layoutType, Timestamp startDate, int dayIndex) {
        PlanFragment fragment = new PlanFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layoutType);
        bundle.putInt("dayIndex", dayIndex);
        bundle.putLong("startDateMillis", startDate.toDate().getTime());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
            weatherAPIClient = new WeatherAPIClient();

            fetchAndDisplayWeatherData(rootView);
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
                    // Update in ViewModel and save
                    viewModel.addActivity(dayIndex, activityItem);
                    adapter.notifyDataSetChanged();
                    viewModel.saveTripToDatabase();
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

        final int[] startHour = new int[1];
        final int[] startMinute = new int[1];
        final int[] endHour = new int[1];
        final int[] endMinute = new int[1];
        final boolean[] isStartTimeSelected = {false};
        final boolean[] isEndTimeSelected = {false};

        activityLocation = new AtomicReference<>();

        if (activityItem.getLocation() != null) {
            activityLocation.set(activityItem.getLocation());
            inputLocation.setText(activityItem.getLocationString());
        }

        if (activityItem.getStartTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activityItem.getStartTime().toDate());
            startHour[0] = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute[0] = calendar.get(Calendar.MINUTE);
            startTime.setText(String.format("%02d:%02d", startHour[0], startMinute[0]));
        }

        if (activityItem.getEndTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(activityItem.getEndTime().toDate());
            endHour[0] = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute[0] = calendar.get(Calendar.MINUTE);
            endTime.setText(String.format("%02d:%02d", endHour[0], endMinute[0]));
        }
        inputLocation.setText(activityItem.getLocationString());
        inputNotes.setText(activityItem.getNotes());

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = startHour[0] != 0 ? startHour[0] : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = startMinute[0] != 0 ? startMinute[0] : Calendar.getInstance().get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour[0] = hourOfDay;
                        startMinute[0] = minute;
                        startTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                        isStartTimeSelected[0] = true;
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hour = endHour[0] != 0 ? endHour[0] : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = endMinute[0] != 0 ? endMinute[0] : Calendar.getInstance().get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endHour[0] = hourOfDay;
                        endMinute[0] = minute;
                        endTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                        isEndTimeSelected[0] = true;
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

        autocompleteListView.setOnItemClickListener((parent, view1, pos, id) -> {
            AutocompletePrediction item = autocompleteAdapter.getItem(pos);
            String placeId = item.getPlaceId();
            fetchPlaceFromPlaceId(placeId, inputLocation, autocompleteListView, new OnPlaceFetchedListener() {
                @Override
                public void onPlaceFetched(Location location) {
                    // Set the activityLocation to the fetched location
                    activityLocation.set(location);
                }
            });
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (isStartTimeSelected[0]) {
                    Timestamp startTimestamp = buildTimestamp(startDate, dayIndex, startHour[0], startMinute[0]);
                    activityItem.setStartTime(startTimestamp);
                }
                if (isEndTimeSelected[0]) {
                    Timestamp endTimestamp = buildTimestamp(startDate, dayIndex, endHour[0], endMinute[0]);
                    activityItem.setEndTime(endTimestamp);
                }
                if (activityLocation.get() != null) {
                    activityItem.setLocation(activityLocation.get());
                } else if (activityItem.getLocation() == null && !inputLocation.getText().toString().isEmpty()) {
                    // Handle manual input
                    activityItem.setLocation(new Location("", inputLocation.getText().toString(), "", 0, 0, ""));
                }
                activityItem.setNotes(inputNotes.getText().toString());
                adapter.notifyDataSetChanged();

                // Update in ViewModel and save
                viewModel.updateActivity(dayIndex, position, activityItem);
                viewModel.saveTripToDatabase();
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
                                // Update in ViewModel and save
                                viewModel.removeActivity(dayIndex, position);
                                adapter.notifyDataSetChanged();
                                viewModel.saveTripToDatabase();

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

    private void fetchPlaceFromPlaceId(String placeId, EditText inputLocation, ListView autocompleteListView, OnPlaceFetchedListener listener) {
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
            if (place != null) {
                Location loc = new Location(
                        place.getId(),
                        place.getName(),
                        place.getPlaceTypes().get(0),
                        place.getLatLng().latitude,
                        place.getLatLng().longitude,
                        country
                );
                inputLocation.setText(place.getName());
                autocompleteListView.setVisibility(View.GONE);
                listener.onPlaceFetched(loc);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Toast.makeText(getContext(), "Place not found: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchAndDisplayWeatherData(View rootView) {
        Log.d("Getting weather", "start");

        // bind the adapter
        RecyclerView recyclerView = rootView.findViewById(R.id.weatherRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        weatherAdapter = new WeatherAdapter(rootView.getContext(), allWeatherData);
        recyclerView.setAdapter(weatherAdapter);

        if (locationList == null) {
            Log.d("location list null", "null");
            return;
        }

        // request weather data for all locations in the trip
        for (Location location : locationList) {
            Log.d("Getting_weather", location.getName());
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            int startDateIndex = 1;
            int endDateIndex = startDateIndex + lastingDays;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                Map<Integer, Weather> weatherData = weatherAPIClient.getWeatherForecast(location.getName(), latitude, longitude, startDateIndex, endDateIndex);
                Log.d("Getting weather", weatherData.toString());
                handler.post(() -> {
                    if (!isAdded()) {
                        // Fragment is not attached to the activity anymore, so we can't proceed.
                        return;
                    }
                    if (weatherData != null && !weatherData.isEmpty()) {
                        allWeatherData.add(weatherData);
                        Log.d("weather data", allWeatherData.toString());
                        // Notify the adapter that the data has changed
                        weatherAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                    }
                    executor.shutdown(); // Shut down the executor
                });
            });
        }
    }

    private Timestamp buildTimestamp(Timestamp startDate, int dayIndex, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate.toDate());
        calendar.add(Calendar.DAY_OF_YEAR, dayIndex);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        return new Timestamp(date);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void setLastingDays(int lastingDays) {
        this.lastingDays = lastingDays;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    public void setStartDate(String startDay) {
        this.startDay = startDay;
    }

}
