package com.example.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    // شناسهٔ منحصربه‌فرد برای JobIntentService
    private static final int JOB_ID = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        String sender = "";

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            if (sender.isEmpty()) {
                sender = sms.getDisplayOriginatingAddress();
            }
            fullMessage.append(sms.getMessageBody());
        }

        String messageText = fullMessage.toString();

        // آماده کردن Intent برای سرویس
        Intent serviceIntent = new Intent(context, SmsProcessingService.class);
        serviceIntent.putExtra("sender", sender);
        serviceIntent.putExtra("message", messageText);

        // enqueue به JobIntentService
        SmsProcessingService.enqueueWork(context, serviceIntent);
    }
}
