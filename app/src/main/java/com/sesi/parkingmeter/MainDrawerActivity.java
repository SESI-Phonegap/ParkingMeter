package com.sesi.parkingmeter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.sesi.parkingmeter.fragments.HomeFragment;
import com.sesi.parkingmeter.fragments.ParkingType2Fragment;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.UtilGPS;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private LayoutInflater inflater;
    private AlertDialog dialog;
    private Builder builder;
    private static final int MAX_MIN = 20;
    private int iPreferenceMin;
    public final static int PERMISION_LOCATION = 1002;
    public static LatLng latLng;
    public static UtilGPS sTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id));
        init();


    }

    public void init() {
        sTracker = new UtilGPS(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISION_LOCATION);
        } else {
            startTracker();
        }

        builder = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) MainDrawerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
        iPreferenceMin = PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext());


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_parking1:
                changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
                break;
            case R.id.nav_parking2:
                changeFragment(ParkingType2Fragment.newInstance(), R.id.mainFrame, false, false);
                break;
            case R.id.nav_alarm:
                createDialogConfigAlarm();
                break;

            case R.id.nav_share:
                sharedSocial();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(Fragment fragment, int resource, boolean isRoot, boolean backStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (isRoot) {
            transaction.add(resource, fragment);
        } else {
            transaction.replace(resource, fragment);
        }

        if (backStack) {
            transaction.addToBackStack(null);
        }
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.enter_from_left);
        //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void createDialogConfigAlarm() {
        final View view = inflater.inflate(R.layout.dialog_alarm_preferences, null);

        final SwitchCompat switchVibrate = (SwitchCompat) view.findViewById(R.id.switchVibrador);
        switchVibrate.setChecked(PreferenceUtilities.getPreferenceDefaultVibrate(getApplicationContext()));
        final SwitchCompat switchSound = (SwitchCompat) view.findViewById(R.id.switchSonido);
        switchSound.setChecked(PreferenceUtilities.getPreferenceDefaultSound(getApplicationContext()));

        final TextView tvMinutos = (TextView) view.findViewById(R.id.textViewMin);
        Button btnContinuar = (Button) view.findViewById(R.id.btn_guardar_dialog_alarm);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (switchSound.isChecked() | switchVibrate.isChecked()) {
                    //   int min = Integer.parseInt(tvMinutos.getText().toString());
                    PreferenceUtilities.savePreferenceDefaultMinHour(v.getContext(), iPreferenceMin);
                    PreferenceUtilities.savePreferenceDefaultVibrate(v.getContext(), switchVibrate.isChecked());
                    PreferenceUtilities.savePreferenceDefaultSound(v.getContext(), switchSound.isChecked());
                    dialog.dismiss();
                } else {
                    Toast.makeText(v.getContext(), getResources().getString(R.string.msg_check_switch_alert), Toast.LENGTH_LONG).show();
                }
            }
        });

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarAlarm);
        seekBar.setMax(MAX_MIN);
        seekBar.setProgress(iPreferenceMin);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 10) {
                    tvMinutos.setText("10");
                    iPreferenceMin = 10;
                } else if (progress > 10 && progress <= 15) {
                    tvMinutos.setText("15");
                    iPreferenceMin = 15;
                } else if (progress > 15 && progress <= 20) {
                    tvMinutos.setText("20");
                    iPreferenceMin = 20;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   startTracker();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.msgPermissionDeniedLocation), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (ReminderUtilities.dispatcher != null) {
            ReminderUtilities.dispatcher.cancelAll();
        }
        super.onDestroy();

    }

    public void sharedSocial(){
        List<Intent> targetShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(shareIntent, 0);
        if (!resInfos.isEmpty()) {
            System.out.println("Have package");
            for (ResolveInfo resInfo : resInfos) {
                String packageName = resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);

/*         if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                 || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                 || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                 || packageName.contains("com.google.android.gm") || packageName.contains("com.facebook.orca")
                 || packageName.contains("com.yahoo.mobile") || packageName.contains("com.skype.raider")
                 || packageName.contains("com.android.mms")|| packageName.contains("com.linkedin.android")
                 || packageName.contains("com.google.android.apps.messaging")) {*/
                if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                        || packageName.contains("com.whatsapp")
                        || packageName.contains("com.google.android.apps.plus")) {
                    Intent intent = new Intent();

                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.putExtra("AppName", resInfo.loadLabel(pm).toString());
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "URL de la APP");
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.compartir));
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if (!targetShareIntents.isEmpty()) {
                Collections.sort(targetShareIntents, new Comparator<Intent>() {
                    @Override
                    public int compare(Intent o1, Intent o2) {
                        return o1.getStringExtra("AppName").compareTo(o2.getStringExtra("AppName"));
                    }
                });
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Select app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            } else {
                Toast.makeText(getApplicationContext(), "No app to share.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*********************************************
     * LOCATION OPERATIONS - GPS TRACKING
     ********************************************/

    /**
     * Start location tracker
     */
    public static void startTracker() {
        try {
            if (sTracker != null) {
                sTracker.startTracking();
            }
        } catch (Exception ex) {
            Log.d("STARTGPS-- ", ex.getMessage());
        }
    }

    /**
     * Stop location tracker
     */
    public static void stopTracking() {
        try {
            if (sTracker != null) {
                sTracker.stopUsingGPS();
            }
        } catch (Exception ex) {
            Log.d("STOPGPS--: ", ex.getMessage());
        }

    }

    /**
     * Check if possible get the location user
     *
     * @return
     */
    public static boolean canGetLocation() {
        try {
            return (sTracker != null && sTracker.canGetLocation());
        } catch (Exception e) {
            Log.e("GETLOCATION--: ", e.getMessage());
        }
        return false;
    }

    /**
     * Gets the location user
     *
     * @return
     */
    public static Location getLocation() {
        if (sTracker != null)
            return sTracker.getCurrentLocation();
        else
            return null;
    }

}
