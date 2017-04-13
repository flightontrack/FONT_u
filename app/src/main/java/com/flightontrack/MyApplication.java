package com.flightontrack;

import android.app.Application;
//f import com.facebook.FacebookSdk;
import static com.flightontrack.Const.*;
//import com.facebook.appevents.AppEventsLogger;

//import org.acra.*;
//import org.acra.annotation.*;

import java.io.File;
import java.io.IOException;


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
    public static boolean productionRelease = false;
    //public static APPTYPE fontAppType = APPTYPE.PRIVATE;
    public static APPTYPE fontAppType = APPTYPE.PUBLIC;
    //public static boolean productionRelease = true;
    @Override
    public void onCreate() {
        super.onCreate();
        //f FacebookSdk.sdkInitialize(getApplicationContext());
        // The following line triggers the initialization of ACRA
//        ACRA.init(this);
//        ACRA.getErrorReporter().putCustomData("myKey", "myValue");
        //startLogcat();
    }
//    public static void startLogcat() {
//        if (productionRelease) return;
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
}