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
    //TODO: need a data structure to store extracted weather data
    private HashMap<Integer, Weather> res = new HashMap<Integer, Weather>();

    public Map<Integer, Weather> getWeatherForecast(double lat, double lon, int startDateIndex, int endDateIndex) {
        // make get request
        String urlString = "https://api.openweathermap.org/data/2.5/forecast/climate?lat=" + lat + "&lon=" +
            lon + "&cnt=" + endDateIndex + "&appid=" + API_KEY + "&units=metric";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(urlString)
            .build();

        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JSONObject data = new JSONObject(jsonData);
            JSONArray forecasts = data.getJSONArray("list");
            //TODO: get the forecasts for specific days
            for (int i=startDateIndex; i<=endDateIndex; i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                JSONObject temp = forecast.getJSONObject("temp");
                double maxTemp = temp.getDouble("max");
                double minTemp = temp.getDouble("min");
                JSONObject weatherData = forecast.getJSONObject("weather");
                String description = weatherData.getString("description");
                String icon = weatherData.getString("icon");

                Weather weather = new Weather(minTemp, maxTemp, description, icon);
                res.put(i, weather);
            }
            return res;

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

    }

}
