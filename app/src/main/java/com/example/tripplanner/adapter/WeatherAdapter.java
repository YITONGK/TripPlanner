package com.example.tripplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripplanner.R;
import com.example.tripplanner.entity.Weather;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Map<Integer, Weather>> allWeatherData;

    public WeatherAdapter(Context context, ArrayList<Map<Integer, Weather>> allWeatherData) {
        this.context = context;
        this.allWeatherData = allWeatherData;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View weatherItemView = inflater.inflate(R.layout.weather_single_location, parent, false);
        return new WeatherAdapter.ViewHolder(weatherItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        Map<Integer, Weather> weatherData = allWeatherData.get(position);
        // Clear any previously added views in the forecast container
        holder.forecastLinearLayout.removeAllViews();

        holder.locationName.setText(weatherData.get(1).getLocationName());

        for (int i = 1; i <= weatherData.size(); i++) {
            Weather weather = weatherData.get(i);
            // Inflate a single forecast item view
            View forecastView = LayoutInflater.from(context).inflate(R.layout.weather_forecast_item, holder.forecastLinearLayout, false);

            // Populate the forecast view with weather data
            TextView weatherDate = forecastView.findViewById(R.id.weatherDate);
//            TextView weatherDescription = forecastView.findViewById(R.id.weatherDescription);
            TextView weatherTemperature = forecastView.findViewById(R.id.weatherTemperature);
            ImageView weatherIcon = forecastView.findViewById(R.id.weatherIcon);

            // Set views
            String dateString = getDateForIndex(i);
            weatherDate.setText(dateString);
//            weatherDescription.setText(weather.getDescription());
            weatherTemperature.setText(String.format(Locale.getDefault(), "%.1f°C - %.1f°C", weather.getMinTemp(), weather.getMaxTemp()));

            String iconName = "icon_" + weather.getIcon().substring(0,2);
            weatherIcon.setImageResource(context.getResources().getIdentifier(iconName, "drawable", context.getPackageName()));

            // Add the populated forecast view to the LinearLayout inside the card
            holder.forecastLinearLayout.addView(forecastView);
        }
    }

    @Override
    public int getItemCount() {
        return allWeatherData.size();
    }

    private String getDateForIndex(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, index);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView locationName;
        private LinearLayout forecastLinearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            forecastLinearLayout = itemView.findViewById(R.id.forecastLinearLayout);  // The container for weather forecasts
        }
    }
}
