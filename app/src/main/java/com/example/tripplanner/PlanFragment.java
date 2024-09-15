package com.example.tripplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlanFragment extends Fragment implements OnMapReadyCallback {

    static int OVERVIEW = R.layout.plan_overview;
    static int PLAN_SPECIFIC_DAY = R.layout.plan_specific_day;
    static String LAYOUT_TYPE = "type";
    private int layout = R.layout.plan_overview;
    private int day;
    private GoogleMap mMap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE);
        }

        View rootView;
        if (this.layout != R.layout.plan_overview) {
            rootView = inflater.inflate(R.layout.plan_specific_day, container, false);
        } else {
            rootView = inflater.inflate(R.layout.plan_overview, container, false);
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    static Fragment newInstance(int layout) {
        Fragment fragment = new PlanFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layout);
        fragment.setArguments(bundle);
        return fragment;
    }
}
