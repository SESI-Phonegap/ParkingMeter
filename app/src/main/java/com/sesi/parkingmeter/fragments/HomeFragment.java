package com.sesi.parkingmeter.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.activities.CameraReaderActivity;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView cardviewFecha, cardviewHora, cardviewHoraVence;
    private TextInputEditText tidHhora, tidHoraVence;
    private Button btn_inicio, btn_cancelar;
    private final static String ACTION_HOUR_VENCE = "";
    private int horaIni, horaVence;
    private int minIni, minVence;
    private int min_inicial;
    private static final int TIME_LIMIT = 10;
    private FloatingActionButton fab;


    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init() {

        btn_inicio = (Button) getActivity().findViewById(R.id.btnInicio);
        btn_inicio.setOnClickListener(this);

        btn_cancelar = (Button) getActivity().findViewById(R.id.btnCancelar);
        btn_cancelar.setOnClickListener(this);

        btn_inicio.setEnabled(false);
        btn_inicio.setAlpha(0.7f);

        btn_cancelar.setEnabled(PreferenceUtilities.getStatusButtonCancel(getContext()));
        btn_cancelar.setAlpha(0.7f);


        cardviewFecha = (TextView) getActivity().findViewById(R.id.cardviewFecha);
        cardviewHora = (TextView) getActivity().findViewById(R.id.cardviewHora);
        cardviewHoraVence = (TextView) getActivity().findViewById(R.id.cardViewHoraVence);

        tidHoraVence = (TextInputEditText) getActivity().findViewById(R.id.textInputEditTextHoraVence);
        tidHoraVence.setOnClickListener(this);

        tidHhora = (TextInputEditText) getActivity().findViewById(R.id.textInputEditTextHora);
        tidHhora.setOnClickListener(this);

        Utils.showDate(cardviewFecha);
        Utils.showHour(cardviewHora, tidHhora);

        cardviewHoraVence.setText(PreferenceUtilities.getPreferencesFinalHour(getContext()));
        tidHoraVence.setText(PreferenceUtilities.getPreferencesFinalHour(getContext()));

        this.fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        this.fab.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        Utils.showHour(cardviewHora, tidHhora);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnInicio:
                int min_inicial = Utils.convertActualHourToMinutes();
                int min_vence = Utils.convertHourToMinutes(horaVence, minVence);
                if (min_inicial < min_vence) {
                    int iMinAlarm = PreferenceUtilities.getPreferenceDefaultMinHour(getContext());
                    Log.d("AAA-", "" + iMinAlarm);
                    int calMin = (min_vence - min_inicial) - iMinAlarm;
                    Log.d("AAA-cal-min", "" + calMin);
                  //  if (calMin >= TIME_LIMIT) {
                        int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                        ReminderUtilities.scheduleChargingReminder(getContext(), secondsStart, secondsStart);
                        btn_inicio.setEnabled(false);
                        btn_inicio.setAlpha(0.7f);
                        btn_cancelar.setEnabled(true);
                        btn_cancelar.setAlpha(1.0f);
                        PreferenceUtilities.changeStatusButtonCancel(getContext(), true);
                        PreferenceUtilities.savePreferencesFinalHour(getContext(), cardviewHoraVence.getText().toString());
                  /*  } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.msgLimiteTiempo), Toast.LENGTH_LONG).show();
                    }*/
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.msgHoraVencMenor), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnCancelar:
                ReminderUtilities.sInitialized = false;
                ReminderUtilities.dispatcher.cancelAll();
                btn_cancelar.setAlpha(0.7f);
                btn_cancelar.setEnabled(false);
                PreferenceUtilities.changeStatusButtonCancel(getContext(), false);
                PreferenceUtilities.savePreferencesFinalHour(getContext(), getResources().getString(R.string.horaCero));
                //    PreferenceUtilities.savePreferenceHourIni(this,getResources().getString(R.string.horaCero));

                cardviewHoraVence.setText(getResources().getString(R.string.horaCero));
                Utils.showHour(cardviewHora, tidHhora);
                tidHoraVence.setText(getResources().getString(R.string.horaCero));
                break;

            case R.id.textInputEditTextHora:
                //showDialogHour();
                break;

            case R.id.textInputEditTextHoraVence:
                showDialogHourVence();
                break;

            case R.id.fab:
                final Intent cameraActivity = new Intent(this.getContext(), CameraReaderActivity.class);
                startActivityForResult(cameraActivity, 9999);
                break;
        }

    }


    public void showDialogHourVence() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker1 = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                cardviewHoraVence.setText(selectedHour + ":" + selectedMinute);
                tidHoraVence.setText(selectedHour + ":" + selectedMinute);
                horaVence = selectedHour;
                minVence = selectedMinute;
                checkHour();
            }
        }, hour, minute, false);

        timePicker1.show();
        checkHour();
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

    public void updateGuiButtonCancel() {
        boolean status = PreferenceUtilities.getStatusButtonCancel(getContext());
        if (status) {
            btn_cancelar.setAlpha(1.0f);
        } else {
            btn_cancelar.setAlpha(0.7f);
        }
        btn_cancelar.setEnabled(status);

    }

    public void updateGuiTextviewInitalHour() {
        String hour = PreferenceUtilities.getPreferencesInitialHour(getContext());
        cardviewHora.setText(hour);
        tidHhora.setText(hour);
    }

    public void updateGuiTexviewFinalHour() {
        String hour = PreferenceUtilities.getPreferencesFinalHour(getContext());
        cardviewHoraVence.setText(hour);
        tidHoraVence.setText(hour);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.STATUS_BUTTON_CANCEL.equals(key)) {
            updateGuiButtonCancel();
        } else if (PreferenceUtilities.SAVE_INITIAL_HOUR.equals(key)) {
            updateGuiTextviewInitalHour();
        } else if (PreferenceUtilities.SAVE_FINAL_HOUR.equals(key)) {
            updateGuiTexviewFinalHour();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            final String sData = data.getStringExtra("time");
            Toast.makeText(this.getContext(), sData, Toast.LENGTH_LONG).show();
        }
    }
}
