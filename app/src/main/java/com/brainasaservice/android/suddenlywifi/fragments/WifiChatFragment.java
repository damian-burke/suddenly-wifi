package com.brainasaservice.android.suddenlywifi.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.brainasaservice.android.suddenlywifi.R;
import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.interfaces.OnWifiP2pIncomingDataListener;
import com.brainasaservice.android.suddenlywifi.model.WifiFragment;
import com.brainasaservice.android.suddenlywifi.model.WifiPacket;
import com.brainasaservice.android.suddenlywifi.model.WifiPeer;
import com.brainasaservice.android.suddenlywifi.services.FileSenderService;

/**
 * 
 * @author Damian Burke
 * 
 *         Fragment holding and displaying chat information.
 * 
 */
public class WifiChatFragment extends WifiFragment implements
		OnWifiP2pIncomingDataListener {
	private static WifiChatFragment mInstance = null;

	private Handler mHandler = new Handler();

	private ListView mChatList = null;
	private Button mChatButton = null;
	private EditText mChatEdit = null;

	private ChatListAdapter mChatListAdapter = new ChatListAdapter();
	private ArrayList<ChatMessage> mChatMessageList = new ArrayList<ChatMessage>();
	private ArrayList<WifiPeer> mAnnouncedPeers = new ArrayList<WifiPeer>();

	public WifiChatFragment() {
	}

	public static WifiChatFragment getInstance() {
		if (mInstance == null)
			mInstance = new WifiChatFragment();
		return mInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		final View view = inflater.inflate(R.layout.fragment_wifichat, null);
		mChatList = (ListView) view.findViewById(R.id.fragment_wifichat_list);
		mChatEdit = (EditText) view.findViewById(R.id.fragment_wifichat_edit);
		mChatButton = (Button) view.findViewById(R.id.fragment_wifichat_btn);

		mChatList.setAdapter(mChatListAdapter);
		mChatButton.setOnClickListener(lChatSendListener);

		getWifiController().addWifiP2pIncomingDataListener(this);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getWifiController().removeWifiP2pIncomingDataListener(this);
	}

	/**
	 * Send-Button listener. Checks whether valid text is entered, then sends it
	 * to each peer.
	 */
	private View.OnClickListener lChatSendListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mChatEdit.getEditableText() != null) {
				String txt = mChatEdit.getEditableText().toString();
				for (WifiPeer cur : mWifiController.getPeerList()) {
					if (cur.isSelf || cur.IP_ADDRESS == null)
						continue;

					Intent out = new Intent(getActivity(),
							FileSenderService.class);
					out.putExtra(Config.Extra.RECEIVER_HOST, cur.IP_ADDRESS);

					byte[] bytes = (String.valueOf(WifiPacket.Code.TEXT_MESSAGE) + ".,."
							+ String.valueOf(txt.getBytes().length) + ".,." + txt)
							.getBytes();

					out.putExtra(Config.Extra.PACKET_BYTES, bytes);
					getActivity().startService(out);
				}

				mChatEdit.setText("");

				ChatMessage msg = new ChatMessage("YOU", txt, false);
				mChatMessageList.add(msg);
				mChatListAdapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	public void onWifiP2pConnect() {
		if (mChatButton != null)
			mChatButton.setEnabled(true);
		if (mChatEdit != null)
			mChatEdit.setEnabled(true);
	}

	@Override
	public void onWifiP2pDisconnect() {
		if (mChatButton != null)
			mChatButton.setEnabled(false);
		if (mChatEdit != null)
			mChatEdit.setEnabled(false);
	}

	@Override
	public void onWifiP2pGroupChange(WifiPeer[] peers) {
		for (WifiPeer cur : peers) {
			if (!mAnnouncedPeers.contains(cur) && cur.IP_ADDRESS != null) {
				mChatMessageList.add(new ChatMessage("SYSTEM",
						"New peer joined: " + cur.IP_ADDRESS, true));
				mChatListAdapter.notifyDataSetChanged();
				mAnnouncedPeers.add(cur);
			}
		}
	}

	/**
	 * 
	 * @author Damian Burke
	 * 
	 *         Chat list adapter extending base adapter. Used for the ListView
	 *         to display each message in a new row. Also displaying system
	 *         messages.
	 * 
	 */
	private class ChatListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mChatMessageList.size();
		}

		@Override
		public ChatMessage getItem(int position) {
			return mChatMessageList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.fragment_wifichat_chatmessage, null);
			}

			final ChatMessage msg = getItem(position);
			final TextView mSender = (TextView) v
					.findViewById(R.id.fragment_wifichat_item_sender);
			final TextView mText = (TextView) v
					.findViewById(R.id.fragment_wifichat_item_text);
			final TextView mDate = (TextView) v
					.findViewById(R.id.fragment_wifichat_item_date);

			if (msg.received) {
				mText.setGravity(Gravity.RIGHT);
			} else
				mText.setGravity(Gravity.LEFT);

			mSender.setText(msg.sender);
			mDate.setText(msg.getTime());
			mText.setText(msg.text);

			return v;
		}

	}

	/**
	 * 
	 * @author Damian Burke
	 * 
	 *         Chat message structure. Keeping reference of the sender's IP
	 *         address, date of retrieval, the message itself as well as whether
	 *         or not it is an incoming or outgoing message.
	 * 
	 */
	private class ChatMessage {
		public String sender;
		public long date;
		public String text;
		public boolean received;

		/**
		 * 
		 * @param sender Sender's IP address
		 * @param text Text message
		 * @param received Was this message received?
		 */
		public ChatMessage(String sender, String text, boolean received) {
			this.sender = sender;
			this.text = text;
			this.received = received;
			this.date = System.currentTimeMillis();
		}

		/**
		 * 
		 * @return Time of retrieval as string.
		 */
		public String getTime() {
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss",
					Locale.getDefault());
			return formatter.format(date);
		}
	}

	@Override
	public void onIncomingUDP(WifiPacket packet) {
		// do not care. text = tcp!
	}

	@Override
	public void onIncomingTCP(WifiPacket packet) {
		if (packet.mCode != WifiPacket.Code.TEXT_MESSAGE)
			return;

		final ChatMessage msg = new ChatMessage(packet.mSender, new String(
				packet.mData), true);
		mChatMessageList.add(msg);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mChatListAdapter.notifyDataSetChanged();
				mChatList.setSelection(mChatListAdapter.getCount() - 1);
			}
		});
	}
}
