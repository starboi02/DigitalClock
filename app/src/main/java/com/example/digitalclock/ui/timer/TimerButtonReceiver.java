package com.example.digitalclock.ui.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TimerButtonReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        switch (intent.getExtras().getString("action")) {
            case "stop":
                sendMessage("stop");
                break;
            case "pause":
                sendMessage("pause");
                break;
            case "resume":
                sendMessage("resume");
                break;
        }
    }

    public void sendMessage(String msg){
        Log.d("sender", "Broadcasting message of timer action " + msg);
        Intent intent = new Intent("timer-action");
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
