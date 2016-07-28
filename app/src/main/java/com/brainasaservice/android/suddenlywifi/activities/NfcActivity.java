package com.brainasaservice.android.suddenlywifi.activities;

import com.brainasaservice.android.suddenlywifi.R;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Damian Burke
 * 
 * This activity extends the superclass NfcActivity. It is used in the sample application to initiate the NFC handshake, meaning either to:
 *  - Transfer the device's MAC address or
 *  - receive the peer's MAC address.
 * In both cases after a NFC data transmission the user is redirected to the WifiActivity. 
 *
 */
public class NfcActivity extends com.brainasaservice.android.suddenlywifi.model.NfcActivity {
	private static final String TAG = "NfcActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		final WifiManager wm = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);

		getNfcController().setNdefPayload(
				wm.getConnectionInfo().getMacAddress());
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		Intent out = new Intent(getApplicationContext(), WifiActivity.class);
		startActivity(out);
		finish();
	}

	@Override
	public void onNdefDiscovery(String[] payload) {
		Log.d(TAG, "Received some NFC!");
		for (String cur : payload) {
			Log.d(TAG, "Received " + cur);
			if (cur != null)
				if (cur.startsWith(Config.NFC_MESSAGE_PREFIX)) {
					cur = cur.substring(Config.NFC_MESSAGE_PREFIX.length());
					// only message transferred is mac address.
					Log.d(TAG, "Received MAC address: " + cur);

					// getWifiController().sendInvitation(cur);

					Intent out = new Intent(getApplicationContext(),
							WifiActivity.class);
					out.putExtra("invite", cur);
					startActivity(out);
					finish();
				}
		}

	}

}
