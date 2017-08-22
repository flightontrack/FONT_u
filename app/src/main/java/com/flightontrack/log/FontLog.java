package com.flightontrack.log;

import android.os.Environment;
import android.util.Log;

import com.flightontrack.flight.Route;
import com.flightontrack.shared.GetTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

/**
 * Created by hotvk on 8/3/2017.
 */

public class FontLog{

    private static final String TAG = "FontLog:";

    private static String getDateTimeNow() {
        /// this method id identical to GetTime default method
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }

    public static void appendLog(String text,char type) {

        switch (type) {
            case 'd': Log.d(GLOBALTAG, text);
                break;
            case 'e': Log.e(GLOBALTAG,text);
                //startLogcat("appendLog"); TODO need to check permission first
                break;
        }

        //try {
            if (!SessionProp.pIsDebug) return;
            //if (getIsDebug()) return; //TODO disabled to check permissions
            //String timeStr= (new Flight(ctx).get_ActiveFlightID())+"*"+time.format("%H:%M:%S")+"*";
            //String timeStr = Flight.get_ActiveFlightID() + "*" + getDateTimeNow() + "*";
            String timeStr = (Route.activeRoute !=null&&!(null== Route.activeRoute.activeFlight)? Route.activeRoute.activeFlight.flightNumber :FLIGHT_NUMBER_DEFAULT) + "[" + getDateTimeNow() + "]";
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
            File logFile = new File(dir, "FONT_LogFile.txt");
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(timeStr + text + LINE_SEPARATOR);
                buf.close();
            }
            catch (IOException e) {
                Log.e(TAG, "AppendLog IO "+e);
                e.printStackTrace();
                startLogcat("appendLogIOException");
                return;
            }
        //}
//        catch (Exception e){
//            Log.e(TAG, "AppendLog Exception: probable cause TBD2");
//            e.printStackTrace();
//            startLogcat("appendLogException");
//        }
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
