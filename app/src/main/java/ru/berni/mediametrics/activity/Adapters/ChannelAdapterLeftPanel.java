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

public class ChannelAdapterLeftPanel extends ArrayAdapter<Channel> {

    public ChannelAdapterLeftPanel(final Context context, final ArrayList<Channel> listChannel) {
        super(context, 0, listChannel);
    }

    private static class ViewHolderChannel {
        TextView channelTitle;
        TextView channelCount;
        TextView channelUrl;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
            ViewHolderChannel holder = new ViewHolderChannel();
            // final Channel channel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).
                        inflate(R.layout.row_channel_left_panel, parent, false);
                holder.channelTitle = (TextView) convertView.findViewById(R.id.row_channel_left_panel_title);
                holder.channelCount = (TextView) convertView.findViewById(R.id.row_channel_left_panel_count);
                holder.channelUrl = (TextView) convertView.findViewById(R.id.row_channel_left_panel_url);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderChannel) convertView.getTag();
            }

            holder.channelTitle.setText(Html.fromHtml(getItem(position).getTitle()));
            holder.channelCount.setText(String.valueOf(getItem(position).getCount()));
            holder.channelUrl.setText(getItem(position).getUrl());

        return convertView;
    }
}
