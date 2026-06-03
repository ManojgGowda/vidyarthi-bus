package com.vidyarthi.bus.utils;

import com.vidyarthi.bus.models.Alternative;
import com.vidyarthi.bus.models.BusRoute;
import com.vidyarthi.bus.models.BusStop;
import java.util.Arrays;
import java.util.List;

/**
 * Static seed data for bus routes and alternative transport options.
 * In production, load this from Firebase /routes node at app start.
 *
 * Coordinates are in Nagpur, Maharashtra (real approximate values).
 */
public class RouteDataProvider {

    public static List<BusRoute> getAllRoutes() {
        return Arrays.asList(route7B(), route12A(), route3C());
    }

    // ---- Route 7-B --------------------------------------------------------
    private static BusRoute route7B() {
        List<BusStop> stops = Arrays.asList(
            new BusStop("7b_1", "Nagpur Depot",   21.1458, 79.0882, 0),
            new BusStop("7b_2", "Sitabuldi",       21.1469, 79.0830, 1),
            new BusStop("7b_3", "Kamptee Road",    21.1543, 79.0918, 2),
            new BusStop("7b_4", "Nari Road",       21.1602, 79.0972, 3),
            new BusStop("7b_5", "GEC Campus",      21.1678, 79.1045, 4)
        );
        return new BusRoute("route_7b", "Route 7-B",
                "Nagpur Depot → GEC Campus", stops, 52);
    }

    // ---- Route 12-A -------------------------------------------------------
    private static BusRoute route12A() {
        List<BusStop> stops = Arrays.asList(
            new BusStop("12a_1", "Wardha Road",    21.1193, 79.0522, 0),
            new BusStop("12a_2", "Shankar Nagar",  21.1290, 79.0600, 1),
            new BusStop("12a_3", "Ambazari Lake",  21.1340, 79.0525, 2),
            new BusStop("12a_4", "VNIT College",   21.1317, 79.0501, 3)
        );
        return new BusRoute("route_12a", "Route 12-A",
                "Wardha Road → VNIT College", stops, 52);
    }

    // ---- Route 3-C --------------------------------------------------------
    private static BusRoute route3C() {
        List<BusStop> stops = Arrays.asList(
            new BusStop("3c_1", "Hingna",          21.1120, 78.9803, 0),
            new BusStop("3c_2", "Wadi Road",       21.1180, 78.9920, 1),
            new BusStop("3c_3", "Nandanvan",       21.1257, 79.0093, 2),
            new BusStop("3c_4", "Laxminarayan",    21.1324, 79.0203, 3)
        );
        return new BusRoute("route_3c", "Route 3-C",
                "Hingna → Laxminarayan College", stops, 52);
    }

    // ---- Alternatives near Kamptee Road stop ------------------------------
    public static List<Alternative> getAlternatives() {
        return Arrays.asList(
            new Alternative("Raju Shared Auto",
                    "Kamptee Rd → GEC · ₹20/seat · ~6 seats",
                    "+919876543210", "auto"),
            new Alternative("Sunita Auto Stand",
                    "Nari Road → VNIT · ₹15/seat",
                    "+919856321470", "auto"),
            new Alternative("College Cycle Shuttle",
                    "Free · 7:30–8:30 AM · From Sitabuldi",
                    "+919823456780", "cycle"),
            new Alternative("MSRTC Bus #54",
                    "Nagpur Main → College Road · ₹12",
                    "1800-123-4567", "bus")
        );
    }
}
