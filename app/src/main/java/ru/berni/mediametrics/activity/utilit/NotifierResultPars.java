package ru.berni.mediametrics.activity.utilit;

import android.util.Log;

import ru.berni.mediametrics.activity.classHelper.ParserUrl;

import static ru.berni.mediametrics.activity.ExchangeServices.sendMessageParsListener;

public class NotifierResultPars implements Runnable{

    public enum ResultPars{
        RESULT_OK,
        ERR_URL,
        ERR_INTERNET,
        ERR_CONTENT,
        ERR_DB,
        RUNNING,
        NULL,
        THREAD_END
    }

    private final static long CHECK_RUNNING_AFTER = 30L;
    private final static String LOG_TAG = "NotifierResultPars";

    private ParserUrl runningThread;

    public NotifierResultPars(final ParserUrl runningThread) {
        this.runningThread = runningThread;
    }

    @Override
    public void run() {
        while (ParserUrl.isRunning()){
            try {
                Thread.sleep(CHECK_RUNNING_AFTER);
                sendMessageParsListener(runningThread.getResultPars());
            } catch (final InterruptedException exc){
                Log.d(LOG_TAG, "Thread sleep exception");
            }
        }
        sendMessageParsListener(ResultPars.THREAD_END);
        runningThread = null;
    }
}
