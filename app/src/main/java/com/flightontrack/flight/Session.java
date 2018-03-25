package com.flightontrack.flight;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;

//import static com.flightontrack.communication.SvcComm.commBatchSize;
import static com.flightontrack.flight.RouteBase.*;
import static com.flightontrack.flight.FlightBase.*;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

import com.flightontrack.communication.LoopjAClient;
import com.flightontrack.communication.Response;
//import com.flightontrack.communication.SvcComm;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.mysql.Location;
import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
//import java.util.EnumMap;


/**
 * Created by hotvk on 7/6/2017.
 */

public class Session implements EventBus{
    public enum SACTION {
        SEND_CACHED_LOCATIONS,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS,
        CLOSE_FLIGHTS
    }

    static Session sessionInstance = null;
    public static Integer commBatchSize = COMM_BATCH_SIZE_MAX;
    static EnumMap<EVENT,SACTION> eventReaction = new EnumMap<>(EVENT.class);
    Map<Integer,Location> locRequestList = new HashMap<Integer,Location>();
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
    void addLocToRequestList(Location l){
        if (locRequestList.containsKey((int) l.itemId)) return;
        if (l.i >= commBatchSize) return;
        locRequestList.put((int) l.itemId, l);
    }
    void startLocationRequest() {
        ArrayList<Location> locList = sqlHelper.getDataLocationList();
        for (Location l : locList) {
            addLocToRequestList(l);
        }
        sendNext();
    }
    void startLocationRequest(String flightNum) {
        ArrayList<Location> locList = sqlHelper.getFlightLocationList(flightNum);
        for (Location l : locList) {
            addLocToRequestList(l);
        }
        sendNext();
    }
    void sendNext(){
        if (locRequestList.isEmpty()) {
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
            return;
        }
        Map.Entry<Integer, Location> e = locRequestList.entrySet().iterator().next();
        Location l = e.getValue();
        int k = e.getKey();
        FontLog.appendLog(TAG + "Entry : " + e.getValue(), 'd');
        RequestParams requestParams = new RequestParams();
        requestParams.put("isdebug", SessionProp.pIsDebug);
        requestParams.put("speedlowflag", l.sl == 1);
        requestParams.put("rcode", l.rc);
        requestParams.put("latitude", l.la);
        requestParams.put("longitude", l.lo);
        requestParams.put("flightid", l.ft);
        requestParams.put("accuracy", l.ac);
        requestParams.put("extrainfo", l.al);
        requestParams.put("wpntnum", l.wp);
        requestParams.put("gsmsignal", l.sg);
        requestParams.put("speed", l.sd);
        requestParams.put("date", l.dt);
        requestParams.put("elevcheck", l.irch == 1);
        postLocation(k, requestParams);
    }
//    static void startLocationCommService() {
//
//        ArrayList<Location> locList = sqlHelper.getDataLocationList();
//        for (Location l:locList) {
//
//            if (l.i >= commBatchSize) break;
//            Intent intentComm = new Intent(ctxApp, SvcComm.class);
//            Bundle bundle = new Bundle();
//            bundle.putLong("itemId", l.itemId);
//            bundle.putInt("rc", l.rc);
//            bundle.putString("ft", l.ft);
//            bundle.putBoolean("sl", l.sl==1);
//            bundle.putString("sd", l.sd);
//            bundle.putString("la", l.la);
//            bundle.putString("lo", l.lo);
//            bundle.putString("ac", l.ac);
//            bundle.putString("al", l.al);
//            bundle.putInt("wp", l.wp);
//            bundle.putString("sg", l.sg);
//            bundle.putString("dt", l.dt);
//            bundle.putBoolean("irch", l.irch == 1);
//
//            intentComm.putExtras(bundle);
//            FontLog.appendLog(TAG + "startServiceComm request: " + l.itemId+"-"+l.ft+"-"+l.wp, 'd');
//            ctxApp.startService(intentComm);
//            if (l.irch == 1){delay(1000);}
//        }
//    }
//    static void startLocationCommServiceForFlight(String flightNum) {
//
//        ArrayList<Location> locList = sqlHelper.getDataLocationList();
//        for (Location l:locList) {
//            if (l.i >= commBatchSize) break;
//            Intent intentComm = new Intent(ctxApp, SvcComm.class);
//            Bundle bundle = new Bundle();
//            bundle.putLong("itemId", l.itemId);
//            bundle.putInt("rc", l.rc);
//            bundle.putString("ft", l.ft);
//            bundle.putBoolean("sl", l.sl==1);
//            bundle.putString("sd", l.sd);
//            bundle.putString("la", l.la);
//            bundle.putString("lo", l.lo);
//            bundle.putString("ac", l.ac);
//            bundle.putString("al", l.al);
//            bundle.putInt("wp", l.wp);
//            bundle.putString("sg", l.sg);
//            bundle.putString("dt", l.dt);
//            bundle.putBoolean("irch", l.irch == 1);
//
//            intentComm.putExtras(bundle);
//            FontLog.appendLog(TAG + "startServiceComm request: " + l.itemId+"-"+l.ft+"-"+l.wp, 'd');
//            ctxApp.startService(intentComm);
//            if (l.irch == 1){delay(1000);}
//        }
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
//                    if (f.getLocationFlightCount() == 0){
////                        if (f.flightNumStatus == FlightBase.FLIGHTNUMBER_SRC.REMOTE_DEFAULT) {
////                            f.set_flightState(FlightBase.FLIGHT_STATE.READY_TOBECLOSED);
////                            FontLog.appendLog(TAG + " flightToClose: " + f.flightNumber, 'd');
////                        }
//                    }
                }
                break;
            case SEND_CACHED_LOCATIONS:
                    if (Util.isNetworkAvailable()) {
                        //startLocationCommService();
                        startLocationRequest();
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
                        //if (isFlightNumberInList(fn)) continue;
                        FontLog.appendLog(TAG + "Get flight number for " + fn, 'd');
                        if (Util.isNetworkAvailable()) new FlightBase(fn).set_flightState(FLIGHT_STATE.GETTINGFLIGHT);
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
                break;
        }
    }

    void postLocation(int dbId,RequestParams requestParams){
        if (Util.isNetworkAvailable()) {
            try {
                final LoopjAClient aSyncClient = new LoopjAClient(dbId);
                FontLog.appendLog(TAG + "Post: requestParams: " + requestParams, 'd');
                aSyncClient.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (commBatchSize == COMM_BATCH_SIZE_MIN) {
                            commBatchSize = COMM_BATCH_SIZE_MAX;
                        }
                        Response response = new Response(new String(responseBody));
                        //Util.appendLog(TAG+ "onSuccess Got response : " + responseBody,'d');
                        if (response.jsonErrorCount > 0) {
                            FontLog.appendLog(TAG + "onSuccess :JSON ERROR COUNT :" + response.jsonErrorCount, 'd');
                            if (response.jsonErrorCount > MAX_JSON_ERROR) {
                                /// raise this event as NOTIF
                                EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONSUCCESS_NOTIF));
                            }
                            return;
                        }
                        try {
                            if (response.responseAckn != null) {
                                sqlHelper.rowLocationDeleteOnId(aSyncClient.getID(), response.responseFlightNum);  /// TODO should be moved to Router
                                FontLog.appendLog(TAG + "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseFlightNum + ":" + response.responseAckn+ ": id" +aSyncClient.getID(), 'd');
                            }
                            if (response.responseNotif != null) {
                                FontLog.appendLog(TAG + "onSuccess :RESPONSE_TYPE_NOTIF :" + response.responseNotif, 'd');
                                EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONSUCCESS_NOTIF));
                            }
                            if (response.responseCommand != null) {
                                FontLog.appendLog(TAG + "onSuccess : RESPONSE_TYPE_COMMAND : " + response.responseCommand, 'd');
                                if (response.iresponseCommand == COMMAND_TERMINATEFLIGHT && SessionProp.pIsRoad)
                                    return;
                                EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONSUCCESS_COMMAND)
                                        .setEventMessageValueInt(response.iresponseCommand)
                                        .setEventMessageValueString(response.responseFlightNum));
