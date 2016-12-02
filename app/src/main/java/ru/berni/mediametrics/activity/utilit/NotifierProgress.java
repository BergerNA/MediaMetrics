package ru.berni.mediametrics.activity.utilit;

import android.util.Log;

import java.util.concurrent.Future;

import static ru.berni.mediametrics.activity.ExchangeServices.sendMessageExchangeListener;

public class NotifierProgress implements Runnable{

    private final static long CHECK_RUNNING_AFTER = 30L;
    private final static boolean THREAD_RUNS = true;
    private final static boolean THREAD_NOT_RUNS = false;
    private final static String LOG_TAG = "NotifierProgress";
    private Future<Boolean> runningThread;

    public NotifierProgress(final Future<Boolean> runningThread) {
        this.runningThread = runningThread;
    }

    @Override
    public void run() {
        while (!runningThread.isDone()){
            try {
                Thread.sleep(CHECK_RUNNING_AFTER);
                sendMessageExchangeListener(THREAD_RUNS);
            } catch (final InterruptedException exc){
                Log.d(LOG_TAG, "Thread sleep exception");
            }
        }
        sendMessageExchangeListener(THREAD_NOT_RUNS);
        runningThread = null;
    }
}
