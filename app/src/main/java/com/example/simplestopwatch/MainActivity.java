package com.example.simplestopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView stopwatchDisplay;
    private Button startStopButton;

    private boolean isRunning;
    private long startTime = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopwatchDisplay = findViewById(R.id.stopwatchDisplay);
        startStopButton = findViewById(R.id.startStopButton);
        startStopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startStopButton) {
            if (isRunning) {
                // Stop the stopwatch
                isRunning = false;
                handler.removeCallbacks(updateTimer);
                startStopButton.setText("Start");
                stopStopwatchService();
            } else {
                // Start the stopwatch
                isRunning = true;
                startTime = System.currentTimeMillis();
                handler.post(updateTimer);
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
        Intent intent = new Intent(this, StopwatchService.class);
        intent.setAction("STOP_STOPWATCH");
        stopService(intent);
    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            int seconds = (int) (elapsedTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds %= 60;
            minutes %= 60;

            String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            stopwatchDisplay.setText(time);

            if (isRunning) {
                handler.postDelayed(this, 1000);
            }
        }
    };
}
