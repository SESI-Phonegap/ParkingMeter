package com.sesi.parkingmeter.view.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.billingclient.api.BillingClient;
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

    private BillingProvider mBillingProvider;
    private RecyclerView mRecyclerView;
    public PurchaseFragment() {
        // Required empty public constructor
    }


    public static PurchaseFragment newInstance() {
        PurchaseFragment fragment = new PurchaseFragment();
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
        View root = inflater.inflate(R.layout.fragment_purchase, container, false);
        init();

        // Inflate the layout for this fragment
        return root;
    }

    public void init(){
        mRecyclerView = getActivity().findViewById(R.id.list);
        if (mBillingProvider != null) {
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
       // setWaitScreen(true);
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


}
