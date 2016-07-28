package com.brainasaservice.android.suddenlywifi.model;

import com.brainasaservice.android.suddenlywifi.control.NfcController;
import com.brainasaservice.android.suddenlywifi.control.WifiController;
import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pConnectivityListener;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pGroupListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 
 * @author Damian Burke
 * 
 *         Abstract class for all wifi-related activities, taking care of
 *         initiating a Wifi Controller as well as the activity's life-cycle
 *         (onPause, onResume, ..) Also initializing NFC controller for later
 *         usage, for example inviting more peers.
 * 
 */
public abstract class WifiActivity extends FragmentActivity implements
		OnWifiP2pConnectivityListener, OnWifiP2pGroupListener {
	protected WifiController mWifiController;
	protected NfcController mNfcController;
	protected App mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (App) getApplication();
		mWifiController = mApplication.getWifiController();
		mNfcController = mApplication.getNfcController();
		// insure we have a valid wifi controller.
		if (mWifiController == null) {
			mWifiController = new WifiController(mApplication);
			mApplication.setWifiController(mWifiController);
		}
		mWifiController.addWifiP2pConnectivityListener(this);
		mWifiController.addWifiP2pGroupChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mNfcController != null)
			mNfcController.onResume();
		mWifiController.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mNfcController != null)
			mNfcController.onPause();
		mWifiController.onPause();
	}

	/**
	 * 
	 * @return Reference to current wifi controller.
	 */
	public WifiController getWifiController() {
		if (mWifiController == null) {
			if (mApplication.getWifiController() == null) {
				mWifiController = new WifiController(mApplication);
				mApplication.setWifiController(mWifiController);
			} else
				mWifiController = mApplication.getWifiController();
		}
		return mWifiController;
	}

	/**
	 * 
	 * @return Reference to current nfc controller.
	 */
	protected NfcController getNfcController() {
		if (mNfcController == null) {
			if (mApplication.getWifiController() == null) {
				mNfcController = new NfcController(this);
				mNfcController.onResume();
				mApplication.setNfcController(mNfcController);
			} else
				mNfcController = mApplication.getNfcController();
		}
		return mNfcController;
	}
}
