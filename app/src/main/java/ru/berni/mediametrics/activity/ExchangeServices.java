package ru.berni.mediametrics.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExchangeServices extends Service {

    static ArrayList<Channel> listChannel = new ArrayList<>();
    private static ArrayList<Item> listItem = new ArrayList<>();

    private final IBinder binder = new ExchangeBinders();

    private final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final static ArrayList<RssUpdateListener> listListener = new ArrayList<>();

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
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                6L,
                SECONDS,
                new LinkedBlockingQueue<Runnable>()
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

    void getChannelItems(final Channel channel) {
        if (channel == null || channel.getUrl() == null) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                listItem = databaseHelper.getItem(channel);
                sendMessageItemListeners();
            }
        });
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

    void setListener(final RssUpdateListener listener) {
        listListener.add(listener);
    }

    void delListener(final RssUpdateListener listener) {
        listListener.remove(listener);
    }

    synchronized static void sendMessageListeners() {
        for (final RssUpdateListener listener : listListener) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate();
                }
            });
        }
    }

    private synchronized static void sendMessageItemListeners() {
        for (final RssUpdateListener listener : listListener) {
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
        for (final RssUpdateListener listener : listListener) {
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

    boolean listChannelIsEmpty() {
        return listChannel.size() == 0;
    }

    boolean listItemIsEmpty() {
        return listItem.size() == 0;
    }
}
