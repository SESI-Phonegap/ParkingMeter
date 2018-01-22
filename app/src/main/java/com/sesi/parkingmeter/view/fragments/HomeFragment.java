package com.sesi.parkingmeter.view.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sesi.parkingmeter.utilities.Gps;
import com.sesi.parkingmeter.view.activity.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.sync.ParkingReminderFirebaseJobService;
import com.sesi.parkingmeter.task.DownloadTask;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.UtilGPS;
import com.sesi.parkingmeter.utilities.UtilNetwork;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements Gps, View.OnClickListener, SwitchCompat.OnCheckedChangeListener,SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback{

    private TextView tvContador;
    private TextInputEditText tidHoraVence;
    private Button btnInicio;
    private Button btnCancelar;
    private int horaVence;
    private int minVence;
    private static final int TIME_LIMIT = 5;
    private CountDownTimer countTimer;
    private static final String FORMAT = "%02d:%02d:%02d";
    public static GoogleMap mMap;
    public static final int PERMISION_LOCATION = 1002;
    private LatLng latLng;
    private LatLng vehicleLatLng;
    public  TextView tvDetails;
    private RelativeLayout relativeLayoutDatos;
    private ConstraintLayout relativeHora;
    private boolean statusHour = false;
    private InterstitialAd mInterstitialAd;
    private AdView mAdview;
    private SwitchCompat switchLocation;
    private TextView tvDatos;
    private UtilGPS sTracker;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init() {
        if (!MainDrawerActivity.mIsSuscrip){
            cargaPublicidad();
        }

        tvContador = getActivity().findViewById(R.id.contador);
        switchLocation = getActivity().findViewById(R.id.switchLocation);
        switchLocation.setOnCheckedChangeListener(this);
        btnInicio = getActivity().findViewById(R.id.btnInicio);
        btnInicio.setOnClickListener(this);
        btnCancelar = getActivity().findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(this);
        btnInicio.setEnabled(false);
        btnInicio.setAlpha(0.7f);
        btnCancelar.setEnabled(false);
        btnCancelar.setAlpha(0.7f);
        tidHoraVence = getActivity().findViewById(R.id.textInputEditTextHoraVence);
        tidHoraVence.setOnClickListener(this);
        relativeHora = getActivity().findViewById(R.id.relativeDetails);
        tvDatos = getActivity().findViewById(R.id.tvDatos);
        relativeLayoutDatos = getActivity().findViewById(R.id.relativeDatos);

        sTracker = new UtilGPS(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISION_LOCATION);
        } else {
            startTracker();
        }
    }

    public void cargaPublicidad(){
        mAdview = getActivity().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
        mAdview.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mAdview.loadAd(new AdRequest.Builder().build());
            }
        });

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.banner_intersticial));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();
        btnCancelar.setEnabled(PreferenceUtilities.getStatusButtonCancel(getContext()));
        btnCancelar.setAlpha(0.7f);
        if (null != vehicleLatLng) {
            addMarkers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnInicio:
                int iMinInicial = Utils.convertActualHourToMinutes();
                int iMinVence = Utils.convertHourToMinutes(horaVence, minVence);
                if (iMinInicial < iMinVence) {
                    int iMinAlarm = PreferenceUtilities.getPreferenceDefaultMinHour(getContext());
                    int calMin = (iMinVence - iMinInicial) - iMinAlarm;
                    int timerMin = (iMinVence - iMinInicial);
                    if (calMin >= TIME_LIMIT) {

                        //Segundos para lanzar la alerta
                        int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                        //Segundos para Contador
                        int secondsContador = (int) (TimeUnit.MINUTES.toSeconds(timerMin));
                        controlTimer(secondsContador);
                        ReminderUtilities.scheduleChargingReminder(getContext(), secondsStart, secondsStart);
                        btnInicio.setEnabled(false);
                        btnInicio.setAlpha(0.7f);
                        btnCancelar.setEnabled(true);
                        btnCancelar.setAlpha(1.0f);
                        PreferenceUtilities.changeStatusButtonCancel(getContext(), true);
                        tidHoraVence.setEnabled(false);
                        tidHoraVence.setAlpha(0.7f);
                        switchLocation.setEnabled(false);

                        if (!MainDrawerActivity.mIsSuscrip){
                            showInterstitialAd();
                        }
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.msgLimiteTiempo), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.msgHoraVencMenor), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnCancelar:
                cancel();
                break;

            case R.id.textInputEditTextHoraVence:
                showDialogHourVence();
                break;

            default:
                Log.d("No valid option:","Opcion no valida");
                break;
            /*case R.id.fab:
                Intent cameraActivity = new Intent(getContext(), OcrCaptureActivity.class);
                cameraActivity.putExtra(OcrCaptureActivity.AutoFocus, true);
                cameraActivity.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult(cameraActivity, RC_OCR_CAPTURE);
                break; */
        }

    }

    public void showInterstitialAd(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }


    public void showDialogHourVence() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker1 = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                String sHora = selectedHour + ":" + selectedMinute;
                tidHoraVence.setText(sHora);
                horaVence = selectedHour;
                minVence = selectedMinute;
                btnInicio.setEnabled(true);
                btnInicio.setAlpha(1);

            }
        }, hour, minute, true);

        timePicker1.show();
    }


    public void updateGuiButtonCancel() {
        boolean status = PreferenceUtilities.getStatusButtonCancel(getContext());
        if (status) {
            btnCancelar.setAlpha(1.0f);
        } else {
            btnCancelar.setAlpha(0.7f);
        }
        btnCancelar.setEnabled(status);

    }

    public void updateGuiTextviewInitalHour() {
        PreferenceUtilities.getPreferencesInitialHour(getContext());
    }

    public void updateGuiTexviewFinalHour() {
        String hour = PreferenceUtilities.getPreferencesFinalHour(getContext());
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


    public void controlTimer(int min) {
        countTimer = new CountDownTimer(min * 1000 + 100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String contador =  String.format(Locale.US,FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                tvContador.setText(contador);

            }

            @Override
            public void onFinish() {
                tvContador.setText(getString(R.string.cero));
                btnCancelar.setEnabled(false);
                btnCancelar.setAlpha(0.7f);
                resetTimer();
            }
        }.start();
    }


    public void resetTimer() {
        tvContador.setText(getString(R.string.cero));

        countTimer.cancel();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        tvDetails = getActivity().findViewById(R.id.tvDatos);
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (getActivity().findViewById(R.id.tablet_view) == null) {
                    if (!statusHour) {
                        relativeHora.animate().translationY(-450).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                //Empty
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                relativeHora.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                //Empty
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                //Empty
                            }
                        }).start();
                        statusHour = true;
                    } else {
                        relativeHora.animate().translationY(0).alpha(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                //Empty
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                relativeHora.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                //Empty
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                //Empty
                            }
                        }).start();

                        statusHour = false;
                    }
                }
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (relativeLayoutDatos.getVisibility() == View.VISIBLE) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                    alphaAnimation.setDuration(500);
                    alphaAnimation.setRepeatMode(1);
                    relativeLayoutDatos.startAnimation(alphaAnimation);
                    relativeLayoutDatos.animate().translationY(280).setDuration(600).start();

                }
            }
        });


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //Empty
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (relativeLayoutDatos.getVisibility() == View.VISIBLE) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(1100);
                    alphaAnimation.setRepeatMode(1);
                    relativeLayoutDatos.startAnimation(alphaAnimation);
                    relativeLayoutDatos.animate().translationY(0).setDuration(600).start();
                }
            }
        });

        addMarkers();


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public void addMarkers() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISION_LOCATION);
        } else {
           if (null != mMap) {
                mMap.clear();
               addUserMarker();
               addCarMarker();
            }
        }
    }

    public void addUserMarker(){
        Bitmap bitmapUser;
        Location location;
        if (Build.VERSION.SDK_INT >= 21) {
            bitmapUser = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_human_male));
        } else {
            bitmapUser = BitmapFactory.decodeResource(getResources(), R.drawable.ic_human_male_black_24dp);
        }
        location = getLocation();

        if (location != null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Yo")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapUser)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.setMinZoomPreference(15.0f);
            mMap.setMaxZoomPreference(23.0f);
        }
    }
    public void addCarMarker(){
        Bitmap bitmapCar;
        if (vehicleLatLng != null && latLng != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                bitmapCar = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_sedan_car_front));
            } else {
                bitmapCar = BitmapFactory.decodeResource(getResources(), R.drawable.sedan_car_front);
            }
            relativeLayoutDatos.setVisibility(View.VISIBLE);
            mMap.addMarker(new MarkerOptions()
                    .position(vehicleLatLng)
                    .title("Mi Auto")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapCar)));

            String url = Utils.obtenerDireccionesURL(latLng,vehicleLatLng);
            DownloadTask downloadTask = new DownloadTask(tvDetails,mMap);
            downloadTask.execute(url);

        }
    }

    public void cancel() {
        if (ReminderUtilities.dispatcher != null) {
            ReminderUtilities.dispatcher.cancelAll();
        }
        ReminderUtilities.sInitialized = false;
        btnCancelar.setAlpha(0.7f);
        btnCancelar.setEnabled(false);
        PreferenceUtilities.changeStatusButtonCancel(getContext(), false);
        PreferenceUtilities.savePreferencesFinalHour(getContext(), getResources().getString(R.string.horaCero));
        tidHoraVence.setText(getResources().getString(R.string.horaCero));
        if (countTimer != null && mMap != null) {
            countTimer.cancel();
            mMap.clear();
        }
        tvContador.setText(getString(R.string.cero));
        switchLocation.setChecked(false);
        switchLocation.setEnabled(true);
        tvDatos.setText("");
        vehicleLatLng = null;

        if (ReminderUtilities.dispatcher != null) {
            ReminderUtilities.dispatcher.cancelAll();
            tidHoraVence.setEnabled(true);
            tidHoraVence.setAlpha(1);
        }

        if (null != ParkingReminderFirebaseJobService.mBackgroundTask){
            ParkingReminderFirebaseJobService.mBackgroundTask.cancel(true);
            Log.d("TASK CANCEL---","True");
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (UtilNetwork.isOnline(getActivity())) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISION_LOCATION);
                } else {
                    if (null == vehicleLatLng) {
                        if (canGetLocation()) {
                            Location location = getLocation();
                            if (location != null) {
                                vehicleLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                addMarkers();
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.noGps), Toast.LENGTH_LONG).show();
                            switchLocation.setChecked(false);

                            sTracker = new UtilGPS(getActivity());
                            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISION_LOCATION);
                            } else {
                                startTracker();
                            }

                        }
                    }
                }
            } else {
                Toast.makeText(getActivity(),getString(R.string.noInternet),Toast.LENGTH_LONG).show();
                switchLocation.setChecked(false);
            }
        } else {
            mMap.clear();
            tvDetails.setText("");
            vehicleLatLng = null;
        }
    }

    @Override
    public void startTracker() {
        try {
            if (sTracker != null) {
                sTracker.startTracking();
            }
        } catch (Exception ex) {
            Log.d("STARTGPS-- ", ex.getMessage());
        }
    }

    @Override
    public void stopTracking() {
        try {
            if (sTracker != null) {
                sTracker.stopUsingGPS();
            }
        } catch (Exception ex) {
            Log.d("STOPGPS--: ", ex.getMessage());
        }
    }

    @Override
    public boolean canGetLocation() {
        try {
            return (sTracker != null && sTracker.canGetLocation());
        } catch (Exception e) {
            Log.e("GETLOCATION--: ", e.getMessage());
        }
        return false;
    }

    @Override
    public Location getLocation() {
        if (sTracker != null)
            return sTracker.getCurrentLocation();
        else
            return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracker();

                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.msgPermissionDeniedLocation), Toast.LENGTH_LONG).show();
                }
                break;

            default:
                Log.d("Invalida", "Opcion Invalida");
                break;
        }
    }
}
