package com.brainasaservice.android.suddenlywifi.interfaces;

import android.nfc.NfcEvent;

/**
 * 
 * @author Damian Burke Interface for NFC message sending.
 * 
 */
public interface OnNdefPushComplete {
	/**
	 * Called on successfully pushing out a NdefMessage.
	 * 
	 * @param event
	 *            The returned NFCEvent object from the NFCAdapter.
	 */
	public void onNdefPushComplete(NfcEvent event);
}
