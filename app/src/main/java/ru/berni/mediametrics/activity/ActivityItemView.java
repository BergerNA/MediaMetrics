package ru.berni.mediametrics.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.R;

public class ActivityItemView extends Activity {

    private final static String LOG_TAG = "ActivityItemView";
    private final static String HTML_IMAGE_WIDTH_SETTINGS = "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    private final static String DATE_FORMAT = "dd.MM.yyyy hh:mm:ss";

    public final static String EXTRA_MESSAGE_ID = "_id";
    private final static String KEY_SCROLL_Y = "ScrollY";

    private final static int START_SCROLL_VALUE = 0;
    private final static int FAILED_DB_VALUE = -1;
    private int Y = START_SCROLL_VALUE;

    private ScrollView scrollView;
    private String url;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);
        scrollView = (ScrollView) findViewById(R.id.ScrollView);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SCROLL_Y)) {
            Y = savedInstanceState.getInt(KEY_SCROLL_Y, START_SCROLL_VALUE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        final Item item = databaseHelper.getItem(getExtra(getIntent()));

        if (item == null) {
            return;
        }
        try {
            final TextView title = (TextView) findViewById(R.id.item_title);
            //title.setText(item.getTitle());
            title.setText(Html.fromHtml(item.getTitle()));

            final TextView description = (TextView) findViewById(R.id.item_description);
            description.setText(Html.fromHtml(item.getDescription()));

            final WebView content = (WebView) findViewById(R.id.item_content);
            content.getSettings().setBuiltInZoomControls(true);
            content.getSettings().setDisplayZoomControls(false);
            content.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            content.getSettings().setJavaScriptEnabled(true);
            content.loadDataWithBaseURL(null, HTML_IMAGE_WIDTH_SETTINGS + item.getContent(), "text/html", "UTF-8", null);

            url = item.getUrl();

            final TextView date = (TextView) findViewById(R.id.item_date);
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            date.setText(dateFormat.format(item.getDate()));
        } catch (final Throwable exc) {
            Log.e(LOG_TAG, "Error when trying extraction data from item.");
        }
        if (Y != 0) {
            final ViewTreeObserver vto = scrollView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    scrollView.scrollTo(0, Y);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCROLL_Y, scrollView.getScrollY());
    }

    private long getExtra(@NonNull final Intent intent) {
        return intent.getLongExtra(EXTRA_MESSAGE_ID, FAILED_DB_VALUE);
    }

    public void buttonItemUrl_onClick(final View view) {
        try {
            final Uri address = Uri.parse(url);
            final Intent openLink = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openLink);
        } catch (final NullPointerException exc) {
            Log.e(LOG_TAG, "Error when try pars String url to Uri address.");
            Toast.makeText(getApplicationContext(), R.string.itemView_openUrl_ErrorUrl, Toast.LENGTH_LONG).show();
        } catch (final ActivityNotFoundException exc) {
            Log.e(LOG_TAG, "Error when try open News link in other activity.");
            Toast.makeText(getApplicationContext(), R.string.itemView_openUrl_ErrorBrowser, Toast.LENGTH_LONG).show();
        }

    }
}
