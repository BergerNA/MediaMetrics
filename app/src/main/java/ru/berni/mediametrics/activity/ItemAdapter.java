package ru.berni.mediametrics.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.berni.mediametrics.newsParser.Item;
import ru.berni.mediametrics.R;

class ItemAdapter extends ArrayAdapter<Item> {

    private final static int MILLISECOND_DAY = 86400000;
    private final static int MILLISECOND_HOUR = 3600000;
    private final static int MILLISECOND_MINUTE = 60000;

    private enum timeConst {DAY, HOUR, MINUTE}

    ItemAdapter(final Context context, final ArrayList<Item> listItem) {
        super(context, 0, listItem);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final Item item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_item_view, parent, false);
        }
        if (item != null) {
            ((TextView) convertView.findViewById(R.id.listView_textView_item_title)).setText(Html.fromHtml(item.getTitle()));
            ((TextView) convertView.findViewById(R.id.listView_textView_item_date)).setText(getTimeSpend(item));
        }
        return convertView;
    }

    private String getTimeSpend(final Item item) {
        final long t = System.currentTimeMillis() - item.getDate().getTime();
        if (t >= MILLISECOND_DAY) {
            final long days = (t / MILLISECOND_DAY);
            return str(days, timeConst.DAY);
        } else if (t >= MILLISECOND_HOUR) {
            final long hour = (t / MILLISECOND_HOUR);
            return str(hour, timeConst.HOUR);
        } else if (t > MILLISECOND_MINUTE) {
            final long minute = (t / MILLISECOND_MINUTE);
            return str(minute, timeConst.MINUTE);
        } else {
            return "менее минуты назад";
        }
    }

    private String str(final long i, final timeConst period) {
        final long mod = i % 10;
        if ((i > 4 & i <= 20) || mod > 4 || mod == 0) {
            switch (period) {
                case DAY:
                    return i + " дней назад";
                case HOUR:
                    return i + " часов назад";
                case MINUTE:
                    return i + " минут назад";
            }
        } else if (mod != 1) {
            switch (period) {
                case DAY:
                    return i + " дня назад";
                case HOUR:
                    return i + " часа назад";
                case MINUTE:
                    return i + " минуты назад";
            }
        } else {
            switch (period) {
                case DAY:
                    return i + " день назад";
                case HOUR:
                    return i + " час назад";
                case MINUTE:
                    return i + " минута назад";
            }
        }
        return "";
    }

}
