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
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class EditPlanActivity extends AppCompatActivity {

    private String selectedPlace;
    private int days = 4;
    private ActivityEditPlanBinding binding;
    private ArrayList<Fragment> fragments = new ArrayList<>();

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
            String dayAndNight;
            if (days == 1) {
                dayAndNight = "1 day";
            }
            else if (days == 2) {
                dayAndNight = "2 days and 1 night";
            } else {
                dayAndNight = days + " days" + " and " + (days - 1) + " nights";
            }
            String day = days > 1 ? " days" : " day";
            tripTo.setText(days + day + " trip to " + selectedPlace);

            TextView daysAndNight = findViewById(R.id.textViewDaysAndNights);
            daysAndNight.setText(dayAndNight);
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

        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addTab(tabLayout.newTab().setText("Overview"));
        fragments.add(PlanFragment.newInstance(PlanFragment.OVERVIEW));
        for (int i = 0; i < days; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("Day " + (i + 1)));
            fragments.add(PlanFragment.newInstance(PlanFragment.PLAN_SPECIFIC_DAY));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab selection
                Fragment selectedFragment;
                int position = tab.getPosition();
                Log.d("TabSelected", "Selected tab position: " + position);
                selectedFragment = fragments.get(position);
                loadFragment(selectedFragment);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselection
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
}