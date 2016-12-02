package ru.berni.mediametrics.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

class AppPreferences{

    private AppPreferences(){}

    private final static String KEY_CURRENT_CHANNEL_ID = "CurrentChannel";
    private final static String KEY_LIST_ITEMS_SCROLL_Y = "scrollY";

    private static SharedPreferences appPreferences;

    static void init(final Context context)
    {
        if(appPreferences == null)
            appPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    static int getCurrentChannelID(){
        return appPreferences.getInt(KEY_CURRENT_CHANNEL_ID,MODE_PRIVATE);
    }

    static void setCurrentChannelID(final int idChannel){
        final SharedPreferences.Editor prefsEditor = appPreferences.edit();
        prefsEditor.putInt(KEY_CURRENT_CHANNEL_ID, idChannel);
        prefsEditor.apply();
    }

    static int getListItemScrollY(){
        return appPreferences.getInt(KEY_LIST_ITEMS_SCROLL_Y,MODE_PRIVATE);
    }

    static void setListItemScrollY(final int spinnerY){
        final SharedPreferences.Editor prefsEditor = appPreferences.edit();
        prefsEditor.putInt(KEY_LIST_ITEMS_SCROLL_Y, spinnerY);
        prefsEditor.apply();
    }


}
