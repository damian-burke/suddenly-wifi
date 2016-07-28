package com.brainasaservice.android.suddenlywifi.receivers;

import com.brainasaservice.android.suddenlywifi.control.WifiController;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

/**
 * Simple BroadcastReceiver extension to notify the user interface on each
 * important change on the wifi system services, for example if wifi module has
 * been turned off, etc.
 * 
 * @author Damian Burke
 * 
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "WifiDirectBroadcast";

	private WifiP2pManager manager;
	private Channel channel;
	private WifiController control;

	/**
	 * @param manager
	 *            WifiP2pManager system service
	 * @param channel
	 *            Wifi p2p channel
	 * @param control
	 *            WifiController instance
	 */
	public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			WifiController control) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.control = control;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
//		Log.d(TAG, "Received action: " + action);
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			// UI update to indicate wifi p2p status.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				Log.d(TAG, "Wifi is enabled.");
				control.setWifiP2pEnabled(true);
			} else {
				control.setWifiP2pEnabled(false);
				Log.d(TAG, "Wifi is disabled.");
			}
			// Log.d(NfcActivity.TAG, "P2P state changed - " + state);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			Log.d(TAG, "Peers changed.. ");
			// request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			if (manager != null) {
				Log.d(TAG, "requesting peers.");
				// manager.requestPeers(channel, activity);
				manager.requestPeers(channel, control.getPeerListListener());
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {

			if (manager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {
				control.setWifiP2pConnection(true);
				manager.requestConnectionInfo(channel,
						control.getConnectionInfoListener());
				manager.requestGroupInfo(channel,
						control.getGroupInfoListener());
				// we are connected with the other device, request connection
				// info to find group owner IP
			} else {
				// It's a disconnect
				control.setWifiP2pConnection(false);
				// activity.resetData();
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
//			control.setDeviceState((WifiP2pDevice) intent
//					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
		}
	}
}