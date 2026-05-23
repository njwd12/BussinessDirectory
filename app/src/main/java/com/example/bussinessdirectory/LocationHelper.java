package com.example.bussinessdirectory;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationListener listener;
    private LocationCallback locationCallback;
    private static final String TAG = "LocationHelper";
    private boolean isTracking = false;

    // Последна испратена локација (за да избегнеме дупликати)
    private Location lastNotifiedLocation;
    private static final float MIN_DISTANCE_TO_NOTIFY = 50; // 50 метри

    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public LocationHelper(Context context, LocationListener listener) {
        this.context = context;
        this.listener = listener;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Метод за постојано следење на локација (за real-time)
    // Метод за постојано следење на локација (за real-time)
    public void startLocationTracking() {
        if (isTracking) return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        // Креирај LocationRequest на компатибилен начин (работи со сите верзии)
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);          // ажурирај на секои 10 секунди
        locationRequest.setFastestInterval(5000);    // најбрзо на 5 секунди
        locationRequest.setSmallestDisplacement(MIN_DISTANCE_TO_NOTIFY); // 50 метри поместување

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = location;
                        android.util.Log.d(TAG, "GPS Location: " + location.getLatitude() + ", " + location.getLongitude());
                        if (shouldNotifyLocationChanged(location)) {
                            lastNotifiedLocation = new Location(location);
                            listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        isTracking = true;
        android.util.Log.d(TAG, "Started location tracking with min displacement " + MIN_DISTANCE_TO_NOTIFY + "m");
    }

    // Проверува дали локацијата се сменила доволно за да известиме
    private boolean shouldNotifyLocationChanged(Location newLocation) {
        if (lastNotifiedLocation == null) return true;
        float distance = lastNotifiedLocation.distanceTo(newLocation);
        android.util.Log.d(TAG, "Distance since last notification: " + distance + "m");
        return distance >= MIN_DISTANCE_TO_NOTIFY;
    }

    // Стопирај следење (за да заштедиш батерија)
    public void stopLocationTracking() {
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
            android.util.Log.d(TAG, "Stopped location tracking");
        }
    }

    // Еднократно земање на локација
    public void getCurrentLocation() {
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
        ).addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                android.util.Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                listener.onLocationReceived(location.getLatitude(), location.getLongitude());
            } else {
                android.util.Log.e(TAG, "Location is null, trying last known location...");
                getLastLocationAsFallback();
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
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        android.util.Log.d(TAG, "Last location received: " + location.getLatitude() + ", " + location.getLongitude());
                        listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        android.util.Log.e(TAG, "No location available");
                        listener.onLocationError("Could not get location. Make sure GPS is enabled.");
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

        return distance < 50; // 500 метри
    }

    public Location getCurrentLocationObject() {
        return currentLocation;
    }

    public boolean isTracking() {
        return isTracking;
    }
}