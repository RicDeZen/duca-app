package com.gov.ducadegliabruzzitreviso.ducaapp.threads;

import android.os.AsyncTask;

/**
 * Class Representing a Thread that cancels an AsyncTask after a certain amount of time has passed.
 *
 * @author Riccardo De Zen
 */
public class Timeout extends Thread {
    private AsyncTask task;
    private long time;

    public Timeout(AsyncTask taskToCancel, long millisecondsToWait) {
        task = taskToCancel;
        time = millisecondsToWait;
    }

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            return;
        }
        if (task.getStatus() != AsyncTask.Status.FINISHED) task.cancel(true);
    }
}
