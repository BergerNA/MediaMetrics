package ru.berni.mediametrics.activity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Parser;
import ru.berni.mediametrics.R;

public class ServiceParseURL extends IntentService {

    final static String EXTRA_MESSAGE_URL = "url";
    private Handler handler;

    public ServiceParseURL() {
        super("ServiceParseURL");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startID) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());

        final String url = getExtras(intent);
        if (url == null || url.equals("")) {
            showInfoText(getResources().getString(R.string.serviceParsURL_UrlFail));
            return;
        }
        final Parser parser = new Parser();
        try {
            final Channel channel = parser.parsChannel(new URL(url));
            if (channel != null) {
                channel.setId(db.addChannel(channel));
                db.addChannelItems(channel);
                showInfoText(getResources().getString(R.string.serviceParsURL_infoAdd));
            } else {
                showInfoText(getResources().getString(R.string.serviceParsURL_resultFail));
            }
        } catch (final MalformedURLException e) {
            showInfoText(getResources().getString(R.string.serviceParsURL_UrlIncorrect));
        } finally {
            this.stopSelf();
        }
    }

    private String getExtras(final Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_MESSAGE_URL);
    }

    private void showInfoText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
