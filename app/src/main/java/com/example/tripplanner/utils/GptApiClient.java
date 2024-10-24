package com.example.tripplanner.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tripplanner.BuildConfig;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.DistanceMatrixEntry;
import com.example.tripplanner.entity.Location;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.entity.Weather;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GptApiClient {
    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = BuildConfig.GPT_API_KEY;
    private static final String DEFAULT_PROMPT = "Please plan the trip to ";
    private static final String REPLAN_PROMPT =
            "As a trip planner, your task is to schedule activities for one day of a trip based on details provided by the users. You need to adjust the existing plan by considering the current plans, weather conditions, travel times, and the user's preferred schedule. Information such as distance matrices, weather data, and existing activities will be provided like:\n"+
                "- Current Plan\n" +
                "- Weather Forecast\n" +
                "- Distance Matrix\n" +
                "- Context\n\n"+
            "Please respond in the following JSON format:\n" +
            "{\"activityItem\": [{\"name\": \"string\"," +
            "      \"startTime\": \"yyyy-MM-dd HH:mm:ss\"," +
            "      \"endTime\": \"yyyy-MM-dd HH:mm:ss\"," +
            "      \"locationName\": \"Location Name\"," +
            "      \"notes\": \"short string\"}], " +
            "\"reason\": \"string\"}\n"+
            "Guidelines:\n"+
            "- Please ensure that your response is a valid JSON format. \n"+
            "- Please ensure that the location is real and exists. \n"+
            "- Please take the weather forecast into account when making your plans. If it's going to rain, plan indoor activities."+
            "- Please keep the original plans as much as possible rather than generate new one.\n" +
            "- Please consider the trip plans outlined for each day in the context. Make sure there are no duplicate activities or locations in your plan."+
            "- Please provide your reason to briefly explain why you're rescheduling this in no more than one sentence.";

    private static final String RECOMMENDATION_PROMPT = "You are a trip planner generating no more 3 activity items for one day in a trip based on the following details which will be given by users:\n"+
        "- Destination\n" +
        "- Weather Forecast\n" +
        "- User Preferences\n"+
        "- Context\n\n"+
        "Please respond in the following JSON format:\n" +
        "{\"activityItem\": [{\"name\": \"string\"," +
        "      \"startTime\": \"yyyy-MM-dd HH:mm:ss\"," +
        "      \"endTime\": \"yyyy-MM-dd HH:mm:ss\"," +
        "      \"locationName\": \"Location Name\"," +
        "      \"notes\": \"short string\"}]}\n"+
        "Guidelines:\n"+
            "- Please ensure that your response is a valid JSON format. \n"+
            "- Please ensure that the location is real and exists. \n"+
            "- Please take the weather forecast into account when making your plans. If it's going to rain, plan indoor activities."+
            "- Please consider the trip plans outlined for each day in the context. Make sure there are no duplicate activities in your plan.";
//            "- Please ensure that the returned data is wrapped in {}. \n" +
//            "- Please do not output any prompt words or markdown code format such as ```json. \n";

    private static final String SHAKE_PROMPT = "You are a trip planner generating activity items for one day in a trip based on the following details which will be given by users:\n"+
            "- Nearby places\n"+
            "- Environment (Temperature and Humidity) \n" +
            "- User Preferences\n\n"+
            "Please respond in the following JSON format:\n" +
            "{\"tripName\": \"string\", " +
            "activityItem\": [{\"name\": \"string\"," +
            "      \"startTime\": \"yyyy-MM-dd HH:mm:ss\"," +
            "      \"endTime\": \"yyyy-MM-dd HH:mm:ss\"," +
            "      \"locationName\": \"Location Name\"," +
            "      \"notes\": \"short string\"}]}\n"+
            "Guidelines:\n"+
            "- Please ensure that your response is a valid JSON format. \n"+
            "- Please ensure that the location is real and exists. \n" +
            "- Please take the given nearby locations into account." +
            "- Please consider the environmental sensor data (temperature and humidity) when making your plans. If the humidity suggests it's raining, or if the temperature is too high or too low, please plan indoor activities.";


    public interface GptApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void getChatCompletion(String prompt, String userMessage, GptApiCallback callback) {
        Log.d("PLAN", "[getChatCompletion] START");
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-4o-mini");
            jsonObject.put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", prompt))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage)));
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
            Log.d("PLAN", "[getChatCompletion]"+e.getMessage());
            return;
        }

        Log.d("PLAN", "[getChatCompletion] Data ready");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        Log.d("PLAN", "[getChatCompletion] Got response");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e.getMessage());
                Log.d("PLAN", "[getChatCompletion]"+e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("GptApiClient", "[getChatCompletion] success");
                    String responseBody = response.body().string();
                    Log.d("GptApiClient", "Response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");

                        String finishReason = firstChoice.optString("finish_reason", "");
                        if ("length".equals(finishReason)) {
                            // Handle incomplete JSON due to context length
                            callback.onFailure("Response was too long and incomplete.");
                            return;
                        }

//                        if (message.has("refusal")) {
//                            // Handle refusal
//                            String refusalReason = message.getString("refusal");
//                            callback.onFailure("Request refused: " + refusalReason);
//                            return;
//                        }

                        if ("content_filter".equals(finishReason)) {
                            // Handle content filter
                            callback.onFailure("Response was filtered due to restricted content.");
                            return;
                        }

                        if ("stop".equals(finishReason)) {
                            String content = message.getString("content");
                            callback.onSuccess(content);
                        } else {
                            callback.onFailure("Unexpected finish reason: " + finishReason);
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse JSON: " + e.getMessage());
                    }


                } else {
                    Log.d("PLAN", "[getChatCompletion] failed: "+response.message());
                    callback.onFailure(response.message());
                }
            }
        });
    }

    public static void recommendTripPlan(String destination, String weatherForecast, String userPreferences, Trip trip, GptApiCallback callback) {
        String tripData = "Plans in all days:  " + trip.getPlans().toString();
        String userMessage = 
            "- Destination: " + destination + "\n" +
            "- Weather Forecast: " + weatherForecast + "\n" +
            "- User Preferences: " + userPreferences + "\n" +
            "- Context: " + tripData + "\n";

        Log.d("PlanFragment", "User Message: "+userMessage);

        getChatCompletion(RECOMMENDATION_PROMPT, userMessage, callback);
    }

    public static void generateOneDayTripPlan(String sensorData, List<Place> nearbyPlaces, String userPreferences, GptApiCallback callback) {
        StringBuilder placesStringBuilder = new StringBuilder();
        for (Place place : nearbyPlaces) {
            placesStringBuilder.append("Name: ").append(place.getName())
                    .append(", Address: ").append(place.getAddress())
                    .append("\n");
        }
        String placesString = placesStringBuilder.toString();
        Log.d("SENSOR", "Places: " + placesString);

        String userMessage =
                "- Nearby places: " + placesString + "\n"+
                "- Environment: " + sensorData +  "\n" +
                "- User Preferences: " + userPreferences + "\n\n";

        Log.d("PlanFragment", "User Message: "+userMessage);

        getChatCompletion(SHAKE_PROMPT, userMessage, callback);
    }

    public static String cleanJsonResponse(String response){
        response = response.replace("```json", "");
        response = response.replace("```", "");
        return response;
    }

    public static String getStringFromJsonResponse(String response, String key){
        String value = null;
        try{
            response = cleanJsonResponse(response);
            value = new JSONObject(response).optString(key, "New");
        } catch (JSONException e){
            Log.d("GptApiClient", "[getStringFromJsonResponse] Invalid json");
        }
        return value;
    }

    private static String extractJsonContent(String response) {
        // Use regex to extract JSON content from the response
        String jsonPattern = "\\{.*\\}";
        Pattern pattern = Pattern.compile(jsonPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public static void getTripPlan(String tripData, GptApiCallback callback) {
        getChatCompletion(DEFAULT_PROMPT, tripData, callback);
    }

    public static void rePlanTripByWeather(List<ActivityItem> currentPlan, String weather, List<DistanceMatrixEntry> distanceMatrix, Trip trip, GptApiCallback callback) {
        String tripData = "Plans in all days:  " + trip.getPlans().toString();
        String userMessage =
                "- Current Plan: " + currentPlan + "\n" +
                "- Weather Forecast: " + weather + "\n" +
                "- Distance Matrix: " + distanceMatrix + "\n" +
                "- Context: " + tripData + "\n\n";

        Log.d("PlanFragment", "User Message: "+userMessage);

        getChatCompletion(REPLAN_PROMPT, userMessage, callback);
    }

    public static List<ActivityItem> parseActivityItemsFromJson(String destination, String jsonResponse, PlacesClient placesClient, OnActivityItemsParsedListener listener) {
        // Clean the response
        jsonResponse = cleanJsonResponse(jsonResponse);

        List<ActivityItem> activityItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray activityArray = jsonObject.getJSONArray("activityItem");

            if (activityArray.length() == 0) {
                listener.onActivityItemsParsed(activityItems);
                return activityItems;
            }

            AtomicInteger remainingItems = new AtomicInteger(activityArray.length());

            for (int i = 0; i < activityArray.length(); i++) {
                JSONObject activityObject = activityArray.getJSONObject(i);

                String name = activityObject.getString("name");
                String startTimeStr = activityObject.getString("startTime");
                String endTimeStr = activityObject.getString("endTime");
                String locationName = activityObject.getString("locationName");
                String notes = activityObject.optString("notes", "");

                // Convert startTime and endTime from String to Timestamp
                Timestamp startTime = ActivityItem.convertStringToTimestamp(startTimeStr);
                Timestamp endTime = ActivityItem.convertStringToTimestamp(endTimeStr);

                ActivityItem activityItem = new ActivityItem(name);
                activityItem.setStartTime(startTime);
                activityItem.setEndTime(endTime);
                activityItem.setNotes(notes);

                // Fetch location details from Google Places API
                locationName += " ";
                locationName += destination;
                String finalLocationName = locationName;
                fetchLocationFromGooglePlaces(locationName, placesClient, new OnPlaceFetchedListener() {
                    @Override
                    public void onPlaceFetched(Location location) {
                        activityItem.setLocation(location);
                        activityItems.add(activityItem);
                        Log.d("GptApiClient", "Add into activity: " + activityItem);

                        if (remainingItems.decrementAndGet() == 0) {
                            listener.onActivityItemsParsed(activityItems);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d("GptApiClient", "Error in finding location generated by gpt: " + finalLocationName);

                        if (remainingItems.decrementAndGet() == 0) {
                            listener.onActivityItemsParsed(activityItems);
                        }

                    }
                });
            }
        } catch (JSONException e) {
            Log.d("GptApiClient", "Error in parseActivityItemsFromJson: " + e.getMessage());
            listener.onActivityItemsParsed(activityItems);
        }
        return activityItems;
    }

    private static void fetchLocationFromGooglePlaces(String locationName, PlacesClient placesClient, OnPlaceFetchedListener listener) {
        // Create a request for autocomplete predictions based on the location name
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(locationName)
                .build();

        // Use the PlacesClient to find autocomplete predictions
        placesClient.findAutocompletePredictions(predictionsRequest)
                .addOnSuccessListener(response -> {
                    if (!response.getAutocompletePredictions().isEmpty()) {
                        // Get the first prediction from the list
                        AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                        String placeId = prediction.getPlaceId();

                        // Specify the fields to return (ID, Name, LatLng, Address Components, Types)
                        List<Place.Field> placeFields = Arrays.asList(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.LAT_LNG,
                                Place.Field.ADDRESS_COMPONENTS,
                                Place.Field.TYPES
                        );

                        // Create a FetchPlaceRequest using the place ID
                        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                        // Fetch the place details
                        placesClient.fetchPlace(fetchPlaceRequest)
                                .addOnSuccessListener(fetchPlaceResponse -> {
                                    Place place = fetchPlaceResponse.getPlace();
                                    Location location = Location.getInstanceFromPlace(place);

                                    // Notify the listener that the place has been fetched
                                    listener.onPlaceFetched(location);
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("GptApiClient", "Error fetching place details for ID: " + placeId);
                                    listener.onPlaceFetched(null);
                                });
                    } else {
                        Log.d("GptApiClient", "No autocomplete predictions found for: " + locationName);
                        listener.onPlaceFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("GptApiClient", "Error finding autocomplete predictions for: " + locationName);
                    listener.onPlaceFetched(null);
                });
    }


    public interface OnPlaceFetchedListener {
        void onPlaceFetched(Location location);
        void onError(String errorMessage);
    }

    public interface OnActivityItemsParsedListener {
        void onActivityItemsParsed(List<ActivityItem> activityItems);
    }



}