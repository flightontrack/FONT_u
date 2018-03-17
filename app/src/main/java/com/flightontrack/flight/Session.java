package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.flightontrack.activity.MainActivity;

import static com.flightontrack.communication.SvcComm.commBatchSize;
import static com.flightontrack.flight.RouteBase.activeFlight;
import static com.flightontrack.flight.RouteBase.flightList;
import static com.flightontrack.flight.RouteBase.isFlightNumberInList;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

import com.flightontrack.communication.SvcComm;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.mysql.Location;
import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;

import java.util.ArrayList;
import java.util.EnumMap;
//import java.util.EnumMap;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session implements EventBus{
    public enum SACTION {
        SEND_CACHED_LOCATIONS,
        //START_COMMUNICATION,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS,
        CLOSE_FLIGHTS,
        LAST_CHANCE
    }

    static Session sessionInstance = null;
    static EnumMap<EVENT,SACTION> eventReaction = new EnumMap<>(EVENT.class);
    EVENT ev;
    EventMessage eventMessage;

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
//        eventReaction.put(MACT_BACKBUTTON_ONCLICK,SACTION.CHECK_CACHE_FIRST);
//        eventReaction.put(CLOCK_ONTICK,SACTION.START_COMMUNICATION);
//        eventReaction.put(ALERT_SENTPOINTS:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.SEND_STORED_LOCATIONS);
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(ALERT_STOPAPP:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(SETTINGACT_BUTTONSENDCACHE_CLICKED,SACTION.GET_OFFLINE_FLIGHTS);
//        eventReaction.put(FLIGHT_REMOTENUMBER_RECEIVED:
//
//        flightToClose.add((FlightBase) eventMessage.eventMessageValueObject);
//        set_sAction(SACTION.SEND_CACHED_LOCATIONS);)

    }

    static void startLocationCommService() {

//        Cursor locations = sqlHelper.getCursorDataLocation();
//        try {
//            FontLog.appendLog(TAG + "SvcComm.commBatchSize :" + commBatchSize, 'd');
//            while (locations.moveToNext()) {
//                if (locations.getPosition() >= commBatchSize) break;
//                Intent intentComm = new Intent(ctxApp, SvcComm.class);
//                //Intent intentComm = new Intent(context, SvcIntentComm.class);
//                Bundle bundle = new Bundle();
//                bundle.putLong("itemId", locations.getLong(locations.getColumnIndexOrThrow(DBSchema._ID)));
//                bundle.putInt("rc", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1)));
//                bundle.putString("ft", locations.getString(locations.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
//                bundle.putBoolean("sl", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_speedlowflag)) == 1);
//                bundle.putString("sd", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4)));
//                bundle.putString("la", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6)));
//                bundle.putString("lo", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7)));
//                bundle.putString("ac", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8)));
//                bundle.putString("al", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9)));
//                bundle.putInt("wp", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_wpntnum)));
//                bundle.putString("sg", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11)));
//                bundle.putString("dt", locations.getString(locations.getColumnIndexOrThrow(DBSchema.LOC_date)));
//                bundle.putBoolean("irch", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check)) == 1);
//
//                intentComm.putExtras(bundle);
//                ctxApp.startService(intentComm);
//                if (locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check)) == 1){delay(1000);}
//            }
//        }
//        finally {
//            locations.close();
//            sqlHelper.dbw.close();
//        }

        ArrayList<Location> locList = sqlHelper.getDataLocationList();
        for (Location l:locList) {
            if (l.i >= commBatchSize) break;
            Intent intentComm = new Intent(ctxApp, SvcComm.class);
            Bundle bundle = new Bundle();
            bundle.putLong("itemId", l.itemId);
            bundle.putInt("rc", l.rc);
            bundle.putString("ft", l.ft);
            bundle.putBoolean("sl", l.sl==1);
            bundle.putString("sd", l.sd);
            bundle.putString("la", l.la);
            bundle.putString("lo", l.lo);
            bundle.putString("ac", l.ac);
            bundle.putString("al", l.al);
            bundle.putInt("wp", l.wp);
            bundle.putString("sg", l.sg);
            bundle.putString("dt", l.dt);
            bundle.putBoolean("irch", l.irch == 1);

            intentComm.putExtras(bundle);
            ctxApp.startService(intentComm);
            if (l.irch == 1){delay(1000);}
        }
    }
