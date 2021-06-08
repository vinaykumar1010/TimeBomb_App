package com.example.timebomb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

public class SplashActivity extends AppCompatActivity {
    Handler handler;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // change status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.off_white));
        }
        splashScreen();

    }

    public void splashScreen() {

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}