package ru.berni.mediametrics.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.R;

public class ActivityItemView extends Activity {

    private final static String LOG_TAG = "ActivityItemView";
    private final static String HTML_IMAGE_WIDTH_SETTINGS = "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    private final static String DATE_FORMAT = "dd.MM.yyyy hh:mm:ss";

    private final static String EXTRA_MESSAGE_ID = "_id";
    private final static String EXTRA_MESSAGE_ARRAY_ID = "array_id";
    private final static String EXTRA_MESSAGE_ARRAY_LENGTH = "array_length";

    private final static String KEY_SCROLL_Y = "scrollY";
    private final static String CHARSET = "UTF-8";
    private final static String CONTENT_TYPE = "text/html";

    private final static int START_SCROLL_VALUE = 0;
    private final static int START_SCROLL_VALUE_1 = 1;
    private final static int FAILED_DB_VALUE = -1;
    private int scrollToY = START_SCROLL_VALUE;

    private ScrollView scrollView;
    private String url;
    private long[] listItemId;
    private long itemId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);
        scrollView = (ScrollView) findViewById(R.id.item_ScrollView);
        getExtra(getIntent());
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SCROLL_Y)) {
            scrollToY = savedInstanceState.getInt(KEY_SCROLL_Y, START_SCROLL_VALUE);
            itemId = savedInstanceState.getLong(EXTRA_MESSAGE_ID, itemId);
        }

        final Button buttonItemUrlOnBrowser = (Button) findViewById(R.id.item_button_url);
        buttonItemUrlOnBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                buttonItemUrl();
            }
        });

        final GestureDetector gdt = new GestureDetector(new GestureListener());
        final ScrollView scrollView = (ScrollView) findViewById(R.id.item_ScrollView);
        final WebView content = (WebView) findViewById(R.id.item_webView_content);
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch( final View v, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return false;
            }
        });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return false;
            }
        });
    }

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            try {
                return Math.abs(distanceY) > Math.abs(distanceX);
            } catch (final Exception ignored) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                buttonNext();
                return true;
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                buttonPrev();
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        final Item item = databaseHelper.getItem(itemId);

        if (item == null) {
            return;
        }
        try {
            final TextView title = (TextView) findViewById(R.id.item_textView_title);
            title.setText(Html.fromHtml(item.getTitle()));

            final TextView description = (TextView) findViewById(R.id.item_textView_description);
            description.setText(Html.fromHtml(item.getDescription()));

            final WebView content = (WebView) findViewById(R.id.item_webView_content);
            if(! item.getContent().equals("")) {
                content.setEnabled(true);
                content.getSettings().setBuiltInZoomControls(false);
                content.getSettings().setDisplayZoomControls(false);
                content.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                content.getSettings().setJavaScriptEnabled(true);
                content.setHorizontalScrollBarEnabled(false);
                content.loadDataWithBaseURL(null, HTML_IMAGE_WIDTH_SETTINGS + item.getContent(), CONTENT_TYPE, CHARSET, null);
            } else{
                content.setEnabled(false);
            }

            url = item.getUrl();

            final TextView date = (TextView) findViewById(R.id.item_textView_date);
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            date.setText(dateFormat.format(item.getDate()));
        } catch (final Throwable exc) {
            Log.e(LOG_TAG, "Error when trying extraction data from item.");
        }

        if (scrollToY != 0) {
            final ViewTreeObserver vto = scrollView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    scrollView.scrollTo(0, scrollToY);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCROLL_Y, scrollView.getScrollY());
        outState.putLong(EXTRA_MESSAGE_ID, itemId);
    }

    private void buttonNext() {
        int num = 0;
        for (final long i : listItemId) {
            num++;
            if (i == itemId) {
                if (listItemId.length <= num) {
                    itemId = listItemId[0];
                } else {
                    itemId = listItemId[num];
                }
                scrollToY = START_SCROLL_VALUE_1;
                onStart();
                break;
            }
        }
    }

    private void buttonPrev() {
        int num = 0;
        for (final long i : listItemId) {
            num++;
            if (i == itemId) {
                if (1 >= num) {
                    itemId = listItemId[listItemId.length - 1];
                } else {
                    num -= 2;
                    itemId = listItemId[num];
                }
                scrollToY = START_SCROLL_VALUE_1;
                onStart();
                break;
            }
        }
    }

    private void buttonItemUrl() {
        try {
            final Uri address = Uri.parse(url);
            final Intent openLink = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openLink);
        } catch (final NullPointerException exc) {
            Log.e(LOG_TAG, "Error when try pars String url to Uri address.");
            Toast.makeText(getApplicationContext(), R.string.itemView_openUrl_ErrorUrl, Toast.LENGTH_LONG).show();
        } catch (final ActivityNotFoundException exc) {
            Log.e(LOG_TAG, "Error when try open News link in other activity.");
            Toast.makeText(getApplicationContext(), R.string.itemView_openUrl_ErrorBrowser, Toast.LENGTH_SHORT).show();
        }
    }

    public static void setIntentExtra(@NonNull final Intent intent, final ArrayList<Item> items, final long id) {
        intent.putExtra(EXTRA_MESSAGE_ID, id);
        final long[] arrayId = new long[items.size()];
        int i = 0;
        for (final Item item : items) {
            arrayId[i++] = item.getId();
        }
        intent.putExtra(EXTRA_MESSAGE_ARRAY_ID, arrayId);
        intent.putExtra(EXTRA_MESSAGE_ARRAY_LENGTH, i);
    }

    private void getExtra(@NonNull final Intent intent) {
        itemId = intent.getLongExtra(EXTRA_MESSAGE_ID, FAILED_DB_VALUE);
        final int size = intent.getIntExtra(EXTRA_MESSAGE_ARRAY_LENGTH, 0);
        listItemId = new long[size];
        listItemId = intent.getLongArrayExtra(EXTRA_MESSAGE_ARRAY_ID);
    }
}

