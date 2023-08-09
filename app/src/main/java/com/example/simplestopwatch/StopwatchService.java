package com.example.simplestopwatch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.lifecycle.LifecycleService;

public class StopwatchService extends LifecycleService {

    private TextView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayLayoutParams;
    private final Stopwatch stopwatch = Stopwatch.getInstance();

    private boolean overlayVisible = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("StopwatchService", "onStartCommand");
//        if (intent == null || intent.getAction() == null) {
//            return START_STICKY;
//        }
//        assert intent.getAction().equals("START_STOPWATCH");
        if (!overlayVisible) {
            showOverlayView();
        } else {
            removeOverlayView();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Stopwatch.getInstance().reset();
        removeOverlayView();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    private void showOverlayView() {
        if (overlayView == null) {
            // Create the overlay view (a TextView) dynamically
            final LayoutInflater layoutInflater = LayoutInflater.from(this);
            overlayView = (TextView) layoutInflater.inflate(R.layout.overlay, null);

            Stopwatch.getInstance().getFormattedTime().observe(this, s -> overlayView.setText(s));

            overlayView.setOnClickListener(view -> stopwatch.startOrPause());

            overlayView.setOnLongClickListener(view -> { stopwatch.reset(); return false; });

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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Set the overlay to appear on top of other apps
                    PixelFormat.TRANSLUCENT
            );

            // Set the initial position of the overlay (e.g., top-left corner)
            overlayLayoutParams.gravity = Gravity.TOP | Gravity.START;
            overlayLayoutParams.x = 0;
            overlayLayoutParams.y = 0;
        }

        // Add the overlay view to the window manager
        windowManager.addView(overlayView, overlayLayoutParams);
        overlayVisible = true;
    }

    private void removeOverlayView() {
        Log.i("StopwatchService", "removeOverlayView");
        if (overlayView != null && windowManager != null) {
            // Remove the overlay view from the window manager
            windowManager.removeView(overlayView);
            overlayView = null;
            overlayVisible = false;
        }
    }
}

