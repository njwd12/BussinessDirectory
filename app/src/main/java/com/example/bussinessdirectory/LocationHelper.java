package com.example.bussinessdirectory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationListener listener;
    private static final String TAG = "LocationHelper";

    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public LocationHelper(Context context, LocationListener listener) {
        this.context = context;
        this.listener = listener;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation() {
        // Провери дали имаме дозвола
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        android.util.Log.d(TAG, "Getting current location...");

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.getToken()
        ).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    android.util.Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    android.util.Log.e(TAG, "Location is null, trying last known location...");
                    // Ако не успее, пробај со last location
                    getLastLocationAsFallback();
                }
            }
        }).addOnFailureListener(e -> {
            android.util.Log.e(TAG, "Failed to get current location: " + e.getMessage());
            getLastLocationAsFallback();
        });
    }

    private void getLastLocationAsFallback() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            android.util.Log.d(TAG, "Last location received: " + location.getLatitude() + ", " + location.getLongitude());
                            listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        } else {
                            android.util.Log.e(TAG, "No location available");
                            listener.onLocationError("Could not get location");
                        }
                    }
                });
    }

    public boolean isNearby(double businessLat, double businessLon) {
        if (currentLocation == null) {
            android.util.Log.d(TAG, "currentLocation is null");
            return false;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                businessLat, businessLon,
                results
        );

        float distance = results[0];
        android.util.Log.d(TAG, "Distance to business: " + distance + " meters");

        return distance < 50; // 50 метри
    }

    public void setMockLocation(double lat, double lon) {
        currentLocation = new Location("mock");
        currentLocation.setLatitude(lat);
        currentLocation.setLongitude(lon);
        android.util.Log.d(TAG, "Mock location set to: " + lat + ", " + lon);
    }
}