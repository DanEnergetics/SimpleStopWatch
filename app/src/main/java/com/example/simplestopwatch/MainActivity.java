package com.example.simplestopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView stopwatchDisplay;
    private Button startStopButton;
    private Button resetButton;

    private boolean isRunning;
    private long startTime = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopwatchDisplay = findViewById(R.id.stopwatchDisplay);
        startStopButton = findViewById(R.id.startPauseButton);

        resetButton = findViewById(R.id.resetButton);

        // connect button background to stopwatch running state
        Stopwatch.getInstance().getState().observe(this, state -> {
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
        Stopwatch.getInstance().getFormattedTime().observe(this, time -> {
            stopwatchDisplay.setText(time);
        });


        // startStopButton.setOnClickListener(this);
        startStopButton.setOnClickListener(view -> {
            Stopwatch.getInstance().startOrPause();
        });

        resetButton.setOnClickListener(view -> {
            Stopwatch.getInstance().reset();
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startPauseButton) {
            if (isRunning) {
                // Stop the stopwatch
                isRunning = false;
                startStopButton.setText("Start");
                stopStopwatchService();
            } else {
                // Start the stopwatch
                isRunning = true;
                startTime = System.currentTimeMillis();
                startStopButton.setText("Stop");
                startStopwatchService();
            }
        }
    }

    private void startStopwatchService() {
        Intent intent = new Intent(this, StopwatchService.class);
        intent.setAction("START_STOPWATCH");
        startService(intent);
    }

    private void stopStopwatchService() {
        Log.i("MainActivity", "stopStopwatchService");
        Intent intent = new Intent(this, StopwatchService.class);
        intent.setAction("STOP_STOPWATCH");
        stopService(intent);
    }
}
