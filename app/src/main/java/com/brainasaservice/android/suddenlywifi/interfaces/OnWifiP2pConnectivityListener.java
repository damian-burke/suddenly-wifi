package com.brainasaservice.android.suddenlywifi.interfaces;

/**
 * 
 * @author Damian Burke
 * 
 *         Event listener interface for connection-related information.
 * 
 */
public interface OnWifiP2pConnectivityListener {
	/**
	 * Called whenever a Wifi-Direct connection is successfully established.
	 */
	public void onWifiP2pConnect();

	/**
	 * Called whenever a Wifi-Direct connection has been disconnected.
	 */
	public void onWifiP2pDisconnect();
}
