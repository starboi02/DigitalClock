package com.example.digitalclock.ui.alarm;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class AlarmFragment extends Fragment implements View.OnClickListener {

    ImageButton add_btn;
    EditText hour,minute;
    String h,m,today;
    LinearLayout edit;
    View item;
    TextView textView,repeat_text;
    AlarmItem alarmItem;
    SwitchMaterial toggle;
    CheckBox checkBox;
    LinearLayout days;
    TextView delete;
    TextView sun,mon,tue,wed,thu,fri,sat;
    ConstraintLayout rootLayout;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    ArrayList<String> daysOfWeek= new ArrayList<>(Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"));
    ArrayList<String> daysOfAlarm= new ArrayList<>(Arrays.asList("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"));
    ArrayList<String> urlList= new ArrayList<>(Arrays.asList("https://www.fesliyanstudios.com/soundeffects-download.php?id=4433","https://www.fesliyanstudios.com/soundeffects-download.php?id=4434","https://www.fesliyanstudios.com/soundeffects-download.php?id=5340","https://www.fesliyanstudios.com/soundeffects-download.php?id=4436","https://www.fesliyanstudios.com/soundeffects-download.php?id=4440","https://www.fesliyanstudios.com/soundeffects-download.php?id=4450","https://www.fesliyanstudios.com/soundeffects-download.php?id=4460"));


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_alarm, container, false);

        add_btn=root.findViewById(R.id.btn);
        hour=root.findViewById(R.id.hour);
        minute=root.findViewById(R.id.minute);
        edit =root.findViewById(R.id.edit);
        item=root.findViewById(R.id.item);
        rootLayout=root.findViewById(R.id.rootLayout);
        toggle=item.findViewById(R.id.toggle);
        checkBox=item.findViewById(R.id.checkbox);
        days=item.findViewById(R.id.days);
        delete=item.findViewById(R.id.delete);
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

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

        String background_color_hex=sharedPreferences.getString("background_color","121212");
        rootLayout.setBackgroundColor(Color.parseColor("#"+background_color_hex));

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
                checkBox.setChecked(false);
                days.setVisibility(View.GONE);
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
                milli=fixedTime.getTime() + 24*3600*1000-currentTime.getTime();
            }
            else{
                milli=fixedTime.getTime()-currentTime.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Toast.makeText(getContext(),"Set!",Toast.LENGTH_LONG).show();
        download();
        Intent intent = new Intent(getContext(), AlarmService.class);
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
        alarmItem.setSongURL(urlList);
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

    public void download(){

        String url = urlList.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Alarm sound for " + daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1));
        request.setTitle(daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1) + " Sound");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String x= alarmItem.getTime();
        x=x.replace(':','1');
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, daysOfWeek.get(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1)+x+".mp3");

        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

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