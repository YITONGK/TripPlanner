package com.example.tripplanner.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tripplanner.PlanSuggestionDialogFragment;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SensorDetector implements SensorEventListener {

    private Activity activity;
    private SensorManager sensorManager;
    private FusedLocationProviderClient fusedLocationClient;

    // Temperature and Humidity Sensors
    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private boolean isTemperatureSensorAvailable;
    private boolean isHumiditySensorAvailable;
    private float ambientTemperature;
    private float relativeHumidity;
    
    // Shake Sensor and Threshold
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 1.5f;
    private static final int SHAKE_WAIT_TIME_MS = 3000;
    private static boolean isShaken = false;
    private long mShakeTime = 0;
    private boolean isAccelerometerAvailable;

    private OnShakeListener onShakeListener;
    public SensorDetector(Activity activity) {
        this.activity = activity;
        initializeSensors();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public interface OnShakeListener {
        void onShake();
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            
            isTemperatureSensorAvailable = temperatureSensor != null;
            isHumiditySensorAvailable = humiditySensor != null;
            isAccelerometerAvailable = accelerometer != null;
        } else {
            isTemperatureSensorAvailable = false;
            isHumiditySensorAvailable = false;
            isAccelerometerAvailable = false;
        }

        // For testing ShakeEvnet
        if (onShakeListener != null) {
            onShakeListener.onShake();
        }
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.onShakeListener = listener;
    }

    public void registerSensorListeners() {
        if (isTemperatureSensorAvailable) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isHumiditySensorAvailable) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isAccelerometerAvailable) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterSensorListeners() {
        sensorManager.unregisterListener(this);
    }

    // SensorEventListener methods
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            ambientTemperature = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            relativeHumidity = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD) {
                final long now = System.currentTimeMillis();
                // Ignore shake events too close to each other (500ms)
                if (mShakeTime + SHAKE_WAIT_TIME_MS > now || isShaken) {
                    return;
                }
                mShakeTime = now;

                // Handle shake event here
                if (onShakeListener != null) {
                    onShakeListener.onShake();
                    isShaken = true;
                }
            }
        }

    }

    public float getAmbientTemperature() {
        return ambientTemperature;
    }

    public float getRelativeHumidity() {
        return relativeHumidity;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle sensor accuracy changes if needed
    }

    public static void setIsShaken(boolean isShaken) {
        SensorDetector.isShaken = isShaken;
    }

}
