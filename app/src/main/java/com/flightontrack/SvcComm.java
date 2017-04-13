package com.flightontrack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import cz.msebera.android.httpclient.Header;
import static com.flightontrack.Const.*;
import static com.flightontrack.Route.currentFlights;

public class SvcComm extends Service {
    public SvcComm() {}
    private static final String TAG = "SvcComm:";
    private RequestParams requestParams = new RequestParams();
    private static Context ctx;
    private int dbItemId;
    private int trackPointNumber;
    private String flightID;
    private Integer minStartId;
    private static Integer failureCounter=0;
    static Integer commBatchSize = 1;
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
        Util.appendLog(TAG+ "ServiceComm  - onStartCommand requestId : " + startId,'d');
        Bundle extras = intent.getExtras();
        setRequest(extras);
        if (startIdDbItemId.containsValue(dbItemId)) return START_STICKY;
        startIdDbItemId.put(startId,dbItemId);
        if (startIdDbItemId.size()>1){
            for (Map.Entry<Integer, Integer> entry : startIdDbItemId.entrySet()) {
                Util.appendLog("startId = " + entry.getKey() + ", dbItemId = " + entry.getValue(),'d');
            }
        }
        //trackPointNumber
        sendData(startId);
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
    }
    public void setRequest(Bundle extras){

        dbItemId = (int) extras.getLong("itemId");
        trackPointNumber = extras.getInt("wp");
        flightID = extras.getString("ft");
        requestParams.put("isdebug", MainActivity.AppProp.pIsDebug);
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
                Util.appendLog(TAG + "Send: flight: " + flightID + " dbItemId :" + String.valueOf(dbItemId) + " point: " + trackPointNumber,'d');
                //AsyncHttpClient aSyncClient = new AsyncHttpClient();
                final LoopjAClient aSyncClient = new LoopjAClient(startId);
                aSyncClient.post(Util.getTrackingURL() + ctx.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        failureCounter=0;
                        commBatchSize = COMM_BATCH_SIZE_MAX;
                        Response response = new Response(new String(responseBody));
                        //Util.appendLog(TAG+ "onSuccess Got response : " + responseBody,'d');
                        if (response.jsonErrorCount>0) {
                            if (response.jsonErrorCount>MAX_JSON_ERROR) Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                            return;
                        }
                        try {
                            if (response.responseAckn != null) {
                                Util.appendLog(TAG + "onSuccess RESPONSE_TYPE_ACKN :flight:" + response.responseFlightNum+":"+response.responseAckn, 'd');
                                Route.sqlHelper.rowLocationDelete(response.iresponseAckn, response.responseFlightNum);  /// TODO should be moved to Router
                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.ON_COMMUNICATION_SUCCESS);
                            }
                            if (response.responseNotif != null) {
                                Util.appendLog(TAG + "onSuccess :RESPONSE_TYPE_NOTIF :" + response.responseNotif,'d');
                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_FLIGHT_DELETE_ALL_POINTS);
                            }
                            if (response.responseCommand != null) {
                                Util.appendLog(TAG + "onSuccess : RESPONSE_TYPE_COMMAND : " +response.responseCommand,'d');
                                // TBD
                                switch (response.iresponseCommand) {
                                    case COMMAND_CANCELFLIGHT:
                                        if (Route._isRoad) break; /// just ignore the request
                                        else {
                                            Toast.makeText(ctx, R.string.driving, Toast.LENGTH_LONG).show();
                                            Util.appendLog(TAG + "COMMAND_CANCELFLIGHT request", 'd');
                                            Route.sqlHelper.flightLocationsDelete(response.responseFlightNum);
                                            MainActivity.set_isMultileg(false);
                                            Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_FLIGHT_CANCELED);
                                            break;
                                        }
                                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                                        Util.appendLog(TAG + "COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN request",'d');
                                        //Route.sqlHelper.flightLocationsDelete(response.responseFlightId);
                                        for (FlightInstance f : currentFlights ) {
                                            if (f.flightNumber.equals(response.responseFlightNum)&&f.fStatus.equals(FSTATUS.ACTIVE)) {
                                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST);
                                            }
                                        }
                                        break;
                                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                                        Util.appendLog(TAG + "COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED request",'d');
                                        for (FlightInstance f : currentFlights ) {
                                            if (f.flightNumber.equals(response.responseFlightNum)&&f.fStatus.equals(FSTATUS.ACTIVE)) {
                                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_POINTS_LIMIT_REACHED);
                                            }
                                        }
                                        break;
                                    case COMMAND_FLIGHT_STATE_PENDING:
                                        break;
                                    case -1:
                                        break;
                                }
                            }
                            if (response.responseDataLoad!=null){
                                Util.appendLog(TAG + "Data response : "+response.responseDataLoad,'d');
                            }
                        } catch (Exception e) {
                            Util.appendLog(TAG + "onSuccess : EXCEPTION :" + e.getMessage(),'e');
                        } finally {
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Util.appendLog(TAG + "onFailure; startId= " + aSyncClient.getStartID(), 'd');
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
                Util.appendLog(TAG+"sendData"+e.getMessage(),'d');
                return;
            }
//        }
//        else {
//            Util.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
//            Toast.makeText(ctx, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
//        }
    }
    public void onDestroy() {
        Util.appendLog(TAG + "onDestroy minStartId= "+ minStartId, 'd');
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Util.appendLog(TAG + "onTaskRemoved",'d');
        super.onTaskRemoved(rootIntent);
        //ReceiverRouter.alarmDisable = true;
        stopSelf();
    }
}
