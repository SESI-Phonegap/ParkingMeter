package com.sesi.parkingmeter.view.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.sesi.parkingmeter.R;
import com.sesi.parkingmeter.data.api.billing.BillingProvider;
import com.sesi.parkingmeter.view.adapter.CardsWithHeadersDecoration;
import com.sesi.parkingmeter.view.adapter.SkuRowData;
import com.sesi.parkingmeter.view.adapter.SkusAdapter;
import com.sesi.parkingmeter.view.adapter.UiManager;
import java.util.ArrayList;
import java.util.List;

public class PurchaseFragment extends Fragment {

    private static final String TAG = "PurchaseFragment";
    private BillingProvider mBillingProvider;
    private SkusAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView mErrorTextView;
    private ProgressBar progressBar;

    public PurchaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init(){
        mRecyclerView = getActivity().findViewById(R.id.list);
        mErrorTextView = getActivity().findViewById(R.id.error_textview);
        progressBar = getActivity().findViewById(R.id.progressBarPurchase);

        if (mBillingProvider != null) {
            handleManagerAndUiReady();
        }
    }

    /**
     * Refreshes this fragment's UI
     */
    public void refreshUI() {
        Log.d(TAG, "Looks like purchases list might have been updated - refreshing the UI");
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Notifies the fragment that billing manager is ready and provides a BillingProviders
     * instance to access it
     */
    public void onManagerReady(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
        if (mRecyclerView != null) {
            handleManagerAndUiReady();
        }
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
     * Executes query for SKU details at the background thread
     */
    private void handleManagerAndUiReady() {
        // If Billing Manager was successfully initialized - start querying for SKUs
        setWaitScreen(true);
        querySkuDetails();
    }
    /**
     * Queries for in-app and subscriptions SKU details and updates an adapter with new data
     */
    private void querySkuDetails() {
        long startTime = System.currentTimeMillis();

        Log.d("TAG", "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                + (System.currentTimeMillis() - startTime) + "ms");

        if (getActivity() != null && !getActivity().isFinishing()) {
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
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                            Log.w("TAG", "Unsuccessful query for type: " + billingType
                                    + ". Error code: " + billingResult.getResponseCode());
                        } else if (skuDetailsList != null
                                && skuDetailsList.size() > 0) {
                            // If we successfully got SKUs, add a header in front of the row
                            @StringRes int stringRes = (billingType.equals(BillingClient.SkuType.INAPP))
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
                                    Resources res = getActivity().getResources();
                                    mRecyclerView.addItemDecoration(new CardsWithHeadersDecoration(
                                            mAdapter, (int) res.getDimension(R.dimen.header_gap),
                                            (int) res.getDimension(R.dimen.row_gap)));
                                    mRecyclerView.setLayoutManager(
                                            new LinearLayoutManager(getActivity()));
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

    private void displayAnErrorIfNeeded() {
        if (getActivity() == null || getActivity().isFinishing()) {
            Log.i("TAG", "No need to show an error - activity is finishing already");
            return;
        }

       // mLoadingView.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        int billingResponseCode = mBillingProvider.getBillingManager()
                .getBillingClientResponseCode();

        switch (billingResponseCode) {
            case BillingClient.BillingResponseCode.OK:
                // If manager was connected successfully, then show no SKUs error
                mErrorTextView.setText(getText(R.string.error_no_skus));
                break;
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                mErrorTextView.setText(getText(R.string.error_billing_unavailable));
                break;
            default:
                mErrorTextView.setText(getText(R.string.error_billing_default));
        }

    }

    protected UiManager createUiManager(SkusAdapter adapter, BillingProvider provider) {
        return new UiManager(adapter, provider);
    }

    /**
     * Enables or disables "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mRecyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(set ? View.VISIBLE : View.GONE );

    }

}
