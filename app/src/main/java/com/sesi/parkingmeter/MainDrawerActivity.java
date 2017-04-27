package com.sesi.parkingmeter;


import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.TextView;
import android.widget.TimePicker;

import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private TextView cardviewFecha, cardviewHora, cardviewHoraVence;
    private TextInputEditText tidHhora, tidHoraVence;
    private Button btn_inicio, btn_cancelar;
    private final static String ACTION_HOUR_VENCE = "";
    private int horaIni, horaVence;
    private int minIni, minVence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        init();


    }

    public void init() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btn_inicio = (Button) findViewById(R.id.btnInicio);
        btn_inicio.setOnClickListener(this);

        btn_cancelar = (Button) findViewById(R.id.btnCancelar);
        btn_cancelar.setOnClickListener(this);

        btn_inicio.setEnabled(false);
        btn_inicio.setAlpha(0.7f);

        btn_cancelar.setEnabled(Utils.getStatusButtonCancel(this));
        btn_cancelar.setAlpha(0.7f);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        cardviewFecha = (TextView) findViewById(R.id.cardviewFecha);
        cardviewHora = (TextView) findViewById(R.id.cardviewHora);
        cardviewHoraVence = (TextView) findViewById(R.id.cardViewHoraVence);

        tidHoraVence = (TextInputEditText) findViewById(R.id.textInputEditTextHoraVence);
        tidHoraVence.setOnClickListener(this);

        tidHhora = (TextInputEditText) findViewById(R.id.textInputEditTextHora);
        tidHhora.setOnClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Utils.showDate(cardviewFecha);
        Utils.showHour(cardviewHora, tidHhora);

        cardviewHoraVence.setText(Utils.getPreferencesFinalHour(this));
        tidHoraVence.setText(Utils.getPreferencesFinalHour(this));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    public void showDialogHour() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker1 = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                cardviewHora.setText(selectedHour + ":" + selectedMinute);
                tidHhora.setText(selectedHour + ":" + selectedMinute);
                horaIni = selectedHour;
                minIni = selectedMinute;
                checkHour();
            }
        }, hour, minute, true);
        horaIni = hour;
        minIni = minute;
        timePicker1.show();
        checkHour();
    }

    public void showDialogHourVence() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker1 = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                cardviewHoraVence.setText(selectedHour + ":" + selectedMinute);
                tidHoraVence.setText(selectedHour + ":" + selectedMinute);
                horaVence = selectedHour;
                minVence = selectedMinute;
                checkHour();
            }
        }, hour, minute, true);

        timePicker1.show();
        checkHour();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnInicio:
                int min_inicial = Utils.convertHourToMinutes(horaIni, minIni);
                int min_vence = Utils.convertHourToMinutes(horaVence, minVence);
                int calMin = (min_vence - min_inicial) - 15;
                int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                ReminderUtilities.scheduleChargingReminder(this, secondsStart, secondsStart);
                btn_inicio.setEnabled(false);
                btn_inicio.setAlpha(0.7f);
                btn_cancelar.setEnabled(true);
                btn_cancelar.setAlpha(1.0f);
                Utils.changeStatusButtonCancel(this,true);
                Utils.savePreferencesFinalHour(this,cardviewHoraVence.getText().toString());

                break;

            case R.id.btnCancelar:
                ReminderUtilities.dispatcher.cancelAll();
                btn_cancelar.setAlpha(0.7f);
                btn_cancelar.setEnabled(false);
                Utils.changeStatusButtonCancel(this,false);
                Utils.savePreferencesFinalHour(this,getResources().getString(R.string.horaCero));
                cardviewHora.setText(getResources().getString(R.string.horaCero);
                cardviewHoraVence.setText(getResources().getString(R.string.horaCero);
                tidHhora.setText(getResources().getString(R.string.horaCero);
                tidHoraVence.setText(getResources().getString(R.string.horaCero);
                break;

            case R.id.textInputEditTextHora:
                showDialogHour();
                break;

            case R.id.textInputEditTextHoraVence:
                showDialogHourVence();
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    public void checkHour() {
        if (!cardviewHora.getText().toString().equals(getResources().getString(R.string.horaCero)) &&
                !cardviewHoraVence.getText().toString().equals(getResources().getString(R.string.horaCero))) {
            btn_inicio.setEnabled(true);
            btn_inicio.setAlpha(1.0f);

        } else {
            btn_inicio.setEnabled(false);
            btn_inicio.setAlpha(0.7f);
            btn_cancelar.setEnabled(false);
            btn_cancelar.setAlpha(0.7f);
        }
    }
}
