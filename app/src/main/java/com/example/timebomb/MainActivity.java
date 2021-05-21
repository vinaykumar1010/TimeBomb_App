package com.example.timebomb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS = 0;
    private TextView aTextViewCountdown;
    private TextView aEditTextMinInput;
    private Button aButtonStartPause;
    private Button aButtonReset;
    private boolean aTimerRunning;
    private CountDownTimer mCountDownTimer;
    private long aTimeLeftInMillis = START_TIME_IN_MILLIS;
    private long aStartTimeInMillis;
    private long millisInput = 1500000;
    private long aEndTime;
    private VideoView bombView;

    /**
     *
        1. Increase circle minutes size
        2. Increase minutes edit text size
        3. On start take user input and set in aTextViewCountdown
        4. Remove set button
        5. Remove hr code
        6. Show Pause and Stop after starting, Hide minute edit text
        7. Handle if minute is <= 0 or > 60. Handle when ,min is 60
     8. fix orientation</>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // means we call on create function of (super ) parent class   so that
        // only after all parent class functions execution then our written funactionality start excuting
        setContentView(R.layout.activity_main);

        aTextViewCountdown = findViewById(R.id.tv_Countdown);
        aButtonStartPause = findViewById(R.id.bt_start);
        aButtonReset = findViewById(R.id.bt_reset);
        aEditTextMinInput = findViewById(R.id.et_input_min);
        bombView = findViewById(R.id.virtual_view);

        handleStartButtonAction();
    }

    private void handleStartButtonAction() {
        // One button is being used to handle start and pause action.
        aButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aTimerRunning) {
                    pauseTimer();
                } else {
                    updateUIAndStartTimer();
                }
            }
        });
    }

    private void updateUIAndStartTimer() {
        // 1. Get user input minutes from minutes edit text
        String input = aEditTextMinInput.getText().toString();
        // 2. Set these minutes in circle text view.
        setMinutesInCircle(input);

        // 3. Convert user input minutes in millisecond. 1 min = 1 * 60 * 1000 milliseconds = 60000 milliseconds
        long timerMilliseconds = Long.parseLong(String.valueOf(input)) * 60000;

        // 4. Start timer with user input
        startTimer(timerMilliseconds);
    }

    private void startTimer(long millisecond) {
        // constructor obj dega ,  2 fun ki defination b batao
        mCountDownTimer = new CountDownTimer(millisecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("TIMER", "Timer Remaining millisecond: " + millisUntilFinished);

                // Convert milliseconds in minutes to display on UI. 1 min = 1/ 60 sec, 1 sec = 1/1000 millisecond.
                int minutes = (int) (millisUntilFinished / 1000 % 3600) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                // Format minutes to display on UI example: 15:00
                String formattedMinutes = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                // Update remaining minutes in countdown timer
                aTextViewCountdown.setText(formattedMinutes);
            }

            @Override
            public void onFinish() {
                aTimerRunning = false;
                aButtonStartPause.setText("Start");
                aButtonReset.setVisibility(View.INVISIBLE);
                playBomb();
            }
        }.start();
        aTimerRunning = true;
        bombView.stopPlayback();
        aButtonStartPause.setText("Pause");

        // start function  return object of countdowntimer type , constructor does the same thing
        updateWatchInterface();
//        aTimerRunning = true;
//        aButtonStartPause.setText("pause");
//        aButtonReset.setVisibility(View.INVISIBLE);
    }

    private void setMinutesInCircle(String minutes) {
        Log.d("TIMER", "min string: " + minutes);
        int min = Integer.parseInt(minutes);
        Log.d("TIMER", "int min: " + min);
        // 1. Check if input is a valid minute or not.
        if (min <=0 || min > 60) {
            Toast.makeText(MainActivity.this, "Enter time between 0 to 60", Toast.LENGTH_SHORT).show();
            return;
        }
//        // 2. Format minutes so that it looks like 15:00 instead of just 15
        String formattedTime = String.format(Locale.getDefault(),
                "%02d:%02d", min, 0);
//        // 3. Set this value in timer circle
        aTextViewCountdown.setText(formattedTime);
    }

//    private void isUserDoneEditingMinutes() {
//        aEditTextMinInput.setOnEditorActionListener(
//                new OnEditorActionListener() {
//                    @Override
//                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                        Log.d("TIMER", "onEditorAction");
//                        return true;
//                    }
//                }
//        );
//    }

    private void handleResetButtonAction() {
        aButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
                setTime(0);

            }
        });
    }
    private void setTime(long milliseconds) {
        aStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    public void playBomb() {
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.bomb);
        Log.d("BOMB", String.valueOf(uri));
        bombView.setVideoURI(uri);
        bombView.setVisibility(View.VISIBLE);
        bombView.start();
        bombView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                bombView.setVisibility(bombView.INVISIBLE);
            }
        });
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
            aButtonReset.setVisibility(View.INVISIBLE);
            aButtonStartPause.setText("Pause");
        } else {
            aEditTextMinInput.setVisibility(View.VISIBLE);
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putLong("startTimeInMillis", aStartTimeInMillis);
//        editor.putLong("millisLeft", aTimeLeftInMillis);
//        editor.putBoolean("timerRunning", aTimerRunning);
//        editor.putLong("endTime", aEndTime);
//        editor.apply();
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
//        aStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
//        aTimeLeftInMillis = prefs.getLong("millisLeft", aStartTimeInMillis);
//        aTimerRunning = prefs.getBoolean("timerRunning", false);
//        updateCountDownText();
//        updateWatchInterface();
//        if (aTimerRunning) {
//            aEndTime = prefs.getLong("endTime", 0);
//            aTimeLeftInMillis = aEndTime - System.currentTimeMillis();
//            if (aTimeLeftInMillis < 0) {
//                aTimeLeftInMillis = 0;
//                aTimerRunning = false;
//                updateCountDownText();
//                updateWatchInterface();
//            } else {
//                startTimer();
//            }
//        }
//    }

}