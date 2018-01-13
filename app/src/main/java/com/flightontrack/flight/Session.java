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
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session implements EventBus{
    public enum SESSIONREQUEST{
        SEND_STORED_LOCATIONS,
        START_COMMUNICATION,
        STOP_CLOCK,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
    }
    private static Session sessionInstance = null;
    public static Session getInstance() {
        if(sessionInstance == null) {
            sessionInstance = new Session();
        }
        return sessionInstance;
    }
    static final String TAG = "Session:";
//    static void set_SessionRequest(SESSIONREQUEST request) {
//        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
//        switch (request) {
//            case STOP_CLOCK:
//
//                break;
//
//            case CLOSEAPP_BUTTON_BACK_PRESSED_WITH_CACHE_CHECK:
//                if (dbLocationRecCount > 0) {
//                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
//                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
//                } else {
//                    set_SessionRequest(SESSIONREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK);
//                }
//                break;
//            case CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK:
//                if (!(Route.activeRoute ==null)) Route.activeRoute.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE_TIMER_CLOCKONLY);
//                mainactivityInstance.finishActivity();
//            break;
////            case BUTTON_STOP_PRESSED:
////                if (dbLocationRecCount > 0) {
////                    set_SessionRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
////                }
////                Route.activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
////                break;
//            case SEND_STORED_LOCATIONS:
//                //sendStoredLocations();
//                break;
////            case ON_COMMUNICATION_SUCCESS:
////                break;
//            case START_COMMUNICATION:
//                for (Route r : Route.routeList) {
//                    r.set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
//                }
//                if (Util.isNetworkAvailable()) {
//                    if (dbLocationRecCount > 0) {
//                        startLocationCommService();
//                    }
//
////                    if (dbTempFlightRecCount > 0) {
////                        for (Route r : Route.routeList) {
////                            for (Flight f:r.flightList){
////                                f.set_fAction(F_ACTION.REQUEST_FLIGHTNUMBER);
////                            }
////                        }
////                    }
//                } else {
//                    FontLog.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
//                    Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }

    public static void initProp(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mainactivityInstance = maInstance;
        sqlHelper = new SQLHelper();
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

//    static Flight get_FlightInstance(String flightNumber){
//        for (Route r : Route.routeList) {
//            for (Flight f : r.flightList) {
//                if (f.flightNumber.equals(flightNumber)) {
//                    return f;
//                }
//            }
//        }
//        return Route.activeRoute.activeFlight;
//    }

    static void sendStoredLocations(){
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
            set_InternalRequest(SESSIONREQUEST.START_COMMUNICATION);
        }
    }
    static void set_InternalRequest(SESSIONREQUEST request) {
        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
        switch (request) {
//            case STOP_CLOCK:
//                break;
            case CHECK_CACHE_FIRST:
                if (dbLocationRecCount > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
                } else {
                    set_InternalRequest(SESSIONREQUEST.CLOSEAPP_NO_CACHE_CHECK);
                }
                break;
            case CLOSEAPP_NO_CACHE_CHECK:
                //if (!(Route.activeRoute ==null)) Route.activeRoute.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE_TIMER_CLOCKONLY);
                mainactivityInstance.finishActivity();
                break;
//            case BUTTON_STOP_PRESSED:
//                if (dbLocationRecCount > 0) {
//                    set_InternalRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
//                }
//                // REPLACED Route.activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
//                break;
            case SEND_STORED_LOCATIONS:
                //sendStoredLocations(); //restore it back
                break;
//            case ON_COMMUNICATION_SUCCESS:
//                break;
            case START_COMMUNICATION:
//                for (Route r : Route.routeList) {
//                    r.set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
//                }
                if (Util.isNetworkAvailable()) {
                    if (dbLocationRecCount > 0) {
                        startLocationCommService();
                    }

//                    if (dbTempFlightRecCount > 0) {
//                        for (Route r : Route.routeList) {
//                            for (Flight f:r.flightList){
//                                f.set_fAction(F_ACTION.REQUEST_FLIGHTNUMBER);
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

    @Override
    public void eventReceiver(EventMessage eventMessage){
        FontLog.appendLog(TAG + " eventReceiver Interface is called on MainActivity", 'd');
        EVENT ev = eventMessage.event;
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_InternalRequest(SESSIONREQUEST.CHECK_CACHE_FIRST);
                break;
            case CLOCK_ONTICK:
                set_InternalRequest(SESSIONREQUEST.START_COMMUNICATION);
                break;
            case ALERT_SENTPOINTS:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_InternalRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_InternalRequest(SESSIONREQUEST.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case ALERT_STOPAPP:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_InternalRequest(SESSIONREQUEST.CLOSEAPP_NO_CACHE_CHECK);
                break;

        }
    }
    //session does react to events. it never initiate events

}
