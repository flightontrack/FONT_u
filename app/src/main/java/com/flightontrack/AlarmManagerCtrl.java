package com.flightontrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Date;

import static com.flightontrack.Const.*;

public class AlarmManagerCtrl {
    public AlarmManagerCtrl() {
    }
    private static final String timestamp = "127.1";
    private static final String TAG = "AlarmManagerCtrl:";
    static AlarmManager alarmManager;
    static PendingIntent pendingReceiverIntent;
    static Intent receiverIntent;

    public static void initAlarm(Context ctx) {
        //AlarmManagerCtrl.ctx=ctx;
        receiverIntent = new Intent();
        receiverIntent.setAction(HEALTHCHECK_BROADCAST_RECEIVER_FILTER);
        pendingReceiverIntent = PendingIntent.getBroadcast(ctx, 0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    }

    public static void setAlarm() {
        Util.appendLog(TAG+ "setAlarm",'d');
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                getAlarmNextTimeUTCmsec(),
                pendingReceiverIntent);
    }

    public static void stopAlarm() {
        Util.appendLog(TAG+ "stopAlarm",'d');
        //initIntent();
        alarmManager.cancel(pendingReceiverIntent);
    }
    public static boolean getAlarm(Context ctx) {
        return (PendingIntent.getBroadcast(ctx, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static long getAlarmNextTimeUTCmsec() {
        long currTime = new Date().getTime();
        return currTime+ ALARM_TIME_SEC * 1000;
    }
}
