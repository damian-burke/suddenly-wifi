package com.brainasaservice.android.suddenlywifi.etc;

import android.app.Application;

import com.brainasaservice.android.suddenlywifi.control.NfcController;
import com.brainasaservice.android.suddenlywifi.control.WifiController;

/**
 * Extending the main application to offer application-wide features and
 * communication ways.
 * 
 * @author Damian Burke
 * 
 */
public class App extends Application {
	private static NfcController mNfcController = null;
	private static WifiController mWifiController = null;

	/**
	 * Simple getter for the nfc controller.
	 * 
	 * @return NfcController reference, or null it not correctly initialized.
	 */
	public NfcController getNfcController() {
		return mNfcController;
	}

	/**
	 * Simple getter for the wifi controller.
	 * 
	 * @return WifiController reference, or null it not correctly initialized.
	 */
	public WifiController getWifiController() {
		return mWifiController;
	}

	/**
	 * Simple setter method for the nfc controller.
	 * 
	 * @param c
	 *            Nfc controller reference.
	 */
	public void setNfcController(NfcController c) {
		App.mNfcController = c;
	}

	/**
	 * Simple setter method for the wifi controller.
	 * 
	 * @param c
	 *            Wifi controller reference.
	 */
	public void setWifiController(WifiController c) {
		App.mWifiController = c;
	}

}
