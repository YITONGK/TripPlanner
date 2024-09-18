package com.example.tripplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.tripplanner.databinding.PlanDurationBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import android.widget.LinearLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

public class PlanDurationActivity extends AppCompatActivity {
    private PlanDurationBinding binding;
    private JSONObject planDetails ;
    private JSONArray locationList;

    //Object 类型
//    {
//        "location":[],
//        "days": int,
//        "startdate": Date,
//        "enddate": Date;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PlanDurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //1. get intent data from previous activity (String location)
        String location1 = "Shanghai";
        String location2 = "Beijing";

        locationList = new JSONArray();
        locationList.put(location1);
        locationList.put(location2);

        //hard code for testing

        //Remove Button (Dynamic add based on the JSON Objects)
        //3. add the button based on the JSONARRAY
        ButtonDecorator buttonDecorator = new ButtonDecorator( findViewById(R.id.constraint_layout));
        buttonDecorator.addButtonsFromJson(locationList);
        ArrayList<Button> buttonList =  buttonDecorator.getButtonArrayList();


        //Back Button Functions
        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish PlanDuration Activity
                finish();
            }
        });

        //Button Add Location Functions
        //New location will be add into the JSONArray
        Button addLocationButton = findViewById(R.id.button_add_location);
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("test_add_button");

                // Set BottomSheetDialog and get the xml view
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PlanDurationActivity.this);
                View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
                bottomSheetDialog.setContentView(bottomSheetView);

                bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            // Set BottomSheet Height as 90%
                            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
                            bottomSheet.setLayoutParams(layoutParams);

                            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            behavior.setSkipCollapsed(true);
                        }
                    }
                });


                // 显示 BottomSheetDialog
                bottomSheetDialog.show();
            }
        });

        //TabLayout Functions
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
