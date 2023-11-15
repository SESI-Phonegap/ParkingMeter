package com.sesi.parkingmeter.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.sesi.parkingmeter.R;

public class PurchaseFragment extends Fragment {

    private static final String TAG = "PurchaseFragment";
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

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
