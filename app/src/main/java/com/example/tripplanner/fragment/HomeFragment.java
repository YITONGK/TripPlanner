package com.example.tripplanner.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.example.tripplanner.EditPlanActivity;
import com.example.tripplanner.LoginActivity;
import com.example.tripplanner.MainActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeFragment extends Fragment implements OnMapReadyCallback, AllPlanInterface {

    public static int PLAN = R.layout.home_fragment_layout_plan;
    public static int LOCATION = R.layout.home_fragment_layout_location;
    public static String LAYOUT_TYPE = "type";

    private int layout = R.layout.home_fragment_layout_plan;
    private GoogleMap mMap;

    private ArrayList<Trip> allPlans = new ArrayList<>();
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
                    .findFragmentById(R.id.map_fragment);
            if (mapFragment != null) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                FirestoreDB firestoreDB = FirestoreDB.getInstance();

                if (currentUser == null) {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    firestoreDB.getPastTripsByUserId(currentUser.getUid(), trips -> {
                        for (Trip trip : trips) {
                            Set<String> keys = trip.getPlans().keySet();
                            List<LatLng> latLngList = new ArrayList<>();
                            Map<String, List<ActivityItem>> plans = trip.getPlans();
                            for (String key : keys) {
                                List<com.example.tripplanner.entity.ActivityItem> rawActivityItems = trip.getPlans().get(key);
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
                    });
                }
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
                allPlans.clear();
                allPlans.addAll(trips);
                adapter.notifyDataSetChanged();

                // display instruction if no trips in current account
                TextView instruction = rootView.findViewById(R.id.instruction);
                ImageView arrow = rootView.findViewById(R.id.instructionArrow);
                if (!allPlans.isEmpty()) {
                    arrow.setVisibility(View.INVISIBLE);
                    instruction.setVisibility(View.INVISIBLE);
                } else {
                    arrow.setVisibility(View.VISIBLE);
                    instruction.setVisibility(View.VISIBLE);
                }

            }, e -> {
            });
        }

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                new AlertDialog.Builder(getContext())
                    .setCancelable(false)
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteTrip(viewHolder.getAbsoluteAdapterPosition());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Reset the swiped row back to its original position
                            adapter.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition()); // Reset the row
                        }
                    })
                    .show();
            }

            @Override
            public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,float dX, float dY,int actionState, boolean isCurrentlyActive){
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                  .addBackgroundColor(ContextCompat.getColor(getContext(), R.color.my_background))
                    .addActionIcon(R.drawable.baseline_delete_24)
                    .create()
                    .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // Attach the ItemTouchHelper to RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void deleteTrip(int pos) {
        String tripId = allPlans.get(pos).getId();
        FirestoreDB firestoreDB = FirestoreDB.getInstance();

        // Perform the deletion in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                firestoreDB.deleteTripById(tripId, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle successful deletion
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle deletion failure
                    }
                });
            }
        }).start();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (tripLocationMap.keySet() == null) {
            return;
        }
        mMap = googleMap;

        double sumLat = 0.0;
        double sumLng = 0.0;
        int count = 0;

        for (String key : tripLocationMap.keySet()) {
            HashMap<String, List<LatLng>> locationMap = tripLocationMap.get(key);
            for (String name : locationMap.keySet()) {
                List<LatLng> latLngList = locationMap.get(name);
                getRoutePoints(latLngList, name, key);

                // Accumulate coordinates for the central point
                for (LatLng latLng : latLngList) {
                    sumLat += latLng.latitude;
                    sumLng += latLng.longitude;
                    count++;
                }
            }
        }

        // Calculate the central point
        if (count > 0) {
            double centralLat = sumLat / count;
            double centralLng = sumLng / count;
            LatLng centralPoint = new LatLng(centralLat, centralLng);

            // Move camera to the central point with a fixed zoom level
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralPoint, 5)); // Adjust zoom level as needed
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the tripId from the marker's tag
                String tripId = (String) marker.getTag();
                if (tripId != null) {
                    // Start EditPlanActivity and pass the tripId
                    Intent intent = new Intent(getActivity(), EditPlanActivity.class);
                    intent.putExtra("tripId", tripId);
                    intent.putExtra("From", "Memory");
                    startActivity(intent);
                }
                return true;
            }
        });

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

    private void getRoutePoints(List<LatLng> latLngList, String title, String tripId) {
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
                        }

                        @Override
                        public void onRouteStart() {
                        }

                        @Override
                        public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {

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
                                    Polyline polyline = mMap.addPolyline(polylineOptions);
                                    polylines.add(polyline);
                                    int middleIndex = routePoints.size() / 2;
                                    LatLng middlePoint = routePoints.get(middleIndex);
                                    middlePoints.add(middlePoint);
                                    addMarkerForOverviewRoute(title);

                                    addMarkerForOverviewRoute(title, tripId);
                                }
                            }
                        }

                        @Override
                        public void onRouteCancelled() {
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

    private void addMarkerForOverviewRoute(String title, String tripId) {
        LatLng location = middlePoints.get(middlePoints.size() - 1);
        if (location != null) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(title))));

            // Store the tripId in the marker's tag
            if (marker != null) {
                marker.setTag(tripId);
            }
        }
    }

}
