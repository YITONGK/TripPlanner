package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import com.example.tripplanner.fragment.NumberPickerFragment;
import com.shawnlin.numberpicker.NumberPicker;

import com.example.tripplanner.databinding.ActivityPlanSettingBinding;

public class PlanSettingActivity extends AppCompatActivity implements NumberPickerFragment.OnNumberSelectedListener {

    private ActivityPlanSettingBinding binding;
    private EditText editTripName;
    private TextView timeDuration;
    private int days;

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
        editTripName.setText(getIntent().getStringExtra("tripName"));
        timeDuration = findViewById(R.id.edit_time_duration);
        days = getIntent().getIntExtra("days", 0);
        timeDuration.setText(days + (days > 1 ? " days" : " day"));

//        timeDuration.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openNumberPickerDialog(days);
//            }
//        });

        timeDuration.setOnClickListener(v -> {
            loadNumberPickerFragment();
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
            setResult(RESULT_OK, resultIntent);

            finish();
        });
    }

//    private void openNumberPickerDialog(int days) {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View dialogView = inflater.inflate(R.layout.plan_duration_fragment_days, null);
//
//        final NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);
//        numberPicker.setValue(days);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Number of Days");
//        builder.setView(dialogView);
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                int currentDays = numberPicker.getValue();
//                timeDuration.setText(currentDays + (currentDays > 1 ? " days" : " day"));
//            }
//        });
//        builder.setNegativeButton("Cancel", null);
//
//        builder.show();
//    }
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
    }
}