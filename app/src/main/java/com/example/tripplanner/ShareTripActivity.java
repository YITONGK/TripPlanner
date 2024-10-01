package com.example.tripplanner;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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

        if (tripId != null && !tripId.isEmpty()) {
            // Generate and display the QR code
            shareTrip(tripId);
        } else {
            // Handle the case where Trip ID is not available
            Log.d("PLAN", "Trip ID is missing.");
//            finish(); // Close the activity if no Trip ID is provided
        }
    }

    public void shareTrip(String tripId) {
        ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);
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
            Log.d("PLAN", "Error generating QR code: " + e.getMessage());
            return null;
        }
    }
}
