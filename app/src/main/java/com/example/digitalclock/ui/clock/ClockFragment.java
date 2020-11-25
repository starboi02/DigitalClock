package com.example.digitalclock.ui.clock;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.digitalclock.R;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockFragment extends Fragment {

    public SimpleDateFormat time;
    public SimpleDateFormat date;
    public TextView analog_time,analog_date;
    public SharedPreferences sharedPreferences;
    public ConstraintLayout rootLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_clock, container, false);
        analog_time=root.findViewById(R.id.analog_time);
        analog_date=root.findViewById(R.id.analog_date);
        rootLayout=root.findViewById(R.id.rootLayout);

        CustomAnalogClock customAnalogClock = (CustomAnalogClock) root.findViewById(R.id.analog_clock);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        customAnalogClock.setTimezone(TimeZone.getTimeZone(sharedPreferences.getString("time_zone","GMT+5:30")));

        String background_color_hex=sharedPreferences.getString("background_color","121212");
        rootLayout.setBackgroundColor(Color.parseColor("#"+background_color_hex));

        String text_color_hex=sharedPreferences.getString("text_color","606060");
        analog_time.setTextColor(Color.parseColor("#"+text_color_hex));

        String text_size=sharedPreferences.getString("text_size","Medium");

        if(sharedPreferences.getString("clock_type","Digital").equals("Digital")){
            customAnalogClock.setVisibility(View.GONE);
            analog_time.setVisibility(View.VISIBLE);
            if(sharedPreferences.getString("font","M").equals("M")){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Typeface montserrat = getResources().getFont(R.font.montserrat);
                    analog_time.setTypeface(montserrat);
                }
            }
            else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Typeface digital = getResources().getFont(R.font.digital);
                    analog_time.setTypeface(digital);
                }
            }

            if(sharedPreferences.getString("hour_format","24").equals("24")){
                time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                time.setTimeZone(TimeZone.getTimeZone(sharedPreferences.getString("time_zone","GMT+5:30")));
                analog_time.setTextSize(80);
            }
            else{
                analog_time.setTextSize(60);
                time = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                time.setTimeZone(TimeZone.getTimeZone(sharedPreferences.getString("time_zone","GMT+5:30")));
            }
        }
        else{
            time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            time.setTimeZone(TimeZone.getTimeZone(sharedPreferences.getString("time_zone","GMT+5:30")));
            analog_time.setVisibility(View.GONE);
            customAnalogClock.setVisibility(View.VISIBLE);
        }

        String dateFormat = sharedPreferences.getString("date_format","EE, dd MMM yyyy");
        date = new SimpleDateFormat(dateFormat,Locale.getDefault());
        date.setTimeZone(TimeZone.getTimeZone(sharedPreferences.getString("time_zone","GMT+5:30")));

        if(text_size.equals("Medium")){
            analog_date.setTextSize(20);
            if(sharedPreferences.getString("hour_format","24").equals("24")){
                analog_time.setTextSize(80);
            }
            else{
                analog_time.setTextSize(60);
            }
        }
        else if(text_size.equals("Small")){
            analog_date.setTextSize(15);
            analog_time.setTextSize(40);
        }
        else{
            analog_date.setTextSize(30);
            if(sharedPreferences.getString("hour_format","24").equals("24")){
                analog_time.setTextSize(100);
            }
            else{
                analog_time.setTextSize(80);
            }
        }


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