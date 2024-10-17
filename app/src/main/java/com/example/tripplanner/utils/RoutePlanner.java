package com.example.tripplanner.utils;

import android.util.Log;

import com.example.tripplanner.adapter.DistanceMatrixCallback;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.DistanceMatrixEntry;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {
    private static final String API_KEY = "AIzaSyB6ERXJUvrKoEbEBTVt4Ofgg_3G3z6tFcQ";
//    private List<DistanceMatrixEntry> distanceMatrix;

    public static void fetchDistanceMatrix(List<ActivityItem> activityItems, String mode, DistanceMatrixCallback callback) {
        List<String> locations = new ArrayList<>();
        for (ActivityItem item : activityItems) {
            if (item.getLocation() != null) {
                locations.add(item.getLocation().getName());
            }
        }

        try {
            String url = buildDirectionsUrl(locations, mode);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e("RoutePlannerUtil", "Error fetching distance matrix: " + e.getMessage());
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        List<DistanceMatrixEntry> distanceMatrix = parseDistanceMatrixResponse(responseData, locations, mode);
                        callback.onSuccess(distanceMatrix);
                    } else {
                        Log.e("RoutePlannerUtil", "Error: " + response.code());
                        callback.onFailure(new IOException("Error: " + response.code()));
                    }
                }
            });
        } catch (Exception e) {
            Log.e("RoutePlannerUtil", "Error building URL: " + e.getMessage());
            callback.onFailure(e);
        }
    }

     private static List<DistanceMatrixEntry> parseDistanceMatrixResponse(String jsonResponse, List<String> locations, String mode) {
        List<DistanceMatrixEntry> entries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray rows = jsonObject.getJSONArray("rows");

            for (int i = 0; i < rows.length(); i++) {
                JSONArray elements = rows.getJSONObject(i).getJSONArray("elements");
                for (int j = 0; j < elements.length(); j++) {
                    JSONObject element = elements.getJSONObject(j);
                    String distance = element.getJSONObject("distance").getString("text");
                    String duration = element.getJSONObject("duration").getString("text");

                    entries.add(new DistanceMatrixEntry(
                            locations.get(i),
                            locations.get(j),
                            mode,
                            distance,
                            duration
                    ));
                }
            }
        } catch (Exception e) {
            Log.e("RoutePlannerUtil", "Error parsing JSON response: " + e.getMessage());
        }
        return entries;
    }


    private static String buildDirectionsUrl(List<String> locations, String mode) throws Exception {
        StringBuilder locationBuilder = new StringBuilder();
        for (String location : locations) {
            if (locationBuilder.length() > 0) {
                locationBuilder.append("|");
            }
            locationBuilder.append(URLEncoder.encode(location, "UTF-8"));
        }

        return "https://maps.googleapis.com/maps/api/distancematrix/json?"
                + "origins=" + locationBuilder.toString()
                + "&destinations=" + locationBuilder.toString()
                + "&mode=" + URLEncoder.encode(mode, "UTF-8")
                + "&key=" + API_KEY;
    }

    public static DistanceMatrixEntry getDistanceMatrixEntry(List<DistanceMatrixEntry> distanceMatrix, String from, String to) {
        for (DistanceMatrixEntry entry : distanceMatrix) {
            if (entry.getFrom().equals(from) && entry.getTo().equals(to)) {
                return entry;
            }
        }
        return null; //  if no matching entry is found
    }

    public static List<ActivityItem> calculateBestRoute(List<DistanceMatrixEntry> distanceMatrix, List<ActivityItem> activityItems) {
        List<ActivityItem> bestRoute = new ArrayList<>();
        if (activityItems.isEmpty() || distanceMatrix.isEmpty()) {
            return bestRoute;
        }

        boolean[] visited = new boolean[activityItems.size()];
        int currentIndex = 0;
        visited[currentIndex] = true;
        bestRoute.add(activityItems.get(currentIndex));

        for (int step = 1; step < activityItems.size(); step++) {
            int nextIndex = -1;
            int minTime = Integer.MAX_VALUE;

            for (int i = 0; i < activityItems.size(); i++) {
                if (!visited[i]) {
                    DistanceMatrixEntry entry = getDistanceMatrixEntry(
                            distanceMatrix,
                        activityItems.get(currentIndex).getLocationString(), 
                        activityItems.get(i).getLocationString());

                    if (entry != null) {
                        int duration = parseDuration(entry.getDuration());
                        if (duration < minTime) {
                            minTime = duration;
                            nextIndex = i;
                        }
                    }
                }
            }

            if (nextIndex != -1) {
                visited[nextIndex] = true;
                bestRoute.add(activityItems.get(nextIndex));
                currentIndex = nextIndex;
            }
        }

        return bestRoute;
    }

    private static int parseDuration(String duration) {
        // Assuming duration is in the format "X mins" or "X hours Y mins"
        String[] parts = duration.split(" ");
        int totalMinutes = 0;
        for (int i = 0; i < parts.length; i += 2) {
            int value = Integer.parseInt(parts[i]);
            if (parts[i + 1].startsWith("hour")) {
                totalMinutes += value * 60;
            } else if (parts[i + 1].startsWith("min")) {
                totalMinutes += value;
            }
        }
        return totalMinutes;
    }

}
