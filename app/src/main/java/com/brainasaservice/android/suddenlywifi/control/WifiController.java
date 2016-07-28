package com.brainasaservice.android.suddenlywifi.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.brainasaservice.android.suddenlywifi.etc.App;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.etc.Support;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pConnectivityListener;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pGroupListener;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pIncomingDataListener;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pOutgoingDataListener;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;
import com.brainasaservice.android.suddenlywifi.receivers.WifiDirectBroadcastReceiver;
import com.brainasaservice.android.suddenlywifi.services.AudioReceiverService;
import com.brainasaservice.android.suddenlywifi.services.FileReceiverService;
import com.brainasaservice.android.suddenlywifi.services.FileSenderService;

/**
 * Wifi controller. Keeps reference of all wifi related information and data.
 * Also sends commands to Android's system-services. Keeps references of
 * wifi-event listeners and broadcasts data.
 * 
 * @author Damian Burke
 * 
 */
public class WifiController {
	// Tag for logging purposes
	private static final String TAG = "WifiController";

	/**
	 * The group owner address for internal purposes.
	 */
	private String mGroupOwnerAddress = null;

	/**
	 * Reference to the main application.
	 */
	private final App app;
	/**
	 * Reference of the activity's context.
	 */
	private final Context ctx;

	/**
	 * Reference to the system service.
	 */
	private final WifiP2pManager mWifiManager;
	/**
	 * Reference to the used wifi channel.
	 */
	private final Channel mChannel;
	/**
	 * IntentFilter to receive important / related intents.
	 */
	private final IntentFilter mIntentFilter = new IntentFilter();

	/**
	 * Broadcast receiver to receive wifi related intents.
	 */
	private BroadcastReceiver mBroadcastReceiver;

	/**
	 * Custom listener for new peer list information.
	 */
	private PeerListListener mCustomPeerListListener = null;
	/**
	 * Custom listener for connection related information.
	 */
	private ConnectionInfoListener mCustomConnectionInfoListener = null;

	/**
	 * List of peers in the group.
	 */
	private List<WifiP2pDevice> mWifiP2pDeviceList = new ArrayList<WifiP2pDevice>();;

	/**
	 * Boolean: Wifi-Direct enabled?
	 */
	private boolean isWifiP2pEnabled = false;
	/**
	 * Boolean: Wifi-Direct group established?
	 */
	private boolean isWifiP2pEstablished = false;
	/**
	 * If an invitation is triggered before the peers were discovered, the MAC
	 * address is stored.
	 */
	private String mSendInvitationTo = null;

	/**
	 * To update all connection and group information, a timer.
	 */
	private Timer mUpdateTimer = new Timer();
	/**
	 * Whether or not the IP address has been sent to the owner.
	 */
	private boolean hasSentIPToOwner = false;
	/**
	 * Whether or not the sent out IP address has been received/acknowledged.
	 */
	private boolean isIPAcknowledged = false;

	/**
	 * Incoming/outgoing byte counter for TCP/UDP
	 */
	private long mIncomingTCP = 0;
	private long mOutgoingTCP = 0;
	private long mIncomingUDP = 0;
	private long mOutgoingUDP = 0;

	/**
	 * Constructor will get access to the wifi manager, initialize it and thus
	 * obtain a channel for further wireless communication. Also setting up a
	 * broadcast receiver to receive wifi-relevant changes.
	 * 
	 * @param activity
	 *            The activity from which the controller is being instantiated
	 *            and in which it's used.
	 */
	public WifiController(App app) {
		this.app = app;
		this.ctx = app.getApplicationContext();
		this.mWifiManager = (WifiP2pManager) ctx
				.getSystemService(Context.WIFI_P2P_SERVICE);
		this.mChannel = mWifiManager.initialize(ctx, ctx.getMainLooper(),
				mChannelListener);
		mBroadcastReceiver = new WifiDirectBroadcastReceiver(mWifiManager,
				mChannel, this);
		initializeIntentFilter();
		Intent tcpService = new Intent(ctx, FileReceiverService.class);
		ctx.startService(tcpService);
		Intent udpService = new Intent(ctx, AudioReceiverService.class);
		ctx.startService(udpService);

	}

	/**
	 * @return Amount of bytes sent via TCP.
	 */
	public long getOutgoingTCP() {
		return mOutgoingTCP;
	}

	/**
	 * @return Amount of bytes received via TCP
	 */

