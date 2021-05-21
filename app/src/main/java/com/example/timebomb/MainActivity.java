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

    private static final long START_TIME_IN_MILLIS = 1500000; // 25min
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
     * 8. fix orientation</>
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

        aTextViewCountdown.setText("25:00");
        aButtonReset.setVisibility(View.INVISIBLE);

        addListenerOnStartButtonAndHandleAction();
        addListenerOnResetButtonAndHandleAction();
    }

    private void addListenerOnStartButtonAndHandleAction() {
        // One button is being used to handle start and pause action.
        aButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aTimerRunning) {
                    pauseTimer();
                } else {
                    takeInputWhileStart();
                }
            }
        });
    }

    private void takeInputWhileStart() {
        // 1. Get user input minutes from minutes edit text
        String input = aEditTextMinInput.getText().toString();
        Log.d("TIMER", "input: " + input);
        if (input.isEmpty()) {
            String remainingMinute = formatTime(aTimeLeftInMillis);
            aTextViewCountdown.setText(remainingMinute);
            startTimer(aTimeLeftInMillis);
        }
        else if (Integer.parseInt(input) > 0 && Integer.parseInt(input) <= 60){
            // 2. Convert user input minutes in millisecond. 1 min = 1 * 60 * 1000 milliseconds = 60000 milliseconds
            long timerMilliseconds = Long.parseLong(String.valueOf(input)) * 60000;
            setMinutesInCircle(input);
            startTimer(timerMilliseconds);
        }
        else {
            Toast.makeText(MainActivity.this, "Enter time between 1 to 60", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void startTimer(long millisecond) {
        // constructor obj dega ,  2 fun ki defination b batao
        mCountDownTimer = new CountDownTimer(millisecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                aTimeLeftInMillis = millisUntilFinished;
                // Update remaining minutes in countdown timer
                aEditTextMinInput.setVisibility(View.INVISIBLE);
               //TODO
                  aEditTextMinInput.setText("");

                String formattedminute = formatTime(aTimeLeftInMillis);
                aTextViewCountdown.setText(formattedminute);
            }

            @Override
            public void onFinish() {
                aTimerRunning = false;
                aButtonStartPause.setText("Start");
                aTimeLeftInMillis = START_TIME_IN_MILLIS;
                aButtonReset.setVisibility(View.INVISIBLE);
                playBomb();
            }
        }.start();
        bombView.setVisibility(View.INVISIBLE);
        closeKeyboard();
        aTimerRunning = true;
        bombView.stopPlayback();
        aButtonStartPause.setText("Pause");
        aButtonReset.setVisibility(View.VISIBLE);
        // start function  return object of countdowntimer type , constructor does the same thing
    }

    private String formatTime(long millisecond) {
        // Convert milliseconds in minutes to display on UI. 1 min = 1/ 60 sec, 1 sec = 1/1000 millisecond.
        int minutes = (int) (millisecond / 1000 % 3600) / 60;
        int seconds = (int) (millisecond / 1000) % 60;
        // Format minutes to display on UI example: 15:00
        String formattedMinutes = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        return formattedMinutes;
    }

    private void setMinutesInCircle(String minutes) {
        int min = Integer.parseInt(minutes);
        // 2. Format minutes so that it looks like 15:00 instead of just 15
        String formattedTime = String.format(Locale.getDefault(),  "%02d:%02d", min, 0);
        // 3. Set this value in timer circle
        aTextViewCountdown.setText(formattedTime);
    }

    private void addListenerOnResetButtonAndHandleAction() {
        aButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               aTimeLeftInMillis = START_TIME_IN_MILLIS;
               aTextViewCountdown.setText("25:00");
               aEditTextMinInput.setVisibility(View.VISIBLE);
               aEditTextMinInput.setText("");
               aButtonStartPause.setText("Start");
               mCountDownTimer.cancel();
               aTimerRunning = false;
            }
        });
    }

    public void playBomb() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bomb);
        Log.d("BOMB", String.valueOf(uri));
        bombView.setVideoURI(uri);
        bombView.setVisibility(View.VISIBLE);
        bombView.start();
        bombView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                bombView.setVisibility(bombView.INVISIBLE);
                aTextViewCountdown.setText("25:00");
            }
        });
    }

    private void pauseTimer() {
        aEditTextMinInput.setVisibility(View.VISIBLE);
        mCountDownTimer.cancel();
        aTimerRunning = false;
        aButtonStartPause.setText("Start");
    }

    private void resetTimer() {
        aTimeLeftInMillis = aStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();

        //    aTextViewCountdown.setText(00:00:00);
    }

    private void updateCountDownText() {
        int minutes = (int) (aTimeLeftInMillis / 1000 % 3600) / 60;
        int seconds = (int) (aTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);
        aTextViewCountdown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (aTimerRunning) {
            aEditTextMinInput.setVisibility(View.INVISIBLE);
            aButtonReset.setVisibility(View.INVISIBLE);
            aButtonStartPause.setText("Pause");
            aEditTextMinInput.setText(0);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}