package com.brainasaservice.android.suddenlywifi.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket.Code;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Damian Burke
 * 
 *         Audio receiver service. Starts another thread and opens a
 *         DatagramSocket (UDP) on a specified port. Can be bound to activities
 *         to start and stop listening for packets.
 * 
 */

public class AudioReceiverService extends Service {
	public static final String TAG = "AudioReceiverService";
	private AudioThread thread;
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * Server thread listening on a specified port for udp packets.
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
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			Log.d(TAG, "Receiver Thread running.");
			final App mApplication = (App) getApplication();
			try {
				DatagramChannel dChannel = DatagramChannel.open();
				// DatagramSocket dSocket = new DatagramSocket();
				DatagramSocket dSocket = dChannel.socket();

				dSocket.setReuseAddress(true);
				dSocket.setSoTimeout(2000);
				dSocket.bind(new InetSocketAddress(Config.UDP_PORT));

				Log.d(TAG, "DatagramSocket open.");

				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

				int minBufferSize = AudioRecord.getMinBufferSize(11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int buffersize = Math.max(Config.BUFFER_SIZE, minBufferSize);

				AudioTrack aTrack = new AudioTrack(
						AudioManager.STREAM_VOICE_CALL, 11025,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, buffersize,
						AudioTrack.MODE_STREAM);

				DatagramPacket dPacket = new DatagramPacket(
						new byte[buffersize], buffersize);
				Log.d(TAG, "Packet with buffersize=" + buffersize);
				aTrack.play();
				Log.d(TAG, "Playing track..");

				byte[] buffer = new byte[buffersize];

				while (isRunning && !isInterrupted()) {
					try {
						Log.d(TAG, "Receiving..");
						dSocket.receive(dPacket);
						Log.d(TAG, "Received, playback!");
						buffer = dPacket.getData();

						WifiPacket mPacket = new WifiPacket(Code.AUDIO, buffer,
								dPacket.getAddress().getHostName());
						mApplication.getWifiController().onIncomingUDPTransfer(
								mPacket);

						aTrack.setPlaybackRate(11025);

						aTrack.write(buffer, 0, buffer.length);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Log.d(TAG, "Stopping playback!");
				aTrack.stop();

			} catch (SocketException e) {
			} catch (Exception e) {
			}

			Log.d(TAG, "Thread starting...");
		}
	}

	/**
	 * Start the server thread.
	 */
	public void startListening() {
		Log.d(TAG, "Starting receiver service!");

		thread = new AudioThread();
		thread.start();
	}

	public void stopListening() {
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
		public AudioReceiverService getService() {
			return AudioReceiverService.this;
		}
	}
}