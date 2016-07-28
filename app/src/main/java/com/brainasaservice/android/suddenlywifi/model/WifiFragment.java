package com.brainasaservice.android.suddenlywifi.model;

import com.brainasaservice.android.suddenlywifi.control.WifiController;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pConnectivityListener;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pGroupListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author Damian Burke
 * 
 *         Abstract class for fragmented activities relying on Wifi-Direct. As
 *         soon as the view is created, the fragment registers itself with the
 *         wifi controller to receive all needed updates.
 * 
 */
public abstract class WifiFragment extends Fragment implements
		OnWifiP2pConnectivityListener, OnWifiP2pGroupListener {

	protected WifiController mWifiController = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mWifiController = ((WifiActivity) getActivity()).getWifiController();
		mWifiController.addWifiP2pConnectivityListener(this);
		mWifiController.addWifiP2pGroupChangeListener(this);
		return null;
	}

	@Override
	public void onDestroyView() {
		mWifiController.removeWifiP2pConnectivityListener(this);
		mWifiController.removeWifiP2pGroupChangeListener(this);
		super.onDestroyView();
	}

	/**
	 * 
	 * @return Instance of wifi controller.
	 */
	public WifiController getWifiController() {
		return mWifiController;
	}

	@Override
	public abstract void onWifiP2pConnect();

	@Override
	public abstract void onWifiP2pDisconnect();

}
