package com.vidyarthi.bus.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ValueEventListener;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.adapters.StopAdapter;
import com.vidyarthi.bus.models.BusRoute;
import com.vidyarthi.bus.models.CrowdReport;
import com.vidyarthi.bus.utils.FirebaseHelper;
import com.vidyarthi.bus.utils.RouteDataProvider;

import java.util.List;

public class CrowdDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID   = "route_id";

    // UI references
    private ProgressBar         crowdProgressBar;
    private TextView            tvCrowdLabel;
    private TextView            tvReportCount;
    private TextView            tvSeatsInfo;
    private ExtendedFloatingActionButton fabReport;
    private View                crowdIndicator;

    // Data
    private String              routeId;
    private BusRoute            currentRoute;
    private FirebaseHelper      firebaseHelper;
    private ValueEventListener  crowdListener;

    // Colors
    private static final int COLOR_GREEN = Color.parseColor("#1AAD4E");
    private static final int COLOR_AMBER = Color.parseColor("#F5A623");
    private static final int COLOR_RED   = Color.parseColor("#E8322A");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_detail);

        routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);

        // Find current route object
        for (BusRoute r : RouteDataProvider.getAllRoutes()) {
            if (r.getRouteId().equals(routeId)) {
                currentRoute = r;
                break;
            }
        }

        setupToolbar();
        bindViews();
        setupStopList();
        attachFirebaseListener();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                currentRoute != null ? currentRoute.getRouteName() : "Crowd Meter");
        }
    }

    private void bindViews() {
        crowdProgressBar = findViewById(R.id.progress_crowd);
        tvCrowdLabel     = findViewById(R.id.tv_crowd_label);
        tvReportCount    = findViewById(R.id.tv_report_count);
        tvSeatsInfo      = findViewById(R.id.tv_seats_info);
        crowdIndicator   = findViewById(R.id.view_crowd_indicator);
        fabReport        = findViewById(R.id.fab_report);

        if (currentRoute != null) {
            TextView tvRoute = findViewById(R.id.tv_route_description);
            if (tvRoute != null) tvRoute.setText(currentRoute.getRouteDescription());
        }

        fabReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_ROUTE_ID, routeId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.none);
        });
    }

    private void setupStopList() {
        if (currentRoute == null) return;
        RecyclerView rv = findViewById(R.id.rv_stops);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new StopAdapter(currentRoute.getStops()));
    }

    private void attachFirebaseListener() {
        if (routeId == null) return;
        firebaseHelper = new FirebaseHelper();

        crowdListener = firebaseHelper.listenToCrowd(routeId,
                new FirebaseHelper.CrowdUpdateListener() {
            @Override
            public void onCrowdUpdated(int crowdPercent, int crowdLevel, int reportCount) {
                updateCrowdUI(crowdPercent, crowdLevel, reportCount);
            }

            @Override
            public void onError(String message) {
                Snackbar.make(fabReport, "Update error: " + message,
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCrowdUI(int percent, int crowdLevel, int reportCount) {
        // Animate the progress bar
        crowdProgressBar.setProgress(percent, true);

        int color;
        String label;
        String seatsInfo;

        switch (crowdLevel) {
            case CrowdReport.LEVEL_FULL:
                color     = COLOR_RED;
                label     = "Bus Full 🔴";
                seatsInfo = "No seats — standing only";
                break;
            case CrowdReport.LEVEL_SEATED:
                color     = COLOR_AMBER;
                label     = "Getting Full 🟡";
                seatsInfo = "Few seats left — board quickly";
                break;
            default:
                color     = COLOR_GREEN;
                label     = "Seats Available 🟢";
                seatsInfo = "Plenty of seats";
                break;
        }

        tvCrowdLabel.setText(label);
        tvCrowdLabel.setTextColor(color);
        tvSeatsInfo.setText(seatsInfo);
        crowdProgressBar.getProgressDrawable().setTint(color);
        crowdIndicator.setBackgroundColor(color);

        if (reportCount == 0) {
            tvReportCount.setText("No reports yet — be the first!");
        } else {
            tvReportCount.setText(reportCount + " student" +
                (reportCount == 1 ? "" : "s") + " reported · expires in 15 min");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseHelper != null && crowdListener != null && routeId != null) {
            firebaseHelper.detachCrowdListener(routeId, crowdListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
