package com.example.tripplanner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.tripplanner.databinding.PlanDurationBinding;
import com.google.android.material.tabs.TabLayout;

public class PlanDurationActivity extends AppCompatActivity {
    private PlanDurationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PlanDurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addTab(tabLayout.newTab().setText("Days"));
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"));

        loadFragment(PlanDurationFragment.newInstance(PlanDurationFragment.DAYS));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment;
                if (tab.getPosition() == 0) {
                    selectedFragment = PlanDurationFragment.newInstance(PlanDurationFragment.DAYS);
                } else {
                    selectedFragment = PlanDurationFragment.newInstance(PlanDurationFragment.CALENDAR);
                }
                loadFragment(selectedFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
