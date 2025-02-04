package com.example.tripplanner.utils;

import com.example.tripplanner.entity.Weather;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WeatherAPIClient {
    private static final String API_KEY = "5ca81bc91f299a36d82575c6b9b1445e";
    private HashMap<Integer, Weather> res = new HashMap<Integer, Weather>();

    public static Map<Integer, Weather> getWeatherForecast(String name, double lat, double lon, int startDateIndex, int endDateIndex) {
        // make get request
        String urlString = "https://api.openweathermap.org/data/2.5/forecast/daily?lat=" + lat + "&lon=" +
            lon + "&cnt=" + endDateIndex + "&appid=" + API_KEY + "&units=metric";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(urlString)
            .build();

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return null;
            }

            String jsonData = response.body().string();
            JSONObject data = new JSONObject(jsonData);
            JSONArray forecasts = data.getJSONArray("list");

            HashMap<Integer, Weather> res = new HashMap<>();
            int forecastCount = Math.min(endDateIndex, forecasts.length() - 1);

            for (int i = startDateIndex; i <= forecastCount; i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                JSONObject temp = forecast.getJSONObject("temp");
                double maxTemp = temp.getDouble("max");
                double minTemp = temp.getDouble("min");

                JSONArray weatherArray = forecast.getJSONArray("weather");
                JSONObject weatherData = weatherArray.getJSONObject(0);
                String description = weatherData.getString("description");
                String icon = weatherData.getString("icon");

                Weather weather = new Weather(name, minTemp, maxTemp, description, icon);
                res.put(i, weather);
            }
            return res;

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

}
