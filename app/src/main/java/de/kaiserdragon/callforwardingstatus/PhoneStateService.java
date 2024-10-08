package de.kaiserdragon.callforwardingstatus;

import static de.kaiserdragon.callforwardingstatus.BuildConfig.DEBUG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.kaiserdragon.callforwardingstatus.ForwardingStatusWidget;
import de.kaiserdragon.callforwardingstatus.R;

public class PhoneStateService extends Service {
    private static final String CHANNEL_ID = "PhoneAndSmsDefault";
    public static boolean currentState;
    Context context;
    final String TAG = "Service";
    private final Executor executor = Executors.newSingleThreadExecutor();


    @TargetApi(Build.VERSION_CODES.R)
    private final PhoneStateListener phoneStateListener = new PhoneStateListener(executor) {
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            if (DEBUG)Log.i(TAG, "onCallForwardingIndicatorChanged  CFI  Old=" + cfi);
            // Get the current state of unconditional call forwarding
            currentState = cfi;
            // Create an Intent with the android.appwidget.action.APPWIDGET_UPDATE action
            Intent intent = new Intent(context, ForwardingStatusWidget.class);
            intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI");
            //Toast.makeText(context, "OLD", Toast.LENGTH_SHORT).show();
            // Add the app widget IDs as an extra
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ForwardingStatusWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

            // Add the CFI value as an extra
            intent.putExtra("cfi", currentState);

            // Send the broadcast
            sendBroadcast(intent);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    super.onCallForwardingIndicatorChanged(cfi);
                }
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        // Create the notification channel
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Call Forwarding Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.NotificationTitle))
                .setContentText(getString(R.string.NotificationText))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_call_forwarding)
                .build();

        // Start the service as a foreground service
        startForeground(1, notification);
        context = this;

        // Register MyPhoneStateListener as a phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerCallForwardingIndicatorListener(telephonyManager);
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
        }
    }

    //todo new api does not work right
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void registerCallForwardingIndicatorListener(TelephonyManager telephonyManager) {
        // New API (API level 31 and above)
        TelephonyCallback callback = new TelephonyCallback();
         TelephonyCallback.CallForwardingIndicatorListener callForwardingListener =
                cfi -> {
                    if (DEBUG)Log.i(TAG, "onCallForwardingIndicatorChanged  CFI New=" + cfi);
                    // Get the current state of unconditional call forwarding
                    //Toast.makeText(context, "New", Toast.LENGTH_SHORT).show();
                    currentState = cfi;
                    // Create an Intent with the android.appwidget.action.APPWIDGET_UPDATE action
                    Intent intent = new Intent(context, ForwardingStatusWidget.class);
                    intent.setAction("de.kaiserdragon.callforwardingstatus.APPWIDGET_UPDATE_CFI");

                    // Add the app widget IDs as an extra
                    int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ForwardingStatusWidget.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

                    // Add the CFI value as an extra
                    intent.putExtra("cfi", currentState);

                    // Send the broadcast
                    sendBroadcast(intent);
                };
        // Register the TelephonyCallback
        //telephonyManager.registerTelephonyCallback(executor,callForwardingListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister MyPhoneStateListener as a phone state listener
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return null as this service is not bound to any activity
        return null;
    }
}
