package com.sesi.parkingmeter.utilities;


import android.location.Location;

public interface Gps {

    /**
     * Start location tracker LOCATION OPERATIONS - GPS TRACKING
     */

    void startTracker();

    void stopTracking();

    boolean canGetLocation();

    Location getLocation();
}
