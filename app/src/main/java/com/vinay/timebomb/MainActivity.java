package com.vinay.timebomb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

enum TimerType {FOCUS, SHORTBREAK, LONGBREAK};

public class MainActivity extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 3000;     //1501000;
    private static final long BREAK_TIME_IN_MILLIS = 2000;    //301000;
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
    private TimerType runningTimerType = TimerType.FOCUS;
    Window window;
    private final String focusTimerText = "25:00";
    private final String breakTimerText = "5:00";
    private String LogPrefix = "MainActivity: ";
    private Button aButtonFocus;
    private Button aButtonBreak;
    private RelativeLayout color;
    private int focusCounter = 0;
    private int breakCounter = 0;
    private TextView aFocusCycle;
    private TextView aBreakCycle;
    private TextView aFocusCycleFixed;
    private TextView aBreakCycleFixed;
    private TextView aFocusCycleTV;
    private TextView aBreakCycleTV;
    private static final String ACTIVITY_BR = "com.vinay.timebomb.mainactivity_br";
    private Intent timerIntent;

// @Override
//protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.main);
//
//    startService(new Intent(this, BroadcastService.class));
//    Log.i(TAG, "Started service");
//}

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
        timerIntent = new Intent(this, BroadcastService.class);

        // change status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.focus_color));
        }

        aTextViewCountdown = findViewById(R.id.tv_Countdown);
        aButtonStartPause = findViewById(R.id.bt_start);
        aButtonReset = findViewById(R.id.bt_reset);
        aEditTextMinInput = findViewById(R.id.et_input_min);
        bombView = findViewById(R.id.virtual_view);
        aTextViewCountdown.setText(focusTimerText);
