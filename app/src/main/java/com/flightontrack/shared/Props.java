package com.flightontrack.shared;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.flight.Session;

import static com.flightontrack.flight.Session.*;
import static com.flightontrack.shared.Const.*;

/**
 * Created by hotvk on 8/1/2017.
 */

public class Props {
    public static class AppProp{
        public static boolean pPublicApp = false;
        public static boolean autostart = false;

        //static String       pUserName;

        public static void save(){
            editor.commit();
        }
        public static void get(){
            //Util.appendLog(TAG + "Restore Properties", 'd');
            //pIsDebug=sharedPreferences.getBoolean("pIsDebug", false);
            //pUserName =  sharedPreferences.getString("pUserName", new Pilot().getMyUserName());
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

        public static void save() {
            editor.putBoolean("pIsMultileg", pIsMultileg);
            editor.putInt("pIntervalLocationUpdateSec", pIntervalLocationUpdateSec);
            editor.putInt("pIntervalSelectedItem", pIntervalSelectedItem);
            editor.putInt("pSpinnerMinSpeedPos", pSpinnerMinSpeedPos);
            //editor.putInt("pSpinnerMinSpeed", pSpinnerMinSpeed);
            editor.putBoolean("pIsEmptyAcftOk", pIsEmptyAcftOk);
            editor.putInt("pSpinnerUrlsPos", pSpinnerUrlsPos);
            editor.putString("pTextRed", pTextRed);
            editor.commit();
        }

        public static void get() {
            set_isMultileg( sharedPreferences.getBoolean("pIsMultileg", true));
            pIsEmptyAcftOk=sharedPreferences.getBoolean("pIsEmptyAcftOk", false);
            pIntervalLocationUpdateSec=sharedPreferences.getInt("pIntervalLocationUpdateSec", MIN_TIME_BW_GPS_UPDATES_SEC);
            pIntervalSelectedItem=sharedPreferences.getInt("pIntervalSelectedItem", DEFAULT_INTERVAL_SELECTED_ITEM);
            pSpinnerUrlsPos=sharedPreferences.getInt("pSpinnerUrlsPos", DEFAULT_URL_SPINNER_POS);
            pSpinnerUrlsPos=sharedPreferences.getInt("pSpinnerMinSpeedPos", DEFAULT_SPEED_SPINNER_POS);
            //pSpinnerUrlsPos=sharedPreferences.getInt("pSpinnerMinSpeed", DEFAULT_SPEED_SPINNER_POS);
            pTextRed = sharedPreferences.getString("pTextRed", Session.ctxApp.getString(R.string.start_flight));
        }

        public static void clear() {
            editor.remove("pIsMultileg").commit();
            editor.remove("pIntervalLocationUpdateSec").commit();
            editor.remove("pIntervalSelectedItem").commit();
            editor.remove("pIsEmptyAcftOk").commit();
            editor.remove("pSpinnerUrlsPos").commit();
            editor.remove("pSpinnerMinSpeedPos").commit();
            editor.remove("pTextRed").commit();

        }

        public static void set_isMultileg(boolean isMultileg) {
            pIsMultileg=isMultileg;
            MainActivity.chBoxIsMultiLeg.setChecked(isMultileg);
        }
        public static void set_pSpinnerMinSpeedPos(int pos) {
            pSpinnerMinSpeedPos = pos;
            pSpinnerMinSpeed = Double.parseDouble(pMinSpeedArray[pos]) * 0.44704;

        }
        public static void resetSessionProp() {
            clear();
            get();
            //save();
        }
    }
}
