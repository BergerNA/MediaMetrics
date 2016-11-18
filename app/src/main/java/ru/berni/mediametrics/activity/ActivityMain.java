package ru.berni.mediametrics.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;
import ru.berni.mediametrics.R;

public class ActivityMain extends Activity {

    private ArrayList<Channel> listChannel = new ArrayList<>();
    private ArrayList<Item> listItem = new ArrayList<>();

    private ArrayAdapter<Item> adapterItems;
    private ArrayAdapter<Channel> adapterChannels;

    private Spinner spinnerChannel;
    private ListView listViewItem;

    private final static String KEY_SCROLL_Y = "ScrollY";
    private ExchangeServices exchange;
    private boolean exchangeConnect = false;

    private RssUpdateListener channelListener = new RssUpdateListener(RssUpdateListener.EntityType.CHANNEL) {
        @Override
        public void onUpdate() {
            listChannel = exchange.getChannelList();
            getChannel();
        }
    };
    private RssUpdateListener itemListener = new RssUpdateListener(RssUpdateListener.EntityType.ITEM) {
        @Override
        public void onUpdate() {
            listItem = exchange.getItemList();
            getItem();
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
            final ExchangeServices.ExchangeBinders exchangeBinder = (ExchangeServices.ExchangeBinders) binder;
            exchange = exchangeBinder.getMainServices();
            exchangeConnect = true;

            exchange.setListener(channelListener);
            exchange.setListener(itemListener);
            if (exchange.listItemIsEmpty()) {
                exchange.getAllItems();
            } else {
                getItem();
            }
            if (exchange.listChannelIsEmpty()) {
                exchange.getChannelsFromDB();
            } else {
                getChannel();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            exchangeConnect = false;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerChannel = (Spinner) findViewById(R.id.spinnerTheme);
        listViewItem = (ListView) findViewById(R.id.listView);
        setChannelViewChannelAdapter();
        setItemViewItemAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Intent intent = new Intent(this, ExchangeServices.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (exchange != null) {
            exchange.setListener(channelListener);
            exchange.setListener(itemListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exchange != null) {
            exchange.delListener(channelListener);
            exchange.delListener(itemListener);
        }
        if (exchangeConnect) {
            unbindService(connection);
            exchangeConnect = false;
        }
    }

    private void getChannel() {
        listChannel = exchange.getChannelList();
        adapterChannels.clear();
        adapterChannels.addAll(listChannel);
    }

    private void getItem() {
        listItem = exchange.getItemList();
        adapterItems.clear();
        adapterItems.addAll(listItem);
    }

    public void buttonAddChannel_onClick(final View view) {
        final Intent intent = new Intent(getApplicationContext(), ActivityAddChannel.class);
        startActivity(intent);
    }

    public void buttonUpdate_onClick(final View view) {
        if (exchange != null)
            exchange.updateChannel();
    }

    private void setItemViewItemAdapter() {
        adapterItems = new ItemAdapter(getApplicationContext(), listItem);
        listViewItem.setAdapter(adapterItems);
        listViewItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                final Intent intentForItemView = new Intent(getApplicationContext(), ActivityItemView.class);
                intentForItemView.putExtra(ActivityItemView.EXTRA_MESSAGE_ID, listItem.get(position).getId());
                startActivity(intentForItemView);
            }
        });
    }

    private void setChannelViewChannelAdapter() {
        adapterChannels = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, listChannel);
        adapterChannels = new ChannelAdapter(getApplicationContext(), listChannel);
        spinnerChannel.setAdapter(adapterChannels);
        spinnerChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                exchange.getChannelItems(listChannel.get(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {

            }
        });
    }
}
