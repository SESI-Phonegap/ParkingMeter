package com.sesi.parkingmeter.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.task.DownloadTask;
import com.sesi.parkingmeter.utilities.Utils;

public class MapFragment extends Fragment implements OnMapReadyCallback {


    public static GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    public final static int PERMISION_LOCATION = 1002;
    LatLng latLng;
    public static TextView tvDetails;
    private RelativeLayout relativeLayoutDatos;

    public MapFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
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
     /*
*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        relativeLayoutDatos = (RelativeLayout) getActivity().findViewById(R.id.relativeDatos);
        tvDetails = (TextView) getActivity().findViewById(R.id.tvDatos);
        mMap = googleMap;

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
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
             /*   AlphaAnimation alphaAnimation = new AlphaAnimation(0,0);
                alphaAnimation.setDuration(300);
                alphaAnimation.setRepeatMode(1);
                relativeLayoutDatos.startAnimation(alphaAnimation);*/
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
                alphaAnimation.setDuration(1100);
                alphaAnimation.setRepeatMode(1);
                relativeLayoutDatos.startAnimation(alphaAnimation);
                relativeLayoutDatos.animate().translationY(0).setDuration(600).start();
            }
        });


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
    /*            mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng)).setTitle("Yo");
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));*/
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


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISION_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.clear();


            Bitmap bitmapUser = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_human_male));
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Yo")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapUser)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.setMinZoomPreference(15.0f);
            mMap.setMaxZoomPreference(23.0f);


            if (MainDrawerActivity.latLng != null) {
                Bitmap bitmapCar = getBitmap((VectorDrawable) getActivity().getResources().getDrawable(R.drawable.ic_sedan_car_front));
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

    private String obtenerDireccionesURL(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String parameters = "units=metric&mode=walking&"+str_origin + "&" + str_dest + "&" + sensor;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

}