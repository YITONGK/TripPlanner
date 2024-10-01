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
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import com.example.tripplanner.PlanConfirmationActivity;

import org.json.JSONObject;

public class WeatherTripPlanner implements SensorEventListener {

    private Activity activity;
    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private boolean isTemperatureSensorAvailable;
    private boolean isHumiditySensorAvailable;
    private float ambientTemperature;
    private float relativeHumidity;

    private String weatherApiKey = "YOUR_WEATHER_API_KEY";

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
        } else {
            // Fallback to weather API
            Log.d("SENSOR", "Sensors unavailable. Fetching data from Weather API.");
            Location location = getCurrentLocation();
            if (location != null) {
//                fetchWeatherData(location);
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

//    private void fetchWeatherData(Location location) {
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//
//        new AsyncTask<Double, Void, JSONObject>() {
//            @Override
//            protected JSONObject doInBackground(Double... params) {
//                String apiUrl = String.format(
//                        "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
//                        params[0], params[1], weatherApiKey);
//                return makeNetworkCall(apiUrl);
//            }
//
//            @Override
//            protected void onPostExecute(JSONObject weatherResponse) {
//                if (weatherResponse != null) {
//                    decideNotification(weatherResponse);
//                } else {
//                    notifyUser("Failed to retrieve weather data.");
//                }
//            }
//        }.execute(latitude, longitude);
//    }

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
//            requestGptReplan("Current conditions", temperature, humidity);
            Log.d("SENSOR", "Need to re-plan");
        } else {
            Log.d("SENSOR", "Real-time environment is good for your trip!");
        }

        // Unregister sensors after use
        unregisterSensorListeners();
    }

//    private void requestGptReplan(String condition, double temp, double humidity) {
//        String prompt = String.format(
//                "Given that the weather is %s with a temperature of %.1f°C and humidity of %.1f%%, suggest a new travel plan.",
//                condition, temp, humidity);
//
//        new AsyncTask<String, Void, String>() {
//            @Override
//            protected String doInBackground(String... prompts) {
//                return makeGptApiCall(prompts[0]);
//            }
//
//            @Override
//            protected void onPostExecute(String gptResponse) {
//                showSuggestedPlan(gptResponse);
//            }
//        }.execute(prompt);
//    }

    private void showSuggestedPlan(String plan) {
        Intent intent = new Intent(activity, PlanConfirmationActivity.class);
        intent.putExtra("suggestedPlan", plan);
        activity.startActivityForResult(intent, 2);
    }

    private void notifyUser(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    // Placeholder methods for network calls (implement with proper network code)
    private JSONObject makeNetworkCall(String apiUrl) {
        // Implement actual network call and JSON parsing
        return new JSONObject();
    }

    private String makeGptApiCall(String prompt) {
        // Implement actual GPT API call
        return "Sample GPT suggested plan based on the current conditions.";
    }

    // SensorEventListener methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            ambientTemperature = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            relativeHumidity = event.values[0];
        }


        if (isTemperatureSensorAvailable && isHumiditySensorAvailable) {
            decideNotificationWithSensorData(ambientTemperature, relativeHumidity);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }


}
