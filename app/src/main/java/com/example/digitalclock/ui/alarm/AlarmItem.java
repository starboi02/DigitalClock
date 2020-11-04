package com.example.digitalclock.ui.alarm;

import java.io.Serializable;
import java.util.ArrayList;

public class AlarmItem implements Serializable {

    String time;
    boolean active;
    boolean repeat;
    ArrayList<String> days;
    ArrayList<String> songURL;

    public AlarmItem(String time, boolean active, boolean repeat){
        this.time=time;
        this.active=active;
        this.repeat=repeat;
    }

    public void setDays(ArrayList<String> days){
        this.days=days;
    }

    public ArrayList<String> getDays(){
        return days;
    }

    public void setSongURL(ArrayList<String> songURL){
        this.songURL=songURL;
    }

    public ArrayList<String> getSongURL(){
        return songURL;
    }

    public void setRepeating(boolean repeat){
        this.repeat=repeat;
    }

    public void setActive(boolean active){
        this.active=active;
    }

    public String getTime(){
        return time;
    }

    public boolean getActive(){
        return active;
    }

    public boolean getRepeating(){
        return repeat;
    }

}
