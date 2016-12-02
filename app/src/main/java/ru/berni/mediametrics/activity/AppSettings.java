package ru.berni.mediametrics.activity;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import ru.berni.mediametrics.R;
import ru.berni.mediametrics.activity.utilit.NotificationTime;

public class AppSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private final static String FAIL_PERIOD = "fail";
    private final static int MILLISECOND_IN_ONE_MINUTE = 60000;
    private final static int DISABLED_SYNC = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        if(key.equals(getString(R.string.setting_internet_period_key))){
            alarmUpdate(prefs, key);
        }

    }

    private void alarmUpdate(final SharedPreferences prefs, final String key){
        prefs.getString(getString(R.string.setting_internet_period_key),FAIL_PERIOD);
        final String periodString = prefs.getString(this.getString(R.string.setting_internet_period_key),"");
        final String[] periodNames = this.getResources().getStringArray(R.array.array_period);

         final int[] periodInt = getResources().getIntArray(R.array.array_period_minutes);
        final int timeSync = getSynchronizationTime(periodString, periodNames, periodInt);
        if(timeSync != DISABLED_SYNC){
            final AlarmManager alarmNextUpdate = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            final Intent intentForUpdate = new Intent(this.getApplicationContext(), NotificationTime.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0,
                    intentForUpdate, PendingIntent.FLAG_CANCEL_CURRENT );
            alarmNextUpdate.cancel(pendingIntent);
            alarmNextUpdate.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + timeSync *  MILLISECOND_IN_ONE_MINUTE, pendingIntent);
        }
    }

    private static int getSynchronizationTime(final String period, final String[] periodNames, final int[] periodMinutes){
        int index = 0;
        for (final String periodArr : periodNames){
            if(periodArr.equals(period)){
                if(index <= periodMinutes.length-1 && index >=0) {
                    return periodMinutes[index];
                }
            }
            index++;
        }
        return DISABLED_SYNC;
    }
}
