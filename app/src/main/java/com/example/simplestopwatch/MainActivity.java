package com.example.simplestopwatch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

public class MainActivity extends AppCompatActivity {

    private TextView stopwatchDisplay;
    private Button startStopButton;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopwatchDisplay = findViewById(R.id.stopwatchDisplay);
        startStopButton = findViewById(R.id.startPauseButton);
        resetButton = findViewById(R.id.resetButton);
        final ImageButton powerButton = findViewById(R.id.powerServiceButton);

        final Stopwatch stopwatch = Stopwatch.getInstance();

        // connect button background to stopwatch running state
        stopwatch.getState().observe(this, state -> {
            switch (state) {
                case RUNNING:
                    Log.i("MainActivity", "Button set selected");
                    startStopButton.setSelected(true);
                    startStopButton.setText(R.string.pause);
                    break;
                case PAUSED:
                case ZERO:
                    Log.i("MainActivity", "Button not selected");
                    startStopButton.setSelected(false);
                    startStopButton.setText(R.string.start);
                    break;
                default:
                    break;
            }
        });

        // connect time display to stopwatch time
        stopwatch.getFormattedTime().observe(this, time -> stopwatchDisplay.setText(time));

        startStopButton.setOnClickListener(view -> stopwatch.startOrPause());

        resetButton.setOnClickListener(view -> stopwatch.reset());

        powerButton.setOnClickListener(view -> {
            final boolean isRunning = startStopwatchService();
            if (!isRunning) {
                stopStopwatchService();
            }
        });
    }

    private boolean startStopwatchService() {
        Intent intent = new Intent(this, StopwatchService.class);
        intent.setAction("START_STOPWATCH");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return startForegroundService(intent) != null;
        }
        return startService(intent) != null;
    }

    private void stopStopwatchService() {
        Log.i("MainActivity", "stopStopwatchService");
        Intent intent = new Intent(this, StopwatchService.class);
        intent.setAction("STOP_STOPWATCH");
        stopService(intent);
    }
}
