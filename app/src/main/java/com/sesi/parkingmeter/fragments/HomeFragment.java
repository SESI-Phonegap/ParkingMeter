package com.sesi.parkingmeter.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
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
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.task.DownloadTask;
import com.sesi.parkingmeter.ui.camera.vision.OcrCaptureActivity;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class HomeFragment extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {

    private TextView tvContador;
    private TextInputEditText tidHoraVence;
    private Button btn_inicio, btn_cancelar;
    private final static String ACTION_HOUR_VENCE = "";
    private int horaIni, horaVence;
    private int minIni, minVence;
    private int min_inicial;
    private static final int TIME_LIMIT = 10;
   // private FloatingActionButton fab;
    private CountDownTimer countTimer;
    private static final String FORMAT = "%02d:%02d:%02d";
    public static GoogleMap mMap;
    //   private LocationManager locationManager;
    private LocationListener locationListener;
    public final static int PERMISION_LOCATION = 1002;
    private LatLng latLng;
    public static TextView tvDetails;
    private RelativeLayout relativeLayoutDatos, relativeMap;
    private ConstraintLayout relativeHora;
    private Location location;
    private boolean statusHour = false;
    private InterstitialAd mInterstitialAd;
    private AdView mAdview;
    private SwitchCompat switchLocation;
    private TextView tvDatos;
    private Bitmap bitmapUser;
    private Bitmap bitmapCar;
    private static final int RC_OCR_CAPTURE = 9003;

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
        mAdview = (AdView) getActivity().findViewById(R.id.adView);
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
        tvContador = (TextView) getActivity().findViewById(R.id.contador);
        switchLocation = (SwitchCompat) getActivity().findViewById(R.id.switchLocation);
        switchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISION_LOCATION);
                    } else {
                        if (MainDrawerActivity.latLng != null) {

                        } else {
                            if (MainDrawerActivity.canGetLocation()) {
                                Location location = MainDrawerActivity.getLocation();
                                if (location != null) {
                                    MainDrawerActivity.latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    addMarkers();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Activa tu localización para activar esta opción.", Toast.LENGTH_LONG).show();
                                /*Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                getActivity().startActivity(callGPSSettingIntent);*/
                            }
                        }
                    }

                }
            }
        });

        btn_inicio = (Button) getActivity().findViewById(R.id.btnInicio);
        btn_inicio.setOnClickListener(this);

        btn_cancelar = (Button) getActivity().findViewById(R.id.btnCancelar);
        btn_cancelar.setOnClickListener(this);

        btn_inicio.setEnabled(false);
        btn_inicio.setAlpha(0.7f);

        btn_cancelar.setEnabled(false);
        btn_cancelar.setAlpha(0.7f);


        tidHoraVence = (TextInputEditText) getActivity().findViewById(R.id.textInputEditTextHoraVence);
        tidHoraVence.setOnClickListener(this);


       // this.fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
       // this.fab.setOnClickListener(this);

        relativeHora = (ConstraintLayout) getActivity().findViewById(R.id.relativeDetails);
        relativeMap = (RelativeLayout) getActivity().findViewById(R.id.relativeMap);

        tvDatos = (TextView) getActivity().findViewById(R.id.tvDatos);
        cancel();
        relativeLayoutDatos = (RelativeLayout) getActivity().findViewById(R.id.relativeDatos);

    }


    @Override
    public void onResume() {
        super.onResume();
        btn_cancelar.setEnabled(PreferenceUtilities.getStatusButtonCancel(getContext()));
        btn_cancelar.setAlpha(0.7f);
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
    public void onDestroyView() {
        super.onDestroyView();
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
                    int timerMin = (min_vence - min_inicial);
                    Log.d("AAA-cal-min", "" + calMin);
                    Log.d("AAA-Mint",""+timerMin);
                    if (calMin >= TIME_LIMIT) {

                        //Segundos para lanzar la alerta
                        int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                        //Segundos para Contador
                        int secondsContador = (int) (TimeUnit.MINUTES.toSeconds(timerMin));
                        controlTimer(secondsContador);
                        ReminderUtilities.scheduleChargingReminder(getContext(), secondsStart, secondsStart);
                        btn_inicio.setEnabled(false);
                        btn_inicio.setAlpha(0.7f);
                        btn_cancelar.setEnabled(true);
                        btn_cancelar.setAlpha(1.0f);
                        PreferenceUtilities.changeStatusButtonCancel(getContext(), true);
                        tidHoraVence.setEnabled(false);
                        tidHoraVence.setAlpha(0.7f);
                        switchLocation.setEnabled(false);

                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
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
                if (ReminderUtilities.dispatcher != null) {
                    ReminderUtilities.dispatcher.cancelAll();
                    tidHoraVence.setEnabled(true);
                    tidHoraVence.setAlpha(1);
                }
                break;

            case R.id.textInputEditTextHoraVence:
                showDialogHourVence();
                break;

            /*case R.id.fab:
                Intent cameraActivity = new Intent(getContext(), OcrCaptureActivity.class);
                cameraActivity.putExtra(OcrCaptureActivity.AutoFocus, true);
                cameraActivity.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult(cameraActivity, RC_OCR_CAPTURE);
                break; */
        }

    }


    public void showDialogHourVence() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker1 = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                tidHoraVence.setText(selectedHour + ":" + selectedMinute);
                horaVence = selectedHour;
                minVence = selectedMinute;
                btn_inicio.setEnabled(true);
                btn_inicio.setAlpha(1);

            }
        }, hour, minute, true);

        timePicker1.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    final String sData = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    Toast.makeText(this.getContext(), sData, Toast.LENGTH_LONG).show();
                    Log.d("TEXT-HORA-",sData);
                    btn_inicio.setEnabled(true);
                    btn_inicio.setAlpha(1);
                }
            } else {
                Toast.makeText(getActivity(),"No se puede leer el ticket.",Toast.LENGTH_LONG).show();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void controlTimer(int min) {
        countTimer = new CountDownTimer(min * 1000 + 100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String contador = "" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                tvContador.setText(contador);
                // updateTimer((int) millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                tvContador.setText(getString(R.string.cero));
                btn_cancelar.setEnabled(false);
                btn_cancelar.setAlpha(0.7f);
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

        tvDetails = (TextView) getActivity().findViewById(R.id.tvDatos);
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (getActivity().findViewById(R.id.tablet_view) == null) {
                    if (!statusHour) {
                        relativeHora.animate().translationY(-450).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                relativeHora.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
                        // relativeMap.animate().translationY(-300).setDuration(700).start();
                        //relativeHora.setVisibility(View.GONE);
                        statusHour = true;
                    } else {
                        relativeHora.animate().translationY(0).alpha(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                relativeHora.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

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
                    // relativeLayoutDatos.setAlpha(0);
                }
            }
        });


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
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


        //locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                addMarkers();
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        addMarkers();


    }

    private String obtenerDireccionesURL(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String parameters = "units=metric&mode=walking&" + str_origin + "&" + str_dest + "&" + sensor;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
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
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISION_LOCATION);
        } else {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.clear();


            if (Build.VERSION.SDK_INT >= 21) {
                bitmapUser = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_human_male));
            } else {
                bitmapUser = BitmapFactory.decodeResource(getResources(), R.drawable.ic_human_male_black_24dp);
            }
            location = MainDrawerActivity.getLocation();

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

            if (MainDrawerActivity.latLng != null && latLng != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    bitmapCar = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_sedan_car_front));
                } else {
                    bitmapCar = BitmapFactory.decodeResource(getResources(), R.drawable.sedan_car_front);
                }
                relativeLayoutDatos.setVisibility(View.VISIBLE);
                mMap.addMarker(new MarkerOptions()
                        .position(MainDrawerActivity.latLng)
                        .title("Mi Auto")
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmapCar)));

            /*    mMap.addPolyline(new PolylineOptions()
                        .add(latLng,MainDrawerActivity.latLng)
                        .width(5).color(Color.BLUE)
                        .geodesic(true)
                        .clickable(true));*/

                String url = obtenerDireccionesURL(latLng, MainDrawerActivity.latLng);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);

            }


        }
    }

    public void cancel() {
        ReminderUtilities.sInitialized = false;
        btn_cancelar.setAlpha(0.7f);
        btn_cancelar.setEnabled(false);
        PreferenceUtilities.changeStatusButtonCancel(getContext(), false);
        PreferenceUtilities.savePreferencesFinalHour(getContext(), getResources().getString(R.string.horaCero));
        //    PreferenceUtilities.savePreferenceHourIni(this,getResources().getString(R.string.horaCero));
        tidHoraVence.setText(getResources().getString(R.string.horaCero));
        if (countTimer != null && mMap != null) {
            countTimer.cancel();
            mMap.clear();
        }
        tvContador.setText(getString(R.string.cero));
        switchLocation.setChecked(false);
        switchLocation.setEnabled(true);
        tvDatos.setText("");
        MainDrawerActivity.latLng = null;

    }
}
