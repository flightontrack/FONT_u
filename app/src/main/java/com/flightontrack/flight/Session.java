package com.flightontrack.flight;

import android.content.Context;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;

//import static com.flightontrack.communication.SvcComm.commBatchSize;
import static com.flightontrack.flight.FlightOffline.*;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.LoopjAClient;
import com.flightontrack.communication.Response;
//import com.flightontrack.communication.SvcComm;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityRequestPostLocation;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.entities.EntityLocation;
import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.flightontrack.ui.ShowAlertClass;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
//import java.util.EnumMap;

public class Session implements EventBus{
    static final String TAG = "Session";

    public enum SACTION {
        SEND_CACHED_LOCATIONS,
        CLOSEAPP_NO_CACHE_CHECK,
        CHECK_CACHE_FIRST,
        GET_OFFLINE_FLIGHTS
    }
    static Session sessionInstance = null;
    public static Integer commBatchSize = COMM_BATCH_SIZE_MAX;
    boolean isSendNextStarted = false;
    //static EnumMap<EVENT,SACTION> eventReaction = new EnumMap<>(EVENT.class);
    Map<Integer,EntityLocation> locRequestList = new HashMap<Integer,EntityLocation>();
    EVENT ev;

    EventMessage eventMessage;
    public static Session getInstance() {
        if(sessionInstance == null) {
            sessionInstance = new Session();
        }
        return sessionInstance;
    }

    public static void initProp(Context ctx, MainActivity maInstance) {
        ctxApp = ctx;
        sharedPreferences = ctx.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mainactivityInstance = maInstance;
        sqlHelper = new SQLHelper();
//        eventReaction.put(MACT_BACKBUTTON_ONCLICK,SACTION.CHECK_CACHE_FIRST);
//        eventReaction.put(CLOCK_ONTICK,SACTION.START_COMMUNICATION);
//        eventReaction.put(ALERT_SENTPOINTS:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.SEND_STORED_LOCATIONS);
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(ALERT_STOPAPP:
//        if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
//        eventReaction.put(SETTINGACT_BUTTONSENDCACHE_CLICKED,SACTION.GET_OFFLINE_FLIGHTS);
//        eventReaction.put(FLIGHT_REMOTENUMBER_RECEIVED:


    }
    void addLocToRequestList(EntityLocation l){
        if (locRequestList.containsKey((int) l.itemId)) return;
        if (l.i >= commBatchSize) return;
        locRequestList.put((int) l.itemId, l);
    }
    void startLocationRequest() {
        ArrayList<EntityLocation> locList = sqlHelper.getAllLocationList();
        for (EntityLocation l : locList) {
            addLocToRequestList(l);
        }
        if(!isSendNextStarted)  sendNext();
    }
    void startLocationRequest(String flightNum) {

        ArrayList<EntityLocation> locList = sqlHelper.getFlightLocationList(flightNum);
        for (EntityLocation l : locList) {
            addLocToRequestList(l);
        }
        if(!isSendNextStarted)  sendNext();
    }
    void sendNext(){

        isSendNextStarted = true;
        if (locRequestList.isEmpty()) {
            EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
            isSendNextStarted = false;
            return;
        }
        for (Map.Entry<Integer, EntityLocation> e : locRequestList.entrySet()){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Entrykey : " + e.getKey() + " Entryvalue : " + e.getValue(), 'd'));
        }
        Map.Entry<Integer, EntityLocation> e = locRequestList.entrySet().iterator().next();
        EntityLocation l = e.getValue();
        int k = e.getKey();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "Key : " + e.getKey()+ "Location : " + e.getValue(), 'd'));
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

        //postLocation(k, requestParams);
        postLocationNew(k, l);
    }

    static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    void set_Action(SACTION request) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "reaction:" + request, 'd'));
        switch (request) {
            case CHECK_CACHE_FIRST:
                if (dbLocationRecCountNormal > 0) {
                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCountNormal);
                    new FontLogAsync().execute(new EntityLogMessage(TAG, " PointsUnsent: " + dbLocationRecCountNormal, 'd'));
                } else {
                    set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                }
                break;
            case CLOSEAPP_NO_CACHE_CHECK:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) mainactivityInstance.finishActivity();
                break;
            case SEND_CACHED_LOCATIONS:
                    if (Util.isNetworkAvailable()) {
                        startLocationRequest();
                    } else {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "Connectivity unavailable Can't send location", 'd'));
                        EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                    }
                break;
            case GET_OFFLINE_FLIGHTS:
                /// firsrt to check all temp flights in not ready to send state.
                /// Get new flight and request flight number.
