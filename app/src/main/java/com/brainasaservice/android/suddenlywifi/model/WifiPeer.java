package com.brainasaservice.android.suddenlywifi.model;

/**
 * 
 * @author Damian Burke
 * 
 *         Class representing a peer in the wifi network. Containing a MAC
 *         address, IP address as well as boolean state variables for group
 *         owner and the current device (self).
 * 
 */
public class WifiPeer {
	public String MAC_ADDRESS = null;
	public String IP_ADDRESS = null;
	public boolean isGroupOwner = false;
	public boolean isSelf = false;

	public WifiPeer(String MAC_ADDRESS) {
		this.MAC_ADDRESS = MAC_ADDRESS;
	}

	public WifiPeer(String MAC_ADDRESS, String IP_ADDRESS) {
		this.MAC_ADDRESS = MAC_ADDRESS;
		this.IP_ADDRESS = IP_ADDRESS;
	}

	public WifiPeer(String MAC_ADDRESS, String IP_ADDRESS, boolean isGroupOwner) {
		this.MAC_ADDRESS = MAC_ADDRESS;
		this.IP_ADDRESS = IP_ADDRESS;
		this.isGroupOwner = isGroupOwner;
	}

	public WifiPeer(String MAC_ADDRESS, String IP_ADDRESS,
			boolean isGroupOwner, boolean self) {
		this.MAC_ADDRESS = MAC_ADDRESS;
		this.IP_ADDRESS = IP_ADDRESS;
		this.isGroupOwner = isGroupOwner;
		this.isSelf = self;
	}

}
