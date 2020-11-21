package com.example.digitalclock.ui.stopwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StopwatchButtonReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context,"clicked",Toast.LENGTH_LONG).show();

        this.context=context;

        sendMessage();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(111);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
        mb.setContentIntent(resultPendingIntent);
    }

    public void sendMessage(){
        Log.d("sender", "Broadcasting message of Alarm action");
        Intent intent = new Intent("stopwatch-action");
        intent.putExtra("message", "stop");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
