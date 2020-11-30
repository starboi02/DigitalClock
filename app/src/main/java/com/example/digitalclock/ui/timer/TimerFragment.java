package com.example.digitalclock.ui.timer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.digitalclock.MainActivity;
import com.example.digitalclock.R;
import com.example.digitalclock.ui.stopwatch.StopwatchBroadcastReceiver;
import com.example.digitalclock.ui.stopwatch.StopwatchFragment;

import java.security.Timestamp;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class TimerFragment extends Fragment {

    public EditText hour,minute,second;
    public ImageButton btn;
    public TextView delete,reset,time;
    public long timerTime=0;
    public LinearLayout linearLayout;
    public boolean running =false;
    public long startTime;
    Thread myThread = null;
    public String displayTime= "00:00:00";
    public MediaPlayer mp;
    public Vibrator vibrator;
    public boolean isVibrating=false;
    ConstraintLayout rootLayout;
    long[] pattern = { 0, 10, 100, 1000, 10000 };
    SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_timer, container, false);
        hour=root.findViewById(R.id.hour);
        minute=root.findViewById(R.id.minute);
        second=root.findViewById(R.id.second);
        btn=root.findViewById(R.id.btn);
        delete=root.findViewById(R.id.delete);
        reset=root.findViewById(R.id.reset);
        time=root.findViewById(R.id.time);
        linearLayout=root.findViewById(R.id.edit);
        rootLayout=root.findViewById(R.id.rootLayout);

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

        String background_color_hex=sharedPreferences.getString("background_color","121212");
        rootLayout.setBackgroundColor(Color.parseColor("#"+background_color_hex));



        btn.setTag(R.drawable.ic_play);

        mp= MediaPlayer.create(getContext(),R.raw.timersound);
        vibrator=(Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getContext());
        isVibrating=sharedPreferences1.getBoolean("vibrate_timer",false);

        sharedPreferences = getContext().getSharedPreferences("timer_running",Context.MODE_PRIVATE);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn.getTag().equals(R.drawable.ic_play)&&hour.getText().toString().isEmpty() && minute.getText().toString().isEmpty() && second.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"Enter Time!",Toast.LENGTH_SHORT).show();
                }
                else if (btn.getTag().equals(R.drawable.ic_play) && (!hour.getText().toString().isEmpty() || !minute.getText().toString().isEmpty() || !second.getText().toString().isEmpty())){
                    String h,m,s;
                    if(hour.getText().toString().isEmpty())
                        h="0";
                    else
                        h= hour.getText().toString();
                    if(minute.getText().toString().isEmpty())
                        m="0";
                    else
                        m=minute.getText().toString();
                    if(second.getText().toString().isEmpty())
                        s="0";
                    else
                        s=second.getText().toString();

                    if(timerTime==0) {
                        timerTime = Integer.parseInt(s) + Integer.parseInt(m) * 60 + Integer.parseInt(h) * 60 * 60;
                        startTime=timerTime;
                        Log.d("startTime", String.valueOf(startTime));
                    }
                    linearLayout.setVisibility(View.INVISIBLE);
                    time.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.VISIBLE);

                    btn.setImageResource(R.drawable.ic_pause);
                    btn.setTag(R.drawable.ic_pause);

                    running=true;

                }

                else{

                    btn.setImageResource(R.drawable.ic_play);
                    btn.setTag(R.drawable.ic_play);
                    running=false;
                    delete.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.VISIBLE);


                    if(mp.isPlaying()) {
                        mp.stop();
                        if(isVibrating)
                            vibrator.cancel();
                        delete();
                    }

                }

            }

        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerTime=0;
                btn.setImageResource(R.drawable.ic_play);
                btn.setTag(R.drawable.ic_play);
                running=false;
                //Log.d("startTime", String.valueOf(startTime));
                updateTime(startTime*1000);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

        Runnable runnable = new TimeShow();
        myThread = new Thread(runnable);
        myThread.start();

        return root;
    }

    public void changeUI(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setImageResource(R.drawable.ic_stop);
                btn.setTag(R.drawable.ic_stop);
            }
        });
    }



    class TimeShow implements Runnable{
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    if(running) {
                        timerTime=timerTime-1;
                        if(timerTime>=0) {
                            updateTime(timerTime * 1000);
                        }
                        else {
                            playSound();
                            changeUI();
                            delete.setVisibility(View.INVISIBLE);
                            reset.setVisibility(View.INVISIBLE);
                            running=false;
                        }
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    Log.d("timer-thread","interrupted-exception");
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                    Log.d("timer-thread","interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences = getContext().getSharedPreferences("timer_running",Context.MODE_PRIVATE);

        SharedPreferences.Editor edit=sharedPreferences.edit();
        edit.putBoolean("wasRunning",running);
        edit.putLong("timerTime",timerTime);
        edit.putLong("startTime",startTime);
        edit.apply();

        if(running) {



            Log.d("timerTime",String.valueOf(timerTime));

            Intent intent = new Intent(getContext(), TimerBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(), 2343, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.nanoTime()/1000000, pendingIntent);
            }
            Log.d("timer-fragment-paused", String.valueOf(System.nanoTime()/1000000000));
        }
        if(mp.isPlaying()) {
            mp.stop();
            vibrator.cancel();
        }

        running=false;


        Log.d("stopwatch","paused");
    }

    @Override
    public void onResume(){
        super.onResume();
        sharedPreferences = getContext().getSharedPreferences("timer_running",Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("wasRunning",false)){

            NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(11111111);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(), 0);
            NotificationCompat.Builder mb = new NotificationCompat.Builder(getContext());
            mb.setContentIntent(resultPendingIntent);

            linearLayout.setVisibility(View.INVISIBLE);
            time.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            reset.setVisibility(View.VISIBLE);

            btn.setImageResource(R.drawable.ic_pause);
            btn.setTag(R.drawable.ic_pause);


            timerTime=sharedPreferences.getLong("timerTime",0);
            startTime=sharedPreferences.getLong("startTime",0);
            running=true;
        }
        else{
            linearLayout.setVisibility(View.VISIBLE);
            time.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
            reset.setVisibility(View.INVISIBLE);
            timerTime=0;

            btn.setImageResource(R.drawable.ic_play);
            btn.setTag(R.drawable.ic_play);
        }
    }


    private void updateTime(long updatedTime) {

        //Log.d("startTime", String.valueOf(updatedTime));

        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(updatedTime),
                TimeUnit.MILLISECONDS.toMinutes(updatedTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(updatedTime) % TimeUnit.MINUTES.toSeconds(1));
        displayTime=hms;
        time.setText(displayTime);
    }

    public void playSound(){

        if(isVibrating){
            vibrator.vibrate(pattern,0);
        }

        mp.start();
        mp.setLooping(true);
    }


    public void delete(){
        reset.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        time.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        timerTime=0;
        second.getText().clear();
        hour.getText().clear();
        minute.getText().clear();
        btn.setImageResource(R.drawable.ic_play);
        btn.setTag(R.drawable.ic_play);
        running=false;
    }


}