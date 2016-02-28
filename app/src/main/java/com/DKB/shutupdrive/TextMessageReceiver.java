package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by kyle on 2/28/16.
 */
public class TextMessageReceiver {

    private BroadcastReceiver textReceiver;
    private Context context;

    public abstract static class OnReceiveText {
        public abstract void onReceive(String phoneNumber, String message);
    }

    public TextMessageReceiver(Context context, final OnReceiveText onReceiveText) {
        this.context = context;
        textReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(
                        "android.provider.Telephony.SMS_RECEIVED")) {
                    Bundle bundle = intent.getExtras();
                    SmsMessage[] msgs;
                    if (bundle != null) {
                        try {
                            Object[] pdus = (Object[]) bundle.get("pdus");
                            msgs = new SmsMessage[pdus.length];
                            for (int i = 0; i < msgs.length; i++) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    msgs[i] = SmsMessage
                                            .createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                                } else {
                                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                }
                                onReceiveText.onReceive(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                            }
                        } catch (Exception e) {
                            Log.d("Text Message Received", e.getMessage());
                        }
                    }
                }
            }
        };
    }


    public void start() {
        if (textReceiver != null)
            context.registerReceiver(textReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    public void stop() {
        if (textReceiver != null)
            context.unregisterReceiver(textReceiver);
    }

}