//        aButtonReset.setVisibility(View.INVISIBLE);

        aButtonFocus = findViewById(R.id.focus_btn);
        aButtonBreak = findViewById(R.id.break_btn);
        color = findViewById(R.id.main_layout);

        aFocusCycle = findViewById(R.id.focus_cycle_tv);
        aBreakCycle = findViewById(R.id.break_cycle_tv);
        aFocusCycleFixed = findViewById(R.id.focus_cycle_fixed);
        aBreakCycleFixed = findViewById(R.id.break_cycle_fixed);
        aFocusCycleTV = findViewById(R.id.focus_cycle_tv);
        aBreakCycleTV = findViewById(R.id.break_cycle_tv);

        addListenerOnStartButtonAndHandleAction();
        addListenerOnResetButtonAndHandleAction();
        clickFocus();
        clickBreak();
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        // onReceive function gets called when service braodcasts
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };

    /**
     * Service function
     */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
        Log.i(Constants.TAG, LogPrefix + "Registered broadcast receiver");
    }

    /**
     * Service function
     */
    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(br);
        Log.i(Constants.TAG, LogPrefix + "onPause: Unregistered broacast receiver");
    }

    /**
     * Service function
     */
    @Override
    public void onDestroy() {
        stopService(new Intent(this, BroadcastService.class));
        Log.i(Constants.TAG, LogPrefix + "onDestroy Stopped service");
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            aTimeLeftInMillis = intent.getLongExtra("countdown", 0);
            String formattedminute = formatTime(aTimeLeftInMillis);
            aTextViewCountdown.setText(formattedminute);
            Log.i(Constants.TAG, LogPrefix + "updateGUI remaining time: " + aTimeLeftInMillis/1000);
            if (aTimeLeftInMillis/1000 == 0) {
                playBomb();
            }
        }
    }

    private void addListenerOnStartButtonAndHandleAction() {
        // One button is being used to handle start and pause action.
        aButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(timerIntent);
                if (aTimerRunning) {
                    stopService(timerIntent);
                } else {
                    takeInputWhileStart();
                }
            }
        });
    }

    private void addListenerOnResetButtonAndHandleAction() {
        aButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(timerIntent);

                if (runningTimerType == TimerType.FOCUS) {

                    aTimeLeftInMillis = START_TIME_IN_MILLIS;
                    timerIntent.putExtra("timerDuration", aTimeLeftInMillis);
                    stopService(timerIntent);
                    aTextViewCountdown.setText(focusTimerText);
                } else {
                    aTimeLeftInMillis = BREAK_TIME_IN_MILLIS;
                    timerIntent.putExtra("timerDuration", aTimeLeftInMillis);
                    stopService(timerIntent);
                    aTextViewCountdown.setText(breakTimerText);
                }

                aEditTextMinInput.setVisibility(View.VISIBLE);
                aEditTextMinInput.setText("");
                aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);
                // mCountDownTimer.cancel();
                aTimerRunning = false;
            }
        });
    }

    private void clickFocus() {
        Log.d(Constants.TAG, LogPrefix + "clickFocus called aButtonFocus: " + aButtonFocus);

        if (aButtonFocus != null) {
            aButtonFocus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleFocusClick();
                }
            });
        }
    }

    private void clickBreak() {
        Log.d(Constants.TAG, LogPrefix + "clickBreak called");
        if (aButtonBreak != null) {
            aButtonBreak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopService(timerIntent);
                    handleBreakClick();
                }
            });
        }
    }

    private void handleFocusClick() {
        stopService(timerIntent);
        runningTimerType = TimerType.FOCUS;
        Log.d(Constants.TAG, LogPrefix + "handleFocusClick");
        color.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.focus_color));
        aTextViewCountdown.setText(focusTimerText);

        timerIntent.putExtra("timerDuration", START_TIME_IN_MILLIS);
        startService(timerIntent);
        if (Build.VERSION.SDK_INT >= 21) {
            window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.focus_color));
        }
    }

    private void handleBreakClick() {
        stopService(timerIntent);
        runningTimerType = TimerType.SHORTBREAK;
        color.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.break_color));
        aTextViewCountdown.setText(breakTimerText);
        aTimeLeftInMillis = BREAK_TIME_IN_MILLIS;
        timerIntent.putExtra("timerDuration", aTimeLeftInMillis);
        startService(timerIntent);
        // status color
        if (Build.VERSION.SDK_INT >= 21) {
            window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.break_color));
        }
    }

    private void takeInputWhileStart() {
        // 1. Get user input minutes from minutes edit text
        String input = aEditTextMinInput.getText().toString();
        Log.d(Constants.TAG, LogPrefix +"input: " + input);
        if (input.isEmpty()) {
            String remainingMinute = formatTime(aTimeLeftInMillis);
            aTextViewCountdown.setText(remainingMinute);
            //  startTimer(aTimeLeftInMillis);
            timerIntent.putExtra("timerDuration", aTimeLeftInMillis);
            startService(timerIntent);
        } else if (Integer.parseInt(input) > 0 && Integer.parseInt(input) <= 60) {
            // 2. Convert user input minutes in millisecond. 1 min = 1 * 60 * 1000 milliseconds = 60000 milliseconds
            long timerMilliseconds = Long.parseLong(String.valueOf(input)) * 60000;
            setMinutesInCircle(input);
            timerIntent.putExtra("timerDuration", timerMilliseconds);
            startService(timerIntent);
            //  startTimer(timerMilliseconds);
        } else {
            Toast.makeText(MainActivity.this, "Enter time between 1 to 60", Toast.LENGTH_SHORT).show();
            return;
        }
    }

