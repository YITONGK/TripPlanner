package com.example.tripplanner.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.AllPlanAdapter;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.Trip;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PastTripsBottomSheet extends BottomSheetDialogFragment {

    private AllPlanAdapter adapter;
    private ArrayList<Trip> pastTrips = new ArrayList<>();

    public PastTripsBottomSheet() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_past_trips, container, false);


        fetchPastTrips(view);

        return view;
    }

    private void fetchPastTrips(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.pastTripsRecyclerView);
        AllPlanAdapter adapter = new AllPlanAdapter(getContext(), pastTrips, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirestoreDB firestoreDB = new FirestoreDB();
            firestoreDB.getPastTripsByUserId(userId, new OnSuccessListener<List<Trip>>() {
                @Override
                public void onSuccess(List<Trip> trips) {
                    if (adapter != null) { // Check if adapter is initialized
                        pastTrips.clear();
                        pastTrips.addAll(trips);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Adapter not initialized", Toast.LENGTH_SHORT).show();
                    }
                }
            }, e -> {
                Toast.makeText(getContext(), "Failed to load past trips", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "No user is signed in.", Toast.LENGTH_SHORT).show();
        }
    }
}