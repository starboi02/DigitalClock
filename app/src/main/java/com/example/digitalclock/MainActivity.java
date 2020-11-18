package com.example.digitalclock;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.digitalclock.ui.timer.TimerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    Thread timerThread;
    public boolean running=false;
    public long timerTime=0;
    MediaPlayer mp;
    Toast timerToast;
    BottomNavigationView navView;
    NavController navController;
    MenuItem settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        mp= MediaPlayer.create(this,R.raw.timersound);

        SharedPreferences.Editor editor = getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE).edit();
        editor.putBoolean("wasRunning",false);
        editor.putLong("startTime",0);
        editor.apply();

        timerToast=Toast.makeText(MainActivity.this,"Timer finished!",Toast.LENGTH_LONG);

        navView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_clock, R.id.navigation_alarm, R.id.navigation_timer,R.id.navigation_stopwatch)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(!isCurrentFragmentSame(item,navController)){
                    navigate(item,navController);
                }
                return false;
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("timer"));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("timer-time")) {
                timerTime = intent.getLongExtra("timer-time", 0);
                Log.d("timer-value-received-m", String.valueOf(timerTime));
                running=true;
                Runnable runnable = new TimeShow();
                timerThread = new Thread(runnable);
                timerThread.start();
            }
            else if(intent.hasExtra("stop-sound")){
                stopSound();
            }

            Log.d("receiver", "Got message: " + timerTime);
        }
    };

    public boolean isCurrentFragmentSame(MenuItem item,NavController navController ){
        return navController.getCurrentDestination().getId() == item.getItemId();
    }

    public void navigate(MenuItem item,NavController navController){

        if(item.getItemId()==R.id.navigation_timer){
            running=false;
            Bundle bundle = new Bundle();
            bundle.putBoolean("sound-playing", mp.isPlaying());
            bundle.putLong("timer-time",timerTime);
            timerTime=0;
            navController.navigate(item.getItemId(),bundle);
        }
        else{
            navController.navigate(item.getItemId());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        settings=menu.findItem(R.id.settings);
        return true;
    }
    @Override
    public void onBackPressed(){
        settings.setVisible(true);
        navView.setVisibility(View.VISIBLE);
        navController.navigate(R.id.navigation_clock);
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.settings) {
            navController.navigate(R.id.navigation_settings);
            navView.setVisibility(View.GONE);
            //item.setVisible(false);
            settings.setVisible(false);
        }
        else{
            Log.d("backPressed","yes");
            onBackPressed();
        }

        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        running=false;
        Log.d("MainActivity","destroyed");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        SharedPreferences.Editor editor = getSharedPreferences("stopwatch_running", Context.MODE_PRIVATE).edit();
        editor.putBoolean("wasRunning",false);
        editor.putLong("startTime",0);
        editor.apply();

    }

    public void playSound(){
        mp.start();
        mp.setLooping(true);
    }

    public void stopSound(){
        mp.stop();
        //mp.release();
    }

    class TimeShow implements Runnable{
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    if(running) {
                        timerTime=timerTime-1;
                        if (timerTime < 0) {
                            playSound();
                            timerToast.show();
                            running=false;
                        }
                        Log.d("timer-main-thread","running");
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

}