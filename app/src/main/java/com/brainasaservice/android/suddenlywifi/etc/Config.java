package com.brainasaservice.android.suddenlywifi.etc;

/**
 * 
 * @author Damian Burke
 * Configuration class, holding some variables.
 *
 */
public class Config {
	public static final String NFC_MESSAGE_PREFIX = "suddenlywifi-nfc";
	
	public static final int TCP_PORT = 12121;
	public static final int UDP_PORT = 12122;

	public static final byte[] PACKET_DELIMITER = ".,.".getBytes();
	public static final int BUFFER_SIZE = 4096;
	
	public static class Extra {
		public static final String PACKET_BYTES = "randomcompany_packet_bytes";
		public static final String RECEIVER_HOST = "randomcompany_receiver_host";
	}
}
