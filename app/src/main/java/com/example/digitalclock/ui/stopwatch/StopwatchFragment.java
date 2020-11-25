package com.example.digitalclock.ui.stopwatch;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalclock.R;

import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class StopwatchFragment extends Fragment {

    private StopwatchViewModel mViewModel;

    public static StopwatchFragment newInstance() {
        return new StopwatchFragment();
    }

    private StopwatchViewModel stopwatchViewModel;
    public TextView textView;
    public ImageButton imageButton;
    ConstraintLayout rootLayout;
    public ImageView restart;
    public String displayTime="00:00:00 00";
    public long startTime,prevTime=0,elapsedMiliseconds;
    private int seconds = 0;
    private boolean running=false;
    private boolean wasRunning=false;
    Thread myThread = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("stopwatch","created");
        stopwatchViewModel =
                new ViewModelProvider(this).get(StopwatchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        textView=root.findViewById(R.id.time);
        restart=root.findViewById(R.id.restart);
        imageButton=root.findViewById(R.id.btn);
        imageButton.setTag(R.drawable.ic_play);
        rootLayout=root.findViewById(R.id.rootLayout);

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

        String background_color_hex=sharedPreferences.getString("background_color","121212");
        rootLayout.setBackgroundColor(Color.parseColor("#"+background_color_hex));

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("00:00:00 00");
                running=false;
                prevTime=0;
            }
        });

        SharedPreferences preferences =getActivity().getSharedPreferences("stopwatch_running",Context.MODE_PRIVATE);
        wasRunning = preferences.getBoolean("wasRunning",false);

        Log.d("thread_running",String.valueOf(running));

        if(wasRunning) {
            running=true;
            imageButton.setImageResource(R.drawable.ic_pause);
            imageButton.setTag(R.drawable.ic_pause);
            startTime=preferences.getLong("startTime",0 );
            prevTime=preferences.getLong("prevTime",0);
            restart.setVisibility(View.INVISIBLE);
        }

        Runnable runnable = new TimeShow();
        myThread = new Thread(runnable);
        myThread.start();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageButton.getTag().equals(R.drawable.ic_play)){
                    imageButton.setImageResource(R.drawable.ic_pause);
                    imageButton.setTag(R.drawable.ic_pause);
                    startTime=System.nanoTime();
                    running=true;
                    restart.setVisibility(View.INVISIBLE);
                }
                else{
                    imageButton.setImageResource(R.drawable.ic_play);
                    imageButton.setTag(R.drawable.ic_play);
                    running=false;
                    prevTime=elapsedMiliseconds;
                    restart.setVisibility(View.VISIBLE);
                }
            }
        });

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getContext().getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE).edit();
        editor.putBoolean("wasRunning",running);
        editor.putLong("startTime",startTime);
        editor.putLong("prevTime",prevTime);
        editor.apply();

        Intent intent = new Intent(getContext(), StopwatchBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 234324, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }

        Log.d("stopwatch","paused");
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences= getContext().getSharedPreferences("stopwatch_running",Context.MODE_PRIVATE);
        if(preferences.getBoolean("wasRunning",false)){
            Log.d("sender", "Broadcasting message of Alarm action");
            Intent intent = new Intent("stopwatch-action");
            intent.putExtra("message", "stop");
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

            NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(111);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(), 0);
            NotificationCompat.Builder mb = new NotificationCompat.Builder(getContext());
            mb.setContentIntent(resultPendingIntent);

            NotificationManager manager1 = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager1.cancel(112);

            PendingIntent resultPendingIntent1 = PendingIntent.getActivity(getContext(), 0, new Intent(), 0);
            NotificationCompat.Builder mb1 = new NotificationCompat.Builder(getContext());
            mb.setContentIntent(resultPendingIntent1);

        }



        else{
            running=false;
            imageButton.setImageResource(R.drawable.ic_play);
            imageButton.setTag(R.drawable.ic_play);
            prevTime=0;
            startTime=0;
            textView.setText("00:00:00 00");
            restart.setVisibility(View.VISIBLE);
        }


        Log.d("stopwatch","resumed");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("stopwatch","destroyed");
    }
    class TimeShow implements Runnable{
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    if(running) {
                        elapsedMiliseconds = (System.nanoTime()  - startTime) / 1000000 + prevTime;
                        updateTime(elapsedMiliseconds);
                        Thread.sleep(10);
                        //Log.d("Stopwatch_thread","running");
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    private void updateTime(long updatedTime) {
        long milli = (updatedTime%1000) / 10;
        String mili = String.format("%02d",milli);
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(updatedTime),
                TimeUnit.MILLISECONDS.toMinutes(updatedTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(updatedTime) % TimeUnit.MINUTES.toSeconds(1));
        displayTime=hms + " " + mili;
        textView.setText(displayTime);
    }

}