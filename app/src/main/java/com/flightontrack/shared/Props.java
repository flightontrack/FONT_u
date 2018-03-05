package com.flightontrack.shared;

import android.content.Context;
import android.content.SharedPreferences;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.flight.Flight;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.SQLHelper;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.AppConfig.*;


public final class Props implements EventBus{
    private static Props propsInstance = null;
    public static Props getInstance() {
        if(propsInstance == null) {
            propsInstance = new Props();
        }
        return propsInstance;
    }
    private static final String TAG = "Props:";
    public static Context ctxApp;
    public static MainActivity mainactivityInstance;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    public static class AppConfig {
        public static String pAppRelease = "1.75";
        public static boolean pIsNFCEnabled =false;
        public static boolean pIsAppTypePublic=true;
        /// if false:   1. start healthcheckalarmreceiver
            ///             2. aicraft activity layout has no nfc
            ///             3. autostart (request flight) is true
            ///             4. app starts on reboot
        public static boolean pAutostart=!pIsAppTypePublic&&SessionProp.pIsStartedOnReboot;
        public static String pAppReleaseSuffix = pIsAppTypePublic?"p":"c";

        /// these properties updated dynamically in run time
        public static String pMainActivityLayout = "full";
        public static boolean pIsNFCcapable=false;

        public static void get(){
            //pIsAppTypePublic = false;
            //pAutostart = false;
            //pIsNFCEnabled = false;
            pIsNFCcapable = false;
        }
    }

    public static class SessionProp implements EventBus{
        public static boolean       pIsMultileg;
        public static int          pIntervalLocationUpdateSec;
        public static int          pIntervalSelectedItem;
        public static boolean      pIsEmptyAcftOk;
        public static int          pSpinnerUrlsPos;
        public static int          pSpinnerTextToPos;
        public static int          pSpinnerMinSpeedPos;
        public static double       pSpinnerMinSpeed;
        public static boolean      pIsRoad = false;
        public static boolean      pIsDebug = false;
        //public static String[]      pMinSpeedArray=ctxApp.getResources().getStringArray(R.array.speed_array);
        public static int[]        pUpdateIntervalSec= {3, 5, 10, 15, 20, 30, 60, 120, 300, 600, 900, 1800};
        public static boolean       pIsOnReboot=!pIsAppTypePublic;
        public static boolean       pIsStartedOnReboot =false;

        public static SQLHelper sqlHelper;
        public static int dbLocationRecCountNormal = 0;
        //public static int dbLocationRecCountTemp = 0;
        public static int dbTempFlightRecCount = 0;
        public static BUTTONREQUEST trackingButtonState = BUTTONREQUEST.BUTTON_STATE_RED;
        public static String        pTextRed;
        public static String        pTextGreen;

        public static void save() {
            editor.putBoolean("pIsMultileg", pIsMultileg);
            //editor.putInt("pIntervalLocationUpdateSec", pIntervalLocationUpdateSec);
            editor.putInt("pIntervalSelectedItem", pIntervalSelectedItem);
            editor.putInt("pSpinnerMinSpeedPos", pSpinnerMinSpeedPos);
            //editor.putInt("pSpinnerMinSpeed", pSpinnerMinSpeed);
            editor.putBoolean("pIsEmptyAcftOk", pIsEmptyAcftOk);
            editor.putInt("pSpinnerUrlsPos", pSpinnerUrlsPos);
            editor.putInt("pSpinnerTextToPos", pSpinnerTextToPos);
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
            pSpinnerTextToPos=sharedPreferences.getInt("pSpinnerTextToPos", 0);
            pTextRed = sharedPreferences.getString("pTextRed", ctxApp.getString(R.string.start_flight));
            pIsOnReboot=sharedPreferences.getBoolean("pIsOnReboot", false);
            pIsStartedOnReboot =sharedPreferences.getBoolean("pIsStartedOnReboot", false);
        }

        public static void set_isMultileg(boolean isMultileg) {
            //String s = Arrays.toString(Thread.currentThread().getStackTrace());
            //FontLog.appendLog(TAG + "StackTrace: "+s,'d');
            pIsMultileg=isMultileg;
            EventBus.distribute(new EventMessage(EVENT.PROP_CHANGED_MULTILEG).setEventMessageValueBool(isMultileg));
            //MainActivity.chBoxIsMultiLeg.setChecked(isMultileg);
        }

        public static void set_pSpinnerMinSpeedPos(int pos) {
            pSpinnerMinSpeedPos = pos;
            String[] minSpeedArray=ctxApp.getResources().getStringArray(R.array.speed_array);
            pSpinnerMinSpeed = Double.parseDouble(minSpeedArray[pos]) * 0.44704;
            mainactivityInstance.spinnerMinSpeed.setSelection(pos);

        }
        public static void set_pIntervalLocationUpdateSecPos(int pos) {
            //FontLog.appendLog(TAG + "set_pIntervalLocationUpdateSecPos:"+pos,'d');
            pIntervalSelectedItem =pos;
            pIntervalLocationUpdateSec =pUpdateIntervalSec[pos];
            mainactivityInstance.spinnerUpdFreq.setSelection(pos);
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
            editor.remove("pSpinnerTextToPos").commit();
            editor.remove("pSpinnerMinSpeedPos").commit();
            editor.remove("pTextRed").commit();
            pIsRoad = false;
            pIsDebug = false;
        }
        public static void resetSessionProp() {
            clearToDefault();
            get();
        }
    }
    public static String getCurrAppContext() {
        return sharedPreferences.getString("a_currAppContext","0");
    }

    public static void setCurrAppContext(String appContext) {
        sharedPreferences.edit().putString("a_currAppContext",appContext).commit();
    }
    public static void clearAll() {
        //Toast.makeText(SessionProp.ctxApp, R.string.user_needs_to_restart_app, Toast.LENGTH_LONG).show();
        sharedPreferences.edit().clear().commit();
    }
    @Override
    public void eventReceiver(EventMessage eventMessage){
        FontLog.appendLog(TAG + " eventReceiver Interface is called on Props", 'd');
        EVENT ev = eventMessage.event;
        switch(ev){
            case MACT_MULTILEG_ONCLICK:
                SessionProp.set_isMultileg(eventMessage.eventMessageValueBool);
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                SessionProp.set_isMultileg(false);
                break;
            case SVCCOMM_ONSUCCESS_COMMAND:
                int server_command = eventMessage.eventMessageValueInt;
                switch (server_command) {
                    case COMMAND_TERMINATEFLIGHT:
                        SessionProp.set_isMultileg(false);
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        break;
                }
                break;
        }
    }


}
