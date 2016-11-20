package ru.berni.mediametrics.activity;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Parser;

class ChannelUpdater extends ExchangeServices implements Runnable {

    private static boolean runThread = false;
    private Context context;

    public ChannelUpdater(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public synchronized void run() {
        runThread = true;
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        final ArrayList<Channel> listChannel = databaseHelper.getAllChannel();
        for (final Channel channel: listChannel) {
            final Parser parser = new Parser();
            try {
               final URL urls = new URL(channel.getUrl());
               final Channel channelResult =  parser.parsChannel(urls);
                if(channelResult != null){
                    channel.setListItem(channelResult.getListItem());
                }
                databaseHelper.addChannelItems(channel);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
        }
        sendMessageExchangeListener();
        context = null;
        runThread = false;
    }

    static boolean isRunning(){
        return runThread;
    }
}
