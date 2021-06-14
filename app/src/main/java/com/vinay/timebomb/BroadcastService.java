package com.vinay.timebomb;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

// Reference:  https://stackoverflow.com/questions/22496863/how-to-run-countdowntimer-in-a-service-in-android
public class BroadcastService extends Service {
    private static final String TAG = "BroadcastService";
    public static final String COUNTDOWN_BR = "com.vinay.timebomb.countdown_br";
    Intent bintent = new Intent(COUNTDOWN_BR);
    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "BroadcastService onCreate");
        cdt = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                bintent.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bintent);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");
            }
        };
        cdt.start();
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
