package com.flightontrack.flight;

import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.Response;
import com.flightontrack.log.FontLog;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;
import com.flightontrack.shared.Util;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.shared.Const.REQUEST_STOP_FLIGHT;
import static com.flightontrack.shared.Const.ROUTE_NUMBER_DEFAULT;
import static com.flightontrack.shared.Props.SessionProp.sqlHelper;
import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;

/**
 * Created by hotvk on 1/16/2018.
 */

public class FlightOffline {
    private static final String TAG = "FlightOffline:";
    public String flightNumber;
    public String tempFlightNumber;
    public boolean isGetFlightNumber = true;
    //public FSTATUS fStatus = FSTATUS.PASSIVE;
//    FACTION lastAction = FACTION.DEFAULT_REQUEST;
//    EVENT lastEvent =EVENT.DEFAULT_EVENT;
    boolean isSpeedAboveMin;
    public String flightTimeString;
    public int lastAltitudeFt;
    public int _wayPointsCount;
    private Route route;
    private float _speedCurrent = 0;
    private float speedPrev = 0;
    private boolean isLimitReached;
    private long _flightStartTimeGMT;
    private int _flightTimeSec;
    //private int flightRequestCounter;
    private boolean isElevationCheckDone;
    private double cutoffSpeed;
    boolean isGetFlightCallSuccess = false;
    //boolean isTempFlightNum = true;

    FlightOffline(String fn) {
        tempFlightNumber = fn;
        //getOfflineFlightID();
    }

    ;

    void getOfflineFlightID() {

        FontLog.appendLog(TAG + "getNewOfflineFlightID", 'd');
        //EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
        RequestParams requestParams = new RequestParams();

        requestParams.put("rcode", Const.REQUEST_FLIGHT_NUMBER);
        requestParams.put("phonenumber", MyPhone._myPhoneId); // Util.getMyPhoneID());
        requestParams.put("username", Pilot.getPilotUserName());
        requestParams.put("userid", Pilot.getUserID());
        requestParams.put("deviceid", MyPhone._myDeviceId);
        requestParams.put("aid", MyPhone.getMyAndroidID());
        requestParams.put("versioncode", String.valueOf(MyPhone.versionCode));
        requestParams.put("AcftNum", Util.getAcftNum(4));
        requestParams.put("AcftTagId", Util.getAcftNum(5));
        requestParams.put("AcftName", Util.getAcftNum(6));
//        requestParams.put("isFlyingPattern", Props.SessionProp.pIsMultileg);
//        requestParams.put("freq", Integer.toString(Props.SessionProp.pIntervalLocationUpdateSec));
//        long speed_thresh = Math.round(Props.SessionProp.pSpinnerMinSpeed);
//        requestParams.put("speed_thresh", String.valueOf(speed_thresh));
        requestParams.put("isdebug", Props.SessionProp.pIsDebug);
        requestParams.put("routeid", ROUTE_NUMBER_DEFAULT);
        isGetFlightNumber = false;

        new AsyncHttpClient().post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getNewFlightID OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));
                        //char responseType = response.responseType;

                        if (response.responseNotif != null) {
                            FontLog.appendLog(TAG + "RESPONSE_TYPE_NOTIF: " + response.responseNotif, 'd');
                            Toast.makeText(ctxApp, "Cant get flight number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.responseFlightNum != null) {
                            {
                                flightNumber = response.responseFlightNum;
                                isGetFlightCallSuccess = true;
                                replaceFlightNumber(response.responseFlightNum);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        if (mainactivityInstance != null)
                            Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFinish() {
                        //FontLog.appendLog(TAG + "onFinish: FlightNumber: " + flightNumber, 'd');
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        FontLog.appendLog(TAG + "getNewFlightID onRetry:" + retryNo, 'd');
                    }
                }
        );
        requestParams = null;
    }

    void getCloseFlight() {
        FontLog.appendLog(TAG + "getOfflineFlightID", 'd');
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_STOP_FLIGHT);
        requestParams.put("speedlowflag", 0);
        requestParams.put("isLimitReached", 0);
        requestParams.put("flightid", flightNumber);
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", Props.SessionProp.pIsDebug);

        new AsyncHttpClient().post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getOfflineFlightID OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));

                        if (response.responseAckn != null) {
                            FontLog.appendLog(TAG + "onSuccess|Flight closed: " + flightNumber, 'd');
                        }
                        if (response.responseNotif != null) {
                            FontLog.appendLog(TAG + "onSuccess|RESPONSE_TYPE_NOTIF:" + response.responseNotif, 'd');
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        FontLog.appendLog(TAG + "getOfflineFlightID onFailure: " + flightNumber, 'd');

                    }

                    public void onFinish() {

                    }
                }
        );
    }

    void replaceFlightNumber(String pFlightNum) {
        if (sqlHelper.updateTempFlightNum(tempFlightNumber, flightNumber) == 1) {
            EventBus.distribute(new EventMessage(EventBus.EVENT.FLIGHT_OFFLINE_DBUPDATE_COMPLETED)
                    .setEventMessageValueString(flightNumber)
                    .setEventMessageValueObject(this));
        }
        ;
    }

    int getLocationFlightCount() {
         return sqlHelper.getLocationFlightCount(flightNumber);
    }
}
