package com.sesi.parkingmeter.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.data.api.billing.BillingManager;
import com.sesi.parkingmeter.data.api.billing.BillingProvider;
import com.sesi.parkingmeter.view.fragments.PurchaseFragment;
import com.sesi.parkingmeter.view.utilities.SoundGallery;
import com.sesi.parkingmeter.view.fragments.HomeFragment;
import com.sesi.parkingmeter.view.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.view.utilities.UtilNetwork;
import com.sesi.parkingmeter.view.utilities.Utils;
import java.io.File;
import java.util.List;
import java.util.Objects;
import static com.sesi.parkingmeter.data.api.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;


public class MainDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BillingProvider {


    // Debug tag, for logging
    private static final String TAG = "MainDrawerActivity";
    private static final String MONTH_SKU = "suscrip_meses";
    private static final String ANO_SKU = "suscrip_ano";
    private LayoutInflater inflater;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private static final int MAX_MIN = 20;
    private int iPreferenceMin;
    public static boolean mIsSuscrip = false;
    private SoundGallery soundGallery;
    private BillingManager mBillingManager;
    private boolean mGoldMonthly;
    private boolean mGoldYearly;
    private static final int ID_SOUND = 999;
    private ImageView imageViewIcon;
    private PurchaseFragment fPurchaseFragment;
    // Tag for a dialog that allows us to find it when screen was rotated
    private static final String DIALOG_TAG = "dialog";
    private TextView tvSound;
    private AdView mAdview;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        MobileAds.initialize(this, initializationStatus -> {});
        init();

