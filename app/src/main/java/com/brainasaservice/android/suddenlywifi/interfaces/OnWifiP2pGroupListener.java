package com.brainasaservice.android.suddenlywifi.interfaces;


import com.brainasaservice.android.suddenlywifi.model.WifiPeer;

/**
 * 
 * @author Damian Burke
 * 
 *         Interface for event listeners to retrieve changes in the Wifi-Direct
 *         group.
 * 
 */
public interface OnWifiP2pGroupListener {
	/**
	 * Called whenever new group information have been received by the
	 * WifiP2pManager.
	 * 
	 * @param peers
	 *            List of peers currently in the network.
	 */
	public void onWifiP2pGroupChange(WifiPeer[] peers);
}
