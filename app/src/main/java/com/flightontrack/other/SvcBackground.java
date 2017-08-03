package com.flightontrack.other;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.flightontrack.activity.MainActivity;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.Props;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.flightontrack.flight.Session.*;

public class SvcBackground extends Service {
    public SvcBackground() {
    }
    private static final String TAG = "SvcBackground:";
    private static final String timestamp = "127.1";
    //ReceiverRouter flightRouterReceiver;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //appendLog(TAG + "onStartCommand", 'd');
        if(!MainActivity.isMainActivityExist()){
            stopSelf();
            return START_NOT_STICKY;
        }
        //IntentFilter filter = new IntentFilter(RECEIVER_FILTER);
        //flightRouterReceiver = new ReceiverRouter();
        //registerReceiver(flightRouterReceiver, filter);
        super.onStartCommand(intent, flags, startId);
        //startLogcat();
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //appendLog(TAG + "onTaskRemoved", 'd');
        //unregisterReceiver(flightRouterReceiver);
        if (sqlHelper != null) {
            if(!(sqlHelper.dbw==null))sqlHelper.dbw.close();
            sqlHelper = null;
        }
        super.onTaskRemoved(rootIntent);
    }
    @Override
    public void onDestroy() {
        //appendLog(TAG + "onDestroy", 'd');
        try {
            //unregisterReceiver(flightRouterReceiver);
        }
        catch(IllegalArgumentException e){
            //appendLog(TAG + "IllegalArgumentException: there is no registered receivers", 'd');
        }
        if (sqlHelper != null) {
            sqlHelper.dbw.close();
            sqlHelper = null;
        }
        super.onDestroy();
    }
    public static void appendLog(String text,char type) {

        switch (type) {
            case 'd': Log.d(Const.GLOBALTAG, text);
                break;
            case 'e': Log.e(Const.GLOBALTAG,text);
                break;
        }
        if (!Props.SessionProp.pIsDebug) return;
        String timeStr = activeRoute.activeFlight.flightNumber + getDateTime() + "*";
        String LINE_SEPARATOR = System.getProperty("line.separator");

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File logFile = new File(dir, "FONT_LogFile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                //if (getIsDebug()) Log.e(TAG, e.toString());
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(timeStr + text + LINE_SEPARATOR);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            //if (getIsDebug()) Log.d(TAG, e.toString());
        }
    }
    public static String getDateTime() {
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }
//    public static void startLogcat() {
//        if (MyApplication.productionRelease) return;
//        int pid= android.os.Process.myPid();
//        try {
//            //clean logcat first
//            String cmd_clean = "logcat -c";
//            Runtime.getRuntime().exec(cmd_clean);
//            File sdcard = Environment.getExternalStorageDirectory();
//            File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat");
//            //create a dir if not exist
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            //start logcat *:W with file rotation
//            String targetLogcatFile = sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat/"+"LogcatWE_"+pid+".log";
//            String cmd_logcatstart = "logcat -f " +targetLogcatFile+" -r 100 -n 10 -v threadtime *:W";
//            Runtime.getRuntime().exec(cmd_logcatstart);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
public static void startLogcat() {
    //if (MyApplication.productionRelease) return;
    try {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat");
        //create a dir if not exist
        if (!dir.exists()) {
            dir.mkdir();
        }
        //start logcat *:W with file rotation
        String targetLogcatFile = sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat/"+"LC."+System.currentTimeMillis()+".txt";
        String cmd_logcatstart = "logcat -f " +targetLogcatFile+" -r 100 -n 10 -v threadtime *:W";
        Runtime.getRuntime().exec(cmd_logcatstart);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}