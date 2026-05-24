package com.example.bussinessdirectory;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationHelper {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationListener listener;
    private LocationCallback locationCallback;
    private DatabaseHelper dbHelper;
    private HashMap<String, Boolean> previouslyNearby = new HashMap<>(); // За следење на влез/излез
    private static final float NEARBY_DISTANCE = 100; // 100 метри
    private static final String CHANNEL_ID = "nearby_companies_channel";

    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
        void onNearbyStatusChanged(); // За освежување на листата
    }

    public LocationHelper(Context context, LocationListener listener) {
        this.context = context;
        this.listener = listener;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        dbHelper = new DatabaseHelper(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Блиски фирми",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Известете ме кога сум во близина или се оддалечувам од фирма");
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(String companyName, String address, boolean isEntering) {
        String title, message;
        if (isEntering) {
            title = "📍 Влеговте во близина!";
            message = "Фирмата " + companyName + " е на помалку од 100 метри од вас";
        } else {
            title = "🚶‍♂️ Се оддалечивте!";
            message = "Се оддалечивте повеќе од 100 метри од " + companyName;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        // LocationRequest за реално време следење
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000  // проверка на секои 3 секунди
        )
                .setMinUpdateIntervalMillis(1000)   // најбрзо на 1 секунда
                .setMaxUpdateDelayMillis(5000)      // максимално одложување 5 секунди
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = location;
                        System.out.println("📍 GPS: " + location.getLatitude() + ", " + location.getLongitude());
                        listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        checkNearbyCompanies(location);
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        System.out.println("📍 Real-time location tracking started");
    }

    private void checkNearbyCompanies(Location location) {
        ArrayList<Company> allCompanies = dbHelper.getAllCompanies();
        boolean needsRefresh = false;

        for (Company company : allCompanies) {
            if (company.getLatitude() == 0 && company.getLongitude() == 0) continue;

            float[] results = new float[1];
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    company.getLatitude(), company.getLongitude(),
                    results
            );
            float distance = results[0];
            String key = company.getId() + "_" + company.getName();
            boolean isNowNearby = (distance <= NEARBY_DISTANCE);
            Boolean wasNearby = previouslyNearby.get(key);

            // ВЛЕЗ ВО БЛИЗИНА (беше подалеку, сега е близу)
            if (isNowNearby && (wasNearby == null || !wasNearby)) {
                System.out.println("✅ ENTER: " + company.getName() + " (" + distance + "m)");
                sendNotification(company.getName(), company.getAddress(), true);
                previouslyNearby.put(key, true);
                needsRefresh = true;
            }
            // ИЗЛЕЗ ОД БЛИЗИНА (беше близу, сега е подалеку)
            else if (!isNowNearby && wasNearby != null && wasNearby) {
                System.out.println("❌ EXIT: " + company.getName() + " (" + distance + "m)");
                sendNotification(company.getName(), company.getAddress(), false);
                previouslyNearby.put(key, false);
                needsRefresh = true;
            }
        }

        // Освежи ја листата ако има промена
        if (needsRefresh && listener != null) {
            listener.onNearbyStatusChanged();
        }
    }

    public void stopLocationTracking() {
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            System.out.println("📍 Location tracking stopped");
        }
    }

    public void resetNearbyStates() {
        previouslyNearby.clear();
    }

    public boolean isNearby(double businessLat, double businessLon) {
        if (currentLocation == null) return false;
        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                businessLat, businessLon,
                results
        );
        return results[0] < NEARBY_DISTANCE;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}