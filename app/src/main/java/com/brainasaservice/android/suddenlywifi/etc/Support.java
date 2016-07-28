package com.brainasaservice.android.suddenlywifi.etc;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;


import android.content.Intent;
import android.util.Log;

import com.brainasaservice.android.suddenlywifi.control.WifiController;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket;
import com.brainasaservice.android.suddenlywifi.services.FileSenderService;

/**
 * 
 * @author Damian Burke
 * Some supporting methods to make life easier.
 */
public class Support {
	/**
	 * Insensitive mac address comparison, only 80% have to be "correct".
	 * 
	 * @param mac1
	 *            Mac address one
	 * @param mac2
	 *            Mac address two
	 * @return Are they almost equal?
	 */
	public static boolean compareMacAddressesInsensitive(String mac1,
			String mac2) {
		if (mac1 == null || mac2 == null)
			return false;
		String[] part1 = mac1.split(":");
		String[] part2 = mac2.split(":");
		if (part1.length != part2.length)
			return false;
		int correct = 0;
		for (int i = 0; i < part1.length; i++) {
			if (part1[i].toLowerCase().equals(part2[i].toLowerCase()))
				correct++;
		}
		if (correct >= part1.length - 1)
			return true;
		return false;
	}

	/**
	 * @param bytes
	 *            Byte count as long.
	 * @return Human-readable string representing the bytes.
	 */
	public static String formatBytes(long bytes) {
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = String.valueOf("KMGTPE".charAt(exp - 1));
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * This method simply takes a string and returns the md5 hash of it, by
	 * using the standard java.security.MessageDigest class.
	 * 
	 * @param s
	 *            String supposed to be turned into a md5 hash.
	 * @return The md5 hash
	 */
	public static final String MD5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Returns the current local IP address filtered by a subnet (should be the one from the group information).
	 * If not filtered, there may be a wrong IP address returned if the device is connected to other networks.
	 * @param subnet Subnet (IP Subnet as string) to filter.
	 * @return Byte array representing the IP address.
	 */
	private static byte[] getLocalIPAddress(String subnet) {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (inetAddress instanceof Inet4Address) { // fix for
																	// Galaxy
																	// Nexus.
																	// IPv4 is
																	// easy to
																	// use :-)
							if (inetAddress.getHostAddress().startsWith(subnet))
								return inetAddress.getAddress();
						}
						// return inetAddress.getHostAddress().toString(); //
						// Galaxy Nexus returns IPv6
					}
				}
			}
		} catch (SocketException ex) {
			// Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		} catch (NullPointerException ex) {
			// Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		}
		return null;
	}

	/**
	 * Returns the current local IP address filtered by a subnet (should be the one from the group information).
	 * If not filtered, there may be a wrong IP address returned if the device is connected to other networks.
	 * @param net Subnet (IP Subnet as string) to filter.
	 * @return String representing the IP address of the device. If no IP address found, an empty string.
	 */
	public static String getLocalIpAddress(String net) {
		// convert to dotted decimal notation:
		String subnet = net.substring(0, 6);
		byte[] ipAddr = getLocalIPAddress(subnet);
		String ipAddrStr = "";
		for (int i = 0; i < ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i] & 0xFF;
		}
		return ipAddrStr;
	}

	/**
	 * Send a wifi packet to the intent service.
	 * @param c Wifi Controller (context)
	 * @param p The packet to send
	 * @param ip Target device IP address
	 */
	public static final void sendWifiPacket(WifiController c, WifiPacket p,
											String ip) {
		Log.d("Support", "Sending packet "+new String(p.mData)+" to "+ip);
		Intent sendIntent = new Intent(c.getContext(), FileSenderService.class);
		sendIntent.setAction("send");
		sendIntent.putExtra(Config.Extra.RECEIVER_HOST, ip);
		sendIntent.putExtra(Config.Extra.PACKET_BYTES, p.toByteArray());
		c.getContext().startService(sendIntent);
	}

	/**
	 * Send packet to the group owner.
	 * @param c Wifi Controller (context)
	 * @param p The packet to send.
	 * @return True if owner was found and packet is sent, false otherwise.
	 */
	public static final boolean sendWifiPacketToOwner(WifiController c,
			WifiPacket p) {
		if (c.getPeerGroupOwner() == null)
			return false;
		sendWifiPacket(c, p, c.getPeerGroupOwner().IP_ADDRESS);
		return true;
	}
}
