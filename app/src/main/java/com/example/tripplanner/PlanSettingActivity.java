package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.fragment.NumberPickerFragment;
import com.example.tripplanner.utils.TimeUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.shawnlin.numberpicker.NumberPicker;

import com.example.tripplanner.databinding.ActivityPlanSettingBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PlanSettingActivity extends AppCompatActivity implements NumberPickerFragment.OnNumberSelectedListener {

    private ActivityPlanSettingBinding binding;
    private EditText editTripName;
    private TextView timeDuration;
    private int days;
    private String startDate;

    private TextView startDateTitle;
    private MaterialCalendarView calendarView;
    private TextView textViewStartDate;
    private Button buttonDone;
    private CalendarDay selectedDate;
    private String selectedDateString;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlanSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTripName = findViewById(R.id.edit_trip_name);
        timeDuration = findViewById(R.id.edit_time_duration);
        startDateTitle = findViewById(R.id.calendar_title);
        textViewStartDate = findViewById(R.id.edit_start_date);
        calendarView = findViewById(R.id.calendar_view);
        buttonDone = findViewById(R.id.button_done);

        // get intent extras
        days = getIntent().getIntExtra("days", 0);
        startDate = getIntent().getStringExtra("startDate");
        String tripId = getIntent().getStringExtra("tripId");

        // set texts
        editTripName.setText(getIntent().getStringExtra("tripName"));
        timeDuration.setText(days + (days > 1 ? " days" : " day"));
        if (startDate != null) {
            textViewStartDate.setText(startDate);
        }

        timeDuration.setOnClickListener(v -> {
            loadNumberPickerFragment();
            startDateTitle.setVisibility(View.GONE);
            textViewStartDate.setVisibility(View.GONE);
        });

        textViewStartDate.setOnClickListener(v -> {
            textViewStartDate.setVisibility(View.GONE);
            calendarView.setVisibility(View.VISIBLE);
            buttonDone.setVisibility(View.VISIBLE);
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                selectedDate = date;
                Date javaDate = date.getDate();
                SimpleDateFormat dateFormat = TimeUtils.CALENDAR_DATE_FORMAT;
                String formattedDate = dateFormat.format(javaDate);
                textViewStartDate.setText(formattedDate);
                Log.d("PlanSettingActivity", "Selecting date: " + formattedDate);
            }
        });

        buttonDone.setOnClickListener(v -> {
            calendarView.setVisibility(View.GONE);
            buttonDone.setVisibility(View.GONE);
            textViewStartDate.setVisibility(View.VISIBLE);
            if (selectedDate != null) {
                Date javaDate = selectedDate.getDate();
                SimpleDateFormat dateFormat = TimeUtils.DEFAULT_DATE_FORMAT;
                selectedDateString = dateFormat.format(javaDate);
                Log.d("PlanSettingActivity", "Start date saved: " + selectedDateString);
            }
        });


        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(v -> {
            String newTripName = editTripName.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("tripName", newTripName);
            resultIntent.putExtra("days", days);
            resultIntent.putExtra("startDate", selectedDateString);
            setResult(RESULT_OK, resultIntent);

            finish();
        });

        LinearLayout deleteButton = findViewById(R.id.delete_trip_layout);
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteTrip(tripId);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    private void loadNumberPickerFragment() {
        NumberPickerFragment fragment = new NumberPickerFragment(days);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onNumberSelected(int selectedDays) {
        days = selectedDays;
        timeDuration.setText(days + (days > 1 ? " days" : " day"));
        // The fragment removes itself, so no need to remove it here

        startDateTitle.setVisibility(View.VISIBLE);
        textViewStartDate.setVisibility(View.VISIBLE);
    }

    private void confirmAndDeleteTrip(String tripId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete this trip?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteTrip(tripId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTrip(String tripId) {

        // Perform the deletion in a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                FirestoreDB.getInstance().deleteTripById(tripId, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle successful deletion
                        Log.d("PLAN", "Trip successfully deleted.");

                        // After deletion, return to MainActivity
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PlanSettingActivity.this, "Trip deleted", Toast.LENGTH_SHORT).show();
                                String from = getIntent().getStringExtra("From");
                                if (Objects.equals(from, "Memory")) {
                                    Intent intent = new Intent(PlanSettingActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("select_navigation_plan", false);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Intent intent = new Intent(PlanSettingActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("select_navigation_plan", true);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle deletion failure
                        Log.e("PLAN", "Error deleting trip", e);
                    }
                });
            }
        }).start();
    }

}