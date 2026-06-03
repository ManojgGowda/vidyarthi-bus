package com.vidyarthi.bus.models;

public class BusStop {
    private String stopId;
    private String stopName;
    private double latitude;
    private double longitude;
    private int sequenceOrder;  // 0 = first stop

    public BusStop() {}

    public BusStop(String stopId, String stopName, double latitude,
                   double longitude, int sequenceOrder) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequenceOrder = sequenceOrder;
    }

    public String getStopId()               { return stopId; }
    public void setStopId(String v)         { this.stopId = v; }

    public String getStopName()             { return stopName; }
    public void setStopName(String v)       { this.stopName = v; }

    public double getLatitude()             { return latitude; }
    public void setLatitude(double v)       { this.latitude = v; }

    public double getLongitude()            { return longitude; }
    public void setLongitude(double v)      { this.longitude = v; }

    public int getSequenceOrder()           { return sequenceOrder; }
    public void setSequenceOrder(int v)     { this.sequenceOrder = v; }
}
