package com.brainasaservice.android.suddenlywifi.activities;

import com.brainasaservice.android.suddenlywifi.R;
import com.brainasaservice.android.suddenlywifi.etc.WifiPagerAdapter;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

/**
 * 
 * @author Damian Burke
 * 
 * This class extends WifiActivity. It's only purpose of existance is to hold and control the view pager which holds the three fragments.
 * If the intent with which the activity was started contains the invite-command, the MAC address received is passed to the wifi controller
 * to initiate the invitation into a wifi-direct group.
 */

public class WifiActivity extends
		com.brainasaservice.android.suddenlywifi.model.WifiActivity {

	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(WifiPagerAdapter.getInstance(
				getApplicationContext(), getSupportFragmentManager()));
		mViewPager.setCurrentItem(1);

		if (getIntent() != null && getIntent().getExtras() != null) {
			final String invite = getIntent().getExtras().getString("invite");
			if (invite != null) {
				getWifiController().sendInvitation(invite);
			}
		}
	}

	@Override
	public void onWifiP2pConnect() {
		Toast.makeText(getApplicationContext(), "Connected!",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onWifiP2pDisconnect() {
		Toast.makeText(getApplicationContext(), "Disconnected!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onWifiP2pGroupChange(WifiPeer[] peers) {
		// Log.d(TAG, "onWifiP2pGroupChange: ");
		// for (WifiPeer cur : peers) {
		// Log.d(TAG, "-- ip=" + cur.IP_ADDRESS + " / mac=" + cur.MAC_ADDRESS
		// + " / owner=" + cur.isGroupOwner + " / self=" + cur.isSelf);
		// }
	}

}
