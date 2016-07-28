package com.brainasaservice.android.suddenlywifi.interfaces;

/**
 * 
 * @author Damian Burke
 * 
 * Interface for NFC message discovery.
 *
 */
public interface OnNdefDiscovered {
	/**
	 * For every record in a received NdefMessage there will be the payload as a string.
	 * @param payload Payload of each NdefMessage as string.
	 */
	public void onNdefDiscovery(String[] payload);
}
