package com.brainasaservice.android.suddenlywifi.fragments;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.brainasaservice.android.suddenlywifi.R;
import com.brainasaservice.android.suddenlywifi.etc.Support;
import com.brainasaservice.android.suddenlywifi.model.WifiFragment;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;

/**
 * 
 * @author Damian Burke
 * 
 *         Fragment displaying basic information about the active connection.
 * 
 */
public class WifiInfoFragment extends WifiFragment {
	private static final String TAG = "WifiInfoFragment";
	private static WifiInfoFragment mInstance = null;
	private long ConnectionTime = 0;
	private Timer Timer = null;
	private UIUpdater mUpdater = null;

	// List of peers
	private WifiPeer[] mPeerArray = null;

	// View elements
	private Button mStatus;
	private Button mConnectionTime;
	private Button mIncoming;
	private Button mOutgoing;
	private Button mPeers;
	private ListView mPeerList;

	// Peer list adapter
	private WifiPeerListAdapter mAdapter = null;

	public WifiInfoFragment() {
	}

	public static WifiInfoFragment getInstance() {
		if (mInstance == null)
			mInstance = new WifiInfoFragment();
		return mInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_wifiinfo, null);

		mStatus = (Button) view.findViewById(R.id.frag_wifiinfo_status);
		mConnectionTime = (Button) view.findViewById(R.id.frag_wifiinfo_since);
		mIncoming = (Button) view.findViewById(R.id.frag_wifiinfo_inc);
		mOutgoing = (Button) view.findViewById(R.id.frag_wifiinfo_out);
		mPeers = (Button) view.findViewById(R.id.frag_wifiinfo_peercount);
		mPeerList = (ListView) view.findViewById(R.id.frag_wifiinfo_peerlist);
		mAdapter = new WifiPeerListAdapter();
		mPeerList.setAdapter(mAdapter);

		super.onCreateView(inflater, container, savedInstanceState);

		return view;
	}

	@Override
	public void onWifiP2pConnect() {
		Log.d(TAG, "OnConnect()");
		mStatus.setText("Online");
		mStatus.setEnabled(true);
		mConnectionTime.setEnabled(true);
		mIncoming.setEnabled(true);
		mOutgoing.setEnabled(true);
		mPeerList.setEnabled(true);
		mPeers.setEnabled(true);
		ConnectionTime = System.currentTimeMillis();
		Log.d(TAG, "ConnectionTime = " + ConnectionTime);
		if (Timer == null) {
			Timer = new Timer();
			if (mUpdater == null)
				mUpdater = new UIUpdater();
			try {
				Timer.schedule(mUpdater, 0, 1000);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onWifiP2pDisconnect() {
		Log.d(TAG, "onDisconnect()");
		mStatus.setText("Offline");
		mStatus.setEnabled(false);
		mConnectionTime.setEnabled(false);
		mIncoming.setEnabled(false);
		mOutgoing.setEnabled(false);
		mPeerList.setEnabled(false);
		mPeers.setEnabled(false);
		ConnectionTime = 0;
		if (mUpdater != null)
			mUpdater.cancel();
		if (Timer != null)
			Timer.cancel();
		Timer = null;
	}

	/**
	 * 
	 * @author Damian Burke
	 * 
	 *         User face updater. Posts a runnable running on the user interface
	 *         thread to update all view elements (buttons, lists, text fields).
	 * 
	 */
	private class UIUpdater extends TimerTask {
		@Override
		public void run() {
			if (getActivity() != null)
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String timeString = Math.round((System
								.currentTimeMillis() - ConnectionTime) / 1000)
								+ "s";
						mPeerArray = getWifiController().getPeerList();
						mConnectionTime.setText(timeString);
						int peerCount = (mPeerArray == null) ? 0
								: mPeerArray.length;
						mPeers.setText(peerCount + " peers");

						mIncoming.setText(Support
								.formatBytes(getWifiController().getIncoming()));
						mOutgoing.setText(Support
								.formatBytes(getWifiController().getOutgoing()));
					}
				});
		}
	}

	/**
	 * 
	 * @author Damian Burke
	 * 
	 *         Peer list adapter displaying all connected peers in the list
	 *         view.
	 * 
	 */
	private class WifiPeerListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return (mPeerArray == null) ? 0 : mPeerArray.length;
		}

		@Override
		public Object getItem(int position) {
			return mPeerArray[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View v = LayoutInflater.from(getActivity()).inflate(
					R.layout.fragment_wifiinfo_peerlistitem, null);
			final WifiPeer peer = mPeerArray[position];

			final TextView mTitle = (TextView) v
					.findViewById(R.id.fragment_wifiinfo_peerlistitem_title);
			final TextView mMeta = (TextView) v
					.findViewById(R.id.fragment_wifiinfo_peerlistitem_meta);

			mTitle.setText((peer.IP_ADDRESS == null || peer.IP_ADDRESS
					.isEmpty()) ? "UNKNOWN" : peer.IP_ADDRESS);
			mMeta.setText(new StringBuilder().append(peer.MAC_ADDRESS)
					.append((peer.isGroupOwner) ? " - owner" : "")
					.append((peer.isSelf) ? " - you" : "").toString());
			return v;
		}

	}

	@Override
	public void onWifiP2pGroupChange(WifiPeer[] peers) {
		mPeers.setText(peers.length + " peers");
		mPeerArray = peers;

		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
		else
			Log.d(TAG, "Adapter=null");
	}
}
