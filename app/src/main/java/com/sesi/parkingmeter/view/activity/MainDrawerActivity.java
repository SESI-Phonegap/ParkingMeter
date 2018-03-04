package com.sesi.parkingmeter.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.MobileAds;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.data.api.billing.BillingManager;
import com.sesi.parkingmeter.data.api.billing.BillingProvider;
import com.sesi.parkingmeter.data.model.Suscriptions;
import com.sesi.parkingmeter.view.adapter.CardsWithHeadersDecoration;
import com.sesi.parkingmeter.view.adapter.SkuRowData;
import com.sesi.parkingmeter.view.adapter.SkusAdapter;
import com.sesi.parkingmeter.view.adapter.UiManager;
import com.sesi.parkingmeter.view.utilities.SoundGallery;
import com.sesi.parkingmeter.view.fragments.HomeFragment;
import com.sesi.parkingmeter.view.fragments.ParkingType2Fragment;
import com.sesi.parkingmeter.view.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.view.utilities.ReminderUtilities;
import com.sesi.parkingmeter.view.utilities.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sesi.parkingmeter.data.api.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;


public class MainDrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, BillingProvider {


    private static final String MONTH_SKU = "suscrip_meses";
    private static final String ANO_SKU = "suscrip_ano";
    private LayoutInflater inflater;
    private AlertDialog dialog;
    private Builder builder;
    private static final int MAX_MIN = 20;
    private int iPreferenceMin;
    public static boolean mIsSuscrip = false;
    private SoundGallery soundGallery;
    private List<Suscriptions> lstSuscriptions;
    private BillingManager mBillingManager;
    private BillingProvider mBillingProvider;
    private boolean mGoldMonthly;
    private boolean mGoldYearly;
    private UpdateListener mUpdateListener;
    private static final int ID_SOUND = 999;
    private SkusAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id));
        init();


    }

    public void init() {

        mUpdateListener = new UpdateListener();
        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mUpdateListener);

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

        changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_parking1:
                changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
                break;
            case R.id.nav_parking2:
                changeFragment(ParkingType2Fragment.newInstance(), R.id.mainFrame, false, false);
                break;
            case R.id.nav_alarm:
                createDialogConfigAlarm();
                break;

            case R.id.nav_share:
                Utils.sharedSocial(this);
                break;

            case R.id.nav_compras:
                if (mBillingManager != null
                        && mBillingManager.getBillingClientResponseCode()
                        > BILLING_MANAGER_NOT_INITIALIZED) {
                    //mAcquireFragment.onManagerReady(this);
                    onManagerReady(this);
                    createDialogCompras();
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
        final View view = inflater.inflate(R.layout.dialog_alarm, null);

        final SwitchCompat switchVibrate = view.findViewById(R.id.switchVibrador);
        switchVibrate.setChecked(PreferenceUtilities.getPreferenceDefaultVibrate(getApplicationContext()));
        final SwitchCompat switchSound = view.findViewById(R.id.switchSonido);
        final TextView tvTitle = view.findViewById(R.id.dialogAlarmTitle);
        tvTitle.setText(getString(R.string.configAlarm, String.valueOf(iPreferenceMin)));
        switchSound.setChecked(PreferenceUtilities.getPreferenceDefaultSound(getApplicationContext()));

        final TextView tvMinutos = view.findViewById(R.id.textViewMin);
        tvMinutos.setText(String.valueOf(PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext())));
        Button btnContinuar = view.findViewById(R.id.btn_guardar_dialog_alarm);
        Button btnTono = view.findViewById(R.id.btn_sound);

        TextView tvSound = view.findViewById(R.id.tvSound);
        String path = PreferenceUtilities.getUriSoundSelected(this).getPath();
        File file = new File(path);
        tvSound.setText(file.getName());
        btnTono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (switchSound.isChecked() | switchVibrate.isChecked()) {
                    PreferenceUtilities.savePreferenceDefaultMinHour(v.getContext(), iPreferenceMin);
                    PreferenceUtilities.savePreferenceDefaultVibrate(v.getContext(), switchVibrate.isChecked());
                    PreferenceUtilities.savePreferenceDefaultSound(v.getContext(), switchSound.isChecked());
                    dialog.dismiss();
                } else {
                    Toast.makeText(v.getContext(), getResources().getString(R.string.msg_check_switch_alert), Toast.LENGTH_LONG).show();
                }
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (ReminderUtilities.dispatcher != null) {
            ReminderUtilities.dispatcher.cancelAll();
        }

        super.onDestroy();

    }


    void complain(String message) {
        Log.e("Error", "**** ParkingMeter Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d("Error", "Showing alert dialog: " + message);
        bld.create().show();
    }


    public void updateUi(boolean isSuscrip) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_alarm).setVisible(isSuscrip);
        menu.findItem(R.id.nav_alarm).setVisible(true);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == ID_SOUND) {
                Uri uri = data.getData();
                soundGallery.setSoundUri(uri);
                PreferenceUtilities.saveUriSoundSelected(this, soundGallery.getPath());
            }
        }
    }

    public void onManagerReady(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;

    }

    public void createDialogCompras() {

        final View view = inflater.inflate(R.layout.dialog_compras, null);
        TextView tvTitle = view.findViewById(R.id.textView2);
        mRecyclerView = view.findViewById(R.id.list);

        setWaitScreen(true);

        builder.setView(view);
        dialog = builder.create();
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        querySkuDetails();


    }


    /**
     * Queries for in-app and subscriptions SKU details and updates an adapter with new data
     */
    private void querySkuDetails() {
        long startTime = System.currentTimeMillis();

        Log.d("TAG", "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                + (System.currentTimeMillis() - startTime) + "ms");

        if (!this.isFinishing()) {
            final List<SkuRowData> dataList = new ArrayList<>();
            mAdapter = new SkusAdapter();
            final UiManager uiManager = createUiManager(mAdapter, mBillingProvider);
            mAdapter.setUiManager(uiManager);
            // Filling the list with all the data to render subscription rows
            List<String> subscriptionsSkus = uiManager.getDelegatesFactory()
                    .getSkuList(BillingClient.SkuType.SUBS);
            addSkuRows(dataList, subscriptionsSkus, BillingClient.SkuType.SUBS, new Runnable() {
                @Override
                public void run() {
                    // Once we added all the subscription items, fill the in-app items rows below
                    List<String> inAppSkus = uiManager.getDelegatesFactory()
                            .getSkuList(BillingClient.SkuType.INAPP);
                    addSkuRows(dataList, inAppSkus, BillingClient.SkuType.INAPP, null);
                }
            });
        }
    }

    private void addSkuRows(final List<SkuRowData> inList, List<String> skusList,
                            final @BillingClient.SkuType String billingType, final Runnable executeWhenFinished) {

        mBillingProvider.getBillingManager().querySkuDetailsAsync(billingType, skusList,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {

                        if (responseCode != BillingClient.BillingResponse.OK) {
                            Log.w("TAG", "Unsuccessful query for type: " + billingType
                                    + ". Error code: " + responseCode);
                        } else if (skuDetailsList != null
                                && skuDetailsList.size() > 0) {
                            // If we successfully got SKUs, add a header in front of the row
                            @StringRes int stringRes = (billingType == BillingClient.SkuType.INAPP)
                                    ? R.string.header_inapp : R.string.header_subscriptions;
                            inList.add(new SkuRowData(getString(stringRes)));
                            // Then fill all the other rows
                            for (SkuDetails details : skuDetailsList) {
                                Log.i("TAG", "Adding sku: " + details);
                                inList.add(new SkuRowData(details, SkusAdapter.TYPE_NORMAL,
                                        billingType));
                            }

                            if (inList.size() == 0) {
                                displayAnErrorIfNeeded();
                            } else {
                                if (mRecyclerView.getAdapter() == null) {
                                    mRecyclerView.setAdapter(mAdapter);
                                    Resources res = getApplicationContext().getResources();
                                    mRecyclerView.addItemDecoration(new CardsWithHeadersDecoration(
                                            mAdapter, (int) res.getDimension(R.dimen.header_gap),
                                            (int) res.getDimension(R.dimen.row_gap)));
                                    mRecyclerView.setLayoutManager(
                                            new LinearLayoutManager(getApplicationContext()));
                                }

                                mAdapter.updateData(inList);
                                setWaitScreen(false);
                            }

                        }

                        if (executeWhenFinished != null) {
                            executeWhenFinished.run();
                        }
                    }
                });
    }

    /**
     * Enables or disables "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
    }

    private void displayAnErrorIfNeeded() {
        if (this.isFinishing()) {
            Log.i("TAG", "No need to show an error - activity is finishing already");
            return;
        }
    }

    protected UiManager createUiManager(SkusAdapter adapter, BillingProvider provider) {
        return new UiManager(adapter, provider);
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
    public boolean isTankFull() {
        return false;
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
            onManagerReady(MainDrawerActivity.this);
        }

        @Override
        public void onConsumeFinished(String token, @BillingClient.BillingResponse int result) {
            Log.d("TAG", "Consumption finished. Purchase token: " + token + ", result: " + result);

            // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
            // check if token corresponding to the expected sku was consumed.
            // If you have more than one sku, you probably need to validate that the token matches
            // the SKU you expect.
            // It could be done by maintaining a map (updating it every time you call consumeAsync)
            // of all tokens into SKUs which were scheduled to be consumed and then looking through
            // it here to check which SKU corresponds to a consumed token.
            if (result == BillingClient.BillingResponse.OK) {
                // Successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d("TAG", "Consumption successful. Provisioning.");
                //mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                //   saveData();
                //     mActivity.alert(R.string.alert_fill_gas, mTank);
            } else {
                //  mActivity.alert(R.string.alert_error_consuming, result);
            }

            //  mActivity.showRefreshedUi();
            Log.d("TAG", "End consumption flow.");
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            mGoldMonthly = false;
            mGoldYearly = false;

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {

                    case MONTH_SKU:
                        mGoldMonthly = true;
                        break;
                    case ANO_SKU:
                        mGoldYearly = true;
                        break;
                }
            }

            // mActivity.showRefreshedUi();
        }
    }
}
