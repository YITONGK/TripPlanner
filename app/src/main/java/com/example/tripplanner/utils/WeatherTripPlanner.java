package com.example.tripplanner.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tripplanner.BuildConfig;
import com.example.tripplanner.CreateNewPlanActivity;
import com.example.tripplanner.PlanConfirmationActivity;
import com.example.tripplanner.PlanSuggestionDialogFragment;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherTripPlanner implements SensorEventListener {

    private Activity activity;
    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private boolean isTemperatureSensorAvailable;
    private boolean isHumiditySensorAvailable;
    private float ambientTemperature;
    private float relativeHumidity;

    // Initialize ExecutorService and Handler
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String WEATHER_API_KEY = "eacef29cbb687c3d27f59dd48cdbd5fb";

    public WeatherTripPlanner(Activity activity) {
        this.activity = activity;
        initializeSensors();
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

            isTemperatureSensorAvailable = temperatureSensor != null;
            isHumiditySensorAvailable = humiditySensor != null;

            Log.d("SENSOR", "Sensors available: " + isTemperatureSensorAvailable + ", " + isHumiditySensorAvailable);
        } else {
            isTemperatureSensorAvailable = false;
            isHumiditySensorAvailable = false;
        }
    }

    public void detectWeatherAndPlanTrip() {
        if (isTemperatureSensorAvailable && isHumiditySensorAvailable) {
            // Use sensor data
            registerSensorListeners();
//            showSuggestedPlan("Here is testing");
        } else {
            // Fallback to weather API
            Log.d("SENSOR", "Sensors unavailable. Fetching data from Weather API.");
            Location location = getCurrentLocation();
            if (location != null) {
                fetchWeatherData(location);
                Log.d("SENSOR", "fallback to weather api");
            } else {
                Log.d("SENSOR", "Unable to retrieve current location.");
            }
        }
    }

    public void registerSensorListeners() {
        Log.d("SENSOR", "Registering sensor listeners.");
        if (isTemperatureSensorAvailable) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isHumiditySensorAvailable) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterSensorListeners() {
        sensorManager.unregisterListener(this);
        Log.d("SENSOR", "Unregistering sensor listeners.");
    }

    private Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return null;
        }
//        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (location != null) {
//            return location;
//        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d("SENSOR", "location: " + location);
        return location;
    }

    private void fetchWeatherData(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        executorService.execute(() -> {
            String apiUrl = String.format(Locale.US,
                 "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                 latitude, longitude, WEATHER_API_KEY);


            JSONObject weatherResponse = makeNetworkCall(apiUrl);

            mainHandler.post(() -> {
                if (weatherResponse != null) {
//                    decideNotification(weatherResponse);
                    Log.d("SENSOR", "Weather data: " + weatherResponse);
                } else {
//                    notifyUser("Failed to retrieve weather data.");
                    Log.d("SENSOR", "Failed to retrieve weather data.");
                }
            });
        });
    }

    private void decideNotification(JSONObject weatherData) {
        try {
            JSONObject main = weatherData.getJSONObject("main");
            double temperature = main.getDouble("temp");
            int humidity = main.getInt("humidity");
            String weatherCondition = weatherData.getJSONArray("weather")
                    .getJSONObject(0).getString("main");

            boolean needReplan = weatherCondition.equalsIgnoreCase("Rain") ||
                    temperature > 35 || temperature < 5;

            if (needReplan) {
//                requestGptReplan(weatherCondition, temperature, humidity);
                Log.d("SENSOR", "needReplan");
            } else {
                Log.d("SENSOR", "Weather is good for your trip!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            notifyUser("Error processing weather data.");
        }
    }

    private void decideNotificationWithSensorData(float temperature, float humidity) {
        boolean needReplan = temperature > 35 || humidity < 5;

        if (needReplan) {
            showSuggestedPlan("Here is testing");
//            requestGptReplan("Current conditions", temperature, humidity);
            Log.d("SENSOR", "Need to re-plan");
        } else {
            Log.d("SENSOR", "Real-time environment is good for your trip!");
        }

        // Unregister sensors after use
        unregisterSensorListeners();
    }

    private void requestGptReplan(String condition, double temp, double humidity) {
        String prompt = String.format(Locale.US,
                "Given that the weather is %s with a temperature of %.1f°C and humidity of %.1f%%, suggest a new travel plan.",
                condition, temp, humidity);

        executorService.execute(() -> {
            // Perform the GPT API call in the background thread
            GptApiClient.rePlanTrip(prompt, new GptApiClient.GptApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("PLAN", "GPT Response: " + response);
                    showSuggestedPlan(response);
                }

                @Override
                public void onFailure(String error) {
                    Log.d("PLAN", "Failed to retrieve a new plan from GPT: " + error);
                }
            });
        });
    }

//    private void showSuggestedPlan(String plan) {
//        Intent intent = new Intent(activity, PlanConfirmationActivity.class);
//        intent.putExtra("suggestedPlan", plan);
//        activity.startActivityForResult(intent, 2);
//    }

    private void showSuggestedPlan(String plan) {
        String message = "Due to adverse weather conditions, we suggest the following plan:";
        Log.d("REPLAN", message);
        PlanSuggestionDialogFragment dialogFragment = PlanSuggestionDialogFragment.newInstance(message, plan);
        dialogFragment.show(((AppCompatActivity) activity).getSupportFragmentManager(), "PlanSuggestionDialog");
    }

    private void notifyUser(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    // Placeholder methods for network calls (implement with proper network code)
    private JSONObject makeNetworkCall(String apiUrl) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        try {
            // Synchronous network call
            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // Parse the response body to JSON
                return new JSONObject(responseBody);
            } else {
                // Handle unsuccessful response
                Log.e("NetworkCall", "Request failed: " + response.code() + " - " + response.message());
                return null;
            }
        } catch (IOException e) {
            // Handle network I/O exceptions
            Log.e("NetworkCall", "IOException: " + e.getMessage());
            return null;
        } catch (JSONException e) {
            // Handle JSON parsing exceptions
            Log.e("NetworkCall", "JSONException: " + e.getMessage());
            return null;
        }
    }

    // SensorEventListener methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            ambientTemperature = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            relativeHumidity = event.values[0];
        }


//        if (isTemperatureSensorAvailable && isHumiditySensorAvailable) {
//            decideNotificationWithSensorData(ambientTemperature, relativeHumidity);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }

    protected void onDestroy() {
        executorService.shutdown();
    }


}
