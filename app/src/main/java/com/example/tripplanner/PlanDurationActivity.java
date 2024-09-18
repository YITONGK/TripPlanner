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

import android.util.Log;

import com.example.tripplanner.databinding.PlanDurationBinding;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PlanDurationActivity extends AppCompatActivity  implements OnFragmentInteractionListener{
    private PlanDurationBinding binding;
    private JSONObject planDetails =  new JSONObject();;
    private JSONArray locationList = new JSONArray();;
    private int receivedDays;
    private String receivedStartDate;
    private String receivedEndDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

        //2. add data into the JSONArray
        String location = getIntent().getStringExtra("selectedPlace");
        locationList.put(location);

        //Remove Button (Dynamic add based on the JSON Objects)
        //3. add the button based on the JSONARRAY
        ButtonDecorator buttonDecorator = new ButtonDecorator( findViewById(R.id.constraint_layout));
        buttonDecorator.addButtonsFromJson(locationList);

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


                // Display BottomSheetDialog
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

        //Button Done
        Button doneButton = findViewById(R.id.button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    planDetails.put("location", locationList);
                    planDetails.put("days" ,receivedDays);
                    planDetails.put("startDate", receivedStartDate);
                    planDetails.put("endDate", receivedEndDate);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Intent intent = new Intent(PlanDurationActivity.this, EditPlanActivity.class);
                intent.putExtra("planDetails", planDetails.toString());
                startActivity(intent);
            }
        });
    }


    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    @Override
    public void DaysInteraction(String data) {


        receivedDays =  Integer.parseInt(data);

        Calendar calendar = Calendar.getInstance();
        receivedStartDate =  dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, receivedDays - 1);
        receivedEndDate=dateFormat.format(calendar.getTime());
    }

    @Override
    public void DatesInteraction(String startDate, String endDate){
        receivedStartDate = startDate;
        receivedEndDate = endDate;

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            startCal.setTime(start);
            endCal.setTime(end);

            int daysDifference = 0;
            while (!startCal.after(endCal)) {
                startCal.add(Calendar.DAY_OF_MONTH, 1);
                daysDifference++;
            }
            receivedDays = daysDifference;

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
