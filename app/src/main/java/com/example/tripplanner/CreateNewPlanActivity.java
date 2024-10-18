package com.example.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripplanner.adapter.AutocompleteAdapter;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.utils.PlacesClientProvider;
import com.example.tripplanner.utils.GptApiClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateNewPlanActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private ListView listViewAutocomplete;
    private AutocompleteAdapter adapter;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_plan);

        placesClient = PlacesClientProvider.getPlacesClient();

        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editTextMessage = findViewById(R.id.editTextMessage);
        listViewAutocomplete = findViewById(R.id.listViewAutocomplete);

        adapter = new AutocompleteAdapter(this, new ArrayList<>());
        listViewAutocomplete.setAdapter(adapter);
        listViewAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = adapter.getItem(position);
            String placeId = prediction.getPlaceId();

            fetchPlaceFromPlaceId(placeId);
        });

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    PlacesClientProvider.performAutocomplete(s.toString(), placesClient, adapter);
                    listViewAutocomplete.setVisibility(View.VISIBLE);
                } else {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    listViewAutocomplete.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button gptPlanButton = findViewById(R.id.gptPlanButton);
        gptPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editTextMessage.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    GptApiClient.getTripPlan("Melbourne", new GptApiClient.GptApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                // Parse and display the response
                                Toast.makeText(CreateNewPlanActivity.this, "GPT Response: " + response, Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(CreateNewPlanActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } else {
                    Toast.makeText(CreateNewPlanActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void fetchPlaceFromPlaceId(String placeId) {
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
            handlePlaceSelection(place);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Toast.makeText(CreateNewPlanActivity.this, "Place not found: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handlePlaceSelection(Place place) {
        editTextMessage.setText(place.getName());

        String country = null;
        if (place.getAddressComponents() != null) {
            for (AddressComponent component : place.getAddressComponents().asList()) {
                if (component.getTypes().contains("country")) {
                    country = component.getName();
                    break;
                }
            }
        }

        Intent intent = new Intent(CreateNewPlanActivity.this, PlanDurationActivity.class);

        Location loc = new Location(
                place.getId(),
                place.getName(),
                place.getPlaceTypes().get(0),
                place.getLatLng().latitude,
                place.getLatLng().longitude,
                country
        );

        Log.d("new_location", loc.toString());

        intent.putExtra("selectedPlace", loc);

        startActivity(intent);
    }
}
