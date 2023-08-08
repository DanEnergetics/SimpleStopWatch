package com.example.simplestopwatch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

public class StopwatchService extends LifecycleService implements View.OnClickListener, View.OnLongClickListener {

    private Handler handler = new Handler();
    private boolean isRunning;
    private long startTime = 0;

    private TextView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayLayoutParams;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("StopwatchService", "onStartCommand");
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("START_STOPWATCH")) {
                // isRunning = true;
                // startTime = System.currentTimeMillis();
                Stopwatch.getInstance().start();
                showOverlayView();

            } else if (intent.getAction().equals("STOP_STOPWATCH")) {
                isRunning = false;
                handler.removeCallbacks(updateNotificationTimer);
                removeOverlayView();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // isRunning = false;
        // handler.removeCallbacks(updateNotificationTimer);
        Stopwatch.getInstance().reset();
        removeOverlayView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showOverlayView() {
        if (overlayView == null) {
            // Create the overlay view (a TextView) dynamically
            overlayView = new TextView(this);
            overlayView.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black background

            overlayView.setTextColor(Color.WHITE);
            overlayView.setTextSize(24);
            overlayView.setGravity(Gravity.CENTER);

            // Set initial text for the stopwatch
            overlayView.setText(getFormattedTime());

            final Observer<String> timeObserver = new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    overlayView.setText(s);
                }
            };
            Stopwatch.getInstance().getFormattedTime().observe((LifecycleOwner) this, timeObserver);


            overlayView.setOnClickListener(this);

            // Get the window manager service
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // Create layout parameters for the overlay view
            int overlayType;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                overlayType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            overlayLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    overlayType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    // Set the overlay to appear on top of other apps
                    PixelFormat.TRANSLUCENT
            );

            // Set the initial position of the overlay (e.g., top-left corner)
            overlayLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            overlayLayoutParams.x = 0;
            overlayLayoutParams.y = 0;
        }

        // Add the overlay view to the window manager
        windowManager.addView(overlayView, overlayLayoutParams);

        // Update timer
        // handler.post(updateNotificationTimer);
    }

    private void removeOverlayView() {
        Log.i("StopwatchService", "removeOverlayView");
        if (overlayView != null && windowManager != null) {
            // Remove the overlay view from the window manager
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    private Runnable updateNotificationTimer = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                overlayView.setText(getFormattedTime());
                handler.postDelayed(this, 1000);
            }
        }
    };

    private String getFormattedTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        int seconds = (int) (elapsedTime / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onClick(View view) {
        Log.i("StopwatchService", "onClick");
        if (isRunning) {
            // Pause the stopwatch
            isRunning = false;
            handler.removeCallbacks(updateNotificationTimer);
        } else {
            // Start the stopwatch
            isRunning = true;
            startTime = System.currentTimeMillis();
            handler.post(updateNotificationTimer);
        }

        // Update the overlay view text to reflect the current state
        overlayView.setText(getFormattedTime());
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}

