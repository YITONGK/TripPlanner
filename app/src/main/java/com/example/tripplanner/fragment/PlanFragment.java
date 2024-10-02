package com.example.tripplanner.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.tripplanner.helperclass.ActivityItem;
import com.example.tripplanner.R;
import com.example.tripplanner.adapter.ActivityItemAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Calendar;


public class PlanFragment extends Fragment implements OnMapReadyCallback {

    public static final int OVERVIEW = 0;
    public static final int PLAN_SPECIFIC_DAY = 1;
    static final String LAYOUT_TYPE = "type";
    private int layout = OVERVIEW;
    private int dayIndex = -1; // To identify the day
    private GoogleMap mMap;


    private PlanViewModel viewModel;

    // For specific day plan
    private TextView addActivityLocation;
    private ListView activityLocationList;
    private ArrayList<ActivityItem> activityItemArray;
    private ActivityItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get ViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(PlanViewModel.class);

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE, OVERVIEW);
            if (this.layout == PLAN_SPECIFIC_DAY) {
                this.dayIndex = getArguments().getInt("dayIndex", -1);
            }
        }
    }

    public static PlanFragment newInstance(int layoutType, int dayIndex) {
        PlanFragment fragment = new PlanFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_TYPE, layoutType);
        bundle.putInt("dayIndex", dayIndex);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        if (this.layout == PLAN_SPECIFIC_DAY) {
            rootView = inflater.inflate(R.layout.plan_specific_day, container, false);

            addActivityLocation = rootView.findViewById(R.id.addActivityLocation);
            activityLocationList = rootView.findViewById(R.id.activityLocationList);

            // Get the activity items list for this day from the ViewModel
            activityItemArray = viewModel.getActivityItemArray(dayIndex);
            adapter = new ActivityItemAdapter(getContext(), activityItemArray);
            activityLocationList.setAdapter(adapter);

            addActivityLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddActivityDialog();
                }
            });

            activityLocationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showEditActivityDialog(position);
                }
            });

        } else {
            rootView = inflater.inflate(R.layout.plan_overview, container, false);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    private void showAddActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add activity");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String activityName = input.getText().toString();
                if (!activityName.isEmpty()) {
                    ActivityItem activityItem = new ActivityItem(activityName);
                    activityItemArray.add(activityItem);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Please enter something", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showEditActivityDialog(int position) {
        ActivityItem activityItem = activityItemArray.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit activity");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_activity, null);
        builder.setView(dialogView);

        EditText startTime = dialogView.findViewById(R.id.startTime);
        EditText endTime = dialogView.findViewById(R.id.endTime);
        EditText inputLocation = dialogView.findViewById(R.id.inputLocation);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);

        startTime.setText(activityItem.getStartTime());
        endTime.setText(activityItem.getEndTime());
        inputLocation.setText(activityItem.getLocation());
        inputNotes.setText(activityItem.getNotes());

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                activityItem.setStartTime(startTime.getText().toString());
                activityItem.setEndTime(endTime.getText().toString());
                activityItem.setLocation(inputLocation.getText().toString());
                activityItem.setNotes(inputNotes.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
