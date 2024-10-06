package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Trip;
import com.example.tripplanner.fragment.HomeFragment;
import com.example.tripplanner.utils.WeatherTripPlanner;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.tripplanner.databinding.ActivityMainBinding;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private GoogleMap mMap;

    private WeatherTripPlanner weatherTripPlanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fragment plan_layout = HomeFragment.newInstance(HomeFragment.PLAN);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, plan_layout)
                .addToBackStack(null)
                .commit();

        // Navigate to profile page
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Setting for Navigation Bar
        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected (@NonNull MenuItem item){

                int id = item.getItemId();

                // To show plan layout demonstration
                if (id == R.id.navigation_plan) {
                    Fragment plan_layout = HomeFragment.newInstance(HomeFragment.PLAN);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, plan_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                // To create new plan activity
                if (id == R.id.navigation_add) {
//                    Intent intent = new Intent(MainActivity.this, PlanDurationActivity.class);
//                    intent.putExtra("selectedPlace", "Sydney");
//                    startActivity(intent);
                    startActivity(new Intent(MainActivity.this, CreateNewPlanActivity.class));

                    return true;
                }
                // To show Location layout demonstration
                else if (id == R.id.navigation_map) {
                    Fragment Location_layout = HomeFragment.newInstance(HomeFragment.LOCATION);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, Location_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Initialize WeatherTripPlanner
        weatherTripPlanner = new WeatherTripPlanner(this);

        // Detect weather and plan trip
        weatherTripPlanner.detectWeatherAndPlanTrip();

//        FirestoreDB firestoreDB = new FirestoreDB();
//        String tripId = "F5jTca4WBQiNeFGalV9i";
//
//        firestoreDB.deleteTripById(tripId, new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                // Handle successful deletion
//                Log.d("PLAN", "Trip successfully deleted.");
//                // Update UI or navigate back
//            }
//        }, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // Handle deletion failure
//                Log.e("PLAN", "Error deleting trip", e);
//            }
//        });
    }

//     @Override
//     public void onSensorChanged(SensorEvent event) {
//         if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
//             currentTemperature = event.values[0];
//             Log.d("SENSOR", "Current temperature: " + currentTemperature);
//         } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
//             currentHumidity = event.values[0];
//             Log.d("SENSOR", "Current humidity: " + currentHumidity);
//         }

//         checkConditionsAndNotify();
//     }

//     @Override
//     public void onAccuracyChanged(Sensor sensor, int accuracy) {
//         // Do something here if sensor accuracy changes.
//     }

//     private void checkConditionsAndNotify() {
//         if (currentTemperature > 30 || currentTemperature < 0 || currentHumidity > 80) {
//             // Notify user and request GPT to re-plan the trip
// //            requestGPTReplan();
// //            Toast.makeText(this, "Temperature or Humidity is too high!", Toast.LENGTH_SHORT).show();
//             Log.d("SENSOR", "Temperature or Humidity is too high!");
//         }
//     }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listeners when the activity is resumed
        weatherTripPlanner.registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listeners when the activity is paused
        weatherTripPlanner.unregisterSensorListeners();
    }



}