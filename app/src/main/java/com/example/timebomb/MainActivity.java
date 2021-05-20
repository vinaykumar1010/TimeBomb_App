package com.example.timebomb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS = 0;
    private TextView aTextViewCountdown;
    private TextView aEditTextMinInput;
    private TextView aEditTextHourInput;
    private Button aButtonStartPause;
    private Button aButtonReset;
    private Button aButtonSet;
    private boolean aTimerRunning;
    private CountDownTimer mCountDownTimer;
    private long aTimeLeftInMillis = START_TIME_IN_MILLIS;
    private long aStartTimeInMillis;
    private long millisInput = 0 ;
    private long aEndTime;
    private VideoView bombView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // means we call on create function of (super ) parent class   so that
        // only after all parent class functions execution then our written funactionality start excuting
        setContentView(R.layout.activity_main);
       // setTime( long  milliseconds = 0 );

        aTextViewCountdown = findViewById(R.id.tv_Countdown);
        aButtonStartPause = findViewById(R.id.bt_start);
        aButtonReset = findViewById(R.id.bt_reset);
        aButtonSet = findViewById(R.id.bt_set);
        aEditTextMinInput = findViewById(R.id.et_input_min);
        aEditTextHourInput = findViewById(R.id.et_input_hour);
        bombView = findViewById(R.id.virtual_view);

        aButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aButtonStartPause.setVisibility(View.VISIBLE );
                String input = aEditTextMinInput.getText().toString();
                Log.d("ded", "input value is :" + input);
                if (input.length() == 0) {
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                 millisInput = Long.parseLong(String.valueOf(input)) * 60000;
                // convert diretly to milisecond
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                aEditTextMinInput.setText("");
            }
        });

        aButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aTimerRunning) {
                    pauseTimer();

                } else {
                    startTimer();
                }
            }
        });


        aButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();

            }
        });
        updateCountDownText();
    }

    private void setTime(long milliseconds) {
        Log.d("tag", "time is set");
        aStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {

        // constructor obj dega ,  2 fun ki defination b batao
        mCountDownTimer = new CountDownTimer(aTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                aButtonStartPause.setText("Pause");
                aTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                aTimerRunning = false;
                aButtonStartPause.setText("start");
                aButtonReset.setVisibility(View.INVISIBLE);
                playBomb();

            }
        }.start();
        // start function  return object of countdowntimer type , constructor does the same thing
        aTimerRunning = true;
        updateWatchInterface();
//        aTimerRunning = true;
//        aButtonStartPause.setText("pause");
//        aButtonReset.setVisibility(View.INVISIBLE);
    }

    public void playBomb() {
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.bomb);
        Log.d("BOMB", String.valueOf(uri));
        bombView.setVideoURI(uri);
        bombView.setVisibility(View.VISIBLE);
        bombView.start();

    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        aTimerRunning = false;
        updateWatchInterface();
    }

    private void resetTimer() {
        aTimeLeftInMillis = aStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
        //    aTextViewCountdown.setText(00:00:00);
    }

    private void updateCountDownText() {
        int hours = (int) (aTimeLeftInMillis / 1000) / 3660;
        int minutes = (int) (aTimeLeftInMillis / 1000 % 3600) / 60;
        int seconds = (int) (aTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        aTextViewCountdown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (aTimerRunning) {
            aEditTextMinInput.setVisibility(View.INVISIBLE);
            aButtonSet.setVisibility(View.INVISIBLE);
            aButtonReset.setVisibility(View.INVISIBLE);
            aButtonStartPause.setText("Pause");
        } else {
            aEditTextMinInput.setVisibility(View.VISIBLE);
            aButtonSet.setVisibility(View.VISIBLE);
            aButtonStartPause.setText("Start");
            if (aTimeLeftInMillis < 1000) {
                aButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                aButtonStartPause.setVisibility(View.VISIBLE);
            }
            if (aTimeLeftInMillis < aStartTimeInMillis) {
                aButtonReset.setVisibility(View.VISIBLE);
            } else {
                aButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startTimeInMillis", aStartTimeInMillis);
        editor.putLong("millisLeft", aTimeLeftInMillis);
        editor.putBoolean("timerRunning", aTimerRunning);
        editor.putLong("endTime", aEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        aStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        aTimeLeftInMillis = prefs.getLong("millisLeft", aStartTimeInMillis);
        aTimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        updateWatchInterface();
        if (aTimerRunning) {
            aEndTime = prefs.getLong("endTime", 0);
            aTimeLeftInMillis = aEndTime - System.currentTimeMillis();
            if (aTimeLeftInMillis < 0) {
                aTimeLeftInMillis = 0;
                aTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }

}