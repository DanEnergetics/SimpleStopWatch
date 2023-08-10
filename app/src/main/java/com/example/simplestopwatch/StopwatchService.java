package com.example.simplestopwatch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;

import java.util.Optional;

public class StopwatchService extends LifecycleService {

    private final static String NOTIF_CHANNEL_ID = "CHANNEL_FOREGROUND_SERVICE";
    private static boolean IS_RUNNING = false;
    private TextView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayLayoutParams;
    private final Stopwatch stopwatch = Stopwatch.getInstance();

    private NotificationManagerCompat notificationManager;

    private boolean overlayVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("StopwatchService", "onStartCommand");
        if (!IS_RUNNING)
            startForeground(startId, createNotification());
        IS_RUNNING |= true;

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

            stopwatch.getFormattedTime().observe(this, s -> {
                if (overlayView != null) overlayView.setText(s);
            });

            stopwatch.getState().observe(this, s -> {
                if (overlayView == null) return;
                final int colorId;
                switch (s) {
                    case ZERO:
                        colorId = R.color.black;
                        break;
                    case RUNNING:
                        colorId = R.color.green;
                        break;
                    case PAUSED:
                        colorId = R.color.yellow;
                        break;
                    default:
                        colorId = R.color.black;
                        break;
                }
                overlayView.getBackground().setTint(
                        getResources().getColor(colorId, null)
                );
            });

            overlayView.setOnClickListener(view -> stopwatch.startOrPause());

            overlayView.setOnLongClickListener(view -> { stopwatch.reset(); return true; });

            // Get the window manager service
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // Create layout parameters for the overlay view
            overlayLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
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
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//            notificationManager = getSystemService(NotificationManager.class);
            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        // If the notification supports a direct reply action, use
        // PendingIntent.FLAG_MUTABLE instead.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
//                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
//                        .setTicker(getText(R.string.ticker_text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();

        return notification;

    }
}

