package ru.berni.mediametrics.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import ru.berni.mediametrics.activity.adapters.ChannelAdapterLeftPanel;
import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.R;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.RssUpdateListener;

public class ActivityChannelTools extends BaseExchangeService implements DialogAddChannel.SendToActivity {

    private ArrayList<Channel> listChannel = new ArrayList<>();
    private ChannelAdapterLeftPanel listChannelAdapter;
    private Channel currentChannel = new Channel();

    private final RssUpdateListener channelListener = new RssUpdateListener(RssUpdateListener.EntityType.CHANNEL) {
        @Override
        public void onUpdate() {
            getChannel();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);

        final ImageButton buttonAddChannel = (ImageButton) findViewById(R.id.activity_channel_settings_add);
        buttonAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addChannel();
            }
        });

        final ImageButton buttonClearDB = (ImageButton) findViewById(R.id.activity_channel_settings_delete);
        buttonClearDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                buttonDeleteChannel();
            }
        });

        final ImageButton buttonCreateChannel = (ImageButton) findViewById(R.id.activity_channel_settings_create);
        buttonCreateChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                createChannel();
            }
        });
        setListViewChannelAdapter();
    }

    private void setListViewChannelAdapter() {
        final ListView listViewChannel = (ListView) findViewById(R.id.activity_channel_listView_channel);
        listChannelAdapter = new ChannelAdapterLeftPanel(getApplicationContext(), listChannel);
        listViewChannel.setAdapter(listChannelAdapter);
        listViewChannel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                currentChannel = listChannel.get(position);
            }
        });
    }

    private void addChannel() {
        final DialogAddChannel dialog = DialogAddChannel.newInstance(DialogAddChannel.ChannelDialogType.ADD_CHANNEL, new Channel());
        dialog.show(getFragmentManager(), DialogAddChannel.class.getName());
    }

    private void createChannel() {
        final DialogAddChannel dialog = DialogAddChannel.newInstance(DialogAddChannel.ChannelDialogType.CREATE_CHANNEL, currentChannel);
        dialog.show(getFragmentManager(), DialogAddChannel.class.getName());
    }

    private void buttonDeleteChannel() {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        if (currentChannel != null) {
            databaseHelper.deleteChannel(currentChannel.getId());
            onUpdate();
        }
    }

    @Override
    void doWhenConnectedServices() {
        final ExchangeServices exchange = getExchange();
        if (exchange != null) {
            addRssListener(channelListener);
            if (getExchange().listChannelIsEmpty()) {
                getExchange().getChannelsFromDB();
            } else {
                getChannel();
            }
        }
    }

    @Override
    void onOnResume() {

    }

    @Override
    void onOnPause() {

    }

    private void getChannel() {
        final ExchangeServices exchange = getExchange();
        if (exchange != null) {
            listChannel = exchange.getChannelList();
            listChannelAdapter.clear();
            listChannelAdapter.addAll(listChannel);
        }
    }

    @Override
    public void onUpdate() {
        if (getExchange() != null) {
            getExchange().getChannelsFromDB();
        }
    }
}
