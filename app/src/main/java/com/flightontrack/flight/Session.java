package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

import com.flightontrack.communication.SvcComm;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;


/**
 * Created by hotvk on 7/6/2017.
 */

public interface Session{
    static final String TAG = "Session:";
    default void set_SessionRequest(SESSIONREQUEST request) {
        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
        switch (request) {
            case STOP_CLOCK:

                break;

            case CLOSEAPP_BUTTON_BACK_PRESSED_WITH_CACHE_CHECK:
                if (dbLocationRecCount > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
                } else {
                    set_SessionRequest(SESSIONREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK);
                }
                break;
                case CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK:
                    if (!(Route.activeRoute ==null)) Route.activeRoute.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE_TIMER_CLOCKONLY);
                    mainactivityInstance.finishActivity();
                break;
            case BUTTON_STOP_PRESSED:
                if (dbLocationRecCount > 0) {
                    set_SessionRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
                }
                Route.activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                break;
            case SEND_STORED_LOCATIONS:
                sendStoredLocations();
                break;
            case ON_COMMUNICATION_SUCCESS:
                break;
            case START_COMMUNICATION:
                for (Route r : Route.routeList) {
                    r.set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
                }
                if (Util.isNetworkAvailable()) {
                    if (dbLocationRecCount > 0) {
                        startLocationCommService();
                    }

//                    if (dbTempFlightRecCount > 0) {
//                        for (Route r : Route.routeList) {
//                            for (Flight f:r.flightList){
//                                f.set_flightRequest(FLIGHTREQUEST.REQUEST_FLIGHTNUMBER);
//                            }
//                        }
//                    }
                } else {
                    FontLog.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
                    Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    default void initProp(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mainactivityInstance = maInstance;
        sqlHelper = new SQLHelper();
    }

    default void setTrackingButtonState(BUTTONREQUEST request) {
        //Util.appendLog(TAG+"trackingButtonState request:" +request,'d');
        switch (request) {
            case BUTTON_STATE_RED:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(setTextRed());
                break;
            case BUTTON_STATE_YELLOW:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                MainActivity.trackingButton.setText("Flight " + (Route.activeRoute.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff));
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

    static String setTextRed() {
        String fid = SessionProp.pTextRed;
        String fTime = "";
        String flightId;

        if (Route.activeRoute == null) {
            FontLog.appendLog(TAG + " setTextRed: flightId IS NULL", 'd');
        } else {
            if (Route.activeRoute!=null && Route.activeRoute.activeFlight!=null) {
                flightId = Route.activeRoute.activeFlight.flightNumber;
                //fTime = Route.activeRoute.activeFlight.flightTimeString.equals(FLIGHT_TIME_ZERO) ? ctxApp.getString(R.string.time) + SPACE + GetTime.getTimeLocal() : ctxApp.getString(R.string.tracking_flight_time) + SPACE + Route.activeRoute.activeFlight.flightTimeString;
                fTime = ctxApp.getString(R.string.tracking_flight_time) + SPACE + Route.activeRoute.activeFlight.flightTimeString;
            }
            else {flightId = FLIGHT_NUMBER_DEFAULT;}
            fid = "Flight " + flightId + '\n' + "Stopped"; // + '\n';
        }
        SessionProp.pTextRed = fid + fTime;
        return SessionProp.pTextRed;
    }

    static String setTextGreen() {
        SessionProp.pTextGreen = "Flight: " + (Route.activeRoute.activeFlight.flightNumber) + '\n' +
                "Point: " + Route.activeRoute.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + Route.activeRoute.activeFlight.flightTimeString + '\n'
                + "Alt: " + Route.activeRoute.activeFlight.lastAltitudeFt + " ft";
        return SessionProp.pTextGreen;
    }

    static void startLocationCommService() {

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
                bundle.putString("ft", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
                bundle.putBoolean("sl", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.LOC_speedlowflag)) == 1);
                bundle.putString("sd", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4)));
                bundle.putString("la", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6)));
                bundle.putString("lo", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7)));
                bundle.putString("ac", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8)));
                bundle.putString("al", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9)));
                bundle.putInt("wp", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.LOC_wpntnum)));
                bundle.putString("sg", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11)));
                bundle.putString("dt", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.LOC_date)));
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

    default Flight get_FlightInstance(String flightNumber){
        for (Route r : Route.routeList) {
            for (Flight f : r.flightList) {
                if (f.flightNumber.equals(flightNumber)) {
                    return f;
                }
            }
        }
        return Route.activeRoute.activeFlight;
    }

    default void sendStoredLocations(){
        int MaxTryCount = 5;
        SvcComm.commBatchSize= dbLocationRecCount;
        int counter =0;
        while (dbLocationRecCount>0){
            if (counter >MaxTryCount) {
                Toast.makeText(mainactivityInstance, R.string.unsentrecords_failed, Toast.LENGTH_SHORT).show();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            counter++;
            Toast.makeText(mainactivityInstance, R.string.unsentrecords_toast, Toast.LENGTH_SHORT).show();
            set_SessionRequest(SESSIONREQUEST.START_COMMUNICATION);
        }
    }
    static void set_InternalRequest(SESSIONREQUEST request) {
        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
        switch (request) {
            case STOP_CLOCK:

                break;

            case CLOSEAPP_BUTTON_BACK_PRESSED_WITH_CACHE_CHECK:
                if (dbLocationRecCount > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
                } else {
                    set_InternalRequest(SESSIONREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK);
                }
                break;
            case CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK:
                if (!(Route.activeRoute ==null)) Route.activeRoute.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE_TIMER_CLOCKONLY);
                mainactivityInstance.finishActivity();
                break;
            case BUTTON_STOP_PRESSED:
                if (dbLocationRecCount > 0) {
                    set_InternalRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
                }
                // REPLACED Route.activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                break;
            case SEND_STORED_LOCATIONS:
                ////   sendStoredLocations(); restore it back
                break;
            case ON_COMMUNICATION_SUCCESS:
                break;
            case START_COMMUNICATION:
                for (Route r : Route.routeList) {
                    r.set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
                }
                if (Util.isNetworkAvailable()) {
                    if (dbLocationRecCount > 0) {
                        startLocationCommService();
                    }

//                    if (dbTempFlightRecCount > 0) {
//                        for (Route r : Route.routeList) {
//                            for (Flight f:r.flightList){
//                                f.set_flightRequest(FLIGHTREQUEST.REQUEST_FLIGHTNUMBER);
//                            }
//                        }
//                    }
                } else {
                    FontLog.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
                    Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public static void eventReceiver(EVENT event) {
        switch (event) {
            case MACT_BIGBUTTON_CLICKED_STOP:
                set_InternalRequest(SESSIONREQUEST.BUTTON_STOP_PRESSED);
                break;
        }
    }
    //session does react to events. it never initiate events

    //events:
    // button back pressed

    // on clock:

    // check location cache
    // start normal communication service to send location
    // request for all routew to check  if any passive flight need to be closed
    //

    // on special request:

    // check cache of unsent location
    // start communication service if cache is not empty


}
