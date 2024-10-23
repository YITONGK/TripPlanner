package com.example.tripplanner.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.example.tripplanner.EditPlanActivity;
import com.example.tripplanner.MapActivity;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.AllPlanAdapter;
import com.example.tripplanner.adapter.AllPlanInterface;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.ActivityItem;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment implements OnMapReadyCallback, AllPlanInterface {

    public static int PLAN = R.layout.home_fragment_layout_plan;
    public static int LOCATION = R.layout.home_fragment_layout_location;
    public static String LAYOUT_TYPE = "type";

    private int layout = R.layout.home_fragment_layout_plan;
    private GoogleMap mMap;

    private ArrayList<Trip> allPlans = new ArrayList<>();;
//    private List<LatLng> pointList = new ArrayList<>();
    private HashMap<String, HashMap<String, List<LatLng>>> tripLocationMap = new HashMap<>();
    private AllPlanAdapter adapter;

    private boolean shouldShowPastTripsBottomSheet = false;
    private Random random = new Random(123);
    private List<Polyline> polylines = new ArrayList<>();
    private List<LatLng> middlePoints = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE);
        }

        View rootView = inflater.inflate(layout, container, false);

        if (this.layout == R.layout.home_fragment_layout_location) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirestoreDB firestoreDB = FirestoreDB.getInstance();


                firestoreDB.getPastTripsByUserId(currentUser.getUid(), trips -> {
                    for (Trip trip : trips) {
                        Set<String> keys = trip.getPlans().keySet();
                        List<LatLng> latLngList = new ArrayList<>();
                        Map<String, List<ActivityItem>> plans = trip.getPlans();
                        for (String key: keys){
                            List<com.example.tripplanner.entity.ActivityItem> rawActivityItems = trip.getPlans().get(key);
                            Log.d("test", "rawActivityItems: "+rawActivityItems.getClass());
                            for (Object item : rawActivityItems) {
                                String input = item.toString();
                                String latitudeRegex = "latitude=([-+]?[0-9]*\\.?[0-9]+)";
                                String longitudeRegex = "longitude=([-+]?[0-9]*\\.?[0-9]+)";

                                Pattern latPattern = Pattern.compile(latitudeRegex);
                                Matcher latMatcher = latPattern.matcher(input);
                                Pattern lonPattern = Pattern.compile(longitudeRegex);
                                Matcher lonMatcher = lonPattern.matcher(input);
                                if (latMatcher.find() && lonMatcher.find()) {
                                    String lat = latMatcher.group(1);
                                    double latitude = Float.valueOf(lat);
                                    String lon = lonMatcher.group(1);
                                    double longitude = Float.valueOf(lon);
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    latLngList.add(latLng);
                                }
                            }
                        }

                        HashMap<String, List<LatLng>> locationMap = new HashMap<>();
                        locationMap.put(trip.getName(), latLngList);

                        tripLocationMap.put(trip.getId(), locationMap);
                    }

                    mapFragment.getMapAsync(this);

                }, e -> {
                    Log.d("DEBUG", "Error in getting past trips");
                });
            }
        } else {
            rootView = inflater.inflate(R.layout.home_fragment_layout_plan, container, false);
            displayAllPlans(rootView);
        }

        return rootView;
    }


    private void displayAllPlans(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.allPlanRecyclerView);

        // sort trips by startDate
        Collections.sort(allPlans, new Comparator<Trip>() {
            @Override
            public int compare(Trip lhs, Trip rhs) {
                return rhs.getStartDate().compareTo(lhs.getStartDate());
            }
        });

        // bind the adapter
        adapter = new AllPlanAdapter(rootView.getContext(), allPlans, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        // get trip data from database
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirestoreDB firestoreDB = FirestoreDB.getInstance();
            firestoreDB.getTripsByUserId(userId, trips -> {
                // Handle the list of trips
                Log.d("PLAN", "getting trips: " + trips.size());
                allPlans.clear();
                allPlans.addAll(trips);
                adapter.notifyDataSetChanged();
                for (Trip trip : trips) {
                    Log.d("PLAN", "Trip: " + trip.toString());
                }
            }, e -> {
                Log.d("PLAN", "Error getting trips: " + e.getMessage());
            });
        } else {
            Log.d("Debug", "No user is signed in.");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        for (String key: tripLocationMap.keySet()){
            HashMap<String, List<LatLng>> locationMap = tripLocationMap.get(key);
            for(String name:locationMap.keySet()){
                List<LatLng> latLngList = locationMap.get(name);
                if (latLngList.size() > 1){
                    Log.d("name", "name is "+ name);
                    Log.d("latLngList", "onMapReady: "+ latLngList.get(0));
                }
                getRoutePoints(latLngList, name);

            }
        }
//        Log.d("HomeFragment", "onMapReady called, map is ready");
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10));
//        LatLng sydney = new LatLng(-34, 151);
//
//        // Get the current location
//        Location currentLocation = getCurrentLocation();
//        if (currentLocation != null) {
//            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15)); // Zoom level can be adjusted
//        } else {
//            // Fallback to a default location if current location is not available
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        }
//
//        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in
//        // Sydney"));
//        TextView textView = new TextView(this.getContext());
//        textView.setText("Hello!!");
//        textView.setBackgroundColor(Color.BLACK);
//        textView.setTextColor(Color.YELLOW);
//
//        Marker marker = mMap.addMarker(
//                new AdvancedMarkerOptions()
//                        .position(sydney)
//                        .iconView(textView));
//        marker.setTag(0);

        // getCurrentLocation();

        // LatLng PERTH = new LatLng(-31.952854, 115.857342);
        // Marker markerPerth = mMap.addMarker(new MarkerOptions()
        // .position(PERTH)
        // .title("Perth"));
        // markerPerth.setTag(0);
        shouldShowPastTripsBottomSheet = true;
        // Show the bottom sheet when the map is ready
        showPastTripsBottomSheetIfPossible();
    }

    // Recommended method to generate new LayoutDemoFragment
    // Instead of calling new LayoutDemoFragment() directly
    public static Fragment newInstance(int layout) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layout);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onItemClick(int position) {
        // Navigate to specific plan detail page
        Log.d("TAG", "click on card");
        Log.d("TAG", getActivity().toString());
        Intent i = new Intent(getActivity(), EditPlanActivity.class);
        i.putExtra("tripId", allPlans.get(position).getId());
        i.putExtra("From", "Main");
        startActivity(i);
    }

    private android.location.Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    1);
            return null;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d("SENSOR", "location: " + location);
        return location;
    }

    private void showPastTripsBottomSheet() {
        PastTripsBottomSheet bottomSheet = new PastTripsBottomSheet();
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }

    private void showPastTripsBottomSheetIfPossible() {
        if (shouldShowPastTripsBottomSheet && isAdded() && !isStateSaved()) {
            showPastTripsBottomSheet();
            shouldShowPastTripsBottomSheet = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showPastTripsBottomSheetIfPossible();
    }

    private void getRoutePoints(List<LatLng> latLngList, String title) {
        if (latLngList == null || latLngList.size() < 2) {
            return;
        }

        List<LatLng> waypoints = new ArrayList<>(latLngList);

        try {
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(getContext())
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(new RouteListener() {
                        @Override
                        public void onRouteFailure(ErrorHandling e) {
                            Log.e("HomeFragment", "Route calculation failed: " + e.getMessage());
                        }

                        @Override
                        public void onRouteStart() {
                            Log.d("TAG", "yes started");
                        }

                        @Override
                        public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {

                            PolylineOptions polylineOptions = new PolylineOptions();
                            int randomColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

                            Log.d("RouteSuccess", "Drawing route for index: " + routeIndexing);

                            for (int i = 0; i < routeInfoModelArrayList.size(); i++) {
                                if (i == routeIndexing) {
                                    List<LatLng> routePoints = routeInfoModelArrayList.get(routeIndexing).getPoints();
                                    polylineOptions.color(randomColor);
                                    polylineOptions.width(12);
                                    polylineOptions.addAll(routePoints);
                                    polylineOptions.startCap(new RoundCap());
                                    polylineOptions.endCap(new RoundCap());
                                    Polyline polyline = mMap.addPolyline(polylineOptions);
                                    polylines.add(polyline);
                                    int middleIndex = routePoints.size() / 2;
                                    LatLng middlePoint = routePoints.get(middleIndex);
                                    middlePoints.add(middlePoint);
                                    addMarkerForOverviewRoute(title);
                                }
                            }
                        }

                        @Override
                        public void onRouteCancelled() {
                            Log.d("TAG", "route canceled");
                        }
                    })
                    .alternativeRoutes(true)
                    .waypoints(waypoints)
                    .build();
            Log.d("HomeFragment", "Executing RouteDrawing");
            routeDrawing.execute();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error in RouteDrawing setup", e);
        }
    }

    private Bitmap createCustomMarker(String text) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(40);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);


        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        int padding = 20;
        int width = textBounds.width() + padding * 2;
        int height = textBounds.height() + padding * 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        float textX = padding;
        float textY = (height / 2) - (textPaint.descent() + textPaint.ascent()) / 2;  // 计算垂直居中的 y 坐标
        canvas.drawText(text, textX, textY, textPaint);

        return bitmap;
    }


    private void addMarkerForOverviewRoute(String title) {
        LatLng location = middlePoints.get(middlePoints.size() - 1);
        if(location != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(title))));

        }
    }

}
