package com.example.tripplanner.entity;

import com.example.tripplanner.adapter.AutocompleteAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

public class PlacesClientProvider {
    private static PlacesClient placesClient;

    public static void initialize(PlacesClient client) {
        placesClient = client;
    }

    public static PlacesClient getPlacesClient() {
        return placesClient;
    }

    public static void performAutocomplete(String query, PlacesClient placesClient, AutocompleteAdapter adapter) {
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
            }
        });
    }
}
