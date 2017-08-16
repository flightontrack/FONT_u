package com.flightontrack.shared;

import android.content.Context;
import android.content.SharedPreferences;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.flight.Route;
import com.flightontrack.mysql.SQLHelper;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.AppProp.*;

/**
 * Created by hotvk on 8/1/2017.
 */

public class Props {
    public static Context ctxApp;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static class AppProp{
        public static boolean pIsAppTypePublic=true;
        /// if false:   1. start healthcheckalarmreceiver
            ///             2. aicraft activity layout has no nfc
            ///             3. autostart (request flight) is true
            ///             4. app starts on reboot
        public static boolean pAutostart=!pIsAppTypePublic;
        public static boolean pIsNFCEnabled =false;
        public static String pAppRelease = "1.73";
        public static String pAppReleaseSuffix = pIsAppTypePublic?"p":"c";

        /// these properties updated dynamically in run time
        public static String pMainActivityLayout = "full";
        public static boolean pIsNFCcapable=false;
        public static boolean pIsOnRebootCheckBoxEnabled=false;

        public static void get(){
            //pIsAppTypePublic = false;
            //pAutostart = false;
            //pIsNFCEnabled = false;
            pIsNFCcapable = false;
        }
    }

    public static class SessionProp {
        public static boolean       pIsMultileg;
        public static int          pIntervalLocationUpdateSec;
        public static int          pIntervalSelectedItem;
        public static boolean      pIsEmptyAcftOk;
        public static int          pSpinnerUrlsPos;
        public static int          pSpinnerMinSpeedPos;
        public static double       pSpinnerMinSpeed;
        public static boolean      pIsRoad = false;
        public static boolean      pIsDebug = false;
        public static String        pTextRed;
        public static String        pTextGreen;
        public static String[]      pMinSpeedArray;
        public static int[]        pUpdateIntervalSec= {3, 5, 10, 15, 20, 30, 60, 120, 300, 600, 900, 1800};
        public static boolean       pIsOnReboot=!pIsAppTypePublic;

        public static MainActivity mainactivityInstance;
        public static Route activeRoute;
        public static SQLHelper sqlHelper;

        public static void save() {
            editor.putBoolean("pIsMultileg", pIsMultileg);
            //editor.putInt("pIntervalLocationUpdateSec", pIntervalLocationUpdateSec);
            editor.putInt("pIntervalSelectedItem", pIntervalSelectedItem);
            editor.putInt("pSpinnerMinSpeedPos", pSpinnerMinSpeedPos);
            //editor.putInt("pSpinnerMinSpeed", pSpinnerMinSpeed);
            editor.putBoolean("pIsEmptyAcftOk", pIsEmptyAcftOk);
            editor.putInt("pSpinnerUrlsPos", pSpinnerUrlsPos);
            editor.putString("pTextRed", pTextRed);
            editor.putBoolean("pIsOnReboot", pIsOnReboot);
            editor.commit();
        }

        public static void get() {
            set_isMultileg( sharedPreferences.getBoolean("pIsMultileg", true));
            set_pSpinnerMinSpeedPos(sharedPreferences.getInt("pSpinnerMinSpeedPos", DEFAULT_SPEED_SPINNER_POS));
            set_pIntervalLocationUpdateSecPos(sharedPreferences.getInt("pIntervalSelectedItem", DEFAULT_INTERVAL_SELECTED_ITEM));
            pIsEmptyAcftOk=sharedPreferences.getBoolean("pIsEmptyAcftOk", false);
            //pIntervalLocationUpdateSec=sharedPreferences.getInt("pIntervalLocationUpdateSec", MIN_TIME_BW_GPS_UPDATES_SEC);
            pSpinnerUrlsPos=sharedPreferences.getInt("pSpinnerUrlsPos", DEFAULT_URL_SPINNER_POS);
            //pSpinnerUrlsPos=sharedPreferences.getInt("pSpinnerMinSpeed", DEFAULT_SPEED_SPINNER_POS);
            pTextRed = sharedPreferences.getString("pTextRed", ctxApp.getString(R.string.start_flight));
            pIsOnReboot=sharedPreferences.getBoolean("pIsOnReboot", false);
        }

        public static void set_isMultileg(boolean isMultileg) {
            pIsMultileg=isMultileg;
            MainActivity.chBoxIsMultiLeg.setChecked(isMultileg);
        }
        public static void set_pSpinnerMinSpeedPos(int pos) {
            pSpinnerMinSpeedPos = pos;
            pSpinnerMinSpeed = Double.parseDouble(pMinSpeedArray[pos]) * 0.44704;
            MainActivity.spinnerMinSpeed.setSelection(pos);

        }
        public static void set_pIntervalLocationUpdateSecPos(int pos) {
            pIntervalSelectedItem =pos;
            pIntervalLocationUpdateSec =pUpdateIntervalSec[pos];
            MainActivity.spinnerUpdFreq.setSelection(pos);
        }

        public static void clearOnDestroy() {
            editor.remove("pIsMultileg").commit();
            editor.remove("pIsEmptyAcftOk").commit();
            editor.remove("pTextRed").commit();
            pIsRoad = false;
            pIsDebug = false;
        }
        public static void clearToDefault() {
            editor.remove("pIsMultileg").commit();
            editor.remove("pIntervalLocationUpdateSec").commit();
            editor.remove("pIntervalSelectedItem").commit();
            editor.remove("pIsEmptyAcftOk").commit();
            editor.remove("pSpinnerUrlsPos").commit();
            editor.remove("pSpinnerMinSpeedPos").commit();
            editor.remove("pTextRed").commit();
            pIsRoad = false;
            pIsDebug = false;
        }
        public static void resetSessionProp() {
            clearToDefault();
            get();
            //save();
        }

    }
    public static void clearAll() {
        //Toast.makeText(SessionProp.ctxApp, R.string.user_needs_to_restart_app, Toast.LENGTH_LONG).show();
        sharedPreferences.edit().clear().commit();
    }
}
