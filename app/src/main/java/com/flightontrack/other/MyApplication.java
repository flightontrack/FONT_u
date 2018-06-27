package com.flightontrack.other;

import android.app.Application;
import android.os.Environment;
//f import com.facebook.FacebookSdk;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import java.io.File;
import java.io.IOException;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.AppConfig.pIsRelease;
//import com.facebook.appevents.AppEventsLogger;

//import org.acra.*;
//import org.acra.annotation.*;


//@ReportsCrashes(
//        //formKey = "", // will not be used
//        mailTo = "support@flightontrack.com",
//        customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text,
//        forceCloseDialogAfterToast = true,
//        logcatArguments = { "-t", "100", "-v", "long", "ActivityManager:I", "FLIGHT_ON_TRACK:V", "*:S" }
//)

//@ReportsCrashes(//formKey = "", // will not be used
//        formUri = "http://postnget-test.azurewebsites.net/C09r12a649sH8767.aspx",
//        customReportContent = {ReportField.REPORT_ID,
//                ReportField.USER_CRASH_DATE,
//                ReportField.USER_EMAIL,
//                ReportField.APP_VERSION_CODE,
//                ReportField.APP_VERSION_NAME,
//                ReportField.ANDROID_VERSION,
//                ReportField.PHONE_MODEL,
//                ReportField.CUSTOM_DATA,
//                ReportField.SHARED_PREFERENCES,
//                ReportField.STACK_TRACE,
//                ReportField.LOGCAT },
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text,
//        logcatArguments = { "-t", "100", "-v", "long", "FLIGHT_ON_TRACK:V", "*:W" })
public class MyApplication extends Application {
    private static Thread.UncaughtExceptionHandler defaultHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        if (pIsRelease) return;
        if (defaultHandler == null) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        }
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler ((thread, e) -> {
            e.printStackTrace();
            startLogcat();
            Crashlytics.logException(e);
            defaultHandler.uncaughtException(thread, e); //this will show crash dialog.
            System.exit(1); // kill off the crashed app
        });
    }
    public static void startLogcat() {
        //if (productionRelease) return;
        int pid= android.os.Process.myPid();
        try {
            //clean logcat first
            String cmd_clean = "logcat -c";
            Runtime.getRuntime().exec(cmd_clean);
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat");
            //create a dir if not exist
            if (!dir.exists()) {
                dir.mkdir();
            }
            //start logcat *:W with file rotation
            String targetLogcatFile = sdcard.getAbsolutePath() + "/FONT_LogFiles/Logcat/"+"LogcatWE_"+pid+".txt";
            String cmd_logcatstart = "logcat -f " +targetLogcatFile+" -r 100 -n 10 -v threadtime *:W";
            //"logcat -d -v time"
            //getRuntime().exec("logcat >> /sdcard/logcat.log");
            Runtime.getRuntime().exec(cmd_logcatstart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}