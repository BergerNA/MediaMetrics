package ru.berni.mediametrics.activity.classHelper;

import android.content.Context;

import ru.berni.mediametrics.activity.ExchangeServices;
import ru.berni.mediametrics.dataBase.DatabaseHelper;

public final class ChannelGetter extends ExchangeServices implements Runnable{

    private static boolean isRunning = false;
    private Context context;

    public ChannelGetter(final Context context){
        this.context = context;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        isRunning = true;
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        listChannel = databaseHelper.getAllChannel();
        sendMessageChannelListeners();
        context = null;
        isRunning = false;
    }
}
