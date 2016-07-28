package com.brainasaservice.android.suddenlywifi.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.brainasaservice.android.suddenlywifi.R;
import com.brainasaservice.android.suddenlywifi.model.WifiFragment;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;
import com.brainasaservice.android.suddenlywifi.services.AudioReceiverService;
import com.brainasaservice.android.suddenlywifi.services.AudioSenderService;

/**
 * 
 * @author Damian Burke
 *
 * 		Audio fragment, displaying a certain layout and offering
 *         its functionality.
 * 
 */
public class WifiAudioFragment extends WifiFragment {
	private static WifiAudioFragment mInstance = null;
	private static final String TAG = "WifiAudioFragment";

	/**
	 * Reference to the UDP sender service
	 */
	private AudioSenderService mAudioSenderService = null;
	/**
	 * Reference to the TCP sender service
	 */
	private AudioReceiverService mAudioReceiverService = null;

	/**
	 * View references
	 */
	private ImageButton mButton;
	private ToggleButton mToggle;
	private TextView mTitle;
	private TextView mStatus;

	/**
	 * State variables
	 */
	private boolean isWifiConnected = false;
	private boolean isServiceBound = false;

	public WifiAudioFragment() {
	}

	public static WifiAudioFragment getInstance() {
		if (mInstance == null)
			mInstance = new WifiAudioFragment();
		return mInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		getActivity().bindService(
				new Intent(getActivity(), AudioReceiverService.class),
				mAudioReceiverServiceConnection, Context.BIND_AUTO_CREATE);

		getActivity().bindService(
				new Intent(getActivity(), AudioSenderService.class),
				mAudioSenderServiceConnection, Context.BIND_AUTO_CREATE);

		final View view = inflater.inflate(R.layout.fragment_wifiaudio, null);

		mButton = (ImageButton) view
				.findViewById(R.id.fragment_wifiaudio_button);
		mTitle = (TextView) view.findViewById(R.id.fragment_wifiaudio_text);
		mStatus = (TextView) view.findViewById(R.id.fragment_wifiaudio_status);
		mToggle = (ToggleButton) view
				.findViewById(R.id.fragment_wifiaudio_toggle);

		if (mButton == null)
			Log.e(TAG, "Button==null?!");

		mButton.setOnTouchListener(lOnButtonTouchListener);
		mToggle.setOnCheckedChangeListener(lOnToggleCheckListener);
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().unbindService(mAudioSenderServiceConnection);
		getActivity().unbindService(mAudioReceiverServiceConnection);
	}

	/**
	 * Listener for the toggle button to check whether it's on or off.
	 */
	private OnCheckedChangeListener lOnToggleCheckListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				mButton.setEnabled(false);
				mTitle.setText("Speak now!");
				startAudio();
			} else {
				stopAudio();
				mButton.setEnabled(true);
				mTitle.setText("Push to Talk!");
			}
		}
	};

	/**
	 * Touch listener for the button. On "push", voice transmission is started,
	 * on "release", it's stopped.
	 */
	private View.OnTouchListener lOnButtonTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// push, start sending audio
				mTitle.setText("Speak now!");
				startAudio();
				break;
			case MotionEvent.ACTION_UP:
				// release, stop sending audio
				mTitle.setText("Push to Talk!");
				stopAudio();
				break;
			}
			return false;
		}
	};

	/**
	 * Send command to start audio transmission in service.
	 */
	private void startAudio() {
		if (mAudioSenderService != null) {
			mAudioSenderService.startTransmission();
		} else
			Log.d(TAG, "AudioSender == null");
	}

	/**
	 * Send command to stop audio transmission in service.
	 */
	private void stopAudio() {
		if (mAudioSenderService != null) {
			mAudioSenderService.stopTransmission();
		}
	}

	@Override
	public void onWifiP2pConnect() {
		isWifiConnected = true;
		checkStatus();
		if (mAudioReceiverService != null)
			mAudioReceiverService.startListening();
	}

	@Override
	public void onWifiP2pDisconnect() {
		isWifiConnected = false;
		checkStatus();
		if (mAudioReceiverService != null)
			mAudioReceiverService.stopListening();
	}

	/**
	 * Check the status, change views properly (activate/deactivate buttons etc).
	 */
	private void checkStatus() {
		if (mButton == null || mStatus == null)
			return;

		if (isWifiConnected && isServiceBound) {
			mButton.setEnabled(true);
			mStatus.setText("Ready.");
		} else {
			if (!isWifiConnected && isServiceBound) {
				mButton.setEnabled(false);
				mStatus.setText("No connection.");
			} else {
				mButton.setEnabled(false);
				mStatus.setText("Audio service error.");
			}
		}
	}

	@Override
	public void onWifiP2pGroupChange(WifiPeer[] peers) {

	}

	/**
	 * Service connection to bind UDP sender service to activity.
	 */
	protected ServiceConnection mAudioSenderServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Audio sendr Service connected: " + name);
			mAudioSenderService = ((AudioSenderService.LocalBinder) service)
					.getService();
			isServiceBound = true;
			checkStatus();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "Audio sendr service disconnected: " + name);
			mAudioSenderService = null;
			isServiceBound = false;
			checkStatus();
		}

	};

	/**
	 * Service connection to bind UDP receiver service to activity.
	 */
	protected ServiceConnection mAudioReceiverServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Service connected: " + name);
			mAudioReceiverService = ((AudioReceiverService.LocalBinder) service)
					.getService();
			isServiceBound = true;
			mAudioReceiverService.startListening();
			checkStatus();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mAudioReceiverService = null;
			isServiceBound = false;
			checkStatus();
		}

	};
}
