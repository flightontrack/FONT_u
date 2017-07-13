package com.flightontrack.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.util.Log;

import com.flightontrack.activity.MainActivity;

import static com.flightontrack.shared.Const.*;

public class ReceiverBoot extends BroadcastReceiver {
    private static final String TAG = "ReceiverBoot:";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive intent: "+intent);
        if (intent.getAction().contains("BOOT_COMPLETED")) {
            Log.d(TAG, " FONT:ReceiverBoot Started: isOnBoot: "+context.getSharedPreferences("com.flightontrack", Context.MODE_PRIVATE).getBoolean("a_isOnBoot", false));
            if(!context.getSharedPreferences("com.flightontrack", Context.MODE_PRIVATE).getBoolean("a_isOnBoot", false)) return;
            Intent alarmIntent = new Intent(context, ReceiverBoot.class);
            alarmIntent.setAction(FONT_RECEIVER_FILTER);
            PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, APPBOOT_DELAY_MILLISEC, pendingAlarmIntent);
        }
        else if (intent.getAction().equals(FONT_RECEIVER_FILTER)) {
            Log.d(TAG, " FONT:ReceiverBoot FONT_RECEIVER_FILTER intend");
            Intent intentActivity = new Intent(context, MainActivity.class);
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainActivity.AppProp.autostart = true;
            context.startActivity(intentActivity);
        }
        }
    }
