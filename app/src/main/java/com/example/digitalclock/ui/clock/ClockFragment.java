package com.example.digitalclock.ui.clock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.digitalclock.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClockFragment extends Fragment {

    private ClockViewModel clockViewModel;
    public SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    public SimpleDateFormat date = new SimpleDateFormat("EE, dd MMM yyyy",Locale.getDefault());
    TextView analog_time,analog_date;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        clockViewModel =
                new ViewModelProvider(this).get(ClockViewModel.class);
        View root = inflater.inflate(R.layout.fragment_clock, container, false);
        analog_time=root.findViewById(R.id.analog_time);
        analog_date=root.findViewById(R.id.analog_date);
        //final TextView textView = root.findViewById(R.id.text_home);
        clockViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        Thread myThread = null;

        Runnable runnable = new TimeShow();
        myThread= new Thread(runnable);
        myThread.start();

        return root;
    }
    public void doWork() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try{
                    Calendar c = Calendar.getInstance();
                    analog_time.setText(time.format(c.getTime()));
                    analog_date.setText(date.format(c.getTime()));

                }catch (Exception e) {}
            }
        });
    }
    class TimeShow implements Runnable{
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }
}