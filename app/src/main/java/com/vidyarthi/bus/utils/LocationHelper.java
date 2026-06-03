package com.vidyarthi.bus.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.*;
import com.vidyarthi.bus.models.BusStop;
import java.util.List;

/**
 * Wraps FusedLocationProviderClient.
 * Key method: isNearAnyStop() — enforces the "must be near the route" rule
 * before allowing a crowd report.
 */
public class LocationHelper {

    /** Radius (in metres) within which a user is considered "at a stop" */
    public static final float GEOFENCE_RADIUS_METRES = 500f;

    public interface LocationCallback {
        void onLocationReceived(double lat, double lng);
        void onLocationError(String message);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Context context) {
        this.context     = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /** Returns true if both fine/coarse location permissions are granted */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests a single fresh location fix.
     * Falls back to the last-known location if a fresh fix isn't available quickly.
     */
    public void getCurrentLocation(@NonNull LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        // Try last-known location first (fast)
        try {
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    // Request a fresh fix
                    requestFreshLocation(callback);
                }
            }).addOnFailureListener(e -> callback.onLocationError(e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }

    private void requestFreshLocation(@NonNull LocationCallback callback) {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMaxUpdates(1)
                .build();

        com.google.android.gms.location.LocationCallback gmsCallback =
                new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    callback.onLocationReceived(loc.getLatitude(), loc.getLongitude());
                } else {
                    callback.onLocationError("Could not get current location");
                }
                fusedClient.removeLocationUpdates(this);
            }
        };

        try {
            fusedClient.requestLocationUpdates(request, gmsCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            callback.onLocationError("Security exception: " + e.getMessage());
        }
    }

    /**
     * Returns true if (lat, lng) is within GEOFENCE_RADIUS_METRES of
     * at least one stop on the route.
     *
     * This is the "Success Criterion" check that prevents off-route reporting.
     */
    public static boolean isNearAnyStop(double userLat, double userLng,
                                        List<BusStop> stops) {
        if (stops == null || stops.isEmpty()) return false;
        for (BusStop stop : stops) {
            float[] result = new float[1];
            Location.distanceBetween(userLat, userLng,
                    stop.getLatitude(), stop.getLongitude(), result);
            if (result[0] <= GEOFENCE_RADIUS_METRES) return true;
        }
        return false;
    }

    /**
     * Returns the name of the nearest stop, or null if none within radius.
     */
    public static String nearestStopName(double userLat, double userLng,
                                         List<BusStop> stops) {
        if (stops == null || stops.isEmpty()) return null;
        BusStop nearest = null;
        float minDist = Float.MAX_VALUE;
        for (BusStop stop : stops) {
            float[] result = new float[1];
            Location.distanceBetween(userLat, userLng,
                    stop.getLatitude(), stop.getLongitude(), result);
            if (result[0] < minDist) {
                minDist  = result[0];
                nearest  = stop;
            }
        }
        return (nearest != null && minDist <= GEOFENCE_RADIUS_METRES)
                ? nearest.getStopName() : null;
    }
}