	public long getIncomingTCP() {
		return mIncomingTCP;
	}

	/**
	 * @return Amount of bytes sent via UDP.
	 */
	public long getOutgoingUDP() {
		return mOutgoingUDP;
	}

	/**
	 * @return Amount of bytes received via UDP.
	 */
	public long getIncomingUDP() {
		return mIncomingUDP;
	}

	/**
	 * @return Amount of bytes received.
	 */
	public long getIncoming() {
		return mIncomingTCP + mIncomingUDP;
	}

	/**
	 * @return Amount of bytes sent.
	 */
	public long getOutgoing() {
		return mOutgoingTCP + mOutgoingUDP;
	}

	/**
	 * @return MAC address of this device.
	 */
	public String getLocalMacAddress() {
		final WifiManager wm = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
	}

	/**
	 * @return Reference to the application object.
	 */
	public App getApplication() {
		return app;
	}

	/**
	 * @return Current context.
	 */
	public Context getContext() {
		return ctx;
	}

	/**
	 * Supposed to be called whenever the application is resumed, to re-enable
	 * wifi features.
	 */
	public void onResume() {
		ctx.registerReceiver(mBroadcastReceiver, mIntentFilter);
		mWifiManager.discoverPeers(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				// Log.d(TAG, "Discovering peers..");
			}

			@Override
			public void onFailure(int reason) {
				// Log.e(TAG, "Could not discover peers.. " + reason);
			}
		});
		mWifiManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
		mWifiManager.requestGroupInfo(mChannel, mGroupInfoListener);
		mUpdateTimer = new Timer();
		mUpdateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mWifiManager.requestConnectionInfo(mChannel,
						mConnectionInfoListener);
				mWifiManager.requestGroupInfo(mChannel, mGroupInfoListener);
			}
		}, 2000, 2000);
	}

	/**
	 * Supposed to be called whenever the application is paused, to disable
	 * ui-updates for a non-existing ui. Wifi features should be kept alive.
	 */
	public void onPause() {
		mUpdateTimer.cancel();
		mUpdateTimer = null;
		try {
			ctx.unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			// Log.e(TAG, "Wifi Direct BroadcastReceiver was not registered!");
		}
	}

	/**
	 * Send invitation into Wifi-Direct group.
	 * 
	 * @return True, if it's sent directly. False if it's delayed until the peer
	 *         has been discovered.
	 */
	public boolean sendInvitation(final String macAddress) {
		// check peer list for matching devices
		// Log.i(TAG, "Inviting " + macAddress);
		if (mWifiP2pDeviceList != null)
			for (WifiP2pDevice dev : mWifiP2pDeviceList) {
				// Log.i(TAG, "Found device with mac: " + dev.deviceAddress);
				if (Support.compareMacAddressesInsensitive(dev.deviceAddress,
						macAddress)) {
					// Log.i(TAG,
					// "Sending out invitation! No need to save it for later use.");
					sendWifiGroupInvitation(dev.deviceAddress);

					return true;
				}
			}
		mSendInvitationTo = macAddress;
		// Log.i(TAG, "Storing " + mSendInvitationTo
		// + " for inviting someone on discovery!");
		return false;
	}

	/**
	 * Send the actual invitation via wifi manager. WifiP2pConfig is being
	 * created with a predefined setup procedure for minimal user interaction.
	 */
	private void sendWifiGroupInvitation(final String macAddress) {
		WifiP2pConfig c = new WifiP2pConfig();
		c.deviceAddress = macAddress;
		// c.wps.setup = WpsInfo.PBC;
		c.wps.setup = WpsInfo.PBC;
		mWifiManager.connect(mChannel, c, null);
	}

	/**
	 * Set up the intent filter to receive all wifi related broadcasted intents.
	 */
	private void initializeIntentFilter() {
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}

	/**
	 * @return True if wifi is enabled, false otherwise.
	 */
	public boolean isWifiP2pEnabled() {
		return isWifiP2pEnabled;
	}

	/**
	 * Register additional connectivity listeners. They are instantly being
	 * informed on the current state.
	 * 
	 * @param l
	 *            Connectivity listener.
	 */
	public void addWifiP2pConnectivityListener(OnWifiP2pConnectivityListener l) {
		mWifiConnectivityListeners.add(l);
		if (isWifiP2pEstablished)
			l.onWifiP2pConnect();
		else
			l.onWifiP2pDisconnect();
	}

	/**
	 * Remove a listener.
	 * 
	 * @param l
	 *            The connectivity listener to be removed.
	 */
	public void removeWifiP2pConnectivityListener(
			OnWifiP2pConnectivityListener l) {
		mWifiConnectivityListeners.remove(l);
	}

	/**
	 * Change the state of the wifi-p2p module. On disconnect, all connectivity
	 * listeners are informed.
	 * 
	 * @param isWifiP2pEnabled
	 */
	public void setWifiP2pEnabled(boolean isWifiP2pEnabled) {
		if (this.isWifiP2pEnabled && this.isWifiP2pEstablished
				&& !isWifiP2pEnabled) {
			for (OnWifiP2pConnectivityListener cur : mWifiConnectivityListeners) {
				cur.onWifiP2pDisconnect();
			}
		}
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	/**
	 * @return Listener for peer list changes.
	 */
	public PeerListListener getPeerListListener() {
		return mPeerListListener;
	}

	/**
	 * 
	 * @return Listener for group information changes.
	 */
	public GroupInfoListener getGroupInfoListener() {
		return mGroupInfoListener;
	}

	/**
	 * 
	 * @return Listener for connection information changes.
	 */
	public ConnectionInfoListener getConnectionInfoListener() {
		return mConnectionInfoListener;
	}

	/**
	 * Change the peer list listener.
	 * 
	 * @param mPeerListListener
	 *            The new listener.
	 */
	public void setPeerListListener(PeerListListener mPeerListListener) {
		this.mPeerListListener = mPeerListListener;
	}

	/**
	 * Change state of the wifi-p2p connection.
	 * 
	 * @param state
	 *            Connected or not?
	 */
	public void setWifiP2pConnection(boolean state) {
		if (isWifiP2pEstablished && !state) {
			for (OnWifiP2pConnectivityListener cur : mWifiConnectivityListeners) {
				cur.onWifiP2pDisconnect();
			}
		} else if (!isWifiP2pEstablished && state) {
			for (OnWifiP2pConnectivityListener cur : mWifiConnectivityListeners) {
				cur.onWifiP2pConnect();
			}
		}
		isWifiP2pEstablished = state;
	}

	/**
	 * Trigger a disconnect. This will cancel any ongoing group-establishment as
	 * well as disconnect the device from any groups.
	 */
	public void disconnect() {
		mWifiManager.cancelConnect(mChannel, null);
		mWifiManager.removeGroup(mChannel, null);
	}

	/**
	 * Initiate a peer discovery on the specified channel.
	 */
	public void initiatePeerDiscovery() {
		mWifiManager.discoverPeers(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				// Log.i(TAG, "Discovering peers...");
			}

			@Override
			public void onFailure(int reason) {
				// Log.d(TAG, "Cant discover peers #" + reason);
			}
		});
	}

	/**
	 * Channel listener for wifi peer to peer is informing us if we're
	 * disconnected from the current channel.
	 */
	private ChannelListener mChannelListener = new ChannelListener() {
		@Override
		public void onChannelDisconnected() {
			// Log.i(TAG, "Channel disconnected!");
		}
	};

	private PeerListListener mPeerListListener = new PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			// if (peers.getDeviceList().size() == 0)
			// Log.e(TAG, "Peer list is zero.");
			if (mCustomPeerListListener != null)
				mCustomPeerListListener.onPeersAvailable(peers);
			mWifiP2pDeviceList.clear();
			mWifiP2pDeviceList.addAll(peers.getDeviceList());
			// for (WifiP2pDevice dev : mWifiP2pDeviceList) {
			// Log.i(TAG, "Peer: " + dev.deviceAddress);
			// }
			if (mSendInvitationTo != null) {
				// Log.i(TAG, "Sent invitation to " + mSendInvitationTo);
				if (sendInvitation(mSendInvitationTo))
					mSendInvitationTo = null;
			}
			// else
			// Log.i(TAG, "Did not send invitation.");
		}
	};

	/**
	 * Group information listener. Will broadcast information to all registered
	 * listeners. Also updates wifi-p2p connection state.
	 */
	private GroupInfoListener mGroupInfoListener = new GroupInfoListener() {
		@Override
		public void onGroupInfoAvailable(WifiP2pGroup group) {
			if (group == null)
				return;
			setWifiGroupInformation(group);

			// if (mCustomGroupInfoListener != null)
			// mCustomGroupInfoListener.onGroupInfoAvailable(group);
			if (!isWifiP2pEstablished) {
				mPeerList.clear();
				setWifiP2pConnection(true);
			}
		}
	};
	/**
	 * Connection information listener, will check state of wifi-p2p connection
	 * as well as inform all registered listeners.
	 */
	private ConnectionInfoListener mConnectionInfoListener = new ConnectionInfoListener() {
		@Override
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			// Log.i(TAG, "Connection information available: groupFormed: "
			// + info.groupFormed + ", owner: " + info.groupOwnerAddress
			// + ", isOwner: " + info.isGroupOwner);
			if (info.groupFormed == false) {
				setWifiP2pConnection(false);
				return;
			}
			mGroupOwnerAddress = info.groupOwnerAddress.getHostAddress();
			if (mCustomConnectionInfoListener != null)
				mCustomConnectionInfoListener.onConnectionInfoAvailable(info);
			if (!isWifiP2pEstablished) {
				setWifiP2pConnection(true);
			}
		}
	};

	/**********************************************************************
	 * 
	 * WiFi Global Methods & Declarations
	 * 
	 **********************************************************************/

	/**
	 * Lists of listeners.
	 */
	private static ArrayList<OnWifiP2pGroupListener> mGroupChangeListener = new ArrayList<OnWifiP2pGroupListener>();
	private static ArrayList<OnWifiP2pConnectivityListener> mWifiConnectivityListeners = new ArrayList<OnWifiP2pConnectivityListener>();
	private static ArrayList<OnWifiP2pIncomingDataListener> mWifiIncomingDataListeners = new ArrayList<OnWifiP2pIncomingDataListener>();
	private static ArrayList<OnWifiP2pOutgoingDataListener> mWifiOutgoingDataListeners = new ArrayList<OnWifiP2pOutgoingDataListener>();

	/**
	 * Current group information stored.
	 */
	private WifiP2pGroup mGroupInformation = null;
	/**
	 * List of WifiPeer objects.
	 */
	private List<WifiPeer> mPeerList = new ArrayList<WifiPeer>();

	/******************** START DATA LISTENERS **************/

	/**
	 * 
	 * @param l
	 *            Group listener to add.
	 */
	public void addWifiP2pGroupChangeListener(OnWifiP2pGroupListener l) {
		if (!mGroupChangeListener.contains(l))
			mGroupChangeListener.add(l);
		l.onWifiP2pGroupChange(getPeerList());
	}

	/**
	 * 
	 * @param l
	 *            Remove group listener.
	 */
	public void removeWifiP2pGroupChangeListener(OnWifiP2pGroupListener l) {
		mGroupChangeListener.remove(l);
	}

	/**
	 * @param l
	 *            Add incoming data listener
	 */

	public void addWifiP2pIncomingDataListener(OnWifiP2pIncomingDataListener l) {
		if (!mWifiIncomingDataListeners.contains(l))
			mWifiIncomingDataListeners.add(l);
	}

	/**
	 * @param l
	 *            remove incoming data listener
	 */

	public void removeWifiP2pIncomingDataListener(
			OnWifiP2pIncomingDataListener l) {
		mWifiIncomingDataListeners.remove(l);
	}

	/**
	 * 
	 * @param l
	 *            add outgoing data listener
	 */
	public void addWifiP2pOutgoingDataListener(OnWifiP2pOutgoingDataListener l) {
		if (!mWifiOutgoingDataListeners.contains(l))
			mWifiOutgoingDataListeners.add(l);
	}

	/**
	 * 
	 * @param l
	 *            remove outgoing data listener
	 */
	public void removeWifiP2pOutgoingDataListener(
			OnWifiP2pOutgoingDataListener l) {
		mWifiOutgoingDataListeners.remove(l);
	}

	/**
	 * Method should be called from the TCP socket-thread, to broadcast received
	 * wifipackets. Checks for different codes and forwards depending on it.
	 * 
	 * @param inc
	 *            The wifipacket received.
	 */
	public void onIncomingTCPTransfer(WifiPacket inc) {
		mIncomingTCP += inc.mDataSize;

		if (inc.mCode == WifiPacket.Code.IP_ADDRESS) {
			final String remoteMac = new String(inc.mData);
			for (WifiPeer cur : mPeerList) {
				// if mac match -> set ip
				if (Support.compareMacAddressesInsensitive(cur.MAC_ADDRESS,
						remoteMac)) {
					cur.IP_ADDRESS = inc.mSender;
					// send ACK to sender
					Intent out = new Intent(ctx, FileSenderService.class);
					out.putExtra(Config.Extra.PACKET_BYTES, new WifiPacket(
							WifiPacket.Code.IP_ACK, getLocalMacAddress()).toByteArray());
					out.putExtra(Config.Extra.RECEIVER_HOST, cur.IP_ADDRESS);
					ctx.startService(out);
					break;
				}
			}
			return;
		} else if (inc.mCode == WifiPacket.Code.IP_ACK) {
			// Log.d(TAG, "IP acknowledged.");
			isIPAcknowledged = true;
			return;
		}
		for (OnWifiP2pIncomingDataListener cur : mWifiIncomingDataListeners) {
			cur.onIncomingTCP(inc);
		}
	}

	/**
	 * Should be called from within the UDP socket thread. Checks the code,
	 * forwards depending on it.
	 * 
	 * @param inc
	 */
	public void onIncomingUDPTransfer(WifiPacket inc) {
		mIncomingUDP += inc.mDataSize;

		if (inc.mCode == WifiPacket.Code.IP_ADDRESS) {
			final String remoteMac = new String(inc.mData);
			for (WifiPeer cur : mPeerList) {
				// if mac match -> set ip
				if (Support.compareMacAddressesInsensitive(cur.MAC_ADDRESS,
						remoteMac)) {
					cur.IP_ADDRESS = inc.mSender;
					// send ACK to sender
					Intent out = new Intent(ctx, FileSenderService.class);
					out.putExtra(Config.Extra.PACKET_BYTES, new WifiPacket(
							WifiPacket.Code.IP_ACK, getLocalMacAddress()).toByteArray());
					out.putExtra(Config.Extra.RECEIVER_HOST, cur.IP_ADDRESS);
					ctx.startService(out);
					break;
				}
			}
			return;
		} else if (inc.mCode == WifiPacket.Code.IP_ACK) {
			// Log.d(TAG, "IP acknowledged.");
			isIPAcknowledged = true;
			return;
		}
		for (OnWifiP2pIncomingDataListener cur : mWifiIncomingDataListeners) {
			cur.onIncomingUDP(inc);
		}
	}

	/**
	 * 
	 * @param bytes
	 *            Bytes that have been sent via TCP
	 */
	public void onOutgoingTCPTransfer(int bytes) {
		mOutgoingTCP += bytes;
		for (OnWifiP2pOutgoingDataListener cur : mWifiOutgoingDataListeners) {
			cur.onOutgoingTCP(bytes);
		}
	}

	/**
	 * 
	 * @param bytes
	 *            Bytes that have been sent via UDP
	 */
	public void onOutgoingUDPTransfer(int bytes) {
		mOutgoingUDP += bytes;
		for (OnWifiP2pOutgoingDataListener cur : mWifiOutgoingDataListeners) {
			cur.onOutgoingUDP(bytes);
		}
	}

	/****************** END DATA LISTENERS ***************/

	/**
	 * 
	 * @return Current wifi group information.
	 */
	public WifiP2pGroup getWifiP2pGroupInformation() {
		return mGroupInformation;
	}

	/**
	 * Update wifi group information.
	 * 
	 * @param group
	 *            Group information object from the wifi manager.
	 */
	public void setWifiGroupInformation(WifiP2pGroup group) {
		if (group == null)
			return;
		// Log.d(TAG, "Got group information. " + group.getNetworkName());
		mGroupInformation = group;
		boolean self = false;

		WifiP2pDevice owner = group.getOwner();
		// Log.d(TAG, "Owner is " + owner.deviceAddress);
		if (Support.compareMacAddressesInsensitive(owner.deviceAddress,
				getLocalMacAddress())) {
			// i am the owner. i shall do nothing.
			self = true;
		} else {
			// send ip address to owner.
			if (!hasSentIPToOwner || !isIPAcknowledged) {
				WifiPacket ipPacket = new WifiPacket(WifiPacket.Code.IP_ADDRESS,
						getLocalMacAddress());
				hasSentIPToOwner = Support
						.sendWifiPacketToOwner(this, ipPacket);
				addSelfToPeerList();
			}
		}

		if (getPeerByMac(owner.deviceAddress) != null) {
			// Log.d(TAG, "Already in list.");
		} else {
			// ip address is unknown!
			// Log.d(TAG, "Adding to list.!");
			mPeerList.add(new WifiPeer(owner.deviceAddress, mGroupOwnerAddress,
					true, self));
		}
		// mPeerList.clear();
		for (WifiP2pDevice dev : mGroupInformation.getClientList()) {
			// Log.d(TAG, "Device in Group: " + dev.deviceAddress);

			self = false;
			// Log.d(TAG, "Checking if " + dev.deviceAddress + " equals "
			// + getLocalMacAddress());
			if (Support.compareMacAddressesInsensitive(dev.deviceAddress,
					getLocalMacAddress())) {
				self = true;
			}

			if (dev.isGroupOwner()) {
				// Log.d(TAG, "Is owner, already checked, so nope!");
				continue;
			}
			// device already in list, skip.
			if (getPeerByMac(dev.deviceAddress) != null) {
				// Log.d(TAG, "Already in list.");
				continue;
			}

			// ip address is unknown! -> if not owner, send ip address package
			// to owner.
			// Log.d(TAG, "Adding to list.!");

			mPeerList.add(new WifiPeer(dev.deviceAddress, null, dev
					.isGroupOwner(), self));
		}

		final WifiPeer[] peers = getPeerList();
		for (OnWifiP2pGroupListener cur : mGroupChangeListener) {
			cur.onWifiP2pGroupChange(peers);
		}
	}

	/**
	 * Supporting method to add self to wifi peer list. Devices that are not the
	 * group owner otherwise do not "see" themselves.
	 */
	private void addSelfToPeerList() {
		for (WifiPeer cur : mPeerList) {
			if (cur.MAC_ADDRESS.equals(getLocalMacAddress()))
				return;
		}
		try {
			WifiPeer self = new WifiPeer(getLocalMacAddress(),
					Support.getLocalIpAddress(mGroupOwnerAddress), false, true);
			if (!mPeerList.contains(self))
				mPeerList.add(self);
		} catch (Exception e) {

		}
	}

	/**
	 * Set IP address of peer by MAC address.
	 * 
	 * @param mac
	 *            MAC address of the peer.
	 * @param ip
	 *            IP address of the peer.
	 */
	public void setWifiPeerIPAddress(String mac, String ip) {
		WifiPeer p = getPeerByMac(mac);
		if (p != null)
			p.IP_ADDRESS = ip;
	}

	/**
	 * Add wifi peer to list.
	 * 
	 * @param peer
	 *            WifiPeer object.
	 */
	public void addWifiPeer(WifiPeer peer) {
		mPeerList.add(peer);
	}

	/**
	 * 
	 * @return Array of wifi peer objects representing the list of peers at the
	 *         specific moment.
	 */
	public WifiPeer[] getPeerList() {
		return mPeerList.toArray(new WifiPeer[mPeerList.size()]);
	}

	/**
	 * Return peer by MAC address.
	 * 
	 * @param MAC_ADDRESS
	 *            To be searched for.
	 * @return Null if no peer found, otherwise the peer.
	 */
	public WifiPeer getPeerByMac(String MAC_ADDRESS) {
		for (WifiPeer cur : mPeerList)
			if (Support.compareMacAddressesInsensitive(MAC_ADDRESS,
					cur.MAC_ADDRESS))
				return cur;
		return null;
	}

	/**
	 * Return peer by IP address.
	 * 
	 * @param IP_ADDRESS
	 *            To be searched for.
	 * @return Null if no peer found, otherwise the peer.
	 */
	public WifiPeer getPeerByIp(String IP_ADDRESS) {
		for (WifiPeer cur : mPeerList)
			if (cur.IP_ADDRESS.equals(IP_ADDRESS))
				return cur;
		return null;
	}

	/**
	 * Return peer group owner
	 * 
	 * @return Null if no group owner found, otherwise the group owner.
	 */
	public WifiPeer getPeerGroupOwner() {
		for (WifiPeer cur : mPeerList)
			if (cur.isGroupOwner)
				return cur;
		return null;
	}

	/**
	 * 
	 * @param peer
	 *            Peer to be removed from the list.
	 */
	public void removeWifiPeer(WifiPeer peer) {
		mPeerList.remove(peer);
	}
}
