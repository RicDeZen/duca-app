package com.gov.ducadegliabruzzitreviso.ducaapp.interfaces;

import com.gov.ducadegliabruzzitreviso.ducaapp.classes.LinkTask;

/**
 * Just a renaming for an {@link AsyncTaskListener} for {@link LinkTask}.
 */
public interface LinkTaskListener extends AsyncTaskListener<LinkTask, Boolean> {
    /**
     * Method called when an AsyncTask reaches completion
     *
     * @param finishedTask The task that reached completion.
     * @param success      The result for the task, true if the task was completed successfully
     *                     or false otherwise.
     */
    @Override
    void onTaskFinished(LinkTask finishedTask, Boolean success);
}
