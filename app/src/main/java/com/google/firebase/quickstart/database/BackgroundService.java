package com.google.firebase.quickstart.database;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "On Start Command Method");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                //Something
                Log.e(TAG, "The thread is running");
            }
        };

        Thread testThread = new Thread(r);
        testThread.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
