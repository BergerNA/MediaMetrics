package ru.berni.mediametrics.activity;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;

abstract class BaseDialogBind extends DialogFragment {

    private ExchangeServices exchange;
    private boolean exchangeConnect = false;

    private final ArrayList<ListenerParsUrl> parsUrlListenersListeners = new ArrayList<>();

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
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onOnCreate();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Intent intent = new Intent(getActivity(), ExchangeServices.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    abstract void onOnCreate();

    @Override
    public void onPause() {
        super.onPause();
        if (exchange != null) {
            for (final ListenerParsUrl parsUrlListener: parsUrlListenersListeners){
                exchange.delParsUrlListener(parsUrlListener);
            }
        }
        if (exchangeConnect) {
            getActivity().unbindService(connection);
            exchangeConnect = false;
        }
        onOnStop();
    }

    abstract void onOnStop();

    void addParsListener(final ListenerParsUrl parsUrlListener){
        parsUrlListenersListeners.add(parsUrlListener);
        exchange.setParsUrlListener(parsUrlListener);
    }

    protected ExchangeServices getExchange(){
        return exchange;
    }
}
