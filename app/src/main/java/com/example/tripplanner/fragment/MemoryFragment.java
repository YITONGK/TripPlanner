package com.example.tripplanner.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripplanner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MemoryFragment extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private final LatLng PERTH = new LatLng(-31.952854, 115.857342);
    private final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    private final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);

    private Marker markerPerth;
    private Marker markerSydney;
    private Marker markerBrisbane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_fragment_layout_location);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /** Called when the map is ready. */
    @Override
    public void onMapReady(GoogleMap map) {
        // Create a TextView to use as the marker.
        TextView textView = new TextView(this);
        textView.setText("Hello!!");
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.YELLOW);

        markerSydney = map.addMarker(
                new AdvancedMarkerOptions()
                        .position(SYDNEY)
                        .iconView(textView));
        markerSydney.setTag(0);
        map.moveCamera(CameraUpdateFactory.newLatLng(SYDNEY));

        // Add some markers to the map, and add a data object to each marker.
//        markerPerth = map.addMarker(new MarkerOptions()
//                .position(PERTH)
//                .title("Perth"));
//        markerPerth.setTag(0);

//        markerSydney = map.addMarker(new MarkerOptions()
//                .position(SYDNEY)
//                .title("Sydney"));
//        markerSydney.setTag(0);

//        markerBrisbane = map.addMarker(new MarkerOptions()
//                .position(BRISBANE)
//                .title("Brisbane"));
//        markerBrisbane.setTag(0);

        // Set a listener for marker click.
        map.setOnMarkerClickListener(this);
    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}
