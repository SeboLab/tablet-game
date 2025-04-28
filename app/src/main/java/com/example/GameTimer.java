package com.example.misty;

import android.os.CountDownTimer;

public class GameTimer {
    private static GameTimer instance;
    private CountDownTimer timer;
    private long remainingTime = 6000; // 5 minutes in milliseconds
    private boolean isRunning = false;
    private GameTimerListener listener;

    public interface GameTimerListener {
        void onTimerFinish();
    }

    private GameTimer() {}

    public static GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer();
        }
        return instance;
    }

    public void startTimer(GameTimerListener listener) {
        if (isRunning) return;

        this.listener = listener;
        isRunning = true;

        timer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                isRunning = false;
                if (listener != null) {
                    listener.onTimerFinish();
                }
            }
        };
        timer.start();
    }

    public void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        remainingTime = 6000;
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
