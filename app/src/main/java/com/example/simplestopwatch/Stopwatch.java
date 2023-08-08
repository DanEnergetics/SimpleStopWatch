package com.example.simplestopwatch;

import androidx.lifecycle.MutableLiveData;

import android.os.Handler;

public final class Stopwatch {

    public interface OnStateChangedListener {
        void onChanged(State newState);
    }

    public enum State {
        ZERO,
        RUNNING,
        PAUSED,
    }

    private static Stopwatch INSTANCE;

    // private State state = State.ZERO;
    private MutableLiveData<State> state = new MutableLiveData<>(State.ZERO);

    private long startTime = 0;
    private long savedTime = 0;

    private MutableLiveData<String> formattedTime = new MutableLiveData<>();

    private Stopwatch() {
        savedTime = 0;
    }

    public static Stopwatch getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Stopwatch();
        }

        return INSTANCE;
    }

    public void start() {
        state.postValue(State.RUNNING);
        startTime = System.currentTimeMillis();
        handler.post(updateNotificationTimer);
    }

    public void pause() {
        state.postValue(State.PAUSED);
        savedTime += System.currentTimeMillis() - startTime;
        handler.removeCallbacks(updateNotificationTimer);
    }

    public void startOrPause() {
        switch (state.getValue()) {
            case RUNNING:
                pause();
                break;
            case PAUSED:
            case ZERO:
                start();
                break;
            default:
                assert false;
        }
    }

    public void reset() {
        state.postValue(State.ZERO);
        formattedTime.postValue(formatTime(0));
        savedTime = 0;
        startTime = 0;
        handler.removeCallbacks(updateNotificationTimer);
    }

    private Handler handler = new Handler();

    private Runnable updateNotificationTimer = new Runnable() {
        @Override
        public void run() {
            formattedTime.postValue(getElapsedFormattedTime());
            handler.postDelayed(this, 1000);
        }
    };

    private String getElapsedFormattedTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime + savedTime;
        return formatTime(elapsedTime);
    }

    private String formatTime(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public MutableLiveData<String> getFormattedTime() {
        return formattedTime;
    }

    public MutableLiveData<State> getState() {
        return state;
    }

}
