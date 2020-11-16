package com.example.digitalclock.ui.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.digitalclock.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class AlarmFragment extends Fragment implements View.OnClickListener {

    ImageButton add_btn;
    EditText hour,minute;
    String h,m;
    LinearLayout edit;
    View item;
    TextView textView,repeat_text;
    AlarmItem alarmItem;
    SwitchMaterial toggle;
    CheckBox checkBox;
    LinearLayout days;
    TextView delete,timer;
    TextView sun,mon,tue,wed,thu,fri,sat;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    ArrayList<String> daysOfWeek= new ArrayList<>(Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"));
    ArrayList<String> daysOfAlarm= new ArrayList<>(Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"));


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_alarm, container, false);

        add_btn=root.findViewById(R.id.btn);
        hour=root.findViewById(R.id.hour);
        minute=root.findViewById(R.id.minute);
        edit =root.findViewById(R.id.edit);
        item=root.findViewById(R.id.item);
        toggle=item.findViewById(R.id.toggle);
        checkBox=item.findViewById(R.id.checkbox);
        days=item.findViewById(R.id.days);
        delete=item.findViewById(R.id.delete);
        timer=item.findViewById(R.id.timer);
        textView=item.findViewById(R.id.time);
        repeat_text=item.findViewById(R.id.repeat_text);
        sun=item.findViewById(R.id.sunday);
        mon=item.findViewById(R.id.monday);
        tue=item.findViewById(R.id.tuesday);
        wed=item.findViewById(R.id.wednesday);
        thu=item.findViewById(R.id.thursday);
        fri=item.findViewById(R.id.friday);
        sat=item.findViewById(R.id.saturday);

        sun.setOnClickListener(this);
        sun.setTag("Sunday");
        mon.setOnClickListener(this);
        mon.setTag("Monday");
        tue.setOnClickListener(this);
        tue.setTag("Tuesday");
        wed.setOnClickListener(this);
        wed.setTag("Wednesday");
        thu.setOnClickListener(this);
        thu.setTag("Thursday");
        fri.setOnClickListener(this);
        fri.setTag("Friday");
        sat.setOnClickListener(this);
        sat.setTag("Saturday");

        SharedPreferences preferences =getActivity().getSharedPreferences("alarm",Context.MODE_PRIVATE);
        if(preferences.getInt("count",0)==1){
            Gson gson = new Gson();
            String json = preferences.getString("alarmItem", "");
            alarmItem = gson.fromJson(json, AlarmItem.class);
            add_btn.setVisibility(View.INVISIBLE);
            edit.setVisibility(View.INVISIBLE);
            item.setVisibility(View.VISIBLE);
            loadAlarm();
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmItem.setActive(false);
                SharedPreferences preferences1 =getActivity().getSharedPreferences("alarm",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor= preferences1.edit();
                editor.putInt("count",0);
                Gson gson = new Gson();
                String json = gson.toJson(alarmItem);
                editor.putString("alarmItem",json);
                editor.apply();
                edit.setVisibility(View.VISIBLE);
                add_btn.setVisibility(View.VISIBLE);
                item.setVisibility(View.INVISIBLE);

                AlarmManager alarmManager= (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(getContext(), AlarmService.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getActivity().getApplicationContext(), 234324243, intent, 0);
                alarmManager.cancel(pendingIntent);

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(hour.getText().toString().isEmpty()&&minute.getText().toString().isEmpty())
                    Toast.makeText(getContext(),"Enter time!",Toast.LENGTH_LONG).show();
                else{
                    h=hour.getText().toString();
                    m=minute.getText().toString();
                    if(h.length()==1){
                        h="0"+h;
                    }
                    setAlarm(h,m);

                    edit.setVisibility(View.INVISIBLE);
                    add_btn.setVisibility(View.INVISIBLE);
                    item.setVisibility(View.VISIBLE);
                    textView.setText(h + ":" + m);
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    alarmItem.setActive(b);
                    saveAlarmDetails();
                    if(!b){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            deactivateAlarm();
                        }
                        //stop intent
                    }
                    else{
                        activateAlarm();
                    }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                alarmItem.setRepeating(b);
                if(b){
                    days.setVisibility(View.VISIBLE);
                    alarmItem.setDays(daysOfAlarm);
                    loadDaysOfAlarm();
                }
                else{
                    days.setVisibility(View.GONE);
                }
                saveAlarmDetails();
            }
        });


        return root;
    }

    public void saveAlarmDetails(){

        SharedPreferences preferences = getActivity().getSharedPreferences("alarm", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarmItem);
        editor.putString("alarmItem",json);
        editor.putInt("count",1);
        editor.apply();

    }

    public void startAlarm(){
        long milli=0;
        try {
            Date currentTime = new Date();
            Date fixedTime = sdf.parse(alarmItem.getTime());
            currentTime = sdf1.parse(sdf1.format(currentTime));
            if(currentTime.getTime()>fixedTime.getTime()){
                milli= currentTime.getTime()-fixedTime.getTime() + 24*3600*1000;
            }
            else{
                milli=fixedTime.getTime()-currentTime.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Toast.makeText(getContext(),"Set!",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getContext(), AlarmService.class);
        downloadFile("https://www.fesliyanstudios.com/soundeffects-download.php?id=4434","monday", Environment.getStorageDirectory().toString()+"/DigitalClock");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity().getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + milli, pendingIntent);
        }
        Log.d("alarm","set");
        Log.d("milli", String.valueOf(milli));
    }

    public void setAlarm(String h, String m){
        alarmItem = new AlarmItem(h+":"+m,true,false);
        toggle.setChecked(true);
        alarmItem.setDays(daysOfWeek);
        saveAlarmDetails();
        startAlarm();
    }

    public void checkDaysOfAlarmArray(View view){
        if(daysOfAlarm.contains((String) view.getTag())){
            view.setBackgroundResource(R.drawable.light_circle);
        }
        else{
            view.setBackgroundResource(R.drawable.small_circle);
        }
    }

    static void downloadFile(String dwnload_file_path, String fileName,
                             String pathToSave) {
        int downloadedSize = 0;
        int totalSize = 0;

        try {
            URL url = new URL(dwnload_file_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            // connect
            urlConnection.connect();

            File myDir;
            myDir = new File(pathToSave);
            myDir.mkdirs();

            // create a new file, to save the downloaded file

            String mFileName = fileName;
            File file = new File(myDir, mFileName);

            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();

            // runOnUiThread(new Runnable() {
            // public void run() {
            // pb.setMax(totalSize);
            // }
            // });

            // create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                // update the progressbar //
                // runOnUiThread(new Runnable() {
                // public void run() {
                // pb.setProgress(downloadedSize);
                // float per = ((float)downloadedSize/totalSize) * 100;
                // cur_val.setText("Downloaded " + downloadedSize + "KB / " +
                // totalSize + "KB (" + (int)per + "%)" );
                // }
                // });
            }
            // close the output stream when complete //
            fileOutput.close();
            // runOnUiThread(new Runnable() {
            // public void run() {
            // // pb.dismiss(); // if you want close it..
            // }
            // });

        } catch (final MalformedURLException e) {
            // showError("Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            // showError("Error : IOException " + e);
            e.printStackTrace();
        } catch (final Exception e) {
            // showError("Error : Please check your internet connection " + e);
        }
    }

    public void loadDaysOfAlarm(){
        checkDaysOfAlarmArray(sun);
        checkDaysOfAlarmArray(mon);
        checkDaysOfAlarmArray(tue);
        checkDaysOfAlarmArray(wed);
        checkDaysOfAlarmArray(thu);
        checkDaysOfAlarmArray(fri);
        checkDaysOfAlarmArray(sat);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void loadAlarm(){
        textView.setText(alarmItem.getTime());
        daysOfAlarm=alarmItem.getDays();
        if(alarmItem.getActive())
            activateAlarm();
        else
            deactivateAlarm();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void deactivateAlarm(){
        toggle.setChecked(false);
        textView.setTextColor(Color.GRAY);
        repeat_text.setTextColor(Color.GRAY);
        checkBox.setChecked(alarmItem.getRepeating());
        days.setVisibility(View.GONE);
        checkBox.setClickable(false);
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void activateAlarm(){
        toggle.setChecked(true);
        textView.setTextColor(getResources().getColor(R.color.purple_200));
        repeat_text.setTextColor(Color.WHITE);
        if(alarmItem.getRepeating()) {
            checkBox.setChecked(true);
            loadDaysOfAlarm();
            days.setVisibility(View.VISIBLE);
        }
        else{
            checkBox.setChecked(false);
            days.setVisibility(View.GONE);
        }
        checkBox.setClickable(true);
        checkBox.setButtonTintList(getResources().getColorStateList(R.color.purple_200));
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        if(Objects.equals(view.getBackground().getConstantState(), getActivity().getResources().getDrawable(R.drawable.light_circle).getConstantState())){
            view.setBackgroundResource(R.drawable.small_circle);
            daysOfAlarm.remove((String) view.getTag());
        }
        else{
            view.setBackgroundResource(R.drawable.light_circle);
            daysOfAlarm.add((String) view.getTag());
        }
        alarmItem.setDays(daysOfAlarm);
        if(daysOfAlarm.isEmpty()){
            checkBox.setChecked(false);
            days.setVisibility(View.INVISIBLE);
            alarmItem.setRepeating(false);
        }
        saveAlarmDetails();
    }
}