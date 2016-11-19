package ru.berni.mediametrics.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExchangeServices extends Service {

    static ArrayList<Channel> listChannel = new ArrayList<>();
    static ArrayList<Item> listItem = new ArrayList<>();

    static ArrayList<ExchangeListener> listListener = new ArrayList<>();

    private final IBinder binder = new ExchangeBinders();

    private final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final static ArrayList<RssUpdateListener> listRssListener = new ArrayList<>();

    class ExchangeBinders extends Binder {
        ExchangeServices getMainServices() {
            return ExchangeServices.this;
        }
    }

    static Handler handler;
    private ThreadPoolExecutor executor;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES*2,
                6L,
                SECONDS,
                new ArrayBlockingQueue<Runnable>(24)
        );
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
    }

    void updateChannel() {
        if (!ChannelUpdater.isRunning()) {
            executor.execute(new ChannelUpdater(this));
        }
    }

    void getChannelsFromDB() {
        if (!ChannelGetter.isRunning()) {
            executor.execute(new ChannelGetter(this));
        }
    }

    static Channel selectChannel = null;
    void getChannelItems(final Channel channel) {
        selectChannel = channel;
        if (channel == null || channel.getUrl() == null) {
            return;
        }
       if(!ChannelItems.isRunning()){
           executor.execute(new ChannelItems(this, channel));
       }
    }

    void getAllItems() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                listItem = databaseHelper.getAllItem();
                sendMessageItemListeners();
            }
        });
    }

    ArrayList<Channel> getChannelList() { return listChannel; }

    ArrayList<Item> getItemList() {
        return listItem;
    }

    boolean listChannelIsEmpty() {
        return listChannel.size() == 0;
    }

    boolean listItemIsEmpty() {
        return listItem.size() == 0;
    }


    void setRssListener(final RssUpdateListener listener) {
        listRssListener.add(listener);
    }

    void delListener(final RssUpdateListener listener) {
        listRssListener.remove(listener);
    }

    synchronized static void sendMessageListeners() {
        for (final RssUpdateListener listener : listRssListener) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate();
                }
            });
        }
    }

    synchronized static void sendMessageItemListeners() {
        for (final RssUpdateListener listener : listRssListener) {
            if (listener.getType() == RssUpdateListener.EntityType.ITEM) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdate();
                    }
                });
            }
        }
    }

    synchronized static void sendMessageChannelListeners() {
        for (final RssUpdateListener listener : listRssListener) {
            if (listener.getType() == RssUpdateListener.EntityType.CHANNEL) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdate();
                    }
                });
            }
        }
    }


    void setExchangeListener(final ExchangeListener listener) {
        listListener.add(listener);
    }

    void delExchangeListener(final ExchangeListener listener) {
        listListener.remove(listener);
    }

    synchronized static void sendMessageExchangeListener(){
        for (final ExchangeListener listener: listListener){

            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate();
                }
            });
        }
    }
}
