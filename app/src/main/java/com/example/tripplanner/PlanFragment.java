package com.example.tripplanner;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Calendar;

public class PlanFragment extends Fragment implements OnMapReadyCallback {

    static int OVERVIEW = R.layout.plan_overview;
    static int PLAN_SPECIFIC_DAY = R.layout.plan_specific_day;
    static String LAYOUT_TYPE = "type";
    private int layout = R.layout.plan_overview;
    private GoogleMap mMap;

    // designed for specific day plan
    private TextView addActivityLocation;
    private ListView activityLocationList;
    private ArrayList<ActivityItem> activityItemArray;
    private ActivityItemAdapter adapter;

    private PlanViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取 ViewModel 实例
        viewModel = new ViewModelProvider(requireActivity()).get(PlanViewModel.class);

        if (this.getArguments() != null) {
            this.layout = getArguments().getInt(LAYOUT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        if (this.layout == OVERVIEW) {
            rootView = inflater.inflate(R.layout.plan_overview, container, false);

            EditText tripNoteEditText = rootView.findViewById(R.id.noteInput);

            // 观察 ViewModel 中的 tripNote 数据
            viewModel.getTripNote().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if (!tripNoteEditText.getText().toString().equals(s)) {
                        tripNoteEditText.setText(s);
                    }
                }
            });

            // 监听 EditText 的内容变化，并更新 ViewModel 中的数据
            tripNoteEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    viewModel.getTripNote().setValue(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            // 初始化地图
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

        } else if (this.layout == PLAN_SPECIFIC_DAY) {
            rootView = inflater.inflate(R.layout.plan_specific_day, container, false);

            addActivityLocation = rootView.findViewById(R.id.addActivityLocation);
            activityLocationList = rootView.findViewById(R.id.activityLocationList);

            activityItemArray = new ArrayList<>();
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
            // 默认情况，防止意外
            rootView = inflater.inflate(R.layout.plan_overview, container, false);
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

    private void showAddActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add activity");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void showEditActivityDialog(int position) {
        ActivityItem activityItem = activityItemArray.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit activity");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_activity, null);
        builder.setView(dialogView);

        EditText inputTime = dialogView.findViewById(R.id.inputTime);
        EditText inputLocation = dialogView.findViewById(R.id.inputLocation);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);

        inputTime.setText(activityItem.getTime());
        inputLocation.setText(activityItem.getLocation());
        inputNotes.setText(activityItem.getNotes());

        inputTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        inputTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                activityItem.setTime(inputTime.getText().toString());
                activityItem.setLocation(inputLocation.getText().toString());
                activityItem.setNotes(inputNotes.getText().toString());
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

}
