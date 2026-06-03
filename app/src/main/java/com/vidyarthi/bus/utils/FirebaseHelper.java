package com.vidyarthi.bus.utils;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.vidyarthi.bus.models.CrowdReport;
import java.util.ArrayList;
import java.util.List;

/**
 * All Firebase Realtime Database operations.
 *
 * DB Structure:
 *   /routes/{routeId}/reports/{uid} → CrowdReport
 *
 * - Each user writes exactly one report per route (keyed by uid).
 * - On read, we filter out reports older than 15 minutes client-side
 *   and compute the aggregate crowd level.
 */
public class FirebaseHelper {

    // DB paths
    private static final String PATH_ROUTES  = "routes";
    private static final String PATH_REPORTS = "reports";

    private final DatabaseReference db;
    private final FirebaseAuth       auth;

    public interface CrowdUpdateListener {
        /**
         * Called whenever the crowd data changes for a route.
         * @param crowdPercent  0–100 fill percentage for the progress bar
         * @param crowdLevel    CrowdReport.LEVEL_EMPTY / SEATED / FULL
         * @param reportCount   number of valid (non-expired) reports
         */
        void onCrowdUpdated(int crowdPercent, int crowdLevel, int reportCount);
        void onError(String message);
    }

    public interface ReportSubmitListener {
        void onSuccess();
        void onFailure(String message);
    }

    public FirebaseHelper() {
        this.db   = FirebaseDatabase.getInstance().getReference();
        this.auth = FirebaseAuth.getInstance();
    }

    // -----------------------------------------------------------------------
    // Authentication
    // -----------------------------------------------------------------------

    /** Signs in anonymously (no login screen needed for students). */
    public void signInAnonymously(Runnable onComplete) {
        if (auth.getCurrentUser() != null) {
            onComplete.run();
            return;
        }
        auth.signInAnonymously().addOnCompleteListener(task -> onComplete.run());
    }

    /** Returns the current user's UID (null if not signed in). */
    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // -----------------------------------------------------------------------
    // Real-time crowd listener  (call from CrowdDetailActivity)
    // -----------------------------------------------------------------------

    /**
     * Attaches a ValueEventListener to /routes/{routeId}/reports.
     * Returns the listener reference so the Activity can detach it onStop().
     */
    public ValueEventListener listenToCrowd(@NonNull String routeId,
                                             @NonNull CrowdUpdateListener listener) {
        DatabaseReference ref = db.child(PATH_ROUTES)
                                  .child(routeId)
                                  .child(PATH_REPORTS);

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CrowdReport> validReports = new ArrayList<>();
                long now = System.currentTimeMillis();

                for (DataSnapshot child : snapshot.getChildren()) {
                    CrowdReport report = child.getValue(CrowdReport.class);
                    if (report != null && (now - report.getTimestamp()) < CrowdReport.EXPIRY_MS) {
                        validReports.add(report);
                    }
                }

                int crowdPercent = computeCrowdPercent(validReports);
                int crowdLevel   = computeCrowdLevel(crowdPercent);
                listener.onCrowdUpdated(crowdPercent, crowdLevel, validReports.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };

        ref.addValueEventListener(vel);
        return vel;
    }

    /** Detach a previously attached listener. */
    public void detachCrowdListener(@NonNull String routeId,
                                    @NonNull ValueEventListener listener) {
        db.child(PATH_ROUTES)
          .child(routeId)
          .child(PATH_REPORTS)
          .removeEventListener(listener);
    }

    // -----------------------------------------------------------------------
    // Submit a crowd report
    // -----------------------------------------------------------------------

    /**
     * Writes (or overwrites) the user's report for a route.
     * Key = UID so each user has exactly one active report per route.
     */
    public void submitReport(@NonNull CrowdReport report,
                             @NonNull ReportSubmitListener listener) {
        String uid = getCurrentUid();
        if (uid == null) {
            listener.onFailure("Not authenticated");
            return;
        }
        report.setUserId(uid);
        report.setReportId(uid);

        db.child(PATH_ROUTES)
          .child(report.getRouteId())
          .child(PATH_REPORTS)
          .child(uid)
          .setValue(report)
          .addOnSuccessListener(unused -> listener.onSuccess())
          .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // -----------------------------------------------------------------------
    // Crowd computation helpers
    // -----------------------------------------------------------------------

    /**
     * Weighted average: each report contributes its level (0/1/2).
     * Returns a 0–100 fill percentage for the ProgressBar.
     *
     * Thresholds:
     *   0–33%  → EMPTY  (green)
     *   34–66% → SEATED (amber)
     *   67%+   → FULL   (red)
     */
    private int computeCrowdPercent(List<CrowdReport> reports) {
        if (reports.isEmpty()) return 10; // default: mostly empty
        int sum = 0;
        for (CrowdReport r : reports) sum += r.getCrowdLevel();
        // Max possible sum = reports.size() * 2  (all FULL)
        double ratio = (double) sum / (reports.size() * 2.0);
        return (int) Math.round(ratio * 100);
    }

    private int computeCrowdLevel(int percent) {
        if (percent >= 67) return CrowdReport.LEVEL_FULL;
        if (percent >= 34) return CrowdReport.LEVEL_SEATED;
        return CrowdReport.LEVEL_EMPTY;
    }
}
