package com.example.timebomb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS = 1500000; // 25min
    private static final long BREAK_TIME_IN_MILLIS = 300000;
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
    private boolean isFocusTimer = true;
    Window window;

    private String TAG = "timebb";
    private Button aButtonFocus;
    private Button aButtonBreak;
    private RelativeLayout color;

    /**
     * 8. fix orientation</>
     * 9. If app is in background, timer should keep running untill coundown stopped. and call onstop
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // means we call on create function of (super ) parent class   so that
        // only after all parent class functions execution then our written funactionality start excutin

        // change status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.design_default_color_surface));
        }


        aTextViewCountdown = findViewById(R.id.tv_Countdown);
        aButtonStartPause = findViewById(R.id.bt_start);
        aButtonReset = findViewById(R.id.bt_reset);
        aEditTextMinInput = findViewById(R.id.et_input_min);
        bombView = findViewById(R.id.virtual_view);

        aTextViewCountdown.setText("25:00");
        aButtonReset.setVisibility(View.INVISIBLE);

        aButtonFocus = findViewById(R.id.focus_btn);
        aButtonBreak = findViewById(R.id.break_btn);
        color = findViewById(R.id.main_layout);

        addListenerOnStartButtonAndHandleAction();
        addListenerOnResetButtonAndHandleAction();
        clickFocus();
        clickBreak();
    }

    private void clickFocus() {
        Log.d(TAG, "clickFocus called aButtonFocus: " + aButtonFocus);

        if (aButtonFocus != null) {
            aButtonFocus.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    isFocusTimer = true;
                    Log.d(TAG, "clickFocus inside onclick");
                    color.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.focus_color));
                    aTextViewCountdown.setText("25:00");

                    startTimer(START_TIME_IN_MILLIS, true);
                    if (Build.VERSION.SDK_INT >= 21) {
                        window.setStatusBarColor(MainActivity.this.getColor(R.color.focus_color));
                    }
                }


            });
        }
    }

    private void clickBreak() {
        Log.d(TAG, "clickBreak called");

        if (aButtonBreak != null) {
            aButtonBreak.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    isFocusTimer = false;
                    color.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.break_color));
                    aTextViewCountdown.setText("5:00");
//                    aTimeLeftInMillis = BREAK_TIME_IN_MILLIS;
                    startTimer(BREAK_TIME_IN_MILLIS, false);
                    if (Build.VERSION.SDK_INT >= 21) {
                        window.setStatusBarColor(MainActivity.this.getColor(R.color.break_color));
                    }
                }


            });
        }
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
            startTimer(aTimeLeftInMillis, true);
        } else if (Integer.parseInt(input) > 0 && Integer.parseInt(input) <= 60) {
            // 2. Convert user input minutes in millisecond. 1 min = 1 * 60 * 1000 milliseconds = 60000 milliseconds
            long timerMilliseconds = Long.parseLong(String.valueOf(input)) * 60000;
            setMinutesInCircle(input);
            startTimer(timerMilliseconds, true);
        } else {
            Toast.makeText(MainActivity.this, "Enter time between 1 to 60", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void startTimer(long millisecond, boolean focusTimer) {
        cancelTimer();
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
                aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);
                aTimeLeftInMillis = START_TIME_IN_MILLIS;
                aButtonReset.setVisibility(View.INVISIBLE);
                playBomb();
            }
        }.start();
        bombView.setVisibility(View.INVISIBLE);
        closeKeyboard();
        aTimerRunning = true;
        bombView.stopPlayback();
        aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);

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
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", min, 0);
        // 3. Set this value in timer circle
        aTextViewCountdown.setText(formattedTime);
    }

    private void addListenerOnResetButtonAndHandleAction() {
        aButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFocusTimer == true) {
                    aTimeLeftInMillis = START_TIME_IN_MILLIS;
                    aTextViewCountdown.setText("25:00");
                } else {
                    aTimeLeftInMillis = BREAK_TIME_IN_MILLIS;
                    aTextViewCountdown.setText("5:00");
                }

                aEditTextMinInput.setVisibility(View.VISIBLE);
                aEditTextMinInput.setText("");
                aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);
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
        aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);

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
            aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);

            aEditTextMinInput.setText(0);
        } else {
            aEditTextMinInput.setVisibility(View.VISIBLE);
            aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);

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

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
    }
}