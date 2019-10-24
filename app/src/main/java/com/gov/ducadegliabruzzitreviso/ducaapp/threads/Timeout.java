package com.gov.ducadegliabruzzitreviso.ducaapp.threads;

import android.os.AsyncTask;

public class Timeout extends Thread {
    private AsyncTask t;
    private long time;
    public Timeout(AsyncTask p, long millis){
        t = p;
        time = millis;
    }
    public void run(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try{Thread.sleep(time);}
        catch(InterruptedException e){return;}
        if(t.getStatus() != AsyncTask.Status.FINISHED) t.cancel(true);
    }
}
