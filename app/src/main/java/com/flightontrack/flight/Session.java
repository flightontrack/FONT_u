package com.flightontrack.flight;

import android.content.Context;
import android.content.SharedPreferences;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import static com.flightontrack.shared.Const.*;
import com.flightontrack.shared.Util;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session {
    private static final String TAG = "Session:";
    public static Context ctxApp;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public static MainActivity mainactivityInstance;
    public static Route routeInstance;
    public static int dbLocationRecCount = 0;
    public static BUTTONREQUEST trackingButtonState = BUTTONREQUEST.BUTTON_STATE_RED;

    public Session(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences =ctxApp.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        mainactivityInstance = maInstance;
    }
    public static void setTrackingButtonState(BUTTONREQUEST request) {
        //Util.appendLog(TAG+"trackingButtonState request:" +request,'d');
        switch (request) {
            case BUTTON_STATE_RED:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(setTextRed());
                break;
            case BUTTON_STATE_YELLOW:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                MainActivity.trackingButton.setText("Flight " + (routeInstance.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff));
                //editor.putInt("trackingButtonState", BUTTON_STATE_YELLOW);
                break;
            case BUTTON_STATE_GREEN:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_green);
                MainActivity.trackingButton.setText(setTextGreen());
                //editor.putInt("trackingButtonState", BUTTON_STATE_GREEN);
                break;
            case BUTTON_STATE_GETFLIGHTID:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(ctxApp.getString(R.string.tracking_gettingflight));
                break;
            case BUTTON_STATE_STOPPING:
                //appendLog(LOGTAG+"BUTTON_STATE_STOPPING");
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                //MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_stopping));
                break;
            default:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                // MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_is_off));
        }
        trackingButtonState = request;
    }

    private static String setTextRed() {
        String fid = SessionProp.pTextRed;
        String fTime = "";

        if (routeInstance == null) {
            Util.appendLog(TAG + " setTextRed: flightId IS NULL", 'd');
        } else {
            String flightId = routeInstance.activeFlight.flightNumber;
            fid = "Flight " + flightId + '\n' + "Stopped"; // + '\n';
            fTime = routeInstance.activeFlight.flightTimeString.equals(FLIGHT_TIME_ZERO) ? ctxApp.getString(R.string.time) + SPACE + Util.getTimeLocal() : ctxApp.getString(R.string.tracking_flight_time) + SPACE + routeInstance.activeFlight.flightTimeString;
        }
        SessionProp.pTextRed = fid + fTime;
        return SessionProp.pTextRed;
    }

    private static String setTextGreen() {
        SessionProp.pTextGreen = "Flight: " + (routeInstance.activeFlight.flightNumber) + '\n' +
                "Point: " + routeInstance.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + routeInstance.activeFlight.flightTimeString + '\n'
                + "Alt: " + routeInstance.activeFlight.lastAltitudeFt + " ft";
        return SessionProp.pTextGreen;
    }

    public static class SessionProp {
        static String pTextRed;
        static String pTextGreen;

        public static void save() {
            editor.putString("pTextRed", pTextRed);
            editor.commit();
        }

        public static void get() {
            pTextRed = sharedPreferences.getString("pTextRed", ctxApp.getString(R.string.start_flight));
        }

        public static void clear() {
            editor.remove("pTextRed").commit();
        }
    }
}
