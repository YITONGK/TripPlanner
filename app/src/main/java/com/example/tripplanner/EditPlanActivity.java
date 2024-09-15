package com.example.tripplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tripplanner.databinding.ActivityEditPlanBinding;
import com.google.android.material.navigation.NavigationBarView;

public class EditPlanActivity extends AppCompatActivity {

    private String selectedPlace;
    private int days = 3;
    private ActivityEditPlanBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        selectedPlace = getIntent().getStringExtra("selectedPlace");
        if (selectedPlace != null) {
            // You can now use the selected place string as needed
            TextView tripTo = findViewById(R.id.textViewSelectedPlace);
            String day = days > 1 ? " days" : " day";
            String night = days - 1 > 1 ? " nights" : " night";
            tripTo.setText(days + day + " trip to " + selectedPlace);

            TextView daysAndNight = findViewById(R.id.textViewDaysAndNights);
            daysAndNight.setText(days + day + " and " + (days - 1) + night);
        }

        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditPlanActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        Fragment overview_layout = PlanFragment.newInstance(PlanFragment.OVERVIEW);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, overview_layout)
                .addToBackStack(null)
                .commit();

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.overview) {
                    Log.d("overview", "Overview page selected ");
                    Fragment overview_layout = PlanFragment.newInstance(PlanFragment.OVERVIEW);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, overview_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                } else if (id == R.id.day) {
                    Log.d("Day", "Day page selected");
                    Fragment day_layout = PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainerView, day_layout)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                return false;
            }

        });

    }
}