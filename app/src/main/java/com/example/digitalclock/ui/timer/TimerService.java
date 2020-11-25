package com.example.digitalclock.ui.timer;

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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.alarm.ButtonReceiver;
import com.example.digitalclock.ui.stopwatch.StopwatchBroadcastReceiver;
import com.example.digitalclock.ui.stopwatch.StopwatchButtonReceiver;
import com.example.digitalclock.ui.stopwatch.StopwatchService;

import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    public MediaPlayer mediaPlayer;
    public Vibrator vibrator;
    public boolean isVibrating=false,wasRunning=false;
    long[] pattern = { 0, 10, 100, 1000, 10000 };
    String hms;
    long updatedTime;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("timer-service-started", String.valueOf(System.nanoTime()/1000000000));

        mediaPlayer= MediaPlayer.create(this, R.raw.timersound);
        vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        sharedPreferences= getSharedPreferences("timer_running",Context.MODE_PRIVATE);

        SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(this);
        isVibrating=sharedPreferences1.getBoolean("vibrate_timer",false);

        updatedTime = sharedPreferences.getLong("timerTime",0);

        //Log.d("updatedTime",String.valueOf(updatedTime));

        wasRunning=sharedPreferences.getBoolean("wasRunning",false);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("timer-action"));

        startHandler();

        return START_STICKY;
    }

    public void startHandler(){
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            public void run() {

                Log.d("timer-handler","running");

                if (wasRunning&& updatedTime>0) {

                    hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(updatedTime*1000),
                            TimeUnit.MILLISECONDS.toMinutes(updatedTime*1000) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(updatedTime*1000) % TimeUnit.MINUTES.toSeconds(1));

                    Intent dismissIntent = new Intent(TimerService.this, TimerButtonReceiver.class);
                    dismissIntent.putExtra("action", "pause");

                    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, dismissIntent, 0);

                    NotificationCompat.Builder notificationBuilder;
                    notificationBuilder =
                            new NotificationCompat.Builder(getApplicationContext(), "timer")
                                    .setSmallIcon(R.drawable.ic_clock)
                                    .setContentTitle("timer")
                                    .setContentText(hms)
                                    .setOngoing(true)
                                    .setAutoCancel(false)
                                    .addAction(10, "Pause", dismissPendingIntent)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_ALARM);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("timer",
                                "Channel human readable title timer",
                                NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(11111111, notificationBuilder.build());

                    updatedTime--;

                    sharedPreferences.edit().putLong("timerTime",updatedTime).apply();

                    handler.postDelayed(this, delay);
                }
                else if(updatedTime==0){
                    Intent dismissIntent = new Intent(TimerService.this, TimerButtonReceiver.class);
                    dismissIntent.putExtra("action", "stop");

                    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 30000, dismissIntent, 0);

                    NotificationCompat.Builder notificationBuilder;
                    notificationBuilder =
                            new NotificationCompat.Builder(getApplicationContext(), "timer")
                                    .setSmallIcon(R.drawable.ic_clock)
                                    .setContentTitle("timer")
                                    .setContentText("00:00:00")
                                    .setOngoing(true)
                                    .setAutoCancel(false)
                                    .addAction(10, "Stop", dismissPendingIntent)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_ALARM);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("timer",
                                "Channel human readable title timer",
                                NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(11111111, notificationBuilder.build());

                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    vibrator.vibrate(pattern,0);

                    wasRunning=false;
                    sharedPreferences.edit().putBoolean("wasRunning",false).apply();

                    handler.removeCallbacksAndMessages(null);

                }

            }
        }, delay);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getString("message").equals("stop")){

                mediaPlayer.stop();
                vibrator.cancel();
                mediaPlayer.reset();
                mediaPlayer.release();

                sharedPreferences.edit().putBoolean("wasRunning",false).apply();

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(11111111);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
                NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
                mb.setContentIntent(resultPendingIntent);

                LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);

                stopSelf();

            }
            else if(intent.getExtras().getString("message").equals("pause")){
                wasRunning=false;
                sharedPreferences.edit().putBoolean("wasRunning",false).apply();

                Intent dismissIntent = new Intent(TimerService.this, TimerButtonReceiver.class);
                dismissIntent.putExtra("action", "resume");

                PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 300000, dismissIntent, 0);

                NotificationCompat.Builder notificationBuilder;
                notificationBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), "timer")
                                .setSmallIcon(R.drawable.ic_clock)
                                .setContentTitle("timer")
                                .setContentText(hms)
                                .setOngoing(false)
                                .setAutoCancel(false)
                                .addAction(10, "Resume", dismissPendingIntent)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_ALARM);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("timer",
                            "Channel human readable title timer",
                            NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(11111111, notificationBuilder.build());

            }
            else if(intent.getExtras().getString("message").equals("resume")){
                wasRunning=true;
                sharedPreferences.edit().putBoolean("wasRunning",true).apply();
                startHandler();
            }
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        SharedPreferences.Editor edit=sharedPreferences.edit();
        edit.putBoolean("wasRunning",wasRunning);
        edit.putLong("timerTime",updatedTime);
        edit.apply();

        Intent intent = new Intent(this, TimerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 2343, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }

    }

}
