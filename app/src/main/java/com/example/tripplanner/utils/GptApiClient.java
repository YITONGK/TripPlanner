package com.example.tripplanner.utils;

import android.util.Log;

import com.example.tripplanner.BuildConfig;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Location;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.squareup.okhttp.Call;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GptApiClient {
    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = BuildConfig.GPT_API_KEY;
    private static final String DEFAULT_PROMPT = "Please plan the trip to ";
    private static final String REPLAN_PROMPT = "Re-plan the trip due to bad weather conditions. ";
    private static final String RECOMMENDATION_PROMPT = "Generate a trip plan based on the following details:\n"+
        "- Destination\n" +
        "- Weather Forecast\n" +
        "- User Preferences\n\n" +
        "The response should be in the following JSON format:\n" +
        "{\"activityItem\": [{\"name\": \"Activity Name\",\n" +
        "      \"startTime\": \"YYYY-MM-DD HH:MM\",\n" +
        "      \"endTime\": \"YYYY-MM-DD HH:MM\",\n" +
        "      \"locationName\": \"Location Name\",\n" +
        "      \"notes\": \"Notes\"}]}\n\n" +
        "Guidelines:\n"+
            "- Please ensure that your response is a valid JSON format. \n"+
            "- Please ensure that the returned data is wrapped in {}. \n" +
            "- Please do not output any prompt words or markdown code format such as ```json. \n";

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
            jsonObject.put("response_format", new JSONObject()
                .put("type", "json_object"));
            jsonObject.put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", prompt))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage)));
            jsonObject.put("max_tokens", 150);
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
                    Log.d("PLAN", "[getChatCompletion] success");
                    String result = cleanAndValidateJson(extractContentFromResponse(response.body().string())+"]}");
                    if (result == null){
                        callback.onFailure("Invalid response");
                    }else {
                        callback.onSuccess(result);
                    }

                } else {
                    Log.d("PLAN", "[getChatCompletion] failed: "+response.message());
                    callback.onFailure(response.message());
                }
            }
        });
    }

    public static String extractContentFromResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray choicesArray = jsonObject.getJSONArray("choices");
            if (choicesArray.length() > 0) {
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                JSONObject messageObject = firstChoice.getJSONObject("message");
                return messageObject.getString("content");
            }
        } catch (JSONException e) {
            Log.d("GPT", "Error in parsing error in extractContentFromResponse()");
        }
        return null;
    }

    public static void recommendTripPlan(String destination, String weatherForecast, String userPreferences, GptApiCallback callback) {
    
        String userMessage = 
            "- Destination: " + destination + "\n" +
            "- Weather Forecast: " + weatherForecast + "\n" +
            "- User Preferences: " + userPreferences + "\n\n";

        getChatCompletion(RECOMMENDATION_PROMPT, userMessage, callback);
    }

    public static String cleanAndValidateJson(String response) {
        String jsonContent = extractJsonContent(response);
        if (jsonContent == null) {
            return null;
        }

        // Attempt to parse the JSON to check if it's valid
        try {
            new JSONObject(jsonContent);
            return jsonContent; // Return if valid
        } catch (JSONException e) {
            // Log the error and retry if needed
            Log.d("GptApiClient", "Invalid json: " + jsonContent);
            Log.e("GptApiClient", "Invalid JSON, retrying...");
            // Implement retry logic here if necessary
        }

        return null;
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

    public static void rePlanTrip(String tripData, GptApiCallback callback) {
        getChatCompletion(REPLAN_PROMPT, tripData, callback);
    }

    public static List<ActivityItem> parseActivityItemsFromJson(String jsonResponse, PlacesClient placesClient) {
        List<ActivityItem> activityItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray activityArray = jsonObject.getJSONArray("activityItem");

            for (int i = 0; i < activityArray.length(); i++) {
                JSONObject activityObject = activityArray.getJSONObject(i);

                String name = activityObject.getString("name");
                String startTimeStr = activityObject.getString("startTime");
                String endTimeStr = activityObject.getString("endTime");
                String locationName = activityObject.getString("locationName");
                String notes = activityObject.optString("notes", "");

                // Convert startTime and endTime from String to Timestamp
                Timestamp startTime = ActivityItem.convertStringToTimestamp(startTimeStr.replace("T", " "));
                Timestamp endTime = ActivityItem.convertStringToTimestamp(endTimeStr.replace("T", " "));

                ActivityItem activityItem = new ActivityItem(name);
                activityItem.setStartTime(startTime);
                activityItem.setEndTime(endTime);
                activityItem.setNotes(notes);

                // Fetch location details from Google Places API
                fetchLocationFromGooglePlaces(locationName, placesClient, new OnPlaceFetchedListener() {
                    @Override
                    public void onPlaceFetched(Location location) {
                        activityItem.setLocation(location);
                        activityItems.add(activityItem);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing error
        }

        return activityItems;
    }

    private static void fetchLocationFromGooglePlaces(String locationName, PlacesClient placesClient, OnPlaceFetchedListener listener) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(locationName, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
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
                Location location = new Location(
                        place.getId(),
                        place.getName(),
                        place.getPlaceTypes().get(0).toString(),
                        place.getLatLng().latitude,
                        place.getLatLng().longitude,
                        country
                );
                listener.onPlaceFetched(location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("PlanFragment", "Error in finding location generated by gpt.");
            }
        });
    }

    public interface OnPlaceFetchedListener {
        void onPlaceFetched(Location location);
    }

   
       
}