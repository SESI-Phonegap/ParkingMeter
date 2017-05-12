package com.sesi.parkingmeter;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sesi.parkingmeter.activities.CameraReaderActivity;
import com.sesi.parkingmeter.fragments.HomeFragment;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.Utils;

public class MainDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawer;
    private LayoutInflater inflater;
    private AlertDialog dialog;
    private Builder builder;
    private static final int MAX_MIN = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        final String sTime = this.getIntent().getStringExtra("time");
        init();


    }

    public void init() {

        builder = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) MainDrawerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alarm) {
            createDialogConfigAlarm();
            //changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
            // Handle the camera action
        } else if (id == R.id.nav_findcar) {

        } else if (id == R.id.nav_report) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void createDialogConfigAlarm() {
        final View view = inflater.inflate(R.layout.dialog_alarm_preferences, null);

        final TextView tvMinutos = (TextView) view.findViewById(R.id.textViewMin);
        Button btnContinuar = (Button) view.findViewById(R.id.btn_guardar_dialog_alarm);
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int min = Integer.parseInt(tvMinutos.getText().toString());
                PreferenceUtilities.savePreferenceDefaultMinHour(v.getContext(),min);
                dialog.dismiss();
            }
        });

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarAlarm);
        seekBar.setMax(MAX_MIN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 10){
                    tvMinutos.setText("10");
                } else if (progress > 10 && progress <= 15){
                    tvMinutos.setText("15");
                } else if (progress > 15 && progress <= 20){
                    tvMinutos.setText("20");
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
}
