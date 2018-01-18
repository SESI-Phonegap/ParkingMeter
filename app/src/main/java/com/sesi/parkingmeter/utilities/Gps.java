package com.sesi.parkingmeter.utilities;


import android.location.Location;

public interface Gps {

    /*********************************************
     * LOCATION OPERATIONS - GPS TRACKING
     ********************************************/

    /**
     * Start location tracker
     */

    void startTracker();

    void stopTracking();

    boolean canGetLocation();

    Location getLocation();
}