        // Try to restore dialog fragment if we were showing it prior to screen rotation
        if (savedInstanceState != null) {
            fPurchaseFragment = (PurchaseFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_TAG);
        }


    }

    public void init() {

        UpdateListener mUpdateListener = new UpdateListener();
        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mUpdateListener);
        imageViewIcon = findViewById(R.id.imageView);
        mAdview = findViewById(R.id.adView);

        builder = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) MainDrawerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(HomeFragment.newInstance(mIsSuscrip), R.id.mainFrame, false, false);
        iPreferenceMin = PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext());
        soundGallery = new SoundGallery(getApplicationContext());

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_parking1:
                changeFragment(HomeFragment.newInstance(mIsSuscrip), R.id.mainFrame, false, false);
                break;

            case R.id.nav_alarm:
                createDialogConfigAlarm();
                break;

            case R.id.nav_share:
                Utils.sharedSocial(this);
                break;

            case R.id.nav_compras:

                if (fPurchaseFragment == null) {
                    fPurchaseFragment = new PurchaseFragment();
                }
                if (mBillingManager != null
                        && mBillingManager.getBillingClientResponseCode()
                        > BILLING_MANAGER_NOT_INITIALIZED) {
                    //mAcquireFragment.onManagerReady(this);
                    fPurchaseFragment.onManagerReady(this);
                    // createDialogCompras();
                    changeFragment(fPurchaseFragment, R.id.mainFrame, false, false);
                }
                break;
            default:
                Log.d("No valida", "Opcion no valida");
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 999);

            } else {
                startActivityForResult(soundGallery.openGalleryIntent(), ID_SOUND);
            }
        } else {
            startActivityForResult(soundGallery.openGalleryIntent(), ID_SOUND);
        }

    }

    public void createDialogConfigAlarm() {
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.dialog_alarm, null);

        final SwitchCompat switchVibrate = view.findViewById(R.id.switchVibrador);
        switchVibrate.setChecked(PreferenceUtilities.getPreferenceDefaultVibrate(getApplicationContext()));
        final SwitchCompat switchSound = view.findViewById(R.id.switchSonido);
        final TextView tvTitle = view.findViewById(R.id.dialogAlarmTitle);
        tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
        switchSound.setChecked(PreferenceUtilities.getPreferenceDefaultSound(getApplicationContext()));

        final TextView tvMinutos = view.findViewById(R.id.textViewMin);
        tvMinutos.setText(String.valueOf(PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext())));
        Button btnContinuar = view.findViewById(R.id.btn_guardar_dialog_alarm);
        tvSound = view.findViewById(R.id.tvSound);
        Button btnTono = view.findViewById(R.id.btn_sound);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            btnTono.setOnClickListener(v -> openGallery());
        } else {
            btnTono.setVisibility(View.GONE);
            tvSound.setVisibility(View.GONE);
        }


        String path = PreferenceUtilities.getUriSoundSelected(this).getPath();
        File file = new File(path);
        tvSound.setText(file.getName());


        btnContinuar.setOnClickListener(v -> {

            if (switchSound.isChecked() | switchVibrate.isChecked()) {
                PreferenceUtilities.savePreferenceDefaultMinHour(v.getContext(), iPreferenceMin);
                PreferenceUtilities.savePreferenceDefaultVibrate(v.getContext(), switchVibrate.isChecked());
                PreferenceUtilities.savePreferenceDefaultSound(v.getContext(), switchSound.isChecked());
                dialog.dismiss();
            } else {
                Toast.makeText(v.getContext(), getResources().getString(R.string.msg_check_switch_alert), Toast.LENGTH_LONG).show();
            }
            if (!mIsSuscrip) {
                showInterstitialAd();
            }
        });

        SeekBar seekBar = view.findViewById(R.id.seekBarAlarm);
        seekBar.setMax(MAX_MIN);
        seekBar.setProgress(PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 5) {
                    tvMinutos.setText(getString(R.string.cinco));
                    iPreferenceMin = 5;
                    tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
                } else if (progress > 5 && progress <= 10) {
                    tvMinutos.setText(getString(R.string.dies));
                    iPreferenceMin = 10;
                    tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
                } else if (progress > 10 && progress <= 15) {
                    tvMinutos.setText(getString(R.string.quince));
                    iPreferenceMin = 15;
                    tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
                } else if (progress > 15 && progress <= 20) {
                    tvMinutos.setText(getString(R.string.veinte));
                    iPreferenceMin = 20;
                    tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Empty
            }
        });
        builder.setView(view);
        dialog = builder.create();
        //  dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == ID_SOUND) {
                Uri uri = data.getData();
                soundGallery.setSoundUri(uri);
                String soundPath = soundGallery.getPath();
                PreferenceUtilities.saveUriSoundSelected(this, soundPath);
                String path = PreferenceUtilities.getUriSoundSelected(this).getPath();
                // String sPathSD = Utils.getExternalSdCardPath();
                File fileName = new File(path);
                tvSound.setText(fileName.getName());
            }
        }
    }

    public void cargaPublicidad() {

        if (UtilNetwork.isOnline(Objects.requireNonNull(this))) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdview.setVisibility(View.VISIBLE);
            mAdview.loadAd(adRequest);
            mAdview.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Load the next interstitial.
                    mAdview.loadAd(new AdRequest.Builder().build());
                }
            });

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.banner_intersticial2019));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Load the next interstitial.
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

            });
        } else {
            ImageView imgPubli = findViewById(R.id.img_publi_no_internet);
            imgPubli.setVisibility(View.VISIBLE);

        }
    }

    public void showInterstitialAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    /**
     * Remove loading spinner and refresh the UI
     */
    public void showRefreshedUi() {
        // setWaitScreen(false);
        updateUi();
        if (fPurchaseFragment != null) {
            fPurchaseFragment.refreshUI();
        }
    }

    /**
     * Update UI to reflect model
     */
    @UiThread
    private void updateUi() {
        Log.d(TAG, "Updating the UI. Thread: " + Thread.currentThread().getName());

        if (isSixMonthlySubscribed()) {
            imageViewIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.coin_month));
        } else if (isYearlySubscribed()) {
            imageViewIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.coin_year));
        }
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isSixMonthlySubscribed() {
        return mGoldMonthly;
    }


    @Override
    public boolean isYearlySubscribed() {
        return mGoldYearly;
    }


    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            if (null != fPurchaseFragment)
                fPurchaseFragment.onManagerReady(MainDrawerActivity.this);
        }

        @Override
        public void onConsumeFinished(String token, @BillingClient.BillingResponseCode int result) {
            Log.d("TAG", "Consumption finished. Purchase token: " + token + ", result: " + result);

            // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
            // check if token corresponding to the expected sku was consumed.
            // If you have more than one sku, you probably need to validate that the token matches
            // the SKU you expect.
            // It could be done by maintaining a map (updating it every time you call consumeAsync)
            // of all tokens into SKUs which were scheduled to be consumed and then looking through
            // it here to check which SKU corresponds to a consumed token.
            if (result == BillingClient.BillingResponseCode.OK) {
                // Successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d("TAG", "Consumption successful. Provisioning.");
                //mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                //   saveData();
                //     mActivity.alert(R.string.alert_fill_gas, mTank);
            } else {
                //  mActivity.alert(R.string.alert_error_consuming, result);
                Log.d("TAG", "Error consumption");
            }

            showRefreshedUi();
            Log.d("TAG", "End consumption flow.");
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            mGoldMonthly = false;
            mGoldYearly = false;

            for (Purchase purchase : purchaseList) {
                Log.d("SUSCRIPCION", purchase.getSku());
                switch (purchase.getSku()) {

                    case MONTH_SKU:
                        mGoldMonthly = true;
                        mIsSuscrip = true;
                        break;
                    case ANO_SKU:
                        mGoldYearly = true;
                        mIsSuscrip = true;
                        break;
                }
            }

            if (!mIsSuscrip) {
                Log.d("FALLO", "es true");
                cargaPublicidad();
            }

            // mActivity.showRefreshedUi();
        }
    }
}