//    void sendStoredLocations(){
//
//        commBatchSize= dbLocationRecCountNormal;
//        //SvcComm.commBatchSize= 50;
//        int counter =0;
//        //dbLocationRecCountNormal=sqlHelper.getCursorDataLocation().getCount();
//        int MaxTryCount = 0; //dbLocationRecCountNormal/SvcComm.commBatchSize*2;
//        while (dbLocationRecCountNormal>0){
//            FontLog.appendLog(TAG + " dbLocationRecCountNormal to send: " + dbLocationRecCountNormal, 'd');
//            if (counter >MaxTryCount) {
//                //Toast.makeText(mainactivityInstance, R.string.unsentrecords_failed, Toast.LENGTH_SHORT).show();
//                //sqlHelper.cl.close();
//                break;
//            }
//            delay(2000);
//            counter++;
//            //Toast.makeText(mainactivityInstance, R.string.toast_cachesending, Toast.LENGTH_SHORT).show();
//            set_sAction(SACTION.SEND_CACHED_LOCATIONS);
//        }
//
//    }

    static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    void set_sAction(SACTION request) {
        FontLog.appendLog(TAG + "reaction:" + request, 'd');
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
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) mainactivityInstance.finishActivity();
                break;

            case CLOSE_FLIGHTS:
                for(FlightBase f: flightList) {
                    FontLog.appendLog(TAG + " flightList size: " + flightList.size(), 'd');
                    if (activeFlight!=null && f==activeFlight) continue;
                    FontLog.appendLog(TAG + " flight "+f.flightNumber+" iflightStatet:" + f.flightState, 'd');
                    if (f.getLocationFlightCount() == 0){
                        if (f.flightNumStatus == FlightBase.FLIGHTNUMBER_SRC.REMOTE_DEFAULT) {
                            f.set_flightState(FlightBase.FLIGHT_STATE.READY_TOBECLOSED);
                            FontLog.appendLog(TAG + " flightToClose: " + f.flightNumber, 'd');
                        }
                    }
                }
                break;
            case SEND_CACHED_LOCATIONS:
                    if (Util.isNetworkAvailable()) {
                        startLocationCommService();
                    } else {
                        FontLog.appendLog(TAG + "Connectivity unavailable Can't send location", 'd');
                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                        //Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                    }
                break;
            case GET_OFFLINE_FLIGHTS:
//                /// get flights from flightlist
//                for (FlightBase f : flightList){
//                    if(f.isTempFlightNum) f..set_flightState(FlightBase.FSTATE.GETTINGFLIGHT);
//                }

                /// firsrt to check all temp flights in not ready to send state.
                /// Get new flight and request flight number.
                Cursor flightsTemp = sqlHelper.getCursorTempFlights();
                try {
                    while (flightsTemp.moveToNext()) {
                        String fn = flightsTemp.getString(flightsTemp.getColumnIndexOrThrow(DBSchema.LOC_flightid));
                        if (isFlightNumberInList(fn)) continue;
                        FontLog.appendLog(TAG + "Get flight number for " + fn, 'd');
                        if (Util.isNetworkAvailable()) new FlightBase(fn).set_flightState(FlightBase.FLIGHT_STATE.GETTINGFLIGHT);
                        else {
                            FontLog.appendLog(TAG + "Connectivity unavailable Can't get flight number", 'd');
                            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                        }
                    }
                }
                finally{
                    flightsTemp.close();
                    sqlHelper.dbw.close();
                }

                /// second to check flights is ready to send which are for some reason not in flightList (may left from previous session).
                /// Get new flight on existing flight number
                //Cursor flights = sqlHelper.getCursorReadyToSendFlights();
                ArrayList<String> flightNumberList = sqlHelper.getListReadyToSendFlights();
                for (String fn : flightNumberList){
                    if (RouteBase.isFlightNumberInList(fn)) continue;
                    FontLog.appendLog(TAG+"Get flight number for "+fn,'d');
                    //new FlightBase(fn).set_flightState(FlightBase.FLIGHT_STATE.READY_TOSENDLOCATIONS);
                    new FlightBase(fn).set_flightNumStatus(FlightBase.FLIGHTNUMBER_SRC.REMOTE_DEFAULT);
                }

//                try {
//                    while (flights.moveToNext()) {
//                        String fn = flights.getString(flights.getColumnIndexOrThrow(DBSchema.LOC_flightid));
//                        if (RouteBase.isFlightNumberInList(fn)) continue;
//                        FontLog.appendLog(TAG+"Get flight number for "+fn,'d');
//                        new FlightBase(fn).set_flightState(FlightBase.FSTATE.READY_TOSENDLOCATIONS);
//                    }
//                }
//                finally{
//                    flights.close();
//                    sqlHelper.dbw.close();
//                }
                break;
        }
    }

    @Override
    public void eventReceiver(EventMessage eventMessage){
        //Array eventReaction[EVENT];
        ev = eventMessage.event;
        this.eventMessage = eventMessage;
        FontLog.appendLog(TAG + "eventReceiver: "+ev, 'd');
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_sAction(SACTION.CHECK_CACHE_FIRST);
                break;
            case CLOCK_ONTICK:
                if (dbLocationRecCountNormal > 0) set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                set_sAction(SACTION.GET_OFFLINE_FLIGHTS);
                break;
            case ALERT_SENTPOINTS:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case ALERT_STOPAPP:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_sAction(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                set_sAction(SACTION.GET_OFFLINE_FLIGHTS);
                if (dbLocationRecCountNormal > 0) set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                break;
            case SVCCOMM_LOCRECCOUNT_NOTZERO:
                //commBatchSize=(dbLocationRecCountNormal>COMM_BATCH_SIZE_MAX?dbLocationRecCountNormal:COMM_BATCH_SIZE_MAX);
                if (dbLocationRecCountNormal > 0) {
                    set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                }
                break;
            case FLIGHT_REMOTENUMBER_RECEIVED:
                //flightList.add((FlightBase) eventMessage.eventMessageValueObject);
                //flightToClose.add((FlightBase) eventMessage.eventMessageValueObject);
                set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                break;
            case SVCCOMM_ONDESTROY:
                set_sAction(SACTION.CLOSE_FLIGHTS);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                set_sAction(SACTION.CLOSE_FLIGHTS);
                break;
        }
    }
    //session does react to events. it never initiate events

}
