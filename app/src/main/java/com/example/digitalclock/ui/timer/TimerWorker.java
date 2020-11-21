package com.example.digitalclock.ui.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.stopwatch.StopwatchButtonReceiver;
import com.example.digitalclock.ui.stopwatch.StopwatchService;

import java.util.concurrent.TimeUnit;


public class TimerWorker extends Worker {

    Context context;
    long timerTime;

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context=context;
    }

    @NonNull
    @Override
    public Result doWork() {

        timerTime=getInputData().getLong("timerTime",0);

        final Handler handler = new Handler();
        final int delay = 1000; // 1000 milliseconds == 1 second
        
        handler.postDelayed(new Runnable() {
            public void run() {
//                long updatedTime= (System.nanoTime()  - startTime) / 1000000 + prevTime;
//                hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(updatedTime),
//                        TimeUnit.MILLISECONDS.toMinutes(updatedTime) % TimeUnit.HOURS.toMinutes(1),
//                        TimeUnit.MILLISECONDS.toSeconds(updatedTime) % TimeUnit.MINUTES.toSeconds(1));

                Intent dismissIntent = new Intent(context, StopwatchButtonReceiver.class);
                dismissIntent.putExtra("action","stop");

                PendingIntent dismissPendingIntent= PendingIntent.getBroadcast(getApplicationContext(),300,dismissIntent,0);

                NotificationCompat.Builder notificationBuilder;
                notificationBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), "timer")
                                .setSmallIcon(R.drawable.ic_clock)
                                .setContentTitle("Timer")
                                .setContentText(String.valueOf(timerTime))
                                .setOngoing(true)
                                .setAutoCancel(false)
                                .addAction(3,"Pause",dismissPendingIntent)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_ALARM);

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("timer",
                            "Channel human readable title timer",
                            NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(1111, notificationBuilder.build());

                timerTime--;

                handler.postDelayed(this, delay);
            }
        }, delay);

        return Result.success();

    }
}