//    private void startTimer(long millisecond) {
//        cancelTimer();
//        // constructor obj dega ,  2 fun ki defination b batao
//        mCountDownTimer = new CountDownTimer(millisecond, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                aTimeLeftInMillis = millisUntilFinished;
//                // Update remaining minutes in countdown timer
//                aEditTextMinInput.setVisibility(View.INVISIBLE);
//                //TODO
//                aEditTextMinInput.setText("");
//
//                String formattedminute = formatTime(aTimeLeftInMillis);
//                aTextViewCountdown.setText(formattedminute);
//            }
//
//            @Override
//            public void onFinish() {
//                aTimerRunning = false;
//                aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);
//                aTimeLeftInMillis = START_TIME_IN_MILLIS;
//                aButtonReset.setVisibility(View.INVISIBLE);
//                playBomb();
//            }
//        }.start();
//        bombView.setVisibility(View.INVISIBLE);
//        closeKeyboard();
//        aTimerRunning = true;
//        bombView.stopPlayback();
//        aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);
//
//        aButtonReset.setVisibility(View.VISIBLE);
//        // start function  return object of countdowntimer type , constructor does the same thing
//    }

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


    public void playBomb() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bomb);
        Log.i(Constants.TAG, LogPrefix + String.valueOf(uri));

        aButtonFocus.setVisibility(View.INVISIBLE);
        aButtonBreak.setVisibility(View.INVISIBLE);
        aFocusCycleFixed.setVisibility(View.INVISIBLE);
        aBreakCycleFixed.setVisibility(View.INVISIBLE);
        aFocusCycleTV.setVisibility(View.INVISIBLE);
        aBreakCycleTV.setVisibility(View.INVISIBLE);

        bombView.setVideoURI(uri);
        bombView.setVisibility(View.VISIBLE);
        bombView.start();
        bombView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                bombView.setVisibility(bombView.INVISIBLE);
                aButtonFocus.setVisibility(View.VISIBLE);
                aButtonBreak.setVisibility(View.VISIBLE);
                aFocusCycleFixed.setVisibility(View.VISIBLE);
                aBreakCycleFixed.setVisibility(View.VISIBLE);
                aFocusCycleTV.setVisibility(View.VISIBLE);
                aBreakCycleTV.setVisibility(View.VISIBLE);

                /**
                 * Start break timer if current timer is focus
                 * Start focus timer if current timer is break
                 */
                if (runningTimerType == TimerType.FOCUS) {
                    // start break timer
                    handleBreakClick();
                    focusCounter++;
                    Log.d(Constants.TAG, LogPrefix +"focus cycle" + focusCounter);
                    aFocusCycle.setText(Integer.toString(focusCounter));

                } else {
                    // start focus timer
                    handleFocusClick();
                    breakCounter++;
                    Log.d(Constants.TAG, LogPrefix+ "break cycle" + breakCounter);
                    aBreakCycle.setText(Integer.toString(breakCounter));
                }
//                aTextViewCountdown.setText(focusTimerText);
            }
        });
    }

//    private void pauseTimer() {
//        aEditTextMinInput.setVisibility(View.VISIBLE);
//        mCountDownTimer.cancel();
//        aTimerRunning = false;
//        aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);
//
//    }

//    private void resetTimer() {
//        aTimeLeftInMillis = aStartTimeInMillis;
//        updateCountDownText();
//        updateWatchInterface();
//
//        //    aTextViewCountdown.setText(00:00:00);
//    }

//    private void updateCountDownText() {
//        int minutes = (int) (aTimeLeftInMillis / 1000 % 3600) / 60;
//        int seconds = (int) (aTimeLeftInMillis / 1000) % 60;
//        String timeLeftFormatted = String.format(Locale.getDefault(),
//                "%02d:%02d", minutes, seconds);
//        aTextViewCountdown.setText(timeLeftFormatted);
//    }

//    private void updateWatchInterface() {
//        if (aTimerRunning) {
//            aEditTextMinInput.setVisibility(View.INVISIBLE);
//            aButtonReset.setVisibility(View.INVISIBLE);
//            aButtonStartPause.setBackgroundResource(R.drawable.ic_pause_circle_outline);
//
//            aEditTextMinInput.setText(0);
//        } else {
//            aEditTextMinInput.setVisibility(View.VISIBLE);
//            aButtonStartPause.setBackgroundResource(R.drawable.ic_play_circle_outline);
//
//            if (aTimeLeftInMillis < 1000) {
//                aButtonStartPause.setVisibility(View.INVISIBLE);
//            } else {
//                aButtonStartPause.setVisibility(View.VISIBLE);
//            }
//            if (aTimeLeftInMillis < aStartTimeInMillis) {
//                aButtonReset.setVisibility(View.VISIBLE);
//            } else {
//                aButtonReset.setVisibility(View.INVISIBLE);
//            }
//        }
//    }

//    private void closeKeyboard() {
//        View view = this.getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//    }

//    private void cancelTimer() {
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//    }

    @Override
    protected void onStop() {
//        cancelTimer();

        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }
}