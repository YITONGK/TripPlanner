package com.example.tripplanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.example.tripplanner.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.tabs.TabLayout;
import com.google.android.gms.maps.model.LatLngBounds;


public class MapActivity extends AppCompatActivity implements RouteListener {
    private GoogleMap googleMap;
    private ActivityMapBinding binding;
    private HashMap<String, List<Double[]>> receivedMap;
    private List<Polyline> polylines = new ArrayList<>();
    private Random random = new Random(123);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpCloseButton();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_overview);

        if (mapFragment != null) {
            mapFragment.getMapAsync(gMap -> {
                googleMap = gMap;
                addMarkersToMap(0);

            });
        }

        initializeFragmentsAndTabs();
    }

    private void addMarkersToMap(int tabPosition) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (receivedMap != null) {
            if (tabPosition == 0) {
                for (String key : receivedMap.keySet()) {
                    List<Double[]> latLngList = receivedMap.get(key);
                    String days = String.valueOf((Integer.parseInt(key)+1));
                    addMarkersForLatLngList(latLngList, "DAY"+days, boundsBuilder);
                    //Add route for all days
                    getRoutePoints(latLngList);
                }
            } else {
                String key = String.valueOf(tabPosition - 1);
                List<Double[]> latLngList = receivedMap.get(key);
                if (latLngList == null || latLngList.isEmpty()) {
                    googleMap.clear();
                    return;
                }
                String days = String.valueOf((Integer.parseInt(key)+1));
                addMarkersForLatLngList(latLngList, "DAY"+days, boundsBuilder);
                // Add route drawing for specific day
                getRoutePoints(latLngList);
            }
            int padding = 100;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
        }
    }

    private void addMarkersForLatLngList(List<Double[]> latLngList, String title, LatLngBounds.Builder boundsBuilder) {
        for (Double[] latLng : latLngList) {
            double latitude = latLng[0];
            double longitude = latLng[1];
            LatLng location = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(title))));
            boundsBuilder.include(location);
        }
    }

    private void setUpCloseButton(){
        ImageView backButton = findViewById(R.id.imageView);
        backButton.setClickable(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeFragmentsAndTabs() {
        Intent intent = getIntent();
        receivedMap = (HashMap<String, List<Double[]>>) intent.getSerializableExtra("daysAndLocationsMap", HashMap.class);

        if (receivedMap == null) {
            Log.e("MapActivity", "receivedMap is null!");
        } else {
            Log.d("MapActivity", "receivedMap has been received.");
        }

        int numDays = getIntent().getIntExtra("numDays", 0);
        TabLayout tabLayout = binding.tabLayoutOverview;

        tabLayout.removeAllTabs();

        tabLayout.addTab(tabLayout.newTab().setText("OVERVIEW"));

        for (int i = 0; i < numDays; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("DAY" + (i + 1)));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                addMarkersToMap(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void getRoutePoints(List<Double[]> latLngList) {
        if (latLngList == null || latLngList.size() < 2) {
            Log.d("MapActivity", "Not enough waypoints to draw route");
            return;
        }

        List<LatLng> waypoints = new ArrayList<>();
        for (Double[] location : latLngList) {
            waypoints.add(new LatLng(location[0], location[1]));
            Log.d("location", "lat: "+location[0]+" lon: "+ location[1]);
        }

        Log.d("MapActivity", "Waypoints: " + waypoints);

        try {
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(MapActivity.this)
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(MapActivity.this)
                    .alternativeRoutes(true)
                    .waypoints(waypoints)
                    .build();
            Log.d("MapActivity", "Executing RouteDrawing");
            routeDrawing.execute();
        } catch (Exception e) {
            Log.e("MapActivity", "Error in RouteDrawing setup", e);
        }
    }

    @Override
    public void onRouteFailure(ErrorHandling e) {
        Log.e("MapActivity", "Route calculation failed: " + e.getMessage());
        Toast.makeText(this, "Failed to calculate route: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRouteStart() {
        Log.d("TAG", "yes started");
    }

    @Override
    public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {
        Log.d("MapActivity", "onRouteSuccess called. Routes: " + routeInfoModelArrayList.size());

        boolean isOverviewMode = binding.tabLayoutOverview.getSelectedTabPosition() == 0;

        if (!isOverviewMode && polylines != null) {
            for (Polyline line : polylines) {
                line.remove();
            }
            polylines.clear();
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        // Generate a random color

        int randomColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        for (int i = 0; i < routeInfoModelArrayList.size(); i++) {
            if (i == routeIndexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing" + routeIndexing);
                polylineOptions.color(randomColor);
                polylineOptions.width(12);
                polylineOptions.addAll(routeInfoModelArrayList.get(routeIndexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = googleMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
        }
    }

    @Override
    public void onRouteCancelled() {
        Log.d("TAG", "route canceled");
        // restart your route drawing
    }

    private Bitmap createCustomMarker(String text) {

        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        //background size
        canvas.drawRect(0, 0, 200, 100, backgroundPaint);

        //text
        Paint textPaint = new Paint();
        textPaint.setTextSize(40);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, 100, 60, textPaint);

        return bitmap;
    }

}
