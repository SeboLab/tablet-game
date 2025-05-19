package com.example.misty;

import android.os.CountDownTimer;
import android.util.Log;

import com.example.misty.Socketconnection.TCPClient;

public class GameTimer {
    private static GameTimer instance;
    private CountDownTimer timer;
    //private long remainingTime = 6000; // 5 minutes in milliseconds

    private long remainingTime = 500;
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

        timer = new CountDownTimer(remainingTime, 500) {
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
                new Thread(() -> {
                    TCPClient client = TCPClient.getInstance();
                    if (!client.isConnected()) {
                        client.run();
                    }
                    if (client.isConnected()) {
                        client.sendMessage("TIMER_FINISHED");
                        Log.d("GameTimer", "Sent TIMER_FINISHED over TCP");

                    } else {
                        Log.e("GameTimer", "TCPClient not connected. Could not send TIMER_FINISHED");
                    }

                }).start();
            }
        };
        timer.start();
    }

    public void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        remainingTime = 500;
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
