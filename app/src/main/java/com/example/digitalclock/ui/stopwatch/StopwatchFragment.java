package com.example.digitalclock.ui.stopwatch;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalclock.R;
import com.example.digitalclock.ui.alarm.AlarmViewModel;
import com.example.digitalclock.ui.clock.ClockFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StopwatchFragment extends Fragment {

    private StopwatchViewModel mViewModel;

    public static StopwatchFragment newInstance() {
        return new StopwatchFragment();
    }

    private StopwatchViewModel stopwatchViewModel;
    public TextView textView;
    public ImageButton imageButton;
    public ImageView restart;
    public String displayTime="00:00:00 00";
    public long startTime,prevTime=0,elapsedMiliseconds;
    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        stopwatchViewModel =
                new ViewModelProvider(this).get(StopwatchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        textView=root.findViewById(R.id.time);
        restart=root.findViewById(R.id.restart);
        imageButton=root.findViewById(R.id.btn);
        imageButton.setTag(R.drawable.ic_play);


        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("00:00:00 00");
                running=false;
                prevTime=0;
            }
        });

        Thread myThread = null;

        Runnable runnable = new TimeShow();
        myThread= new Thread(runnable);
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
        running = false;
        Log.d("stopwatch","paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("stopwatch","resumed");
    }

    class TimeShow implements Runnable{
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    if(running) {
                        elapsedMiliseconds = (System.nanoTime()  - startTime) / 1000000 + prevTime;
                        updateTime(elapsedMiliseconds);
                        Thread.sleep(10);
                        Log.d("Stopwatch_thread","running");
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