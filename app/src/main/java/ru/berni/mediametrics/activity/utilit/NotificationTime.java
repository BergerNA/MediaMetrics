package ru.berni.mediametrics.activity.utilit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import ru.berni.mediametrics.R;
import ru.berni.mediametrics.activity.ExchangeServices;

public class NotificationTime extends BroadcastReceiver {

    private final static int MILLISECOND_IN_ONE_MINUTE = 60000;

    private final static int DISABLED_SYNC = -1;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final SharedPreferences prefs = PreferenceManager.
                        getDefaultSharedPreferences(context.getApplicationContext());
        final String periodString = prefs.getString(context.getString(R.string.setting_internet_period_key),"");
        final String[] periodNames = context.getResources().getStringArray(R.array.array_period);
        final int[] periodInt = context.getResources().getIntArray(R.array.array_period_minutes);
        final int timeSync = getSynchronizationTime(periodString, periodNames, periodInt);
        if(timeSync != DISABLED_SYNC){
            final Intent intentToUpdate = ExchangeServices.setIntentUpdate(new Intent(context, ExchangeServices.class));
            context.getApplicationContext().startService(intentToUpdate);
            context.getApplicationContext().stopService(intentToUpdate);

            final AlarmManager alarmNextUpdate = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final Intent intentForUpdate = new Intent(context.getApplicationContext(), NotificationTime.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
                    intentForUpdate, PendingIntent.FLAG_CANCEL_CURRENT );
            alarmNextUpdate.cancel(pendingIntent);
            alarmNextUpdate.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() +  MILLISECOND_IN_ONE_MINUTE, pendingIntent);
        }
    }

    private int getSynchronizationTime(final String period, final String[] periodNames, final int[] periodMinutes){
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

    // TODO: Протестировать с настройками приложения
    public boolean WifiConnected(final Context context) {
        final WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            final WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                final NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return true;
                }
            }
        }
        return false;
    }
}
