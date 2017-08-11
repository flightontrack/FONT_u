package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

import com.flightontrack.communication.SvcComm;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;

import java.util.ArrayList;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session {
    private static final String TAG = "Session:";
    public static MainActivity mainactivityInstance;
    public static Route activeRoute;
    public static SQLHelper sqlHelper;

    public static int dbLocationRecCount = 0;
    public static BUTTONREQUEST trackingButtonState = BUTTONREQUEST.BUTTON_STATE_RED;
    public static ArrayList<Route> routeList = new ArrayList<>();

    public Session(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SessionProp.pMinSpeedArray = ctx.getResources().getStringArray(R.array.speed_array);
        mainactivityInstance = maInstance;
        sqlHelper = new SQLHelper();

    }

    public static void set_SessionRequest(SESSIONREQUEST request) {
        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
        switch (request) {
            case CLOSEAPP_BUTTON_BACK_PRESSED:
                if (dbLocationRecCount > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
                } else {
                    if (!(activeRoute ==null)) activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_FLIGHT_DELETE_ALL_POINTS);
                    mainactivityInstance.finishActivity();
                }
                break;
            case ON_COMMUNICATION_SUCCESS:
                break;
            case START_COMMUNICATION:
                for (Route r : routeList) {
                    r.set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
                }
                if (Util.isNetworkAvailable()) {
                    if (dbLocationRecCount > 0) {
                        startLocationCommService();
                    }
                } else {
                    FontLog.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
                    Toast.makeText(ctxApp, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
                MainActivity.trackingButton.setText("Flight " + (activeRoute.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff));
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
        String flightId;

        if (activeRoute == null) {
            FontLog.appendLog(TAG + " setTextRed: flightId IS NULL", 'd');
        } else {
            if (activeRoute!=null && activeRoute.activeFlight!=null) {
                flightId = activeRoute.activeFlight.flightNumber;
                fTime = activeRoute.activeFlight.flightTimeString.equals(FLIGHT_TIME_ZERO) ? ctxApp.getString(R.string.time) + SPACE + Util.getTimeLocal() : ctxApp.getString(R.string.tracking_flight_time) + SPACE + activeRoute.activeFlight.flightTimeString;
            }
            else {flightId = FLIGHT_NUMBER_DEFAULT;}
            fid = "Flight " + flightId + '\n' + "Stopped"; // + '\n';
        }
        SessionProp.pTextRed = fid + fTime;
        return SessionProp.pTextRed;
    }

    private static String setTextGreen() {
        SessionProp.pTextGreen = "Flight: " + (activeRoute.activeFlight.flightNumber) + '\n' +
                "Point: " + activeRoute.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + activeRoute.activeFlight.flightTimeString + '\n'
                + "Alt: " + activeRoute.activeFlight.lastAltitudeFt + " ft";
        return SessionProp.pTextGreen;
    }

    private static void startLocationCommService() {

        sqlHelper.setCursorDataLocation();
        int count = sqlHelper.getCursorCountLocation();
        //Util.appendLog(TAG+ "getCursorCountLocation :" + count,'d');

        FontLog.appendLog(TAG + "SvcComm.commBatchSize :" + SvcComm.commBatchSize, 'd');
        if (count >= 1) {
            for (int i = 0; i < count; i++) {
                if (i >= SvcComm.commBatchSize) break;
                Intent intentComm = new Intent(ctxApp, SvcComm.class);
                //Intent intentComm = new Intent(context, SvcIntentComm.class);
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", sqlHelper.cl.getLong(sqlHelper.cl.getColumnIndexOrThrow(DBSchema._ID)));
                bundle.putInt("rc", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1)));
                bundle.putString("ft", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL2)));
                bundle.putBoolean("sl", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL3)) == 1);
                bundle.putString("sd", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4)));
                bundle.putString("la", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6)));
                bundle.putString("lo", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7)));
                bundle.putString("ac", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8)));
                bundle.putString("al", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9)));
                bundle.putInt("wp", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL10)));
                bundle.putString("sg", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11)));
                bundle.putString("dt", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL12)));
                bundle.putBoolean("irch", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL13)) == 1);

                intentComm.putExtras(bundle);
                //Log.d(TAG, "FlightRouterThread:" + Thread.currentThread().getId());
                ctxApp.startService(intentComm);
                sqlHelper.cl.moveToNext();
            }
            sqlHelper.cl.close();
            //if(!alarmDisable)
        }
    }

    public static Flight get_FlightInstance(String flightNumber){
        for (Route r : routeList) {
            for (Flight f : r.flightList) {
                if (f.flightNumber.equals(flightNumber)) {
                    return f;
                }
            }
        }
        return activeRoute.activeFlight;
    }

}
