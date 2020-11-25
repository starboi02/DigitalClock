package com.example.digitalclock.ui.stopwatch;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.alarm.AlarmService;
import com.example.digitalclock.ui.alarm.ButtonReceiver;

import java.util.concurrent.TimeUnit;

public class StopwatchService extends Service {

    long startTime=0;
    long prevTime=0;
    boolean wasRunning;
    String hms;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("stopwatch-service","started");
        SharedPreferences preferences =getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE);
        wasRunning = preferences.getBoolean("wasRunning",false);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("stopwatch-action"));

        if(wasRunning){
            startTime=preferences.getLong("startTime",0);
            prevTime=preferences.getLong("prevTime",0);
            final Handler handler = new Handler();
            final int delay = 1000; // 1000 milliseconds == 1 second

            handler.postDelayed(new Runnable() {
                public void run() {
                    if (wasRunning) {
                        long updatedTime = (System.nanoTime() - startTime) / 1000000 + prevTime;
                        hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(updatedTime),
                                TimeUnit.MILLISECONDS.toMinutes(updatedTime) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(updatedTime) % TimeUnit.MINUTES.toSeconds(1));

                        Intent dismissIntent = new Intent(StopwatchService.this, StopwatchButtonReceiver.class);
                        dismissIntent.putExtra("action", "stop");

                        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 300, dismissIntent, 0);

                        NotificationCompat.Builder notificationBuilder;
                        notificationBuilder =
                                new NotificationCompat.Builder(getApplicationContext(), "stopwatch")
                                        .setSmallIcon(R.drawable.ic_clock)
                                        .setContentTitle("Stopwatch")
                                        .setContentText(hms)
                                        .setOngoing(true)
                                        .setAutoCancel(false)
                                        .addAction(3, "Stop", dismissPendingIntent)
                                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_ALARM);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel("stopwatch",
                                    "Channel human readable title stopwatch",
                                    NotificationManager.IMPORTANCE_HIGH);
                            notificationManager.createNotificationChannel(channel);
                        }
                        notificationManager.notify(111, notificationBuilder.build());

                        handler.postDelayed(this, delay);
                    }
                }
            }, delay);
        }

        return START_STICKY;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getString("message").equals("stop")){
                wasRunning=false;
                SharedPreferences preferences =getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("wasRunning",false);
                editor.putLong("startTime",startTime);
                editor.putLong("prevTime",prevTime);
                editor.apply();

                NotificationCompat.Builder notificationBuilder;
                notificationBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), "stopwatch")
                                .setSmallIcon(R.drawable.ic_clock)
                                .setContentTitle("Stopwatch")
                                .setContentText(hms)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_ALARM);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("stopwatch",
                            "Channel human readable title stopwatch",
                            NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(112, notificationBuilder.build());

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(111);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
                NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
                mb.setContentIntent(resultPendingIntent);

                LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);

                stopSelf();
            }
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        SharedPreferences.Editor editor = getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE).edit();
        editor.putBoolean("wasRunning",wasRunning);
        editor.putLong("startTime",startTime);
        editor.putLong("prevTime",prevTime);
        editor.apply();

        Intent intent = new Intent(this, StopwatchBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 234324, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }

}
