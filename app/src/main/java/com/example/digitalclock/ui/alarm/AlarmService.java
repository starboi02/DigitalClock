package com.example.digitalclock.ui.alarm;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.digitalclock.MainActivity;
import com.example.digitalclock.R;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmService extends BroadcastReceiver {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    public SharedPreferences.Editor editor;
    public AlarmItem alarmItem;
    public Context context;
    public String today = null;
    public ArrayList<String> urlList;
    public boolean isVibrating=false;
    ArrayList<String> daysOfWeek= new ArrayList<>(Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"));

    public void makeNotification(){
        String x= alarmItem.getTime();
        x=x.replace(':','1');
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("path-name",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1)+x+".mp3");
        }
        File file =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1)+ x+".mp3");
        if(file.exists()) {
            Uri uri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) + x + ".mp3"));
            mediaPlayer = new MediaPlayer();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            try {
                mediaPlayer.setDataSource(context, uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }
        else {
        mediaPlayer = MediaPlayer.create(context,R.raw.timersound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        }


        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 10, 100, 1000, 10000 };

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
                        .setAutoCancel(false)
                        .setOngoing(true)
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


        if(isVibrating)
        vibrator.vibrate(pattern,0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context=context;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isVibrating=sharedPreferences.getBoolean("vibrate_alarm",false);

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter("alarm-action"));

        Toast.makeText(context,"Received",Toast.LENGTH_LONG).show();

        SharedPreferences preferences =context.getSharedPreferences("alarm",Context.MODE_PRIVATE);
        editor=preferences.edit();
        if(preferences.getInt("count",0)==1){
            Gson gson = new Gson();
            String json = preferences.getString("alarmItem", "");
            alarmItem = gson.fromJson(json, AlarmItem.class);
            urlList=alarmItem.getSongURL();
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
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
            setRecurrent();
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
                if(isVibrating)
                    vibrator.cancel();
            }
            else{
                mediaPlayer.stop();
                if(isVibrating)
                    vibrator.cancel();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String time=sharedPreferences.getString("snooze_time","10");
                int timeF= Integer.parseInt(time);
                Intent intent1 = new Intent(context, AlarmService.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context.getApplicationContext(), 234324243, intent1, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                            + (timeF*60*1000), pendingIntent);
                }
            }
        }
    };

    public void download(){
        String url = urlList.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)%7);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Alarm sound for " + daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)%7));
        request.setTitle(daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)%7) + " Sound");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String x= alarmItem.getTime();
        x=x.replace(':','1');
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)%7)+x+".mp3");

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public void setRecurrent(){
        download();
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
