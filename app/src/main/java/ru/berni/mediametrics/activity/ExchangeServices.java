package ru.berni.mediametrics.activity;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.berni.mediametrics.activity.classHelper.ChannelGetter;
import ru.berni.mediametrics.activity.classHelper.ChannelItems;
import ru.berni.mediametrics.activity.classHelper.ChannelUpdater;
import ru.berni.mediametrics.activity.classHelper.ParserUrl;
import ru.berni.mediametrics.activity.utilit.NotifierProgress;
import ru.berni.mediametrics.activity.utilit.NotifierResultPars;
import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ExchangeServices extends Service {

    protected static ArrayList<Channel> listChannel = new ArrayList<>();
    protected static ArrayList<Item> listItem = new ArrayList<>();
    static ArrayList<ListenerParsUrl> listParserListener = new ArrayList<>();

    private final static ArrayList<ListenerExchange> listListener = new ArrayList<>();
    private final static ArrayList<RssUpdateListener> listRssListener = new ArrayList<>();

    private final IBinder binder = new ExchangeBinders();

    private final static int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private final static long KEEP_ALIVE_TIME = 6L;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = SECONDS;
    private final static int BLOCKING_QUEUE = 10;

    private final static String INTENT_COMMAND_UPDATE = "update_channel";
    private final static int INTENT_COMMAND_FAILED = -1;

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
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if(intent != null){
            final int command = intent.getIntExtra(INTENT_COMMAND_UPDATE, INTENT_COMMAND_FAILED);
            if(command != INTENT_COMMAND_FAILED){
                updateChannel();
            }
        }
        return startId;
    }

    public static Intent setIntentUpdate(final Intent intentToSend){
        return intentToSend.putExtra(INTENT_COMMAND_UPDATE, 101);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        listRssListener.clear();
        listListener.clear();
        listParserListener.clear();
        executor.shutdown();
        super.onDestroy();
    }

    void updateChannelTitle(final long idChannel, final String newTitle) {
        if (!ParserUrl.isRunning()) {
            final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
            databaseHelper.updateChannelTitle(idChannel, newTitle);
        }
    }

    void deleteChannel(final long idChannel) {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        databaseHelper.deleteChannel(idChannel);
    }

    void parsUrl(final String urlForPars, final String userTitleChannel) {
        if (!ParserUrl.isRunning() && isOnline()) {
            final ParserUrl parsUrl = new ParserUrl(this, urlForPars, userTitleChannel);
            executor.submit(parsUrl);
            executor.execute(new NotifierResultPars(parsUrl));
        }
    }

    void updateChannelCanStop() {
        if (!ChannelUpdater.isRunning() && isOnline()) {
            executor.execute(new NotifierProgress(executor.submit(new ChannelUpdater(this))));
        } else {
            ChannelUpdater.stopThread();
        }
    }

    private void updateChannel() {
        if (!ChannelUpdater.isRunning() && isOnline()) {
            executor.submit(new ChannelUpdater(this));
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

    protected synchronized static void sendMessageItemListeners() {
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

    protected synchronized static void sendMessageChannelListeners() {
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


    void setExchangeListener(final ListenerExchange listener) {
        listListener.add(listener);
    }

    void delExchangeListener(final ListenerExchange listener) {
        listListener.remove(listener);
    }

    public synchronized static void sendMessageExchangeListener(final boolean flag) {
        for (final ListenerExchange listener : listListener) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate(flag);
                }
            });
        }
    }

    void setParsUrlListener(final ListenerParsUrl listener) {
        listParserListener.add(listener);
    }

    void delParsUrlListener(final ListenerParsUrl listener) {
        listParserListener.remove(listener);
    }

    public synchronized static void sendMessageParsListener(final NotifierResultPars.ResultPars resultPars) {
        for (final ListenerParsUrl listener : listParserListener) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpdate(resultPars);
                }
            });
        }
    }

    public boolean isOnline() {
        final ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
