package com.example.tripplanner.entity;

import com.google.android.libraries.places.api.net.PlacesClient;

public class PlacesClientProvider {
    private static PlacesClient placesClient;

    public static void initialize(PlacesClient client) {
        placesClient = client;
    }

    public static PlacesClient getPlacesClient() {
        return placesClient;
    }
}