//                Cursor flightsTemp = sqlHelper.getCursorTempFlights();
//                try {
//                    while (flightsTemp.moveToNext()) {
//                        String fn = flightsTemp.getString(flightsTemp.getColumnIndexOrThrow(DBSchema.LOC_flightid));
//                        //if (isFlightNumberInList(fn)) continue;
//                        FontLog.appendLog(TAG + "Get flight number for " + fn, 'd');
//                        if (Util.isNetworkAvailable()) new FlightOffline(fn).set_flightState(FLIGHT_STATE.GETTINGFLIGHT);
//                        else {
//                            FontLog.appendLog(TAG + "Connectivity unavailable Can't get flight number", 'd');
//                            EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
//                        }
//                    }
//                }
//                finally{
//                    flightsTemp.close();
//                    sqlHelper.dbw.close();
//                }

                for (String flightNumTemp:sqlHelper.getTempFlightList()){
                    new FontLogAsync().execute(new EntityLogMessage(TAG, "Get flightBase for " + flightNumTemp, 'd'));
                    if (Util.isNetworkAvailable()) new FlightOffline(flightNumTemp).set_flightState(FLIGHT_STATE.GETTINGFLIGHT);
                    else {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "Connectivity unavailable Can't get flight number", 'd'));
                        EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(false));
                    }
                }

                /// second to check flights is ready to send which are for some reason not in flightList (may left from previous session).
                /// Get new flight on existing flight number

                //ArrayList<String> flightNumberList = sqlHelper.getReadyToSendFlightList();
                for (String fn : sqlHelper.getReadyToSendFlightList()){
                    if (RouteBase.isFlightNumberInList(fn)) continue;
                    new FontLogAsync().execute(new EntityLogMessage(TAG,"Get flight number for "+fn,'d'));
                    //new FlightOffline(fn).set_flightState(FlightOffline.FLIGHT_STATE.READY_TOSENDLOCATIONS);
                    new FlightOffline(fn).set_flightNumStatus(FlightOffline.FLIGHTNUMBER_SRC.REMOTE_DEFAULT);
                }
                break;
        }
    }

    void postLocation(int dbId,RequestParams requestParams){
        if (Util.isNetworkAvailable()) {
            try {
                final LoopjAClient aSyncClient = new LoopjAClient(dbId);
                new FontLogAsync().execute(new EntityLogMessage(TAG,"Post: ID:"+dbId+ "requestParams: " + requestParams, 'd'));
                aSyncClient.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (commBatchSize == COMM_BATCH_SIZE_MIN) {
                            commBatchSize = COMM_BATCH_SIZE_MAX;
                        }
                        Response response = new Response(new String(responseBody));
                        //Util.appendLog(TAG+ "onSuccess Got response : " + responseBody,'d');
                        if (response.jsonErrorCount > 0) {
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :JSON ERROR COUNT :" + response.jsonErrorCount, 'd'));
                            if (response.jsonErrorCount > MAX_JSON_ERROR) {
                                /// raise this event as NOTIF
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                            }
                            return;
                        }
                        try {
                            if (response.responseAckn != null) {
                                sqlHelper.rowLocationDeleteOnId(aSyncClient.getID(), response.responseFlightNum);  /// TODO should be moved to Router
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseFlightNum + ":" + response.responseAckn+ ": id" +aSyncClient.getID(), 'd'));
                            }
                            if (response.responseNotif != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :RESPONSE_TYPE_NOTIF :" + response.responseNotif, 'd'));
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                            }
                            if (response.responseCommand != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : RESPONSE_TYPE_COMMAND : " + response.responseCommand, 'd'));
                                if (response.responseCommand.equals(COMMAND_TERMINATEFLIGHT) && SessionProp.pIsRoad)
                                    return;
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_COMMAND)
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
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "Data response : " + response.responseDataLoad, 'd'));
                            }
                        } catch (Exception e) {
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : EXCEPTION :" + e.getMessage(), 'e'));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure; startId= " + aSyncClient.getID(), 'd'));
                        commBatchSize = COMM_BATCH_SIZE_MIN;
                    }

                    @Override
                    public void onFinish() {
                        locRequestList.remove(aSyncClient.getID());
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFinish removed ID= " + aSyncClient.getID(), 'd'));
                        sendNext();
                    }
                });
            } catch (Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "aSyncClient" + e.getMessage(), 'd'));
                return;
            }
        }

    }
    void postLocationNew(int k,EntityLocation loc){
        String TAG = "NEWFONT";
        if (Util.isNetworkAvailable()) {
            try (
                    EntityRequestPostLocation entityRequestPostLocation = new EntityRequestPostLocation(loc);
                    HttpJsonClient client= new HttpJsonClient(entityRequestPostLocation)
            ){
                new FontLogAsync().execute(new EntityLogMessage(TAG,"Post: ID:"+loc.itemId, 'd'));
                client.post(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                        if (commBatchSize == COMM_BATCH_SIZE_MIN) {
                            commBatchSize = COMM_BATCH_SIZE_MAX;
                        }

                        try (ResponseJsonObj response = new ResponseJsonObj(jsonObject);) {
                            if (response.isException) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :isException", 'd'));
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                                return;
                            }
                            if (response.responseAckn != null) {
                                sqlHelper.rowLocationDeleteOnId((int)loc.itemId, response.responseCurrentFlightNum);
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseCurrentFlightNum + ":" + response.responseAckn+ ": id" +response.responseAckn, 'd'));
                            }
                            if (response.responseException != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess :RESPONSE_TYPE_Exception :" + response.responseException, 'd'));
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_EXCEPTION));
                            }
                            if (response.responseCommand != null) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : RESPONSE_TYPE_COMMAND : " + response.responseCommand, 'd'));
                                if (response.responseCommand.equals(COMMAND_TERMINATEFLIGHT) && SessionProp.pIsRoad)
                                    return;
                                EventBus.distribute(new EventMessage(EVENT.SESSION_ONSUCCESS_COMMAND)
                                        .setEventMessageValueString(response.responseCommand));
                                        //.setEventMessageValueString(response.responseCurrentFlightNum));
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
                        } catch (Exception e) {
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess : EXCEPTION :" + e.getMessage(), 'e'));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure; startId= " + response, 'd'));
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: "+e, 'd'));
                        locRequestList.clear();
                        commBatchSize = COMM_BATCH_SIZE_MIN;
                    }

                    @Override
                    public void onFinish() {
                        locRequestList.remove(k);
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFinish locRequestList removed key= " + k, 'd'));
                        sendNext();
                    }
                });
            } catch (Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "postLocationNew " + e.getMessage(), 'd'));
                return;
            }
        }

    }
    @Override
    public void onClock(EventMessage eventMessage){
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onClock ", 'd'));
        if (dbLocationRecCountNormal > 0) set_Action(SACTION.SEND_CACHED_LOCATIONS);
    }

    @Override
    public void eventReceiver(EventMessage eventMessage){
        //Array eventReaction[EVENT];
        ev = eventMessage.event;
        this.eventMessage = eventMessage;
        new FontLogAsync().execute(new EntityLogMessage(TAG, "eventReceiver: "+ev+":eventString:"+eventMessage.eventMessageValueString, 'd'));
        switch (ev) {
            case MACT_BACKBUTTON_ONCLICK:
                set_Action(SACTION.CHECK_CACHE_FIRST);
                break;
            case ALERT_SENTPOINTS:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.SEND_CACHED_LOCATIONS);
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.NEG) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case ALERT_STOPAPP:
                if(eventMessage.eventMessageValueAlertResponse== ALERT_RESPONSE.POS) set_Action(SACTION.CLOSEAPP_NO_CACHE_CHECK);
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                commBatchSize = COMM_BATCH_SIZE_MAX;
                if (sqlHelper.getLocationTableCountTotal() ==0){
                    EventBus.distribute(new EventMessage(EVENT.SESSION_ONSENDCACHECOMPLETED).setEventMessageValueBool(true));
                    break;
                }
                else if (dbLocationRecCountNormal > 0) set_Action(SACTION.SEND_CACHED_LOCATIONS);
                if (dbTempFlightRecCount>0) set_Action(SACTION.GET_OFFLINE_FLIGHTS);
                break;
//            case SESSION_ONSENDCACHECOMPLETED:
//                /// if still something to send
//                if (dbLocationRecCountNormal > 0) {
//                    set_Action(SACTION.SEND_CACHED_LOCATIONS);
//                }
//                break;
            case FLIGHT_REMOTENUMBER_RECEIVED:
                startLocationRequest(eventMessage.eventMessageValueString);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                set_Action(SACTION.SEND_CACHED_LOCATIONS);
                break;
        }
    }
}
