package com.flightontrack.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.flightontrack.R;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
//import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import cz.msebera.android.httpclient.Header;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class SvcComm extends Service{

    public SvcComm() {}
    private static final String TAG = "SvcComm:";
    public static boolean isServiceStarted;
    RequestParams requestParams;
    //private static Context ctx;
    private int dbItemId;
    private int trackPointNumber;
    private String flightID;
    private Integer minStartId;
    private static Integer failureCounter=0;
    public static Integer commBatchSize = COMM_BATCH_SIZE_MAX;
    boolean sendNextStarted;
    //ArrayList<Integer> startId = new ArrayList<>();
    //ArrayList<Integer> pointNum = new ArrayList<>();
    private static HashMap<Integer,Integer> requestIdDbItemIdMap = new HashMap<>();
    private static HashMap<Integer,RequestParams> requestIdToRequestMap = new HashMap<>();
    //private LoopjAClient aSyncClient

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int requestId) {
        isServiceStarted=true;
        dbItemId = (int) intent.getExtras().getLong("itemId");
        //Bundle extras = intent.getExtras();
        if (requestIdDbItemIdMap.containsValue(dbItemId)) {
            FontLog.appendLog(TAG+ "onStartCommand requestIdDbItemIdMap contains: " + dbItemId + " return",'d');
            return START_STICKY;
        }
        setRequest(requestId,intent.getExtras());
        //requestIdDbItemIdMap.put(requestId,dbItemId);
//        if (requestIdDbItemIdMap.size()>1){
//            for (Map.Entry<Integer, Integer> entry : requestIdDbItemIdMap.entrySet()) {
//                FontLog.appendLog("requestId = " + entry.getKey() + ", dbItemId = " + entry.getValue(),'d');
//            }
//        }
        //trackPointNumber
        if (!sendNextStarted) sendNext();
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        isServiceStarted=false;
        sendNextStarted = false;
        //ctx = getApplicationContext();
    }
    public void setRequest(int reqId,Bundle extras){

        //dbItemId = (int) extras.getLong("itemId");
        trackPointNumber = extras.getInt("wp");
        flightID = extras.getString("ft");
        FontLog.appendLog(TAG+ "setRequest  requestId: " + reqId +" dbItemId :"+ dbItemId+" trackPointNumber :"+trackPointNumber,'d');
        requestParams = new RequestParams();
        requestParams.put("isdebug", SessionProp.pIsDebug);
        requestParams.put("speedlowflag",extras.getBoolean("sl"));
        requestParams.put("rcode",      extras.getInt("rc"));
        requestParams.put("latitude",   extras.getString("la"));
        requestParams.put("longitude",  extras.getString("lo"));
        requestParams.put("flightid",   extras.getString("ft"));
        requestParams.put("accuracy",   extras.getString("ac"));
        requestParams.put("extrainfo",  extras.getString("al"));
        requestParams.put("wpntnum", String.valueOf(extras.getInt("wp")));
        requestParams.put("gsmsignal",  extras.getString("sg"));
        requestParams.put("speed", extras.getString("sd"));
        requestParams.put("date", extras.getString("dt"));
        //if (extras.getInt("wp")==ELEVATIONCHECK_POINT_NUMBER) requestParams.put("elevcheck", extras.getBoolean("irch"));
        if (extras.getBoolean("irch") ) requestParams.put("elevcheck", true);
//        requestParams.setUseJsonStreamer(true);
        //Util.appendLog(TAG + "Request :" + "dbItemId :" + String.valueOf(dbItemId) + " point: " + trackPointNumber,'d');
        requestIdDbItemIdMap.put(reqId,dbItemId);
        requestIdToRequestMap.put(reqId,requestParams);
    }
    public void sendNext() {
        sendNextStarted = true;
        HashMap.Entry<Integer, RequestParams> entry = requestIdToRequestMap.entrySet().iterator().next();
        if (Util.isNetworkAvailable()) {
        try {
            FontLog.appendLog(TAG + "Send: flight: " + flightID + " dbItemId :" + String.valueOf(dbItemId) + " point: " + trackPointNumber, 'd');
            //AsyncHttpClient aSyncClient = new AsyncHttpClient();
            final LoopjAClient aSyncClient = new LoopjAClient(entry.getKey());
            aSyncClient.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), entry.getValue(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    failureCounter = 0;
                    if (commBatchSize == COMM_BATCH_SIZE_MIN) {
                        /// need to call service again because the batch is smal to send all  location
                        commBatchSize = COMM_BATCH_SIZE_MAX;
                        //EventBus.distribute(new EventMessage(EVENT.SVCCOMM_LOCRECCOUNT_NOTZERO)
                                //.setEventMessageValueInt(COMM_BATCH_SIZE_MAX)
                        //);
                    }
                    Response response = new Response(new String(responseBody));
                    //Util.appendLog(TAG+ "onSuccess Got response : " + responseBody,'d');
                    if (response.jsonErrorCount > 0) {
                        FontLog.appendLog(TAG + "onSuccess :JSON ERROR COUNT :" + response.jsonErrorCount, 'd');
                        if (response.jsonErrorCount > MAX_JSON_ERROR) {
                            /// raise this event as NOTIF
                            EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONSUCCESS_NOTIF));
                            //Route.activeRoute.set_rAction(RACTION.CLOSE_BUTTON_STOP_PRESSED);
                        }
                        return;
                    }
                    try {
                        if (response.responseAckn != null) {
                            sqlHelper.rowLocationDelete(response.iresponseAckn, response.responseFlightNum);  /// TODO should be moved to Router
                            FontLog.appendLog(TAG + "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseFlightNum + ":" + response.responseAckn, 'd');
                            ///set_SessionRequest(SACTION.ON_COMMUNICATION_SUCCESS);
                        }
                        if (response.responseNotif != null) {
                            FontLog.appendLog(TAG + "onSuccess :RESPONSE_TYPE_NOTIF :" + response.responseNotif, 'd');
                            EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONSUCCESS_NOTIF));
                            // flight.set_fAction(FACTION.ON_SERVER_N0TIF);
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
//                                        if (SessionProp.pIsRoad) break; /// just ignore the request
//                                        else {
//                                            Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
//                                            FontLog.appendLog(TAG + "COMMAND_TERMINATEFLIGHT request", 'd');
//                                            flight.set_fAction(FACTION.TERMINATE_FLIGHT);
//                                            break;
//                                        }
//                                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
//                                        FontLog.appendLog(TAG + "COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN request",'d');
//                                        flight.set_fAction(FACTION.CHANGESTATE_SPEED_BELOW_MIN);
//                                        break;
//                                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
//                                        FontLog.appendLog(TAG + "COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED request",'d');
//                                        flight.set_fAction(FACTION.CHANGESTATE_COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED);
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
                    } finally {
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    FontLog.appendLog(TAG + "onFailure; startId= " + aSyncClient.getStartID(), 'd');
                    commBatchSize = COMM_BATCH_SIZE_MIN;
                    failureCounter++;
                }

                @Override
                public void onFinish() {
                    //minStartId = Collections.min(requestIdDbItemIdMap.keySet());
                    if (requestIdDbItemIdMap.size() > 1) {
                        for (Map.Entry<Integer, Integer> entry : requestIdDbItemIdMap.entrySet()) {
                            FontLog.appendLog("requestId = " + entry.getKey() + ", dbItemId = " + entry.getValue(), 'd');
                        }
                    }
                    requestIdDbItemIdMap.remove(aSyncClient.getStartID());
                    requestIdToRequestMap.remove(aSyncClient.getStartID());
                    FontLog.appendLog(TAG + "onFinish to remove ; startId= " + aSyncClient.getStartID(), 'd');
                    if (failureCounter > MAX_FAILURE_COUNT) {
                    }
                    ;//TODO
                    //stopSelf(minStartId);
                    if (requestIdDbItemIdMap.isEmpty()) {
                        if (dbLocationRecCountNormal > 0) {
                            EventBus.distribute(new EventMessage(EVENT.SVCCOMM_LOCRECCOUNT_NOTZERO));
                        }
                        else stopSelf();
                        // if still location to send left  - rethrow the call
                    }
                    else sendNext();
                }
            });
        } catch (Exception e) {
            FontLog.appendLog(TAG + "sendNext" + e.getMessage(), 'd');
            return;
        }
    }
    }
    public void onDestroy() {
        FontLog.appendLog(TAG + "onDestroy minStartId= "+ minStartId, 'd');
        super.onDestroy();
        isServiceStarted=false;
        EventBus.distribute(new EventMessage(EVENT.SVCCOMM_ONDESTROY));
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        FontLog.appendLog(TAG + "onTaskRemoved",'d');
        super.onTaskRemoved(rootIntent);
        isServiceStarted=false;
        //ReceiverRouter.alarmDisable = true;
        stopSelf();
    }
}
