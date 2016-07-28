package com.brainasaservice.android.suddenlywifi.interfaces;

/**
 * 
 * @author Damian Burke
 * 
 *         Listener interface for all outgoing data transfers, after they have
 *         been successfully sent. Only for statistics, as the application
 *         should already know what data have been sent.
 * 
 */
public interface OnWifiP2pOutgoingDataListener {
	/**
	 * 
	 * @param bytes
	 *            Bytes sent via UDP.
	 */
	public void onOutgoingUDP(int bytes);

	/**
	 * 
	 * @param bytes
	 *            Bytes sent via TCP.
	 */
	public void onOutgoingTCP(int bytes);
}
