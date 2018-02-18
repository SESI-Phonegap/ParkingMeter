package com.sesi.parkingmeter.view.utilities;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Service that allows to track the current location of the device
 *
 * @author mnaunay
 *
 */
public class UtilGPS extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    private static final long MIN_TIME_BW_UPDATES = 0; // 0 minute
    private static final int MIN_ACCURACY_DISTANCE = 75; // Street >10 meters and <= 100 meters

    private final Context mContext;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location location;
    private LocationManager locationManager;

    /**
     * Constructor
     *
     * @param context
     *            Context
     */
    public UtilGPS(Context context) {
        this.mContext = context;
    }

    @Deprecated
    public Location getLocation() {
        try {
            if (canGetLocation()) {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.removeUpdates(this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.removeUpdates(this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
            }
        } catch (Exception e) {
            Log.e("GPSTracker::getLocation", e.getMessage());
        }

        return location;
    }

    /**
     * Start tracking location
     */
    public void startTracking() {
        try {
            locationManager  = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            canGetLocation   = (isGPSEnabled || isNetworkEnabled);
            if (canGetLocation) {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.i("Request Location Net","Request Location Network Provider");
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    locationManager.sendExtraCommand("gps", "delete_aiding_data", null);
                    Log.i("Request Location GPS","Request Location GPS Provider");
                }
            } else {
                Log.e("Location not available","Location services are not available");
            }
        } catch (Exception e) {
            Log.e("GPS:startTracking", e.getMessage());
        }
    }

    /**
     * Stop tracking location
     * */
    public void stopUsingGPS() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(UtilGPS.this);
                locationManager = null;
            }
            canGetLocation = false;
        } catch (Exception e) {
            Log.e("GPS::stopUsingGPS: ", e.getMessage());
        }
    }

    /**
     * Get current location
     *
     * @return Location
     */
    public synchronized Location getCurrentLocation() {
        return location;
    }

    /**
     * Method to check if best network provider
     *
     * @return boolean
     * */
    public boolean canGetLocation() {
        return canGetLocation;
    }

    public boolean checkLocationService() {
        boolean status = false;
        try {
            LocationManager lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPS || isNetwork) {
                status = true;
            }
        } catch (Exception ex) {
            Log.e("GPSTrackerService:", ex.getMessage());
        }
        return status;
    }

    @Override
    public void onLocationChanged(Location currenLocation) {
        try {
            if ((location == null) || (currenLocation.getAccuracy() < MIN_ACCURACY_DISTANCE)) {
                this.location = currenLocation;
            }
        } catch (Exception ex) {
            Log.e("GPSTrackerService:", ex.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
     //  EMPTY
    }

    @Override
    public void onProviderEnabled(String provider) {
       // EMPTY
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //EMPTY
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}