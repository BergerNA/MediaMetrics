package ru.berni.mediametrics.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExchangeServices extends Service {

    static ArrayList<Channel> listChannel = new ArrayList<>();
    static ArrayList<Item> listItem = new ArrayList<>();

    private final static ArrayList<ExchangeListener> listListener = new ArrayList<>();

    private final IBinder binder = new ExchangeBinders();

    private final static int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private final static long KEEP_ALIVE_TIME = 6L;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = SECONDS;
    private final static int BLOCKING_QUEUE = 10;

    private final static ArrayList<RssUpdateListener> listRssListener = new ArrayList<>();

    class ExchangeBinders extends Binder {
        ExchangeServices getMainServices() {
            return ExchangeServices.this;
        }
    }

    private static Handler handler;
    private ThreadPoolExecutor executor;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new ArrayBlockingQueue<Runnable>(BLOCKING_QUEUE)
        );
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        listListener.clear();
        executor.shutdown();
        super.onDestroy();
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

    void getChannelItems(final Channel channel) {
        if (channel == null || channel.getUrl() == null) {
            return;
        }
        if (!ChannelItems.isRunning()) {
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

    ArrayList<Channel> getChannelList() {
        return listChannel;
    }

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

    void delRssListener(final RssUpdateListener listener) {
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

    synchronized static void sendMessageExchangeListener() {
        for (final ExchangeListener listener : listListener) {
            listener.onUpdate();
        }
    }
}
