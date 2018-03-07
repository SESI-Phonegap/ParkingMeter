package com.sesi.parkingmeter.view.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
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
import com.sesi.parkingmeter.view.utilities.Gps;
import com.sesi.parkingmeter.view.utilities.UtilGPS;
import com.sesi.parkingmeter.view.activity.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.jobservice.ParkingReminderFirebaseJobService;
import com.sesi.parkingmeter.data.api.googlemaps.task.DownloadTask;
import com.sesi.parkingmeter.view.utilities.Constants;
import com.sesi.parkingmeter.view.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.view.utilities.ReminderUtilities;
import com.sesi.parkingmeter.view.utilities.UtilNetwork;
import com.sesi.parkingmeter.view.utilities.Utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class ParkingType2Fragment extends Fragment implements Gps,View.OnClickListener, OnMapReadyCallback, SwitchCompat.OnCheckedChangeListener {
    private InterstitialAd mInterstitialAd;
    private AdView mAdview;
    private LatLng latLng;
    private LatLng vehicleLatLng;
    private TextView tvContador;
    private SwitchCompat switchLocation;
    private TextView tvDatos;

    private static final String FORMAT = "%02d:%02d:%02d";
    public static GoogleMap mMap;

    private Button btnInicio;
    private Button btnCancelar;
    private TextInputEditText tidHoraVence;
    private CountDownTimer countTimer;
    private RelativeLayout relativeLayoutDatos;
    private RelativeLayout relativeHora;
    private boolean statusHour = false;
    public static TextView tvDetails;
    private static final int TIME_LIMIT = 5;
    private UtilGPS sTracker;

    public ParkingType2Fragment() {
        // Required empty public constructor
    }

    public static ParkingType2Fragment newInstance() {
        ParkingType2Fragment fragment = new ParkingType2Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        if (!MainDrawerActivity.mIsSuscrip) {
            cargarPublicidad();
        }
        tvContador = getActivity().findViewById(R.id.contadorType2);
        switchLocation = getActivity().findViewById(R.id.switchLocationType2);
        switchLocation.setOnCheckedChangeListener(this);
        btnInicio = getActivity().findViewById(R.id.btnInicioType2);
        btnInicio.setOnClickListener(this);
        btnCancelar = getActivity().findViewById(R.id.btnCancelarType2);
        btnCancelar.setOnClickListener(this);
        btnInicio.setEnabled(false);
        btnInicio.setAlpha(0.7f);
        btnCancelar.setEnabled(false);
        btnCancelar.setAlpha(0.7f);
        tidHoraVence = getActivity().findViewById(R.id.textInputEditTextHoraVenceType2);
        tidHoraVence.setOnClickListener(this);
        relativeHora = getActivity().findViewById(R.id.relativeDetailsType2);
        tvDatos = getActivity().findViewById(R.id.tvDatosType2);
        tidHoraVence.addTextChangedListener(textWatcher);
        sTracker = new UtilGPS(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISION_LOCATION);
        } else {
            startTracker();
        }
    }

    public void cargarPublicidad() {
        if (UtilNetwork.isOnline(getActivity())) {
            mAdview = getActivity().findViewById(R.id.adViewType2);
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
        }else {
            ImageView imgPubli = getActivity().findViewById(R.id.img_publi_no_internet);
            imgPubli.setVisibility(View.VISIBLE);

        }
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
            case R.id.btnInicioType2:
                if (!tidHoraVence.getText().toString().equals("")) {
                    int iMinVence = Integer.parseInt(tidHoraVence.getText().toString());
                    if (0 < iMinVence) {
                        int iMinAlarm = PreferenceUtilities.getPreferenceDefaultMinHour(getContext());
                        int calMin = iMinVence - iMinAlarm;
                        if (calMin >= TIME_LIMIT) {
                            int secondsStartCount = (int) (TimeUnit.MINUTES.toSeconds(iMinVence));
                            int secondsStart = (int) (TimeUnit.MINUTES.toSeconds(calMin));
                            controlTimer(secondsStartCount);
                            ReminderUtilities.scheduleChargingReminder(getContext(), secondsStart, secondsStart);
                            enableButtonsStartTime();
                            PreferenceUtilities.changeStatusButtonCancel(getContext(), true);
                            if (!MainDrawerActivity.mIsSuscrip) {
                                showInterstitialAd();
                            }

                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.msgLimiteTiempo), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.msgHoraVencMenor), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_falta_dato), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnCancelarType2:
                cancel();
                break;
            default:
                Log.d("Invalida", "Opcion Invalida");
                break;
        }
    }

    public void showInterstitialAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    public void enableButtonsStartTime() {
        btnInicio.setEnabled(false);
        btnInicio.setAlpha(0.7f);
        btnCancelar.setEnabled(true);
        btnCancelar.setAlpha(1.0f);
        switchLocation.setEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        relativeLayoutDatos = getActivity().findViewById(R.id.relativeDatosType2);
        tvDetails = getActivity().findViewById(R.id.tvDatosType2);
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
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(500);
                alphaAnimation.setRepeatMode(1);
                relativeLayoutDatos.startAnimation(alphaAnimation);
                relativeLayoutDatos.animate().translationY(280).setDuration(600).start();
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

        addMarkers();

    }

    public void addMarkers() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISION_LOCATION);
        } else {
            if (null != mMap) {
                mMap.clear();
                addUserMarker();
                addCarMarker();
            }
        }
    }

    public void addUserMarker() {
        Bitmap bitmapUser;
        if (Build.VERSION.SDK_INT >= 21) {
            bitmapUser = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_human_male));
        } else {
            bitmapUser = BitmapFactory.decodeResource(getResources(), R.drawable.ic_human_male_black_24dp);
        }
        Location location = getLocation();

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

    public void addCarMarker() {
        if (vehicleLatLng != null) {
            Bitmap bitmapCar;
            if (Build.VERSION.SDK_INT >= 21) {
                bitmapCar = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_sedan_car_front));
            } else {
                bitmapCar = BitmapFactory.decodeResource(getResources(), R.drawable.sedan_car_front);
            }
            mMap.addMarker(new MarkerOptions()
                    .position(vehicleLatLng)
                    .title("Mi Auto")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapCar)));

            /*    mMap.addPolyline(new PolylineOptions()
                        .add(latLng,vehichleLatLng)
                        .width(5).color(Color.BLUE)
                        .geodesic(true)
                        .clickable(true));*/
            String url = Utils.obtenerDireccionesURL(latLng, vehicleLatLng);
            DownloadTask downloadTask = new DownloadTask(tvDetails,mMap);
            downloadTask.execute(url);

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
                String contador = "" + String.format(Locale.US,FORMAT,
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

    public void cancel() {
        if (ReminderUtilities.dispatcher != null) {
            ReminderUtilities.dispatcher.cancelAll();
        }
        ReminderUtilities.sInitialized = false;
        btnCancelar.setAlpha(0.7f);
        btnCancelar.setEnabled(false);
        PreferenceUtilities.changeStatusButtonCancel(getContext(), false);
        PreferenceUtilities.savePreferencesFinalHour(getContext(), getResources().getString(R.string.horaCero));
        tidHoraVence.setText("");
        if (countTimer != null && mMap != null) {
            countTimer.cancel();
            mMap.clear();
        }
        tvContador.setText(getString(R.string.cero));
        switchLocation.setChecked(false);
        tvDatos.setText("");
        switchLocation.setEnabled(true);
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

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Empty
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                btnInicio.setEnabled(true);
                btnInicio.setAlpha(1);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //Empty
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (UtilNetwork.isOnline(getActivity())) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISION_LOCATION);
                } else {
                    if (vehicleLatLng == null) {
                        if (canGetLocation()) {
                            Location location = getLocation();
                            if (location != null) {
                                vehicleLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                addMarkers();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Activa tu localización para activar esta opción.", Toast.LENGTH_LONG).show();
                            switchLocation.setChecked(false);

                            sTracker = new UtilGPS(getActivity());
                            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISION_LOCATION);
                            } else {
                                startTracker();
                            }
                                /*Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                getActivity().startActivity(callGPSSettingIntent);*/
                        }
                    }
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
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
}
