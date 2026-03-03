package com.example.spamfilter;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

public class SpamFilterImpl extends CallScreeningService {

    private static final String TAG = SpamFilterImpl.class.getSimpleName();
    static final String SPAM_FILTER = "spam_filter_settings";
    static final String LAST_CALL_TIME_CONFIG = "last_call_time_config";
    static final String LAST_CALL_NUMBER_CONFIG = "last_call_number_config";
    @Override
    public void onScreenCall(Call.Details callDetails) {
        String number = callDetails.getHandle().getSchemeSpecificPart();
        Log.i(TAG, "Call incoming " + number);

        if (SpamFilterSettings.callBlocking) {
            Log.i(TAG, "Blocking call to " + number);

            if (notInContacts(number) && notInContacts("+1" + number) && notInContacts("+86" + number)) {

                SharedPreferences sharedPreferences = this.getSharedPreferences(SPAM_FILTER, MODE_PRIVATE);

                String lastCallNumber = sharedPreferences.getString(LAST_CALL_NUMBER_CONFIG, "");
                long lastCallTime = sharedPreferences.getLong(LAST_CALL_TIME_CONFIG, 0);

                if (SpamFilterSettings.allowRepeated && number.equals(lastCallNumber) && System.currentTimeMillis() - lastCallTime < SpamFilterSettings.repeatedWithinMinutes * 1000L * 60L) {
                    Log.i(TAG, "Repeated call to " + number + ". Allowed.");
                } else {
                    respondToCall(callDetails, respond());
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LAST_CALL_NUMBER_CONFIG, number);
                editor.putLong(LAST_CALL_TIME_CONFIG, System.currentTimeMillis());
                editor.apply();

                Log.i(TAG, "Incoming number not in contact list: " + number);
            } else {
                Log.i(TAG, "Incoming number in contact list: " + number);
            }
        }
    }

    private CallResponse respond() {
        return new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(false)
                .setSkipCallLog(false)
                .build();
    }

    private boolean notInContacts(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] phoneNumber = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Log.d(TAG, "Looking up number: " + number + " using URI: " + uri);
        try (Cursor cursor = getContentResolver().query(uri, phoneNumber, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "Number found in contacts. Name: " + cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)) + ", Number: " + cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER)));
                return false;
            }
            Log.d(TAG, "Number not found in contacts.");
        }
        return true;
    }
}
