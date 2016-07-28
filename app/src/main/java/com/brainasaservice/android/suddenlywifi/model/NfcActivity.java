package com.brainasaservice.android.suddenlywifi.model;

import com.brainasaservice.android.suddenlywifi.control.NfcController;
import com.brainasaservice.android.suddenlywifi.control.WifiController;
import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.interfaces.OnNdefDiscovered;
import com.brainasaservice.android.suddenlywifi.interfaces.OnNdefPushComplete;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 
 * @author Damian Burke
 * 
 *         Abstract class for all NFC-related activities. Automatically
 *         establishing a connection with the NFC controller, as well as taking
 *         care of the onPause/onResume/onNewIntent methods.
 * 
 */
public abstract class NfcActivity extends FragmentActivity implements
		OnNdefPushComplete, OnNdefDiscovered {
	protected NfcController mNfcController;
	protected WifiController mWifiController;
	protected App mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (App) getApplication();
		mNfcController = mApplication.getNfcController();
		// insure we have a valid nfc controller.
		if (mNfcController == null) {
			mNfcController = new NfcController(this);
			mApplication.setNfcController(mNfcController);
		}
		mNfcController.setOnNdefDiscovered(this);
		mNfcController.setOnNdefPushComplete(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mNfcController.onPause();
		// delete nfc controller reference.
		mApplication.setNfcController(null);
		if (mWifiController != null)
			mWifiController.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mNfcController.onResume();
		if (mWifiController != null)
			mWifiController.onResume();
	}

	@Override
	public void onNewIntent(Intent in) {
		boolean consumed = mNfcController.onNewIntent(in);
		if (!consumed)
			super.onNewIntent(in);
	}

	/**
	 * 
	 * @return Returns the NFC controller's instance.
	 */
	protected NfcController getNfcController() {
		if (mNfcController == null) {
			if (mApplication.getWifiController() == null) {
				mNfcController = new NfcController(this);
				mApplication.setNfcController(mNfcController);
			} else
				mNfcController = mApplication.getNfcController();
		}
		return mNfcController;
	}
}
