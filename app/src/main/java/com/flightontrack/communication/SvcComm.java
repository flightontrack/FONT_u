package com.flightontrack.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.flightontrack.flight.Flight;
import com.flightontrack.R;
import com.flightontrack.flight.Route;
import com.flightontrack.flight.Session;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.Util;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import cz.msebera.android.httpclient.Header;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class SvcComm extends Service implements Session{
    public SvcComm() {}
    private static final String TAG = "SvcComm:";
    private RequestParams requestParams = new RequestParams();
    //private static Context ctx;
    private int dbItemId;
    private int trackPointNumber;
    private String flightID;
    private Integer minStartId;
    private static Integer failureCounter=0;
    public static Integer commBatchSize = 1;
    //ArrayList<Integer> startId = new ArrayList<>();
    //ArrayList<Integer> pointNum = new ArrayList<>();
    private static HashMap<Integer,Integer> startIdDbItemId = new HashMap<>();
    //private LoopjAClient aSyncClient

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FontLog.appendLog(TAG+ "ServiceComm  - onStartCommand requestId : " + startId,'d');
        Bundle extras = intent.getExtras();
        setRequest(extras);
        if (startIdDbItemId.containsValue(dbItemId)) return START_STICKY;
        startIdDbItemId.put(startId,dbItemId);
        if (startIdDbItemId.size()>1){
            for (Map.Entry<Integer, Integer> entry : startIdDbItemId.entrySet()) {
                FontLog.appendLog("startId = " + entry.getKey() + ", dbItemId = " + entry.getValue(),'d');
            }
        }
        //trackPointNumber
        sendData(startId);
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //ctx = getApplicationContext();
    }
    public void setRequest(Bundle extras){

        dbItemId = (int) extras.getLong("itemId");
        trackPointNumber = extras.getInt("wp");
        flightID = extras.getString("ft");
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
    }
    public void sendData(int startId){
        //if (!Util.isNetworkAvailable()) {
            try {
                FontLog.appendLog(TAG + "Send: flight: " + flightID + " dbItemId :" + String.valueOf(dbItemId) + " point: " + trackPointNumber,'d');
                //AsyncHttpClient aSyncClient = new AsyncHttpClient();
                final LoopjAClient aSyncClient = new LoopjAClient(startId);
                aSyncClient.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        failureCounter=0;
                        commBatchSize = COMM_BATCH_SIZE_MAX;
                        Response response = new Response(new String(responseBody));
                        //Util.appendLog(TAG+ "onSuccess Got response : " + responseBody,'d');
                        if (response.jsonErrorCount>0) {
                            if (response.jsonErrorCount>MAX_JSON_ERROR) Route.activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                            return;
                        }
                        try {
                            Flight flight = get_FlightInstance(response.responseFlightNum);

                            if (response.responseAckn != null) {
                                FontLog.appendLog(TAG + "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseFlightNum+":"+response.responseAckn, 'd');
                                sqlHelper.rowLocationDelete(response.iresponseAckn, response.responseFlightNum);  /// TODO should be moved to Router
                                set_SessionRequest(SESSIONREQUEST.ON_COMMUNICATION_SUCCESS);
                            }
                            if (response.responseNotif != null) {
                                FontLog.appendLog(TAG + "onSuccess :RESPONSE_TYPE_NOTIF :" + response.responseNotif,'d');
                                flight.set_flightRequest(FLIGHTREQUEST.ON_SERVER_N0TIF);
                            }
                            if (response.responseCommand != null) {
                                FontLog.appendLog(TAG + "onSuccess : RESPONSE_TYPE_COMMAND : " +response.responseCommand,'d');
                                // TBD
                                switch (response.iresponseCommand) {
                                    case COMMAND_CANCELFLIGHT:
                                        if (SessionProp.pIsRoad) break; /// just ignore the request
                                        else {
                                            Toast.makeText(ctxApp, R.string.driving, Toast.LENGTH_LONG).show();
                                            FontLog.appendLog(TAG + "COMMAND_CANCELFLIGHT request", 'd');
                                            flight.set_flightRequest(FLIGHTREQUEST.TERMINATE_FLIGHT);
//                                            sqlHelper.flightLocationsDelete(response.responseFlightNum);
//                                            MainActivity.set_isMultileg(false);
//                                            activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_FLIGHT_CANCELED);
                                            break;
                                        }
                                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                                        FontLog.appendLog(TAG + "COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN request",'d');
                                        flight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_SPEED_BELOW_MIN);
//                                        for (Flight f : flightList) {
//                                            if (f.flightNumber.equals(response.responseFlightNum)&&f.fStatus.equals(FSTATUS.ACTIVE)) {
//                                                activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST);
//                                            }
//                                        }
                                        break;
                                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                                        FontLog.appendLog(TAG + "COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED request",'d');
                                        flight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED);
//                                        for (Flight f : flightList) {
//                                            if (f.flightNumber.equals(response.responseFlightNum)&&f.fStatus.equals(FSTATUS.ACTIVE)) {
//                                                activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_POINTS_LIMIT_REACHED);
//                                            }
//                                        }
                                        break;
                                    case COMMAND_FLIGHT_STATE_PENDING:
                                        break;
                                    case -1:
                                        break;
                                }
                            }
                            if (response.responseDataLoad!=null){
                                FontLog.appendLog(TAG + "Data response : "+response.responseDataLoad,'d');
                            }
                        } catch (Exception e) {
                            FontLog.appendLog(TAG + "onSuccess : EXCEPTION :" + e.getMessage(),'e');
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
                        minStartId = Collections.min(startIdDbItemId.keySet());
                        //Util.appendLog(TAG + "onFinish Remove startId= " + aSyncClient.getStartID() + " min " + minStartId, 'd');
                        startIdDbItemId.remove(aSyncClient.getStartID());
                        if (failureCounter>MAX_FAILURE_COUNT){};
                        stopSelf(minStartId);
                    }
                });
            }
            catch (Exception e){
                FontLog.appendLog(TAG+"sendData"+e.getMessage(),'d');
                return;
            }
//        }
//        else {
//            Util.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
//            Toast.makeText(ctx, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
//        }
    }
    public void onDestroy() {
        FontLog.appendLog(TAG + "onDestroy minStartId= "+ minStartId, 'd');
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        FontLog.appendLog(TAG + "onTaskRemoved",'d');
        super.onTaskRemoved(rootIntent);
        //ReceiverRouter.alarmDisable = true;
        stopSelf();
    }
}
