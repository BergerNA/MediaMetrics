package ru.berni.mediametrics.activity.adapters;

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

public class ItemAdapter extends ArrayAdapter<Item> {

    private final static int MILLISECOND_DAY = 1000 * 60 * 60 * 24;
    private final static int MILLISECOND_HOUR = 1000 * 60 * 60;
    private final static int MILLISECOND_MINUTE = 1000 * 60;

    private enum timeConst {DAY, HOUR, MINUTE}

    public ItemAdapter(final Context context, final ArrayList<Item> listItem) {
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
            return getCorrectString(t / MILLISECOND_DAY, timeConst.DAY);
        } else if (t >= MILLISECOND_HOUR) {
            return getCorrectString(t / MILLISECOND_HOUR, timeConst.HOUR);
        } else if (t > MILLISECOND_MINUTE) {
            return getCorrectString(t / MILLISECOND_MINUTE, timeConst.MINUTE);
        } else {
            return this.getContext().getResources().getString(R.string.plurals_less_minute);
        }
    }

    private String getCorrectString(final long spendValue, final timeConst period) {
        final long mod = spendValue % 10;
        if ((spendValue > 4 & spendValue <= 20) || mod > 4 || mod == 0) {
            switch (period) {
                case DAY:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_day_third);
                case HOUR:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_hour_third);
                case MINUTE:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_minute_third);
            }
        } else if (mod != 1) {
            switch (period) {
                case DAY:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_day_second);
                case HOUR:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_hour_second);
                case MINUTE:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_minutes_second);
            }
        } else {
            switch (period) {
                case DAY:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_day_first);
                case HOUR:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_hour_first);
                case MINUTE:
                    return spendValue + this.getContext().getResources().getString(R.string.plurals_minute_first);
            }
        }
        return "";
    }

}
