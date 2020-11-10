package com.example.digitalclock.ui.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ButtonReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context=context;



        if(intent.getExtras().getString("action").equals("dismiss")){
            sendMessage("dismiss");
        }
        else{
            sendMessage("snooze");
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
        mb.setContentIntent(resultPendingIntent);

    }

    private void sendMessage(String msg) {
        Log.d("sender", "Broadcasting message of Alarm action");
        Intent intent = new Intent("alarm-action");
        // You can also include some extra data.
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
