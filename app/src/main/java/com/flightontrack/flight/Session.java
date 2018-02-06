package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import java.util.EnumMap;
import java.util.ArrayList;
//import java.util.EnumMap;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session implements EventBus{
    public enum SACTION {
        //SEND_STORED_LOCATIONS,
        SEND_CACHED_LOCATIONS,
        START_COMMUNICATION,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS,
        CLOSE_FLIGHTS
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
//        eventReaction.put(FLIGHT_OFFLINE_DBUPDATE_COMPLETED:
//
//        flightToClose.add((FlightBase) eventMessage.eventMessageValueObject);
//        set_sAction(SACTION.SEND_CACHED_LOCATIONS);)

    }
    static ArrayList<FlightBase> flightToClose = new ArrayList<>();

    static void startLocationCommService() {

        //sqlHelper.setCursorDataLocation();
        Cursor locations = sqlHelper.getCursorDataLocation();
        try {
            FontLog.appendLog(TAG + "SvcComm.commBatchSize :" + SvcComm.commBatchSize, 'd');
            while (locations.moveToNext()) {
                if (locations.getPosition() >= SvcComm.commBatchSize) break;
                Intent intentComm = new Intent(ctxApp, SvcComm.class);
                //Intent intentComm = new Intent(context, SvcIntentComm.class);
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", locations.getLong(locations.getColumnIndexOrThrow(DBSchema._ID)));
                bundle.putInt("rc", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1)));
                bundle.putString("ft", locations.getString(locations.getColumnIndexOrThrow(DBSchema.LOC_flightid)));
                bundle.putBoolean("sl", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_speedlowflag)) == 1);
                bundle.putString("sd", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4)));
                bundle.putString("la", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6)));
                bundle.putString("lo", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7)));
                bundle.putString("ac", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8)));
                bundle.putString("al", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9)));
                bundle.putInt("wp", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_wpntnum)));
                bundle.putString("sg", locations.getString(locations.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11)));
                bundle.putString("dt", locations.getString(locations.getColumnIndexOrThrow(DBSchema.LOC_date)));
                bundle.putBoolean("irch", locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check)) == 1);

                intentComm.putExtras(bundle);
                ctxApp.startService(intentComm);
                if (locations.getInt(locations.getColumnIndexOrThrow(DBSchema.LOC_is_elevetion_check)) == 1){delay(1000);}
            }
        }
        finally {
            locations.close();
            sqlHelper.dbw.close();
        }
    }
    void sendStoredLocations(){

        SvcComm.commBatchSize= dbLocationRecCountNormal;
        //SvcComm.commBatchSize= 50;
        int counter =0;
        //dbLocationRecCountNormal=sqlHelper.getCursorDataLocation().getCount();
        int MaxTryCount = 0; //dbLocationRecCountNormal/SvcComm.commBatchSize*2;
        while (dbLocationRecCountNormal>0){
            FontLog.appendLog(TAG + " dbLocationRecCountNormal to send: " + dbLocationRecCountNormal, 'd');
            if (counter >MaxTryCount) {
                //Toast.makeText(mainactivityInstance, R.string.unsentrecords_failed, Toast.LENGTH_SHORT).show();
                //sqlHelper.cl.close();
                break;
            }
            delay(2000);
            counter++;
            //Toast.makeText(mainactivityInstance, R.string.toast_cachesending, Toast.LENGTH_SHORT).show();
            set_sAction(SACTION.START_COMMUNICATION);
        }

    }
    public static FlightBase get_FlightInstanceByNumber(String flightNumber){
        FlightBase fr=null;
        for (FlightBase f : flightToClose) {
                if (f.flightNumber.equals(flightNumber)) {
                    fr= f;
                }
        }
        return fr;
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
//            case SEND_STORED_LOCATIONS:
//                //TODO
//                //sendStoredLocations();
//                break;
            case SEND_CACHED_LOCATIONS:
                Cursor flightsReadySend = sqlHelper.getCursorReadyToSendFlights();
                //flightsReadySend.moveToFirst();
                try {
                    while (flightsReadySend.moveToNext()) {
                        //for (int i = 0; i <flightsReadySend.getCount(); i++) {
                        String flNum = flightsReadySend.getString(flightsReadySend.getColumnIndexOrThrow(DBSchema.LOC_flightid));
                        if (!Route.routeList.isEmpty()) {
                            for (Route r : Route.routeList) {
                                for (Flight f : r.flightList) {
                                    if (f.flightNumber.equals(flNum)) {
                                        flightToClose.add(f);
                                    }
                                }
                            }
                        }
                        if (get_FlightInstanceByNumber(flNum) == null) new FlightBase(flNum);
                    }
                }
                finally {
                    flightsReadySend.close();
                    sqlHelper.dbw.close();
                }
                SvcComm.commBatchSize= dbLocationRecCountNormal;
                set_sAction(SACTION.START_COMMUNICATION);
                //sendStoredLocations();
                break;
            case CLOSE_FLIGHTS:
                FontLog.appendLog(TAG + " flightToClose size: " + flightToClose.size(), 'd');
                for(FlightBase f: new ArrayList<>(flightToClose)) {
                    FontLog.appendLog(TAG + " flightToClose : getLocationFlightCount:" + f.getLocationFlightCount(), 'd');
                    if (f.getLocationFlightCount() == 0){
                        FontLog.appendLog(TAG + " flightToClose: " + f.flightNumber, 'd');
                        f.getCloseFlight();
                        flightToClose.remove(f);
                    }
                }
                break;
            case START_COMMUNICATION:
                if (dbLocationRecCountNormal > 0) {
                    if (Util.isNetworkAvailable()) {
                        startLocationCommService();
                    } else {
                        FontLog.appendLog(TAG + "Connectivity unavailable Can't send location", 'd');
                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                        Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case GET_OFFLINE_FLIGHTS:
                Cursor flights = sqlHelper.getCursorTempFlights();
                try {
                    while (flights.moveToNext()) {
                        FontLog.appendLog(TAG+"Get flight number for "+flights.getString(flights.getColumnIndexOrThrow(DBSchema.LOC_flightid)),'d');
                        new FlightBase(flights.getString(flights.getColumnIndexOrThrow(DBSchema.LOC_flightid))).getOfflineFlightID();
                    }
                }
                finally{
                    flights.close();
                    sqlHelper.dbw.close();
                }

                break;
        }
    }
    static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    @Override
    public void eventReceiver(EventMessage eventMessage){
        //Array eventReaction[EVENT];
        ev = eventMessage.event;
        this.eventMessage = eventMessage;
        FontLog.appendLog(TAG + ":eventReceiver:"+ev, 'd');
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_sAction(SACTION.CHECK_CACHE_FIRST);
                break;
            case CLOCK_ONTICK:
                set_sAction(SACTION.START_COMMUNICATION);
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
                set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                break;
            case FLIGHT_OFFLINE_DBUPDATE_COMPLETED:
                flightToClose.add((FlightBase) eventMessage.eventMessageValueObject);
                set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                break;
            case SVCCOMM_ONDESTROY:
                set_sAction(SACTION.CLOSE_FLIGHTS);
                break;
        }
    }
    //session does react to events. it never initiate events

}
