package com.brainasaservice.android.suddenlywifi.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.brainasaservice.android.suddenlywifi.etc.Config;
import android.util.Log;

/**
 * 
 * @author Damian Burke
 * 
 *         Representing a packet sent via Wifi-Direct. Contains a code,
 *         datasize, sender address as well as the data as byte aray.
 * 
 */
public class WifiPacket implements Serializable {
	private static final String TAG = "WifiPacket";
	/**
	 * 
	 */
	private static final long serialVersionUID = -9057367185793080941L;
	public int mCode;
	public int mDataSize;
	public String mSender;
	public byte[] mData;

	public WifiPacket(int code, String data) {
		mCode = code;
		mData = data.getBytes();
		mDataSize = mData.length;
	}

	public WifiPacket(int code, byte[] data, String sender) {
		mCode = code;
		mDataSize = data.length;
		mData = data;
		mSender = sender;
	}

	public WifiPacket(int code, byte[] data) {
		mCode = code;
		mDataSize = data.length;
		mData = data;
	}

	/**
	 * 
	 * @return Packet as byte array, ready to be sent.
	 */
	public byte[] toByteArray() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			buf.write(String.valueOf(mCode).getBytes());
			buf.write(Config.PACKET_DELIMITER);
			buf.write(String.valueOf(mDataSize).getBytes());
			buf.write(Config.PACKET_DELIMITER);
			buf.write(mData);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return buf.toByteArray();
	}

	/**
	 * 
	 * @param sender
	 *            Sender's IP address
	 * @param is
	 *            InputStream (e.g. TCP connection) to read a packet from.
	 * @return The parsed packet. If an error occurs, null.
	 */
	public static WifiPacket fromInputStream(String sender, InputStream is) {
		Log.d(TAG, "Creating new packet from stream..");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int read = 0;
		try {
			while ((read = is.read(buf)) > 0) {
				bos.write(buf, 0, read);
			}
		} catch (IOException e1) {
			Log.d(TAG, "error while reading.");
			e1.printStackTrace();
		}
		byte[] out = bos.toByteArray();

		final String data = new String(out);
		Log.d(TAG, "data=" + data);
		String[] split = data.split("\\.\\,\\.");
		for (String cur : split) {
			Log.d(TAG, "- " + cur);
		}
		int code = Integer.valueOf(split[0]);
		int size = Integer.valueOf(split[1]);
		byte[] file = split[2].getBytes();

		Log.d(TAG, "code=" + code + ", size=" + size + ", file="
				+ new String(data) + ", sender=" + sender);
		try {
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new WifiPacket(code, file, sender);
	}

	/**
	 * 
	 * @author Damian Burke
	 * 
	 *         Static class holding code values for wifi packets. Can be
	 *         extended to add additional codes. Codes should not be overriden
	 *         or changed.
	 * 
	 */
	public static class Code {
		public static final int IP_ADDRESS = 1;
		public static final int IP_ACK = 2;
		public static final int TEXT_MESSAGE = 3;
		public static final int AUDIO = 4;
		public static final int CUSTOM = 5;
	}
}
