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

import com.example.tripplanner.databinding.ActivityPlanSettingBinding;

public class PlanSettingActivity extends AppCompatActivity {

    private ActivityPlanSettingBinding binding;
    private EditText editTripName;
    private TextView timeDuration;

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
        int days = getIntent().getIntExtra("days", 0);
        timeDuration.setText(days + (days > 1 ? " days" : " day"));

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
            setResult(RESULT_OK, resultIntent);

            finish();
        });
    }
}