package com.example.digitalclock.ui.timer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.stopwatch.StopwatchFragment;

import java.util.concurrent.TimeUnit;

public class TimerFragment extends Fragment {

    public EditText hour,minute,second;
    public ImageButton btn;
    public TextView delete,reset,time;
    public long timerTime=0;
    public LinearLayout linearLayout;
    public boolean running =false,soundPlaying=false;
    public long startTime,currentTime,pausedTime,tempTime;
    Thread myThread = null;
    public String displayTime= "00:00:00";
    public MediaPlayer mp;
    public Vibrator vibrator;
    public boolean isVibrating=false;
    long[] pattern = { 0, 10, 100, 1000, 10000 };

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

        btn.setTag(R.drawable.ic_play);

        mp= MediaPlayer.create(getContext(),R.raw.timersound);
        vibrator=(Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isVibrating=sharedPreferences.getBoolean("vibrate_timer",false);


        if(this.getArguments().getBoolean("sound-playing")){
            btn.setTag(R.drawable.ic_pause);
            btn.setImageResource(R.drawable.ic_stop);
            linearLayout.setVisibility(View.INVISIBLE);
            time.setVisibility(View.VISIBLE);
            soundPlaying=true;
        }
        else if(this.getArguments().getLong("timer-time",0)!=0){
            timerTime=this.getArguments().getLong("timer-time");
            Log.d("timer-value-received-t", String.valueOf(timerTime));
            running=true;
            linearLayout.setVisibility(View.INVISIBLE);
            time.setVisibility(View.VISIBLE);
            btn.setTag(R.drawable.ic_pause);
            btn.setImageResource(R.drawable.ic_pause);
            delete.setVisibility(View.VISIBLE);
        }

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
                        timerTime++;
                        startTime=timerTime;
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

                    if(soundPlaying) {
                        sendStopMessage();
                        delete();
                    }

                    if(mp.isPlaying()) {
                        mp.stop();
                        if(isVibrating)
                            vibrator.cancel();
                        //mp.release();
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
                updateTime(startTime);
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
                            delete.setVisibility(View.INVISIBLE);
                            reset.setVisibility(View.INVISIBLE);
                            running=false;
                        }
                        Log.d("timer","running");
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

        if(running) {
            //sendMessage();
            Data data = new Data.Builder()
                    .putLong("timerTime", timerTime)
                    .build();
            final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TimerWorker.class).setInputData(data).build();
            WorkManager.getInstance().enqueue(workRequest);
        }
        if(mp.isPlaying()) {
            mp.stop();
            vibrator.cancel();
            //mp.release();
        }

        running=false;


        Log.d("stopwatch","paused");
    }


    private void updateTime(long updatedTime) {
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

    private void sendMessage() {
        Log.d("sender", "Broadcasting message time");
        Intent intent = new Intent("timer");
        intent.putExtra("timer-time",timerTime);
        Log.d("timer-time-sent", String.valueOf(timerTime));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendStopMessage(){
        Log.d("sender", "Broadcasting message stop");
        Intent intent = new Intent("timer");
        intent.putExtra("stop-sound",true);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

}