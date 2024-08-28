package de.kaiserdragon.callforwardingstatus.sms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;

import androidx.annotation.Nullable;

import de.kaiserdragon.callforwardingstatus.R;

public class SmsReceiverService extends Service {

    BroadcastReceiver receiver;

    private static final String CHANNEL_ID = "PhoneAndSmsDefault";

    public SmsReceiverService() {
        receiver = new SmsReceiver();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        registerReceiver(receiver, filter);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getText(R.string.notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationManager.createNotificationChannel(channel);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_f)
                        .setColor(getColor(R.color.colorPrimary))
                        .setOngoing(true)
                        .build();

        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}