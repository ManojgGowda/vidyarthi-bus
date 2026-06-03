package com.vidyarthi.bus.models;

import java.util.List;

public class BusRoute {
    private String routeId;
    private String routeName;         // "Route 7-B"
    private String routeDescription; // "Nagpur Depot → GEC Campus"
    private List<BusStop> stops;
    private int totalSeats;           // default 52

    // Required for Firebase deserialization
    public BusRoute() {}

    public BusRoute(String routeId, String routeName, String routeDescription,
                    List<BusStop> stops, int totalSeats) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.routeDescription = routeDescription;
        this.stops = stops;
        this.totalSeats = totalSeats;
    }

    // --- Getters & Setters ---
    public String getRouteId()                  { return routeId; }
    public void setRouteId(String v)            { this.routeId = v; }

    public String getRouteName()                { return routeName; }
    public void setRouteName(String v)          { this.routeName = v; }

    public String getRouteDescription()         { return routeDescription; }
    public void setRouteDescription(String v)   { this.routeDescription = v; }

    public List<BusStop> getStops()             { return stops; }
    public void setStops(List<BusStop> v)       { this.stops = v; }

    public int getTotalSeats()                  { return totalSeats; }
    public void setTotalSeats(int v)            { this.totalSeats = v; }
}
