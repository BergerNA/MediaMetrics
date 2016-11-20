package ru.berni.mediametrics.activity;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;

class ChannelItems  extends ExchangeServices implements Runnable {

    private static boolean runThread = false;
    private Context context;
    private final Channel channel;

    public ChannelItems(@NonNull final Context context, final Channel channel) {
        this.context = context;
        this.channel = channel;
    }

    @Override
    public synchronized void run() {
        runThread = true;
        try {
            final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            listItem = databaseHelper.getItem(channel);
            sendMessageItemListeners();
        } catch (final Throwable exc){
            exc.printStackTrace();
        }
        context = null;
        runThread = false;
    }

    static boolean isRunning(){
        return runThread;
    }
}
