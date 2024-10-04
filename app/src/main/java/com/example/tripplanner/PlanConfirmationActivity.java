package com.example.tripplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class PlanConfirmationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_confirmation);

        String suggestedPlan = getIntent().getStringExtra("suggestedPlan");
        TextView planTextView = findViewById(R.id.planTextView);
        planTextView.setText(suggestedPlan);

        Button acceptButton = findViewById(R.id.acceptButton);
        Button declineButton = findViewById(R.id.declineButton);

        acceptButton.setOnClickListener(v -> {
            // Update the trip with the new plan
            updateTrip(suggestedPlan);
            setResult(RESULT_OK);
            finish();
        });

        declineButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void updateTrip(String newPlan) {
        // Implement trip update logic (e.g., save to database or shared preferences)
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) { // Match the request code used when starting PlanConfirmationActivity
            if (resultCode == RESULT_OK) {
                // User accepted the new plan
//                notifyUser("Trip has been updated with the new plan.");
                Log.d("MainActivity", "User accepted the new plan.");
            } else {
                // User declined the new plan
//                notifyUser("Trip remains unchanged.");
                Log.d("MainActivity", "User declined the new plan.");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}

