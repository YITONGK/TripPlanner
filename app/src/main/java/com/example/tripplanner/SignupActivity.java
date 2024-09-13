package com.example.tripplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.tripplanner.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView username = (TextView) findViewById(R.id.username);
        TextView email = (TextView) findViewById(R.id.emailAddress);
        TextView password = (TextView) findViewById(R.id.password);
        TextView confirmPassword = (TextView) findViewById(R.id.confirmPassword);

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if any of the user input is null
                if (username.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                } else if (email.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                }
                // check if the password and confirm password are the same
                else if (! password.getText().toString().equals(confirmPassword.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "Mismatch password", Toast.LENGTH_SHORT).show();
                } else {
                    // store user authentication details to database
                    Toast.makeText(SignupActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}