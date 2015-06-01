package com.DKB.shutupdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class text extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("message received");
        if (CarMode.autoreply) {
            if (intent.getAction().equals(
                    "android.provider.Telephony.SMS_RECEIVED")) {
                System.out.println("SMS received");
                // gets the message
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String msg_from;
                String msg = CarMode.msg;
                if (bundle != null) {
                    // ---retrieve the SMS message received---
                    try {
                        // gets the sender then sends a sms back
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage
                                    .createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            System.out
                                    .println("#### SMS Received: " + msg_from);
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(msg_from, null, msg,
                                    null, null);
                        }
                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }
            }
        }
    }
}
