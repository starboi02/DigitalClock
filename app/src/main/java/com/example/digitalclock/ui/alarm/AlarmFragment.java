package com.example.digitalclock.ui.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import com.example.digitalclock.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(hour.getText().toString().isEmpty()&&minute.getText().toString().isEmpty())
                    Toast.makeText(getContext(),"Enter time!",Toast.LENGTH_LONG).show();
                else{
                    h=hour.getText().toString();
                    m=minute.getText().toString();

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

    public void setAlarm(String h, String m){
        alarmItem = new AlarmItem(h+":"+m,true,false);
        toggle.setChecked(true);
        alarmItem.setDays(daysOfWeek);
        saveAlarmDetails();
    }

    public void checkDaysOfAlarmArray(View view){
        if(daysOfAlarm.contains((String) view.getTag())){
            view.setBackgroundResource(R.drawable.light_circle);
        }
        else{
            view.setBackgroundResource(R.drawable.small_circle);
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