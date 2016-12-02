package ru.berni.mediametrics.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;

import ru.berni.mediametrics.activity.adapters.ChannelAdapter;
import ru.berni.mediametrics.activity.adapters.ItemAdapter;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.newsParser.RssUpdateListener;
import ru.berni.mediametrics.R;

public class ActivityMain extends AppCompatActivity {

    private ArrayList<Channel> listChannel = new ArrayList<>();
    private ArrayList<Item> listItem = new ArrayList<>();

    private ArrayAdapter<Item> adapterItems;
    private ArrayAdapter<Channel> adapterChannels;

    private ProgressBar progressBar;
    private Spinner spinnerChannel;
    private ListView listViewItem;
    private MenuItem buttonUpdateChannels;
    private DrawerLayout mDrawer;

    private ExchangeServices exchange;
    private boolean exchangeConnect = false;

    private Channel currentChannel = null;

    private final RssUpdateListener channelListener = new RssUpdateListener(RssUpdateListener.EntityType.CHANNEL) {
        @Override
        public void onUpdate() {
            getChannel();
        }
    };
    private final RssUpdateListener itemListener = new RssUpdateListener(RssUpdateListener.EntityType.ITEM) {
        @Override
        public void onUpdate() {
            getItem();
        }
    };

    private final ListenerExchange eventUpdate = new ListenerExchange() {
        @Override
        public void onUpdate(final boolean threadIsRunning) {
            if (threadIsRunning) {
                buttonUpdateChannels.setIcon(R.drawable.ic_highlight_off_white_36dp);
                progressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                buttonUpdateChannels.setIcon(R.drawable.ic_cached_white_36dp);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                if (exchange != null && currentChannel != null) {
                    exchange.getChannelItems(currentChannel);
                }
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
            final ExchangeServices.ExchangeBinders exchangeBinder = (ExchangeServices.ExchangeBinders) binder;
            exchange = exchangeBinder.getMainServices();
            exchangeConnect = true;
            exchange.setRssListener(channelListener);
            exchange.setRssListener(itemListener);
            exchange.setExchangeListener(eventUpdate);
            if (!exchange.listItemIsEmpty()) {
                getItem();
            }
            if (exchange.listChannelIsEmpty()) {
                exchange.getChannelsFromDB();
            } else {
                getChannel();
            }
            listViewItem.setSelection(AppPreferences.getListItemScrollY());
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

        final Toolbar toolbar = (Toolbar) findViewById(R.id.mainActivity_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_36dp);
        }
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setupDrawerContent((NavigationView) findViewById(R.id.mainActivity_left_panel));

        AppPreferences.init(getApplicationContext());
        progressBar = (ProgressBar) findViewById(R.id.mainActivity_progressBar);

        spinnerChannel = (Spinner) findViewById(R.id.toolbar_spinner_channel);
        listViewItem = (ListView) findViewById(R.id.mainActivity_listView_item);
        setChannelViewChannelAdapter();
        setItemViewItemAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_update:
                if (exchange != null) {
                    exchange.updateChannelCanStop();
                }
                return true;
            case R.id.action_channel_settings:
                final Intent intentAddChannel = new Intent(getApplicationContext(), ActivityChannelTools.class);
                startActivity(intentAddChannel);
                return true;
            case R.id.action_app_settings:
                final Intent intent = new Intent(this, AppSettings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_drawer_channel_add:
                final Intent intent = new Intent(getApplicationContext(), ActivityChannelTools.class);
                startActivity(intent);
        }
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Intent intent = new Intent(this, ExchangeServices.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (exchange != null) {
            exchange.setRssListener(channelListener);
            exchange.setRssListener(itemListener);
            exchange.setExchangeListener(eventUpdate);
        }
        if(buttonUpdateChannels != null) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            buttonUpdateChannels.setIcon(R.drawable.ic_cached_white_36dp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppPreferences.setCurrentChannelID(spinnerChannel.getSelectedItemPosition());
        AppPreferences.setListItemScrollY(listViewItem.getFirstVisiblePosition());

        if (exchange != null) {
            exchange.delRssListener(channelListener);
            exchange.delRssListener(itemListener);
            exchange.delExchangeListener(eventUpdate);
        }
        if (exchangeConnect) {
            unbindService(connection);

            exchangeConnect = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        buttonUpdateChannels = menu.findItem(R.id.action_update);
        return true;
    }

    private void getChannel() {
        listChannel = exchange.getChannelList();
        adapterChannels.clear();
        adapterChannels.addAll(listChannel);
        spinnerChannel.setSelection(AppPreferences.getCurrentChannelID());
    }

    private void getItem() {
        listItem = exchange.getItemList();
        adapterItems.clear();
        adapterItems.addAll(listItem);
    }

    private void setItemViewItemAdapter() {
        adapterItems = new ItemAdapter(getApplicationContext(), listItem);
        listViewItem.setAdapter(adapterItems);
        listViewItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                final Intent intentForItemView = new Intent(getApplicationContext(), ActivityItemView.class);
                ActivityItemView.setIntentExtra(intentForItemView, listItem, listItem.get(position).getId());
                startActivity(intentForItemView);
            }
        });
    }

    private void setChannelViewChannelAdapter() {
        adapterChannels = new ChannelAdapter(getApplicationContext(), listChannel);
        spinnerChannel.setAdapter(adapterChannels);
        spinnerChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                currentChannel = listChannel.get(position);
                exchange.getChannelItems(currentChannel);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {

            }
        });
    }
}
