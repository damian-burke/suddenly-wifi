package com.brainasaservice.android.suddenlywifi.services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 * 
 * @author Damian Burke
 */
public class FileSenderService extends IntentService {
	public static final String TAG = "FileSenderService";
	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "net.randomcompany.android.suddenlywifi.ACTION_SEND_FILE";
	public static final String ACTION_SEND_TEXT = "net.randomcompany.android.suddenlywifi.ACTION_SEND_TEXT";
	public static final String ACTION_SEND_SYSTEM = "net.randomcompany.android.suddenlywifi.ACTION_SEND_SYSTEM";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
	public static final String EXTRAS_TEXT = "send_text";

	public FileSenderService(String name) {
		super(name);
	}

	public FileSenderService() {
		super("FileSenderService");
	}

	/**
	 * When receiving new intents, two extras are being pulled from it. The byte
	 * array containing the data, as well as the receiver's IP address. After
	 * that, the byte array will be sent via Wifi-Direct and the wifi controller
	 * will be informed.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "File Receiver Service intent got!");
		final App mApplication = (App) getApplication();
		final Bundle extras = intent.getExtras();
		final byte[] bytes = extras.getByteArray(Config.Extra.PACKET_BYTES);
		final String host = extras.getString(Config.Extra.RECEIVER_HOST);

		final Socket s = new Socket();
		Log.d(TAG, "Sending stuff to " + host + "... " + bytes.length
				+ " bytes!");
		try {
			s.bind(null);
			s.connect((new InetSocketAddress(host, Config.TCP_PORT)), 500);
			OutputStream os = s.getOutputStream();
			os.write(bytes, 0, bytes.length);
			os.close();
			mApplication.getWifiController()
					.onOutgoingTCPTransfer(bytes.length);
			Log.d(TAG, "Stuff sent successfully.");
		} catch (Exception e) {
			Log.e(TAG, "Could not send stuff: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (s != null)
				if (s.isConnected()) {
					try {
						s.close();
					} catch (IOException e) {
						Log.e(TAG, "Could not close socket!");
					}
				}
		}
	}
}