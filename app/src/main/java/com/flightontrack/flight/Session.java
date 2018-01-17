package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArraySet;
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

import java.util.ArrayList;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session implements EventBus{
    public enum SACTION {
        SEND_STORED_LOCATIONS,
        START_COMMUNICATION,
        STOP_CLOCK,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS
    }
    private static Session sessionInstance = null;
    public static Session getInstance() {
        if(sessionInstance == null) {
            sessionInstance = new Session();
        }
        return sessionInstance;
    }
    static final String TAG = "Session:";

    public static void initProp(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mainactivityInstance = maInstance;
        sqlHelper = new SQLHelper();
    }
    static ArrayList<FlightOffline> flightOfflineList = new ArrayList<>();

    static void startLocationCommService() {

        sqlHelper.setCursorDataLocation();
//        int count = sqlHelper.getCursorCountLocation();

        FontLog.appendLog(TAG + "SvcComm.commBatchSize :" + SvcComm.commBatchSize, 'd');
        if (dbLocationRecCountNormal >= 1) {
            for (int i = 0; i < dbLocationRecCountNormal; i++) {
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
        SvcComm.commBatchSize= dbLocationRecCountNormal;
        int counter =0;
        while (dbLocationRecCountNormal >0){
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
            set_sAction(SACTION.START_COMMUNICATION);
        }

    }
    static void set_sAction(SACTION request) {
        FontLog.appendLog(TAG + "set_SessionRequest:" + request, 'd');
        switch (request) {
            case CHECK_CACHE_FIRST:
                if (dbLocationRecCountNormal > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCountNormal);
                    FontLog.appendLog(TAG + " PointsUnsent: " + dbLocationRecCountNormal, 'd');
                } else {
                    set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                }
                break;
            case CLOSEAPP_NO_CACHE_CHECK:
                mainactivityInstance.finishActivity();
                break;
//            case BUTTON_STOP_PRESSED:
//                if (dbLocationRecCountNormal > 0) {
//                    set_sAction(SACTION.SEND_STORED_LOCATIONS);
//                }
//                // REPLACED Route.activeRoute.set_rAction(RACTION.CLOSE_BUTTON_STOP_PRESSED);
//                break;
            case SEND_STORED_LOCATIONS:
                sendStoredLocations();
                for(FlightOffline f: new ArrayList<>(flightOfflineList)) {
                    if (f.getLocationFlightCount() == 0){
                        f.getCloseFlight();
                        flightOfflineList.remove(f);
                    }
                }
                break;
            case START_COMMUNICATION:
//                    if (dbTempFlightRecCount > 0) {
//                        for (Route r : Route.routeList) {
//                            for (Flight f:r.flightList){
//                                f.set_fAction(FACTION.REQUEST_FLIGHTNUMBER);
//                            }
//                        }
//                    }
                //FontLog.appendLog(TAG + "dbLocationRecCountNormal:"+ dbLocationRecCountNormal, 'd');
                if (dbLocationRecCountNormal > 0) {
                    if (Util.isNetworkAvailable()) {
                        startLocationCommService();
                    } else {
                        FontLog.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
                        Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case GET_OFFLINE_FLIGHTS:
                sqlHelper.setCursorTempFlights();
                for (int i = 0; i < sqlHelper.ctf.getCount(); i++) {
                    new FlightOffline(sqlHelper.ctf.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.FLIGHTNUM_FlightNumber)));
                }
                break;

        }
    }

    @Override
    public void eventReceiver(EventMessage eventMessage){
        //Array eventReaction[EVENT];
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + ":eventReceiver:"+ev, 'd');
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_sAction(SACTION.CHECK_CACHE_FIRST);
                break;
            case CLOCK_ONTICK:
                set_sAction(SACTION.START_COMMUNICATION);
                break;
            case ALERT_SENTPOINTS:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.SEND_STORED_LOCATIONS);
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case ALERT_STOPAPP:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                set_sAction(SACTION.GET_OFFLINE_FLIGHTS);
                break;
            case FLIGHT_OFFLINE_DBUPDATE_COMPLETED:
                flightOfflineList.add((FlightOffline) eventMessage.eventMessageValueObject);
                set_sAction(SACTION.SEND_STORED_LOCATIONS);
                break;
        }
    }
    //session does react to events. it never initiate events

}
