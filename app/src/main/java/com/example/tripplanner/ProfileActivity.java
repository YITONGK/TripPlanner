package com.example.tripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tripplanner.databinding.ActivityProfileBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.User;
import com.example.tripplanner.entity.UserTripStatistics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

    private User userData;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uid = user.getUid();
            loadUserProfile();
        }

        TextView username = findViewById(R.id.username);
        TextView email = findViewById(R.id.emailAddress);
        TextView preference = findViewById(R.id.preference);

        // get user details from database
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(ProfileActivity.this, "User's details are not available at the moment", Toast.LENGTH_LONG).show();
        } else {
            String uid = user.getUid();

            FirestoreDB.getInstance().getUserById(uid, returnedUser ->{
                userData = returnedUser;

                username.setText(userData.getUsername());
                email.setText(userData.getEmail());
                preference.setText(userData.getPreference());

            }, (e) -> {
            });

            fetchUserTripStatistics(uid);
        }

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user == null) {
                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                } else {
                    // Pass username and email to Edit Profile Activity
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    intent.putExtra("username", username.getText().toString());
                    intent.putExtra("email", email.getText().toString());
                    intent.putExtra("preference", preference.getText().toString());
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
                finish();
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchUserTripStatistics(String userId) {
        FirestoreDB firestoreDB = FirestoreDB.getInstance();

        firestoreDB.getUserTripStatistics(userId, new OnSuccessListener<UserTripStatistics>() {
            @Override
            public void onSuccess(UserTripStatistics statistics) {
                int totalTrips = statistics.getTotalTrips();
                int totalUniqueLocations = statistics.getTotalLocations();
                int totalDays = statistics.getTotalDays();

                // Display the statistics
                TextView numTrip = findViewById(R.id.numTrip);
                TextView numLocation = findViewById(R.id.numLocation);
                TextView numDay = findViewById(R.id.numDay);

                numTrip.setText(String.valueOf(totalTrips));
                numLocation.setText(String.valueOf(totalUniqueLocations));
                numDay.setText(String.valueOf(totalDays));
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profilePictureUrl = documentSnapshot.getString("profilePicture");

                ImageView profile = findViewById(R.id.profilePicture);

                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    Glide.with(this)
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.woman)
                            .into(profile);
                } else {
                    profile.setImageResource(R.drawable.woman);
                }
            }
        }).addOnFailureListener(e -> {
        });
    }

}