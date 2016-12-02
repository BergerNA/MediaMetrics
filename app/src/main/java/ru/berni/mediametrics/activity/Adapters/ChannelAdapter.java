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

import ru.berni.mediametrics.R;
import ru.berni.mediametrics.newsParser.Channel;

public class ChannelAdapter extends ArrayAdapter<Channel>{

    public ChannelAdapter(final Context context, final ArrayList<Channel> listChannel) {
        super(context, 0, listChannel);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final Channel channel = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.row_channel_view, parent, false);
        }

        if (channel != null) {
            ((TextView) convertView.findViewById(R.id.spinner_textView_channel)).
                    setText(Html.fromHtml(channel.getTitle()));
        }
        return convertView;
    }

    @Override
    public View getDropDownView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final Channel channel = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.row_channel_view, parent, false);
        }

        if(channel != null && channel.getTitle() != null) {
            ((TextView) convertView.findViewById(R.id.spinner_textView_channel)).
                    setText(Html.fromHtml(channel.getTitle()));
        }
        return convertView;
    }
}
