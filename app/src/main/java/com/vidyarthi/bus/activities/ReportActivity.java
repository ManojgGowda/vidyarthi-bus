package com.vidyarthi.bus.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import com.google.android.material.snackbar.Snackbar;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.models.BusRoute;
import com.vidyarthi.bus.models.CrowdReport;
import com.vidyarthi.bus.utils.FirebaseHelper;
import com.vidyarthi.bus.utils.LocationHelper;
import com.vidyarthi.bus.utils.RouteDataProvider;

public class ReportActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID = "route_id";
    private static final int PERMISSION_REQUEST_LOCATION = 1001;

    // UI
    private CardView   cardEmpty, cardSeated, cardFull;
    private Button     btnSubmit;
    private TextView   tvLocationStatus;
    private ProgressBar pbLocation;

    // State
    private int        selectedLevel    = -1;
    private boolean    locationVerified = false;
    private double     userLat, userLng;
    private String     routeId;
    private BusRoute   currentRoute;

    private LocationHelper locationHelper;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);
        for (BusRoute r : RouteDataProvider.getAllRoutes()) {
            if (r.getRouteId().equals(routeId)) { currentRoute = r; break; }
        }

        locationHelper = new LocationHelper(this);
        firebaseHelper = new FirebaseHelper();

        setupToolbar();
        bindViews();
        checkLocationPermission();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Crowd Status");
        }
    }

    private void bindViews() {
        cardEmpty   = findViewById(R.id.card_empty);
        cardSeated  = findViewById(R.id.card_seated);
        cardFull    = findViewById(R.id.card_full);
        btnSubmit   = findViewById(R.id.btn_submit);
        tvLocationStatus = findViewById(R.id.tv_location_status);
        pbLocation  = findViewById(R.id.pb_location);

        cardEmpty.setOnClickListener(v  -> selectLevel(CrowdReport.LEVEL_EMPTY));
        cardSeated.setOnClickListener(v -> selectLevel(CrowdReport.LEVEL_SEATED));
        cardFull.setOnClickListener(v   -> selectLevel(CrowdReport.LEVEL_FULL));

        btnSubmit.setEnabled(false);
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    // -----------------------------------------------------------------------
    // Location verification (SUCCESS CRITERION: block off-route reporters)
    // -----------------------------------------------------------------------

    private void checkLocationPermission() {
        if (!locationHelper.hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                             Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_LOCATION);
        } else {
            verifyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            verifyLocation();
        } else {
            tvLocationStatus.setText("⚠ Location permission denied. Cannot report.");
            pbLocation.setVisibility(View.GONE);
        }
    }

    private void verifyLocation() {
        tvLocationStatus.setText("Verifying your location…");
        pbLocation.setVisibility(View.VISIBLE);

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                userLat = lat;
                userLng = lng;
                pbLocation.setVisibility(View.GONE);

                if (currentRoute != null &&
                        LocationHelper.isNearAnyStop(lat, lng, currentRoute.getStops())) {
                    String stop = LocationHelper.nearestStopName(lat, lng, currentRoute.getStops());
                    locationVerified = true;
                    tvLocationStatus.setText("✓ Location verified — near " +
                            (stop != null ? stop : "a bus stop"));
                    tvLocationStatus.setTextColor(getResources().getColor(R.color.green, null));
                    updateSubmitButton();
                } else {
                    // BLOCK THE REPORT — user is not near the route
                    locationVerified = false;
                    tvLocationStatus.setText(
                        "⚠ You are not near any stop on this route.\n" +
                        "Reports are only allowed within 500m of a bus stop.");
                    tvLocationStatus.setTextColor(getResources().getColor(R.color.red, null));
                    // Disable all option cards
                    cardEmpty.setAlpha(0.4f);  cardEmpty.setEnabled(false);
                    cardSeated.setAlpha(0.4f); cardSeated.setEnabled(false);
                    cardFull.setAlpha(0.4f);   cardFull.setEnabled(false);
                }
            }

            @Override
            public void onLocationError(String message) {
                pbLocation.setVisibility(View.GONE);
                tvLocationStatus.setText("⚠ Could not get location: " + message);
            }
        });
    }

    // -----------------------------------------------------------------------
    // Option selection
    // -----------------------------------------------------------------------

    private void selectLevel(int level) {
        if (!locationVerified) return;
        selectedLevel = level;

        // Reset all cards
        int defaultStroke = getResources().getColor(R.color.card_stroke, null);
        cardEmpty.setCardBackgroundColor(getResources().getColor(R.color.white, null));
        cardSeated.setCardBackgroundColor(getResources().getColor(R.color.white, null));
        cardFull.setCardBackgroundColor(getResources().getColor(R.color.white, null));

        // Highlight selected
        int highlight = getResources().getColor(R.color.saffron_light, null);
        switch (level) {
            case CrowdReport.LEVEL_EMPTY:  cardEmpty.setCardBackgroundColor(highlight);  break;
            case CrowdReport.LEVEL_SEATED: cardSeated.setCardBackgroundColor(highlight); break;
            case CrowdReport.LEVEL_FULL:   cardFull.setCardBackgroundColor(highlight);   break;
        }
        updateSubmitButton();
    }

    private void updateSubmitButton() {
        btnSubmit.setEnabled(locationVerified && selectedLevel >= 0);
    }

    // -----------------------------------------------------------------------
    // Submit
    // -----------------------------------------------------------------------

    private void submitReport() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting…");

        CrowdReport report = new CrowdReport(routeId,
                firebaseHelper.getCurrentUid(), selectedLevel, userLat, userLng);

        firebaseHelper.submitReport(report, new FirebaseHelper.ReportSubmitListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ReportActivity.this,
                        "✓ Report submitted! Expires in 15 minutes.",
                        Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(R.anim.none, R.anim.slide_out_down);
            }

            @Override
            public void onFailure(String message) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Report");
                Snackbar.make(btnSubmit, "Failed: " + message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
