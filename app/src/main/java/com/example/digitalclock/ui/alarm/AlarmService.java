package com.example.digitalclock.ui.alarm;

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
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.digitalclock.MainActivity;
import com.example.digitalclock.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmService extends BroadcastReceiver {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    public SharedPreferences.Editor editor;
    public AlarmItem alarmItem;
    public Context context;


    public void makeNotification(){
        mediaPlayer = MediaPlayer.create(context, R.raw.timersound);
        mediaPlayer.setLooping(true);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Intent snoozeIntent = new Intent(context,ButtonReceiver.class);
        snoozeIntent.putExtra("action","snooze");

        Intent dismissIntent = new Intent(context,ButtonReceiver.class);
        dismissIntent.putExtra("action","dismiss");


        PendingIntent snoozePendingIntent= PendingIntent.getBroadcast(context,200,snoozeIntent,0);
        PendingIntent dismissPendingIntent= PendingIntent.getBroadcast(context,300,dismissIntent,0);

        NotificationCompat.Builder notificationBuilder;
        notificationBuilder =
                new NotificationCompat.Builder(context, "alarm")
                        .setSmallIcon(R.drawable.ic_clock)
                        .setContentTitle("Alarm")
                        .setContentText(alarmItem.getTime())
                        .setAutoCancel(true)
                        .addAction(2,"Snooze",snoozePendingIntent)
                        .addAction(3,"Dismiss",dismissPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("alarm",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notificationBuilder.build());


        mediaPlayer.start();

        long[] pattern = { 0, 100, 1000 };
        vibrator.vibrate(pattern, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context=context;

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter("alarm-action"));

        Toast.makeText(context,"Received",Toast.LENGTH_LONG).show();

        SharedPreferences preferences =context.getSharedPreferences("alarm",Context.MODE_PRIVATE);
        editor=preferences.edit();
        if(preferences.getInt("count",0)==1){
            setRecurrent();
            Gson gson = new Gson();
            String json = preferences.getString("alarmItem", "");
            alarmItem = gson.fromJson(json, AlarmItem.class);
            if(alarmItem.getActive() && !alarmItem.getRepeating()){

                alarmItem.setActive(false);
                Gson G = new Gson();
                String j = G.toJson(alarmItem);
                editor.putString("alarmItem",j);
                editor.putInt("count",0);
                editor.apply();


                makeNotification();


            }
            else if(alarmItem.getActive()){
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                String today = null;
                if (day == 2) {
                    today = "Monday";
                } else if (day == 3) {
                    today = "Tuesday";
                } else if (day == 4) {
                    today = "Wednesday";
                } else if (day == 5) {
                    today = "Thursday";
                } else if (day == 6) {
                    today = "Friday";
                } else if (day == 7) {
                    today = "Saturday";
                } else if (day == 1) {
                    today = "Sunday";
                }
                ArrayList<String> daysOfAlarm = alarmItem.getDays();
                if(daysOfAlarm.contains((String)today)){

                    makeNotification();


                }

            }
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getString("message").equals("dismiss")){
                mediaPlayer.stop();
                vibrator.cancel();
            }
            else{
                Intent intent1 = new Intent(context, AlarmService.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context.getApplicationContext(), 234324243, intent1, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                            + (10*60*1000), pendingIntent);
                }
            }
        }
    };

    public void setRecurrent(){
        Intent intent1 = new Intent(context, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 234324243, intent1, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (24*3600*1000), pendingIntent);
        }
    }

}
