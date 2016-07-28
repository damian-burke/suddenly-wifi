package com.brainasaservice.android.suddenlywifi.etc;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.brainasaservice.android.suddenlywifi.fragments.WifiAudioFragment;
import com.brainasaservice.android.suddenlywifi.fragments.WifiChatFragment;
import com.brainasaservice.android.suddenlywifi.fragments.WifiInfoFragment;

/**
 * 
 * @author Damian Burke
 * 
 * Pager adapter for the viewpager in the sample application. Displays three fragments (info/chat/audio).
 *
 */
public class WifiPagerAdapter extends FragmentPagerAdapter {
	private final Context ctx;
	private static WifiPagerAdapter instance = null;

	/**
	 * 
	 * @param ctx Context of the activity.
	 * @param fm Fragment manager.
	 * @return Static instance of the adapter if needed.
	 */
	public static WifiPagerAdapter getInstance(Context ctx, FragmentManager fm) {
		if (WifiPagerAdapter.instance == null)
			WifiPagerAdapter.instance = new WifiPagerAdapter(ctx, fm);
		
		return WifiPagerAdapter.instance;
	}

	private WifiPagerAdapter(Context ctx, FragmentManager fm) {
		super(fm);
		this.ctx = ctx;
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = new WifiChatFragment();
		switch (i) {
		case 0:
			return WifiChatFragment.getInstance();
		case 1:
			return WifiInfoFragment.getInstance();
		case 2:
			return WifiAudioFragment.getInstance();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Chat";
		case 1:
			return "Info";
		case 2:
			return "Audio";
		}
		return "";
	}
}
