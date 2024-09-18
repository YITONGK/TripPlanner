package com.example.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;

public class CreateNewPlanActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private ListView listViewAutocomplete;
    private AutocompleteAdapter adapter;
    private PlacesClient placesClient;
    final String apiKey = BuildConfig.PLACES_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (apiKey.equals("")) {
            Toast.makeText(this, "API key error", Toast.LENGTH_LONG).show();
            return;
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_plan);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        placesClient = Places.createClient(this);

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
            editTextMessage.setText(prediction.getFullText(null));

            // Create an intent to start EditPlanActivity
            Intent intent = new Intent(CreateNewPlanActivity.this, EditPlanActivity.class);
            // Put the selected item's description as an extra in the intent
            intent.putExtra("selectedPlace", prediction.getPrimaryText(null).toString());
            startActivity(intent);
        });


        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    performAutocomplete(s.toString());
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
    }

    private void performAutocomplete(String query) {
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
                Toast.makeText(CreateNewPlanActivity.this, "Error fetching autocomplete predictions: " + apiException.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
