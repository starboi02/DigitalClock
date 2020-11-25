package com.example.digitalclock.ui.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TimerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("timer-broadcast", String.valueOf(System.nanoTime()/1000000000));
        context.startService(new Intent(context,TimerService.class));
    }
}
