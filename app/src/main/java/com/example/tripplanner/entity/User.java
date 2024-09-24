package com.example.tripplanner.entity;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class User {
//    private final String userID;
    private String username;
    private String email;
    private String profileImagePath; // URI or path to the profile image

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // In order to read data into a class
    // each custom class must have a public constructor that takes no arguments
    public User() {
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
    // Update user info with remote database
//    public boolean updateUserData(){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("users").document(userID);
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("username", username);
//        updates.put("email", email);
//        updates.put("profileImagePath", profileImagePath);
//
//        docRef.update(updates).addOnSuccessListener(aVoid -> {
//            Log.d("UpdateUserData", "DocumentSnapshot successfully updated!");
//        }).addOnFailureListener(e -> {
//            Log.w("UpdateUserData", "Error updating document", e);
//        });
//
//        // Update email in authentication database if email is changed
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (user != null && !user.getEmail().equals(email)) {
//            user.updateEmail(email)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("UpdateEmailAuth", "User email address updated.");
//                    }
//                });
//        }
//
//        return true;
//    }
}
