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
import com.example.tripplanner.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView username = findViewById(R.id.username);
        TextView email = findViewById(R.id.emailAddress);
        ImageView profilePicture = findViewById(R.id.profilePicture);

        // display current username and email address
        username.setText(intent.getStringExtra("username"));
        email.setText(intent.getStringExtra("email"));

        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: change profile picture
                // allow users to choose from their photo library

            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile(uid);
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUserProfile(String uid) {
        // update username and profile picture
        TextView newUsername = findViewById(R.id.username);
        ImageView newProfilePicture = findViewById(R.id.profilePicture);

        // check if new username and email address are valid
        if (newUsername.getText().toString().isEmpty()) {
            Toast.makeText(EditProfileActivity.this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
        } else {
            DocumentReference user = db.collection("users").document(uid);
            user.update(
                    "username", newUsername.getText().toString()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "User's details successfully updated!");
                        // Navigate to Profile Activity
                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Error updating user's details", e);
                    }
                });
        }
    }
}