package com.sesi.parkingmeter.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.sesi.parkingmeter.MainDrawerActivity;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.fragments.HomeFragment;

import java.io.IOException;

public class CameraReaderActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<TextBlock> {

    private SurfaceView cameraView;
    private CameraSource cameraSource;
    public final static int PERMISION_CAMERA = 1001;
    private final String SCANING_REGEX = "[0-9]{2}\\:[0-9]{2}";
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_reader);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(this.getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {

            this.setCameraSource(new CameraSource.Builder(getApplication(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build());
            this.setCameraView(surfaceView);
            this.getCameraView().getHolder().addCallback(this);
            textRecognizer.setProcessor(this);
        }
    }


    public SurfaceView getCameraView() {
        return cameraView;
    }

    public void setCameraView(SurfaceView cameraView) {
        this.cameraView = cameraView;
    }

    public CameraSource getCameraSource() {
        return cameraSource;
    }

    public void setCameraSource(CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraReaderActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISION_CAMERA);
                return;
            } else {
                this.getCameraSource().start(this.getCameraView().getHolder());
            }
        } catch (final IOException e) {
            Log.d("CAMERA-", e.getMessage());
        } catch (RuntimeException ex){
            Log.d("CAMERA-RUN-",ex.getMessage());
            Log.d("CAMERA-RUN-",ex.toString());
            Log.d("CAMERA-RUN-",ex.getLocalizedMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("SUFACE-DESTROYED","Se destruyo");
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections detections) {
        final SparseArray<TextBlock> items = detections.getDetectedItems();
        if (items.size() != 0) {
            for (int i = 0; i < items.size(); i++) {
                final TextBlock item = items.valueAt(i);
                if (null != item && item.getValue().matches("[0-9]{2}\\:[0-9]{2}")) {
                    Log.e("Matches", "Text matches with REGEX " + item.getValue());
                    this.getCameraSource().stop();
                    final Intent returnIntent = new Intent();
                    returnIntent.putExtra("time", item.getValue());
                    this.setResult(Activity.RESULT_OK, returnIntent);
                    this.finish();
                    break;
                } else {
                    Log.e("Not Matches", "Text dont matches with REGEX " + item.getValue());
                }
            }
        }
    }

}
