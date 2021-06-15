package com.vinay.timebomb;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

// Reference:  https://stackoverflow.com/questions/22496863/how-to-run-countdowntimer-in-a-service-in-android
public class BroadcastService extends Service {

    public static final String COUNTDOWN_BR = "com.vinay.timebomb.countdown_br";
    Intent bintent = new Intent(COUNTDOWN_BR);
    CountDownTimer cdt = null;
    private String LogPrefix = "BroadcastService";
    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(Constants.TAG, LogPrefix + "onDestroy Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent !=null && intent.getExtras()!=null) {
            long duration = intent.getExtras().getLong("timerDuration");
            if (duration > 0) {
                startTimer(duration);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void startTimer(long duration){
        if (cdt != null) {
            cdt.cancel();
        }
        cdt = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                bintent.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bintent);
            }

            @Override
            public void onFinish() {
                Log.i(Constants.TAG, LogPrefix + "Timer finished");
            }
        };
        cdt.start();
    }
}
