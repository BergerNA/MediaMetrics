package ru.berni.mediametrics.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;


import java.util.ArrayList;

import ru.berni.mediametrics.newsParser.RssUpdateListener;

public abstract class BaseExchangeService extends FragmentActivity {

    private ExchangeServices exchange;
    private boolean exchangeConnect = false;

    private final ArrayList<ListenerExchange> exchangeListeners = new ArrayList<>();
    private final ArrayList<RssUpdateListener> rssUpdateListeners = new ArrayList<>();

    abstract void doWhenConnectedServices();

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
            final ExchangeServices.ExchangeBinders exchangeBinder = (ExchangeServices.ExchangeBinders) binder;
            exchange = exchangeBinder.getMainServices();
            exchangeConnect = true;
            doWhenConnectedServices();
        }
        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            exchangeConnect = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        final Intent intent = new Intent(this, ExchangeServices.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        onOnResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    abstract void onOnResume();

    @Override
    protected void onPause() {
        super.onPause();
        if (exchange != null) {
            for(final ListenerExchange exchangeListener: exchangeListeners){
                exchange.delExchangeListener(exchangeListener);
            }
            for (final RssUpdateListener rssUpdateListener: rssUpdateListeners){
                exchange.delRssListener(rssUpdateListener);
            }
        }
        if (exchangeConnect) {
            unbindService(connection);
            exchangeConnect = false;
        }
        onOnPause();
    }

    abstract void onOnPause();

    void addRssListener(final RssUpdateListener rssUpdateListener){
        rssUpdateListeners.add(rssUpdateListener);
        exchange.setRssListener(rssUpdateListener);
    }

    protected ExchangeServices getExchange(){
        return exchange;
    }
}
