package com.brainasaservice.android.suddenlywifi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brainasaservice.android.suddenlywifi.R;

/**
 * 
 * @author Damian Burke
 * 
 * NFC fragment to display a certain layout.
 *
 */
public class NfcFragment extends Fragment {
    public NfcFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_nfc, null);
    }
}
