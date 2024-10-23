package com.example.tripplanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tripplanner.databinding.ActivityEditProfileBinding;
import com.example.tripplanner.db.FirestoreDB;
import com.example.tripplanner.entity.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private StorageReference storageReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private User userData;
    private String uid;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView username = findViewById(R.id.username);
        TextView email = findViewById(R.id.emailAddress);
        ImageView profilePicture = findViewById(R.id.profilePicture);
        TextView preference = findViewById(R.id.preference);

        // display current username and email address
        username.setText(intent.getStringExtra("username"));
        email.setText(intent.getStringExtra("email"));
        preference.setText(intent.getStringExtra("preference"));

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        loadUserProfile();

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://tripplanner-112a8.appspot.com");
        storageReference = storage.getReference().child("profile_pictures");

        FirestoreDB.getInstance().getUserById(uid, returnedUser ->{
            userData = returnedUser;
            Log.d("FirestoreDB", "UserData: "+userData);
        }, (e) -> {
            Log.d("FirestoreDB", "error in fetching user by id: "+e);
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
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
        TextView newUsername = findViewById(R.id.username);
        ImageView newProfilePicture = findViewById(R.id.profilePicture);
        TextView newPreference = findViewById(R.id.preference);

        // check if new username and email address are valid

        if (newUsername.getText().toString().isEmpty()) {
            Toast.makeText(EditProfileActivity.this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername.getText().toString());
        DocumentReference userRef = db.collection("users").document(uid);
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference fileReference = storageReference.child(uid + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updates.put("profilePicture", imageUrl);
                            userRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        Log.e("TAG", "Error updating profile", e);
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error uploading image", e);
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        } else {

            userData.setUsername(newUsername.getText().toString());
            userData.setPreference(newPreference.getText().toString());
            FirestoreDB.getInstance().updateUserById(uid, userData, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("FirestoreDB", "Successfully updated user");
                    // Navigate to Profile Activity
                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }, (e) -> {
                Log.d("FirestoreDB", "Error in updating user: " + e);
            });
//            DocumentReference user = db.collection("users").document(uid);
//            user.update(
//                    "username", newUsername.getText().toString()
//                )
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("TAG", "User's details successfully updated!");
//                        // Navigate to Profile Activity
//                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
//                        startActivity(intent);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("TAG", "Error updating user's details", e);
//                    }
//                });

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error updating profile", e);
                    });

        }
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            ImageView profilePicture = findViewById(R.id.profilePicture);
            profilePicture.setImageURI(imageUri);
        }
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
            } else {
                Log.d("EditProfileActivity", "user document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e("EditProfileActivity", "fetch user data failed", e);
        });
    }
}