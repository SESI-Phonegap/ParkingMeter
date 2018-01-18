package com.sesi.parkingmeter.view.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.billing.IabBroadcastReceiver;
import com.sesi.parkingmeter.billing.IabHelper;
import com.sesi.parkingmeter.billing.IabResult;
import com.sesi.parkingmeter.billing.Inventory;
import com.sesi.parkingmeter.billing.Purchase;
import com.sesi.parkingmeter.view.fragments.HomeFragment;
import com.sesi.parkingmeter.view.fragments.ParkingType2Fragment;
import com.sesi.parkingmeter.utilities.Constants;
import com.sesi.parkingmeter.utilities.PreferenceUtilities;
import com.sesi.parkingmeter.utilities.ReminderUtilities;
import com.sesi.parkingmeter.utilities.UtilGPS;
import com.sesi.parkingmeter.utilities.UtilNetwork;
import com.sesi.parkingmeter.utilities.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainDrawerActivity extends BaseActivity implements DialogInterface.OnClickListener, IabBroadcastReceiver.IabBroadcastListener, NavigationView.OnNavigationItemSelectedListener {


    private LayoutInflater inflater;
    private AlertDialog dialog;
    private Builder builder;
    private static final int MAX_MIN = 20;
    private int iPreferenceMin;
    public static final int PERMISION_LOCATION = 1002;
    public static LatLng latLng;
    public static boolean mIsSuscrip = false;
    public boolean mAutoRenewEnabled = false;
    public String sSuscripSku = "";
    private String sFirstChoiceSku = "";
    private String sSecondChoiceSku = "";
    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";
    private NavigationView navigationView;
    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        if (UtilNetwork.isOnline(this)) {
            MobileAds.initialize(this, getString(R.string.banner_ad_unit_id));
            init();
        } else {
            Toast.makeText(this,"Necesitas conexion a internet",Toast.LENGTH_LONG).show();
        }

    }

    public void init() {

        startBilling();

        builder = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) MainDrawerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        changeFragment(HomeFragment.newInstance(), R.id.mainFrame, false, false);
        iPreferenceMin = PreferenceUtilities.getPreferenceDefaultMinHour(getApplicationContext());

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
          //  super.onBackPressed();
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
                sharedSocial();
                break;

            case R.id.nav_compras:
                suscripcion();
                break;
            default:
                Log.d("No valida", "Opcion no valida");
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void suscripcion() {

        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        if (mIsSuscrip) {
            complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
            return;
        }

        CharSequence[] options;
        if (!mIsSuscrip || !mAutoRenewEnabled) {
            // Both subscription options should be available
            options = new CharSequence[2];
            options[0] = getString(R.string.Meses6);
            options[1] = getString(R.string.Meses12);
            sFirstChoiceSku = Constants.SKU_MONTHLY;
            sSecondChoiceSku = Constants.SKU_YEARLY;
        } else {
            // This is the subscription upgrade/downgrade path, so only one option is valid
            options = new CharSequence[1];
            if (sSuscripSku.equals(Constants.SKU_MONTHLY)) {
                // Give the option to upgrade to yearly
                options[0] = getString(R.string.Meses12);
                sFirstChoiceSku = Constants.SKU_YEARLY;
            } else {
                // Give the option to downgrade to monthly
                options[0] = getString(R.string.Meses6);
                sFirstChoiceSku = Constants.SKU_MONTHLY;
            }
            sSecondChoiceSku = "";
        }
        int titleResId;
        if (!mIsSuscrip) {
            titleResId = R.string.selectSuscrip;
        } else if (!mAutoRenewEnabled) {
            titleResId = R.string.reactivaSuscrip;
        } else {
            titleResId = R.string.cambiaSuscrip;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(titleResId)
                .setSingleChoiceItems(options, 0 /* checkedItem */, this)
                .setPositiveButton(R.string.continuar, this)
                .setNegativeButton(R.string.cancelar, this);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
/*
           String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, Constants.SKU_SUSCRIP, Constants.RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
           // setWaitScreen(false);
        }*/
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            //  Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                return;
            }

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                //  setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                // setWaitScreen(false);
                return;
            }

            if (purchase.getSku().equals(Constants.SKU_SUSCRIP)) {
                // bought the infinite gas subscription
                Log.d("Mensaje: ", "Suscrito.");
                alert("Thank you for subscribing to infinite gas!");
                mIsSuscrip = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                sSuscripSku = purchase.getSku();
                updateUi(mIsSuscrip);
            }
        }
    };

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

    public void createDialogConfigAlarm() {
        final View view = inflater.inflate(R.layout.dialog_alarm_preferences, null);

        final SwitchCompat switchVibrate = (SwitchCompat) view.findViewById(R.id.switchVibrador);
        switchVibrate.setChecked(PreferenceUtilities.getPreferenceDefaultVibrate(getApplicationContext()));
        final SwitchCompat switchSound = (SwitchCompat) view.findViewById(R.id.switchSonido);
        final TextView tvTitle = (TextView) view.findViewById(R.id.dialogAlarmTitle);
        tvTitle.setText(getString(R.string.configAlarm,String.valueOf(iPreferenceMin)));
        switchSound.setChecked(PreferenceUtilities.getPreferenceDefaultSound(getApplicationContext()));

        final TextView tvMinutos = (TextView) view.findViewById(R.id.textViewMin);
        tvMinutos.setText(String.valueOf(iPreferenceMin));
        Button btnContinuar = (Button) view.findViewById(R.id.btn_guardar_dialog_alarm);
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

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarAlarm);
        seekBar.setMax(MAX_MIN);
        seekBar.setProgress(iPreferenceMin);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 5){
                    tvMinutos.setText("5");
                    iPreferenceMin = 5;
                    tvTitle.setText(getString(R.string.configAlarm,String.valueOf(iPreferenceMin)));
                } else if (progress > 5 && progress <= 10) {
                    tvMinutos.setText("10");
                    iPreferenceMin = 10;
                    tvTitle.setText(getString(R.string.configAlarm,String.valueOf(iPreferenceMin)));
                } else if (progress > 10 && progress <= 15) {
                    tvMinutos.setText("15");
                    iPreferenceMin = 15;
                    tvTitle.setText(getString(R.string.configAlarm,String.valueOf(iPreferenceMin)));
                } else if (progress > 15 && progress <= 20) {
                    tvMinutos.setText("20");
                    iPreferenceMin = 20;
                    tvTitle.setText(getString(R.string.configAlarm,String.valueOf(iPreferenceMin)));
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
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mHelper = null;
        super.onDestroy();

    }

    public void sharedSocial() {
        List<Intent> targetShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(shareIntent, 0);
        if (!resInfos.isEmpty()) {
            for (ResolveInfo resInfo : resInfos) {
                String packageName = resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);

/*         if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                 || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                 || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                 || packageName.contains("com.google.android.gm") || packageName.contains("com.facebook.orca")
                 || packageName.contains("com.yahoo.mobile") || packageName.contains("com.skype.raider")
                 || packageName.contains("com.android.mms")|| packageName.contains("com.linkedin.android")
                 || packageName.contains("com.google.android.apps.messaging")) {*/
                if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                        || packageName.contains("com.whatsapp")
                        || packageName.contains("com.google.android.apps.plus")) {
                    Intent intent = new Intent();

                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                    intent.putExtra(Constants.APPNAME, resInfo.loadLabel(pm).toString());
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, Constants.URL_PLAYSTORE_APP);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.compartir));
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }
            if (!targetShareIntents.isEmpty()) {
                Collections.sort(targetShareIntents, new Comparator<Intent>() {
                    @Override
                    public int compare(Intent o1, Intent o2) {
                        return o1.getStringExtra("AppName").compareTo(o2.getStringExtra("AppName"));
                    }
                });
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Select app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            } else {
                Toast.makeText(getApplicationContext(), "No app to share.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startBilling() {
        String key = Utils.genPK();
        mHelper = new IabHelper(this, key);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                //   Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                } else {
                    Log.d("Mensaje:", "In-app Billing is set up OK");
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
        /*        mBroadcastReceiver = new IabBroadcastReceiver(MainDrawerActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);*/


                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d("Mensaje", "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                    Log.d("Error:", e.getMessage());
                }
            }
        });
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d("Mensaje", "Query inventory was successful.");

            Purchase pSuscripMonthly = inventory.getPurchase(Constants.SKU_MONTHLY);
            Purchase pSuscripYearly = inventory.getPurchase(Constants.SKU_YEARLY);


            if (pSuscripMonthly != null && pSuscripMonthly.isAutoRenewing()) {
                Log.d("Inventario:", pSuscripMonthly.getSku());
                sSuscripSku = Constants.SKU_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (pSuscripYearly != null && pSuscripYearly.isAutoRenewing()) {
                Log.d("Inventario:", pSuscripYearly.getSku());
                sSuscripSku = Constants.SKU_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                sSuscripSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mIsSuscrip = (pSuscripMonthly != null && verifyDeveloperPayload(pSuscripMonthly))
                    || (pSuscripYearly != null && verifyDeveloperPayload(pSuscripYearly));
            Log.d("Mensaje: ", "User " + (mIsSuscrip ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");

            updateUi(mIsSuscrip);

        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    public boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        if (payload.equals(Constants.SKU_MONTHLY) || payload.equals(Constants.SKU_YEARLY)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d("Mensaje", "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    void complain(String message) {
        Log.e("Error", "**** TrivialDrive Error: " + message);
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
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_alarm).setVisible(isSuscrip);
        menu.findItem(R.id.nav_alarm).setVisible(true);

    }


    @Override
    public void onClick(DialogInterface dialog, int id) {

        switch (id) {
            case 0:
                mSelectedSubscriptionPeriod = sFirstChoiceSku;
                break;

            case 1:
                mSelectedSubscriptionPeriod = sSecondChoiceSku;
                break;

            case DialogInterface.BUTTON_POSITIVE:
                String payload = "";
                if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
                    // The user has not changed from the default selection
                    mSelectedSubscriptionPeriod = sFirstChoiceSku;
                }
                List<String> oldSkus = null;
                if (!TextUtils.isEmpty(sSuscripSku)
                        && !sSuscripSku.equals(mSelectedSubscriptionPeriod)) {
                    // The user currently has a valid subscription, any purchase action is going to
                    // replace that subscription
                    oldSkus = new ArrayList<String>();
                    oldSkus.add(sSuscripSku);
                }
                try {
                    mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
                            oldSkus, Constants.RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error launching purchase flow. Another async operation in progress.");
                    // setWaitScreen(false);
                }
                mSelectedSubscriptionPeriod = "";
                sFirstChoiceSku = "";
                sSecondChoiceSku = "";
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                // There are only four buttons, this should not happen
                Log.e("Mensaje: ", "Unknown button clicked in subscription dialog: " + id);
                break;
        }

    }


}
