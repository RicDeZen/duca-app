package com.gov.ducadegliabruzzitreviso.ducaapp.interfaces;

import android.os.AsyncTask;

/**
 * Interface defining a callback for an AsyncTask who reached completion.
 *
 * @author Riccardo De Zen
 */
public interface AsyncTaskListener<A extends AsyncTask, R> {
    /**
     * Method called when an AsyncTask reaches completion
     *
     * @param finishedTask The task that reached completion.
     * @param result       The result for the task.
     */
    void onTaskFinished(A finishedTask, R result);
}
