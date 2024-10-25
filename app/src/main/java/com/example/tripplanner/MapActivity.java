package com.example.tripplanner;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.tabs.TabLayout;
import com.google.android.gms.maps.model.LatLngBounds;



public class MapActivity extends AppCompatActivity  {
    private GoogleMap googleMap;
    private ActivityMapBinding binding;
    private HashMap<String, List<Double[]>> receivedMap;
    private List<Polyline> polylines = new ArrayList<>();
    private Random random = new Random(123);
    private List<LatLng> middlePoints = new ArrayList<>();
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    private Map<Marker, Integer> markerToTabPositionMap = new HashMap<>();
    private HashMap<String, List<String>> receivedLocationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpCloseButton();

        initializeFragmentsAndTabs();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_overview);

        if (mapFragment != null) {
            mapFragment.getMapAsync(gMap -> {
                googleMap = gMap;
                addMarkersToMap(0);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Integer tabPosition = markerToTabPositionMap.get(marker);
                        if (tabPosition != null) {
                            TabLayout.Tab tab = binding.tabLayoutOverview.getTabAt(tabPosition);
                            if (tab != null) {
                                tab.select();
                            }
                        }
                        return false;
                    }
                });
            });
        }

    }

    private void addMarkersToMap(int tabPosition) {
        if (googleMap == null || isMapValid(receivedMap, receivedLocationNames)) {
            return;
        }
        googleMap.clear();
        if (tabPosition == 0) {
            for (String key : receivedMap.keySet()) {
                List<Double[]> latLngList = receivedMap.get(key);
                List<String> nameList = receivedLocationNames.get(key);
                if (latLngList == null || nameList == null || latLngList.size() == 0 || nameList.size() == 0) {
                    continue;
                }
                if (latLngList.size() > 1){
                    String days = String.valueOf((Integer.parseInt(key)+1));
                    addboundsBuilder(latLngList, boundsBuilder);
                    getRoutePoints(latLngList, days);
                }
                else if (latLngList.size() == 1){
                    double latitude = latLngList.get(0)[0];
                    double longitude = latLngList.get(0)[1];
                    LatLng location  = new LatLng(latitude, longitude);
                    addboundsBuilder(latLngList, boundsBuilder);
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(location));
                    markerToTabPositionMap.put(marker, Integer.parseInt(key)+1);
                }
            }
        } else {
            String key = String.valueOf(tabPosition - 1);
            List<Double[]> latLngList = receivedMap.get(key);
            List<String> nameList = receivedLocationNames.get(key);

            if (latLngList == null || latLngList.isEmpty() || nameList == null) {
                googleMap.clear();
                return;
            }

            String days = String.valueOf((Integer.parseInt(key)+1));
            addboundsBuilder(latLngList, boundsBuilder);

            addMarkersForLatLngList(latLngList,nameList);
            // Add route drawing for specific day
            getRoutePoints(latLngList, days);
        }
        int padding = 100;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
    }

    private void addMarkersForLatLngList(List<Double[]> latLngList, List<String> nameList) {
        int index = 0;
        for (Double[] latLng : latLngList) {
            double latitude = latLng[0];
            double longitude = latLng[1];
            LatLng location = new LatLng(latitude, longitude);

            String title = nameList.get(index);

            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(String.valueOf(index+1)))))
                    .setTitle(title);

            index++;
        }
    }

    private void addMarkerForOverviewRoute(String title, int tabPosition) {
        LatLng location = middlePoints.get(middlePoints.size() - 1);
        if(location != null) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(title))));

            markerToTabPositionMap.put(marker, tabPosition);
        }
    }

    private void addboundsBuilder(List<Double[]> latLngList,LatLngBounds.Builder boundsBuilder ){
        for (Double[] latLng : latLngList) {
            double latitude = latLng[0];
            double longitude = latLng[1];
            LatLng location = new LatLng(latitude, longitude);
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
        receivedLocationNames = (HashMap<String, List<String>>) intent.getSerializableExtra("locationNames", HashMap.class);

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

    private void getRoutePoints(List<Double[]> latLngList, String days) {
        if (latLngList == null || latLngList.size() < 2) {
            return;
        }

        List<LatLng> waypoints = new ArrayList<>();
        for (Double[] location : latLngList) {
            waypoints.add(new LatLng(location[0], location[1]));
        }

        try {
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(MapActivity.this)
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(new RouteListener() {
                        @Override
                        public void onRouteFailure(ErrorHandling e) {
                        }

                        @Override
                        public void onRouteStart() {
                        }

                        @Override
                        public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {
                            boolean isOverviewMode = binding.tabLayoutOverview.getSelectedTabPosition() == 0;
                            if (!isOverviewMode && polylines != null) {
                                for (Polyline line : polylines) {
                                    line.remove();
                                }
                                polylines.clear();
                            }
                            PolylineOptions polylineOptions = new PolylineOptions();
                            int randomColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                            for (int i = 0; i < routeInfoModelArrayList.size(); i++) {
                                if (i == routeIndexing) {
                                    List<LatLng> routePoints = routeInfoModelArrayList.get(routeIndexing).getPoints();
                                    polylineOptions.color(randomColor);
                                    polylineOptions.width(12);
                                    polylineOptions.addAll(routePoints);
                                    polylineOptions.startCap(new RoundCap());
                                    polylineOptions.endCap(new RoundCap());
                                    Polyline polyline = googleMap.addPolyline(polylineOptions);
                                    polylines.add(polyline);
                                    int middleIndex = routePoints.size() / 2;
                                    LatLng middlePoint = routePoints.get(middleIndex);
                                    middlePoints.add(middlePoint);
                                    if(isOverviewMode) {
                                        addMarkerForOverviewRoute("DAY" + days, Integer.parseInt(days));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onRouteCancelled() {
                            // restart your route drawing
                        }
                    })
                    .alternativeRoutes(true)
                    .waypoints(waypoints)
                    .build();
            routeDrawing.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap createCustomMarker(String text) {

        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        //background size
        if(binding.tabLayoutOverview.getSelectedTabPosition() == 0){
            canvas.drawRect(0, 0, 200, 100, backgroundPaint);
        }
        else{
            canvas.drawCircle(100, 50, 50, backgroundPaint);
        }

        //text
        Paint textPaint = new Paint();
        textPaint.setTextSize(40);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, 100, 60, textPaint);

        return bitmap;
    }

    private boolean isMapValid(HashMap<String, List<Double[]>> receivedMap, HashMap<String, List<String>> receivedLocationNames) {
        // Check if receivedMap is null or empty
        boolean isEmpty = true;
        // Iterate through receivedMap and check if any list is null or empty
        for (String key : receivedMap.keySet()) {
            List<Double[]> latLngList = receivedMap.get(key);
            if (latLngList.size() > 0) {
                isEmpty = false;
            }
        }
        // Iterate through receivedLocationNames and check if any list is null or empty
        for (String key : receivedLocationNames.keySet()) {
            List<String> nameList = receivedLocationNames.get(key);
            if (nameList.size() > 0) {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

}
