package com.brainasaservice.android.suddenlywifi.interfaces;

import com.brainasaservice.android.suddenlywifi.model.WifiPacket;

/**
 * 
 * @author Damian Burke
 * 
 *         Listener interface for all incoming data transfers, after they have
 *         been successfully received and parsed.
 * 
 */
public interface OnWifiP2pIncomingDataListener {
	/**
	 * 
	 * @param packet
	 *            The packet received via UDP.
	 */
	public void onIncomingUDP(WifiPacket packet);

	/**
	 * 
	 * @param packet
	 *            The packet received via TCP.
	 */
	public void onIncomingTCP(WifiPacket packet);
}
