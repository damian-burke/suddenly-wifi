package com.brainasaservice.android.suddenlywifi.control;

import java.nio.charset.Charset;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.util.Log;

import com.brainasaservice.android.suddenlywifi.etc.Config;
import com.brainasaservice.android.suddenlywifi.interfaces.OnNdefDiscovered;
import com.brainasaservice.android.suddenlywifi.interfaces.OnNdefPushComplete;

/**
 * NFC controller class to handle all the relevant nfc data - for example
 * setting up the foreground dispatch and disabling it, creating the NdefMessage
 * in runtime, read and parse all recognized NdefMessages and their records.
 *
 * @author Damian Burke
 */
public class NfcController {
    /**
     * Tag for logging purpose.
     */
    private static final String TAG = "NfcController";
    /**
     * Ndef message mime type. Should be the same as in the AndroidManifest to
     * properly read and push Ndef messages.
     */
    private static final String NDEF_MIME_TYPE = "application/com.brainasaservice.android";

    private final Activity activity;
    private final Context ctx;

    /**
     * Internal callback to report back to the application after the Ndef
     * message has been pushed.
     */
    private OnNdefPushComplete mOnNdefPushComplete = null;
    /**
     * Internal callback to deliver data from received NdefMessages.
     */
    private OnNdefDiscovered mOnNdefDiscovered = null;
    /**
     * Pending intent to start on dispatching a ndef message.
     */
    private final PendingIntent mPendingIntent;
    /**
     * The nfc adapter to communicate via nfc technology.
     */
    private final NfcAdapter mNfcAdapter;
    /**
     * Payload for the outgoing Ndef message.
     */
    private String NDEF_PAYLOAD = "Hello world.";

    public NfcController(Activity activity) {
        this.activity = activity;
        this.ctx = activity.getApplicationContext();
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(ctx);
        this.mNfcAdapter.setNdefPushMessageCallback(mNdefMessageCallback,
                activity);
        this.mNfcAdapter.setOnNdefPushCompleteCallback(
                mOnNdefPushCompleteCallback, activity);

        Intent intent = new Intent(ctx, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
    }

    /**
     * Method should be called in activity.onResume() to prepare the NfcAdapter.
     */
    public void onResume() {
        try {
            mNfcAdapter.enableForegroundDispatch(activity, mPendingIntent,
                    null, null);
        } catch (Exception e) {
        }
    }

    /**
     * Method should be called in activity.onPause() to disable the NfcAdapter.
     */
    public void onPause() {
        try {
            mNfcAdapter.disableForegroundDispatch(activity);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Disable foreground dispatch earlier!");
        }
    }

    /**
     * @param payload String supposed to be transferred via NFC.
     */
    public void setNdefPayload(String payload) {
        this.NDEF_PAYLOAD = Config.NFC_MESSAGE_PREFIX + payload;
    }

    /**
     * Should be called in activity.onNewIntent
     *
     * @param intent Intent containing information about read tags/messages.
     * @return True if the intent was consumed by the controller. False if it
     * did not contain any important information.
     */
    public boolean onNewIntent(Intent intent) {
        Log.d("onNewIntent", "Action is " + intent.getAction());
        // only intents for discovered ndef messages
        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.d(TAG, "Wrong action");
            return false;
        }

        // read the intent
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs == null) {
            Log.d(TAG, "No messages");
            // no data in intent
            return false;
        }

        // parse each message
        for (int i = 0; i < rawMsgs.length; ++i) {
            NdefMessage msg = (NdefMessage) rawMsgs[i];
            NdefRecord[] records = msg.getRecords();
            String[] payloads = new String[records.length];
            for (NdefRecord record : records) {
                // get record's payload as string
                payloads[i] = new String(record.getPayload());
            }
            if (mOnNdefDiscovered != null)
                mOnNdefDiscovered.onNdefDiscovery(payloads);
        }

        return true;
    }

    /**
     * Register a callback to be fired whenever a NdefMessage is pushed
     * completely.
     *
     * @param callback Callback which is supposed to be executed.
     */
    public void setOnNdefPushComplete(OnNdefPushComplete callback) {
        this.mOnNdefPushComplete = callback;
    }

    /**
     * Register a callback to be fired whenever a NdefMessage with matching mime
     * type is discovered.
     *
     * @param callback Callback which is supposed to be executed.
     */
    public void setOnNdefDiscovered(OnNdefDiscovered callback) {
        this.mOnNdefDiscovered = callback;
    }

    /**
     * This callback will be fired once the Ndef message is pushed completely to
     * the connected device.
     */
    private OnNdefPushCompleteCallback mOnNdefPushCompleteCallback = new OnNdefPushCompleteCallback() {
        @Override
        public void onNdefPushComplete(NfcEvent event) {
            // pushed the ndef message. should change state.
            if (mOnNdefPushComplete != null)
                mOnNdefPushComplete.onNdefPushComplete(event);
        }
    };

    /**
     * This callback will create the Ndef message at runtime, which is supposed
     * to be pushed to the connected device.
     */
    private CreateNdefMessageCallback mNdefMessageCallback = new CreateNdefMessageCallback() {
        @Override
        public NdefMessage createNdefMessage(NfcEvent event) {
            NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    NDEF_MIME_TYPE.getBytes(Charset.defaultCharset()),
                    new byte[0],
                    NDEF_PAYLOAD.getBytes(Charset.defaultCharset()));
            NdefRecord[] records = {record};
            NdefMessage msg = new NdefMessage(records);
            return msg;
        }
    };
}
