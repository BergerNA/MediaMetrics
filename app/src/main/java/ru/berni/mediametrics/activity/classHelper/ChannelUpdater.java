package ru.berni.mediametrics.activity.classHelper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Parser;

public final class ChannelUpdater implements Callable<Boolean> {

    private static boolean runThread = false;
    private Context context;
    private static AtomicBoolean stopThread = new AtomicBoolean(false);

    public ChannelUpdater(@NonNull final Context context) {
        this.context = context;
    }

    public static void stopThread() {
        stopThread.set(true);
    }

    public static boolean isRunning() {
        return runThread;
    }

    @Override
    public Boolean call() throws Exception {
        runThread = true;
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        final ArrayList<Channel> listChannel = databaseHelper.getAllChannel();
        for (final Channel channel : listChannel) {
            if(!stopThread.get()) {
                final Parser parser = new Parser();
                try {
                    final URL urls = new URL(channel.getUrl());
                    final Channel channelResult = parser.parsChannel(urls);
                    if (channelResult != null) {
                        channel.setListItem(channelResult.getListItem());
                    }
                    databaseHelper.addChannelItems(channel);
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        context = null;
        runThread = false;
        stopThread.set(false);
        return true;
    }
}
