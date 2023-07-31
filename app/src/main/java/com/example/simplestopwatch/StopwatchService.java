package com.example.simplestopwatch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class StopwatchService extends Service {

    private Handler handler = new Handler();
    private boolean isRunning;
    private long startTime = 0;

    private TextView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayLayoutParams;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("START_STOPWATCH")) {
                isRunning = true;
                startTime = System.currentTimeMillis();
                handler.post(updateNotificationTimer);
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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
    }

    private void removeOverlayView() {
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
}

