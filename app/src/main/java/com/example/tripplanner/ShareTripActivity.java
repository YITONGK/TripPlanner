package com.example.tripplanner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ShareTripActivity  extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_sharing);

        // Initialize the ImageView
        qrCodeImageView = findViewById(R.id.qrCodeImageView);

        // Retrieve the Trip ID from the Intent extras
        tripId = getIntent().getStringExtra("tripId");

        // Display the trip ID
        TextView tripIdTextView = findViewById(R.id.textViewTripId);
        tripIdTextView.setText(tripId);

        shareTrip(tripId);

        // Set up the copy button
        Button copyButton = findViewById(R.id.buttonCopyTripId);
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Trip ID", tripId);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Trip ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Set up go-back button
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void shareTrip(String tripId) {
        Bitmap qrCodeBitmap = generateQRCode(tripId);
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        }
    }

    public Bitmap generateQRCode(String tripId) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(tripId, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            return bitmap;
        } catch(Exception e) {
            return null;
        }
    }

}
