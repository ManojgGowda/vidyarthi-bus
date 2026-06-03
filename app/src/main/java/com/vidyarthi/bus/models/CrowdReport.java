package com.vidyarthi.bus.models;

public class CrowdReport {
    // Crowd level constants
    public static final int LEVEL_EMPTY   = 0;  // Green  — plenty of seats
    public static final int LEVEL_SEATED  = 1;  // Amber  — few seats left
    public static final int LEVEL_FULL    = 2;  // Red    — no seats, standing only

    private String reportId;
    private String routeId;
    private String userId;       // Firebase anonymous UID
    private int    crowdLevel;   // 0 / 1 / 2
    private long   timestamp;    // System.currentTimeMillis()
    private double userLat;
    private double userLng;

    // Expiry window: 15 minutes (in milliseconds)
    public static final long EXPIRY_MS = 15 * 60 * 1000L;

    public CrowdReport() {}

    public CrowdReport(String routeId, String userId, int crowdLevel,
                       double userLat, double userLng) {
        this.routeId    = routeId;
        this.userId     = userId;
        this.crowdLevel = crowdLevel;
        this.userLat    = userLat;
        this.userLng    = userLng;
        this.timestamp  = System.currentTimeMillis();
    }

    /** Returns true if this report is still within the 15-minute window */
    public boolean isValid() {
        return (System.currentTimeMillis() - timestamp) < EXPIRY_MS;
    }

    /** Human-readable label for the crowd level */
    public static String labelFor(int level) {
        switch (level) {
            case LEVEL_EMPTY:  return "Seats Available";
            case LEVEL_SEATED: return "Getting Full";
            case LEVEL_FULL:   return "Bus Full";
            default:           return "Unknown";
        }
    }

    // --- Getters & Setters ---
    public String getReportId()             { return reportId; }
    public void setReportId(String v)       { this.reportId = v; }

    public String getRouteId()              { return routeId; }
    public void setRouteId(String v)        { this.routeId = v; }

    public String getUserId()               { return userId; }
    public void setUserId(String v)         { this.userId = v; }

    public int getCrowdLevel()              { return crowdLevel; }
    public void setCrowdLevel(int v)        { this.crowdLevel = v; }

    public long getTimestamp()              { return timestamp; }
    public void setTimestamp(long v)        { this.timestamp = v; }

    public double getUserLat()              { return userLat; }
    public void setUserLat(double v)        { this.userLat = v; }

    public double getUserLng()              { return userLng; }
    public void setUserLng(double v)        { this.userLng = v; }
}
