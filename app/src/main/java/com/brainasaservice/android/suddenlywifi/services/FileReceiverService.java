package com.brainasaservice.android.suddenlywifi.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Extended service for receiving files via tcp over an established wifi direct
 * network.
 * 
 * @author Damian Burke
 * 
 */
public class FileReceiverService extends Service {
	public static final String TAG = "FileReceiverService";

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "net.randomcompany.uni.scs.nfcwifi.SEND_FILE";
	public static final String ACTION_SEND_TEXT = "net.randomcompany.uni.scs.nfcwifi.SEND_TEXT";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
	public static final String EXTRAS_TEXT = "send_text";

	public static final int RECEIVER_NOTIFICATION_ID = 23412;

	public static final int PORT = 44344;

	private ServerThread thread;
	private App mApplication;

	private final IBinder mBinder = new LocalBinder();

	// private Map<String, WiFiFile> incoming = new HashMap<String, WiFiFile>();

	@Override
	public void onCreate() {
		super.onCreate();
		// ((App) getApplication()).registerReceiverService(this);
		mApplication = (App) getApplication();
		Log.d(TAG, "OnCreate()");
		start();
	}

	/**
	 * Server thread listening on a specified port for TCP connections.
	 * 
	 * @author Damian Burke
	 * 
	 */
	private class ServerThread extends Thread {
		private boolean isRunning = true;

		public void stopServer() {
			isRunning = false;
		}

		/**
		 * In a loop (as long as thread is running, not interrupted and the
		 * application is alive) checking for new TCP connections.
		 */
		@Override
		public void run() {
			Log.d(TAG, "Thread starting...");
			try {
				ServerSocket socket = new ServerSocket(Config.TCP_PORT);
				Log.d(TAG, "Listening on port " + Config.TCP_PORT);
				socket.setSoTimeout(SOCKET_TIMEOUT);
				while (isRunning && !isInterrupted()) {
					try {
						final Socket inc = socket.accept();
						Log.d(TAG, "Accepted connection, reading..");
						new AsyncSocketReader(inc).start();
					} catch (SocketTimeoutException e) {

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author Damian Burke
	 * 
	 *         Extends thread to parse input streams into WifiPacket objects in
	 *         another thread, not disturbing the server socket.
	 * 
	 */
	private class AsyncSocketReader extends Thread {
		private final Socket mSocket;

		public AsyncSocketReader(Socket s) {
			this.mSocket = s;
		}

		@Override
		public void run() {
			try {
				Log.d(TAG, "Async reading..");
				final InputStream is = mSocket.getInputStream();
				Log.d(TAG, "have InputStream.");
				final WifiPacket mPacket = WifiPacket.fromInputStream(mSocket
						.getInetAddress().getHostAddress(), is);
				Log.d(TAG, "Have packet.");
				mApplication.getWifiController().onIncomingTCPTransfer(mPacket);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (mSocket != null)
					if (mSocket.isConnected())
						try {
							mSocket.close();
						} catch (IOException e) {
							Log.e(TAG, "Could not close socket!");
						}
			}
		}
	}

	/**
	 * Start the server thread.
	 */
	private void start() {
		Log.d(TAG, "Starting receiver service!");
		// final App app = (App) getApplication();

		thread = new ServerThread();
		thread.start();
	}

	/**
	 * Stop the server thread and remove the notification.
	 */
	@Override
	public void onDestroy() {
		if (thread != null) {
			try {
				thread.stopServer();
				Log.d(TAG, "Stopping receiver thread!");
				thread.interrupt();
				thread.join();
				Log.d(TAG, "Thread joined successfully.");
			} catch (Exception e) {
				Log.d(TAG, "Unable to stop server socket!");
			}
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	class LocalBinder extends Binder {
		public FileReceiverService getService() {
			return FileReceiverService.this;
		}
	}
}