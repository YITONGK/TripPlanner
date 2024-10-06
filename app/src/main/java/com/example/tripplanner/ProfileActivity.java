package com.example.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tripplanner.databinding.ActivityProfileBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.User;
import com.example.tripplanner.entity.UserTripStatistics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView username = findViewById(R.id.username);
        TextView email = findViewById(R.id.emailAddress);
        ImageView profilePicture = findViewById(R.id.profilePicture);

        // get user details from database
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(ProfileActivity.this, "User's details are not available at the moment", Toast.LENGTH_LONG).show();
        } else {
            String uid = user.getUid();

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d("TAG", "successfully read the user data");
                    User userData = documentSnapshot.toObject(User.class);
                    Log.d("TAG", userData.getUsername());
                    username.setText(userData.getUsername());
                    email.setText(userData.getEmail());
                }
            });

            fetchUserTripStatistics(uid);
        }

        //TODO: get trip summary from database


        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user == null) {
                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                } else {
                    // Pass username and email to Edit Profile Activity
                    String uid = user.getUid();
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    intent.putExtra("username", username.getText().toString());
                    intent.putExtra("email", email.getText().toString());
                    startActivity(intent);
                }
            }
        });

        binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchUserTripStatistics(String userId) {
        FirestoreDB firestoreDB = new FirestoreDB();

        firestoreDB.getUserTripStatistics(userId, new OnSuccessListener<UserTripStatistics>() {
            @Override
            public void onSuccess(UserTripStatistics statistics) {
                int totalTrips = statistics.getTotalTrips();
                int totalUniqueLocations = statistics.getTotalLocations();
                int totalDays = statistics.getTotalDays();

                // Display the statistics
//                totalTripsTextView.setText("Total Trips: " + totalTrips);
//                totalLocationsTextView.setText("Total Unique Locations: " + totalUniqueLocations);
//                totalDaysTextView.setText("Total Days: " + totalDays);

                Log.d("STATISTICS", "Total Trips: " + totalTrips);
                Log.d("STATISTICS", "Total Unique Locations: " + totalUniqueLocations);
                Log.d("STATISTICS", "Total Days: " + totalDays);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the error
                Log.e("STATISTICS", "Error retrieving trip statistics", e);
            }
        });
    }

}