package com.flightontrack.log;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.flightontrack.flight.Route;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.shared.Props;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.flightontrack.pilot.MyPhone._myPhoneId;
import static com.flightontrack.shared.Const.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.shared.Const.GLOBALTAG;

/**
 * Created by hotvk on 3/29/2018.
 */

public class FontLogAsync extends AsyncTask<LogMessage, Void, Boolean> {
    private static final String TAG = "FontLogAsync";

    @Override
    protected Boolean doInBackground(LogMessage... msgobj) {
        String text =  String.format("%1$-20s", msgobj[0].tag);
        text +=":";
        text +=" af:"+RouteBase.activeFlight==null?"null":RouteBase.activeFlight.flightNumber+" afs:"+RouteBase.activeFlight.flightState+": ";
        text += msgobj[0].msg;
        appendLogcat(text,msgobj[0].msgType);
        if (Props.SessionProp.pIsDebug) appendCustomLog(text);
        return true;
}
    public void appendLogcat(String text,char type) {

        switch (type) {
            case 'd':
                Log.d(GLOBALTAG, text);
                break;
            case 'e':
                Log.e(GLOBALTAG, text);
                //startLogcat("appendLog"); TODO need to check permission first
                break;
        }
    }
    void appendCustomLog(String text){
        String timeStr = "[" + getDateTimeNow() + "]";
        String af = String.format("%1$-10s", " af:"+RouteBase.activeFlight.flightNumber+" afs:"+RouteBase.activeFlight.flightState+": ") ;
        String LINE_SEPARATOR = System.getProperty("line.separator");
        File sdcard=null;
        try {
            sdcard = Environment.getExternalStorageDirectory();
        }
        catch(Exception e){
            Log.e(TAG, "AppendLog nvironment.getExternalStorageDirectory( "+e);
            e.printStackTrace();
        }
        File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File logFile = new File(dir, "FONTLog_"+_myPhoneId+".txt");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(timeStr +af+ text + LINE_SEPARATOR);
            buf.close();
        }
        catch (IOException e) {
            Log.e(TAG, "AppendLog IO "+e);
            e.printStackTrace();
            startLogcat("appendLogIOException");
            return;
        }

    }
    private static String getDateTimeNow() {
        /// this method id identical to GetTime default method
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }
    public static void startLogcat(String source) {
        //if (MyApplication.productionRelease) return;
        Log.e(TAG, "startLogcat :" + source);
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat");
            //create a dir if not exist
            if (!dir.exists()) {
                dir.mkdir();
            }
            //start logcat *:W with file rotation
            String targetLogcatFile = sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat/"+"LC."+System.currentTimeMillis()+".txt";
            String cmd_logcatstart = "logcat -f " +targetLogcatFile+" -v time *:W";
            Runtime.getRuntime().exec(cmd_logcatstart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
