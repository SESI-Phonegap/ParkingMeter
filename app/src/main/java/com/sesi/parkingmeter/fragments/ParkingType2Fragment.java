package com.sesi.parkingmeter.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.task.DownloadTask;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.concurrent.TimeUnit;


public class ParkingType2Fragment extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {
    private InterstitialAd mInterstitialAd;
    private AdView mAdview;
    private LatLng latLng;
    private TextView tvContador;
    private SwitchCompat switchLocation;
    private TextView tvDatos;
    private Bitmap bitmapUser;
    private Bitmap bitmapCar;
    public final static int PERMISION_LOCATION = 1002;
    private static final String FORMAT = "%02d:%02d:%02d";
    public static GoogleMap mMap;
    private LocationListener locationListener;
    private Location location;
    private Button btn_inicio, btn_cancelar;
    private TextInputEditText tidHoraVence;
    private CountDownTimer countTimer;
    private RelativeLayout relativeLayoutDatos, relativeHora, relativeMap;
    private int minVence, horaVence;
    private boolean statusHour = false;
    public static TextView tvDetails;
    private static final int TIME_LIMIT = 10;

    public ParkingType2Fragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ParkingType2Fragment newInstance() {
        ParkingType2Fragment fragment = new ParkingType2Fragment();
        Bundle args = new Bundle();
        //   args.putString(ARG_PARAM1, param1);
        //   args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //   mParam1 = getArguments().getString(ARG_PARAM1);
            //   mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parking_type2, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapType2);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init() {
        mAdview = (AdView) getActivity().findViewById(R.id.adViewType2);
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
        mInterstitialAd.setAdUnitId(getString(R.string.banner_intersticial_2));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        tvContador = (TextView) getActivity().findViewById(R.id.contadorType2);
        switchLocation = (SwitchCompat) getActivity().findViewById(R.id.switchLocationType2);
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
                            Log.d("AAA-", "Latitud: " + MainDrawerActivity.latLng.latitude + " Long: " + MainDrawerActivity.latLng.longitude);
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

        btn_inicio = (Button) getActivity().findViewById(R.id.btnInicioType2);
        btn_inicio.setOnClickListener(this);

        btn_cancelar = (Button) getActivity().findViewById(R.id.btnCancelarType2);
        btn_cancelar.setOnClickListener(this);

        btn_inicio.setEnabled(false);
        btn_inicio.setAlpha(0.7f);

        btn_cancelar.setEnabled(false);
        btn_cancelar.setAlpha(0.7f);

        tidHoraVence = (TextInputEditText) getActivity().findViewById(R.id.textInputEditTextHoraVenceType2);
        tidHoraVence.setOnClickListener(this);

        relativeHora = (RelativeLayout) getActivity().findViewById(R.id.relativeDetailsType2);
        relativeMap = (RelativeLayout) getActivity().findViewById(R.id.relativeMapType2);

        tvDatos = (TextView) getActivity().findViewById(R.id.tvDatosType2);

        tidHoraVence.addTextChangedListener(textWatcher);
        cancel();
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
        //mListener = null;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInicioType2:
                //  int min_inicial = Utils.convertActualHourToMinutes();
                if (!tidHoraVence.getText().toString().equals("")) {
                    int min_vence = Integer.parseInt(tidHoraVence.getText().toString());
                    if (0 < min_vence) {
                        int iMinAlarm = PreferenceUtilities.getPreferenceDefaultMinHour(getContext());
                        Log.d("AAA-", "" + iMinAlarm);
                        int calMin = min_vence - iMinAlarm;
                        Log.d("AAA-cal-min", "" + calMin);
                        if (calMin >= TIME_LIMIT) {
                            int secondsStartCount = (int) (TimeUnit.MINUTES.toSeconds(min_vence));
                            int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                            controlTimer(secondsStartCount);
                            ReminderUtilities.scheduleChargingReminder(getContext(), secondsStart, secondsStart);
                            btn_inicio.setEnabled(false);
                            btn_inicio.setAlpha(0.7f);
                            btn_cancelar.setEnabled(true);
                            btn_cancelar.setAlpha(1.0f);
                            switchLocation.setEnabled(false);
                            PreferenceUtilities.changeStatusButtonCancel(getContext(), true);

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
                } else {
                    Toast.makeText(getContext(),getString(R.string.msg_falta_dato),Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnCancelarType2:
                cancel();
                if (ReminderUtilities.dispatcher != null) {
                    ReminderUtilities.dispatcher.cancelAll();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        relativeLayoutDatos = (RelativeLayout) getActivity().findViewById(R.id.relativeDatosType2);
        tvDetails = (TextView) getActivity().findViewById(R.id.tvDatosType2);
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
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
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(500);
                alphaAnimation.setRepeatMode(1);
                relativeLayoutDatos.startAnimation(alphaAnimation);
                relativeLayoutDatos.animate().translationY(280).setDuration(600).start();
                // relativeLayoutDatos.setAlpha(0);
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
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(1100);
                alphaAnimation.setRepeatMode(1);
                relativeLayoutDatos.startAnimation(alphaAnimation);
                relativeLayoutDatos.animate().translationY(0).setDuration(600).start();
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

    public void addMarkers() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISION_LOCATION);
        } else {
           // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.clear();


            if (Build.VERSION.SDK_INT >= 21) {
                bitmapUser = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_human_male));
            } else {
                bitmapUser = BitmapFactory.decodeResource(getResources(), R.drawable.ic_human_male_black_24dp);
            }
           // location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

            if (MainDrawerActivity.latLng != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    bitmapCar = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_sedan_car_front));
                } else {
                    bitmapCar = BitmapFactory.decodeResource(getResources(), R.drawable.sedan_car_front);
                }
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
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

    private String obtenerDireccionesURL(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String parameters = "units=metric&mode=walking&" + str_origin + "&" + str_dest + "&" + sensor;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    public void cancel() {
        ReminderUtilities.sInitialized = false;
        btn_cancelar.setAlpha(0.7f);
        btn_cancelar.setEnabled(false);
        PreferenceUtilities.changeStatusButtonCancel(getContext(), false);
        PreferenceUtilities.savePreferencesFinalHour(getContext(), getResources().getString(R.string.horaCero));
        //    PreferenceUtilities.savePreferenceHourIni(this,getResources().getString(R.string.horaCero));
        tidHoraVence.setText(getResources().getString(R.string.ceroMinutes));
        if (countTimer != null && mMap != null) {
            countTimer.cancel();
            mMap.clear();
        }
        tvContador.setText(getString(R.string.cero));
        switchLocation.setChecked(false);
        tvDatos.setText("");
        switchLocation.setEnabled(true);

    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                btn_inicio.setEnabled(true);
                btn_inicio.setAlpha(1);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}
