package com.brainasaservice.android.suddenlywifi.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.brainasaservice.android.suddenlywifi.control.WifiController;
import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Damian Burke
 * 
 *         Audio sending. Starts another thread, opening the device's microphone
 *         and sending the input to all peers in the network. Can be bound to
 *         activities to start and stop.
 * 
 */

public class AudioSenderService extends Service {
	public static final String TAG = "AudioSenderService";
	private AudioThread thread;
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * Server thread listening to the microphone, sending buffers to all peers
	 * in the network.
	 * 
	 * @author Damian Burke
	 * 
	 */
	private class AudioThread extends Thread {
		private boolean isRunning = true;

		public void stopServer() {
			isRunning = false;
		}

		/**
		 * In a loop (as long as thread is running, not interrupted and the
		 * application is alive) checking for new udp packets.
		 */
		@Override
		public void run() {
			Log.d(TAG, "Sender thread running!");
			final App mApplication = (App) getApplication();
			final WifiController mWifiController = mApplication
					.getWifiController();
			final WifiPeer[] mPeers = mWifiController.getPeerList();
			try {
				final DatagramSocket dSocket = new DatagramSocket();

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

				Log.d(TAG, "Thread starting...");
				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int buffersize = Math.max(Config.BUFFER_SIZE, minBufferSize);

				AudioRecord arec = new AudioRecord(
						MediaRecorder.AudioSource.MIC, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, buffersize);

				byte[] buffer = new byte[buffersize];
				Log.d(TAG, "Starting to record, buffersize=" + buffersize);
				arec.startRecording();

				while (isRunning && !isInterrupted()) {

					try {
						Log.d(TAG, "Recording..");
						arec.read(buffer, 0, buffersize);
						DatagramPacket dPacket = new DatagramPacket(buffer,
								buffersize);

						for (WifiPeer cur : mPeers) {
							if (cur.isSelf)
								continue;

							dPacket.setAddress(InetAddress
									.getByName(cur.IP_ADDRESS));
							dPacket.setPort(Config.UDP_PORT);
							Log.d(TAG, "Sending data (" + dPacket.getLength()
									+ " bytes) to " + cur.IP_ADDRESS + ":"
									+ Config.UDP_PORT);
							dSocket.send(dPacket);
							mWifiController.onOutgoingUDPTransfer(buffersize);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Log.d(TAG, "Stopping sender service!");
				arec.stop();
				arec.release();
			} catch (SocketException e) {
				Log.e(TAG, "SocketException!");
				e.printStackTrace();
			} finally {

			}
		}
	}

	public void startTransmission() {
		// broadcasting to all peers!
		Log.d(TAG, "Starting transmission!");
		thread = new AudioThread();
		thread.start();
	}

	public void stopTransmission() {
		thread.stopServer();
		thread = null;
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

	public class LocalBinder extends Binder {
		public AudioSenderService getService() {
			return AudioSenderService.this;
		}
	}
}