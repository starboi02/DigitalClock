package com.example.digitalclock.ui.stopwatch;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.alarm.ButtonReceiver;

import java.util.concurrent.TimeUnit;

public class StopwatchBroadcastReceiver extends BroadcastReceiver {
    long startTime=0;
    long prevTime=0;
    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"Stopwatch broadcast happened",Toast.LENGTH_LONG).show();
        Log.d("stopwatch-broadcast","started");
        context.startService(new Intent(context,StopwatchService.class));
    }


}