//                                switch (response.iresponseCommand) {
//                                    case COMMAND_TERMINATEFLIGHT:
//                                          break;
//                                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
//                                        break;
//                                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
//                                        break;
//                                    case COMMAND_FLIGHT_STATE_PENDING:
//                                        break;
//                                    case -1:
//                                        break;
//                                }
                            }
                            if (response.responseDataLoad != null) {
                                FontLog.appendLog(TAG + "Data response : " + response.responseDataLoad, 'd');
                            }
                        } catch (Exception e) {
                            FontLog.appendLog(TAG + "onSuccess : EXCEPTION :" + e.getMessage(), 'e');
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        FontLog.appendLog(TAG + "onFailure; startId= " + aSyncClient.getID(), 'd');
                        commBatchSize = COMM_BATCH_SIZE_MIN;
                    }

                    @Override
                    public void onFinish() {
                        locRequestList.remove(aSyncClient.getID());
                        FontLog.appendLog(TAG + "onFinish removed ID= " + aSyncClient.getID(), 'd');
                        sendNext();
                    }
                });
            } catch (Exception e) {
                FontLog.appendLog(TAG + "aSyncClient" + e.getMessage(), 'd');
                return;
            }
        }

    }
    @Override
    public void onClock(EventMessage eventMessage){
        if (dbLocationRecCountNormal > 0) set_sAction(SACTION.SEND_CACHED_LOCATIONS);
    }

    @Override
    public void eventReceiver(EventMessage eventMessage){
        //Array eventReaction[EVENT];
        ev = eventMessage.event;
        this.eventMessage = eventMessage;
        FontLog.appendLog(TAG + "eventReceiver: "+ev+":eventString:"+eventMessage.eventMessageValueString, 'd');
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_sAction(SACTION.CHECK_CACHE_FIRST);
                break;
//            case CLOCK_ONTICK:
//                if (dbLocationRecCountNormal > 0) set_sAction(SACTION.SEND_CACHED_LOCATIONS);
//                //set_sAction(SACTION.GET_OFFLINE_FLIGHTS);
//                break;
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
                startLocationRequest(eventMessage.eventMessageValueString);
                break;
//            case SVCCOMM_ONDESTROY:
//                set_sAction(SACTION.CLOSE_FLIGHTS);
//                break;
            case CLOCK_SERVICESELFSTOPPED:
                set_sAction(SACTION.SEND_CACHED_LOCATIONS);
                //set_sAction(SACTION.CLOSE_FLIGHTS);
                break;
        }
    }
    //session does react to events. it never initiate events

}
