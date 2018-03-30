package com.flightontrack.other;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.Entities.EntityLogMessage;
import com.flightontrack.shared.Props;

import java.util.Date;

import static com.flightontrack.shared.Const.*;

public class AlarmManagerCtrl {
    public AlarmManagerCtrl() {
    }

    private static final String TAG = "AlarmManagerCtrl";
    static AlarmManager alarmManager;
    static PendingIntent pendingReceiverIntent;
    static Intent receiverIntent;

    public static void initAlarm() {
        //AlarmManagerCtrl.ctx=ctx;
        receiverIntent = new Intent();
        receiverIntent.setAction(HEALTHCHECK_BROADCAST_RECEIVER_FILTER);
        pendingReceiverIntent = PendingIntent.getBroadcast(Props.ctxApp, 0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) Props.ctxApp.getSystemService(Context.ALARM_SERVICE);
    }

    public static void setAlarm() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "setAlarm",'d'));
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                getAlarmNextTimeUTCmsec(),
                pendingReceiverIntent);
    }

    public static void stopAlarm() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "stopAlarm",'d'));
        //initIntent();
        alarmManager.cancel(pendingReceiverIntent);
    }
    public static boolean getAlarm() {
        return (PendingIntent.getBroadcast(Props.ctxApp, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static long getAlarmNextTimeUTCmsec() {
        long currTime = new Date().getTime();
        return currTime+ ALARM_TIME_SEC * 1000;
    }
}
