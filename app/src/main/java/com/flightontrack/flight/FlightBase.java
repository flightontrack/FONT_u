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

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.shared.Props.*;

public class FlightBase implements EventBus{
    static final String TAG = "FlightBase:";

    public enum FSTATE {
        DEFAULT,
        GETTINGFLIGHT,
        PENDING,
        INFLIGHT,
        READY_TOSENDLOCATIONS,
        READY_TOBECLOSED,
        CLOSING,
        CLOSED
    }

    public String flightNumber = FLIGHT_NUMBER_DEFAULT;
    boolean isTempFlightNum = false;
    public FSTATE flightState = FSTATE.DEFAULT;
    boolean isLimitReached  = false;

    public FlightBase(){}

    FlightBase(String fn) {
        flightNumber = fn;
    }

    public void set_flightState(FSTATE fs){
        if (flightState == fs) return;
        flightState = fs;
        switch(fs){
            case READY_TOSENDLOCATIONS:
                EventBus.distribute(new EventMessage(EventBus.EVENT.FLIGHT_STATECHANGEDTO_READYTOSEND)
                        .setEventMessageValueString(flightNumber)
                        .setEventMessageValueObject(this));
                break;
            case READY_TOBECLOSED:
                getCloseFlight();
                break;
            case CLOSED:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
                break;
            case GETTINGFLIGHT:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
                getNewFlightID();
                break;
        }
    }
    public void set_flightNumber(String fn){
        FontLog.appendLog(TAG + "set_flightNumber super fn " + fn, 'd');
        replaceFlightNumber(fn);
        set_flightState(FSTATE.READY_TOSENDLOCATIONS);
    }

    void getNewFlightID() {

        FontLog.appendLog(TAG + "FlightBase-getNewFlightID: " +flightNumber, 'd');
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
        requestParams.put("isFlyingPattern", Props.SessionProp.pIsMultileg);
        requestParams.put("freq", Integer.toString(Props.SessionProp.pIntervalLocationUpdateSec));
        long speed_thresh = Math.round(Props.SessionProp.pSpinnerMinSpeed);
        requestParams.put("speed_thresh", String.valueOf(speed_thresh));
        requestParams.put("isdebug", Props.SessionProp.pIsDebug);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(2,1000);
        client.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    FontLog.appendLog(TAG + "FlightBase-getNewFlightID OnSuccess", 'd');
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
                            //replaceFlightNumber();
                            set_flightNumber(response.responseFlightNum);
                        }
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    if (mainactivityInstance != null){
                        Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                    }
                    EventBus.distribute(new EventMessage(EVENT.FLIGHTBASE_GETFLIGHTNUM).setEventMessageValueBool(false));
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
        client=null;
        requestParams = null;
    }

    void getCloseFlight() {
        FontLog.appendLog(TAG + "getCloseFlight: " + flightNumber, 'd');
        set_flightState(FSTATE.CLOSING);
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_STOP_FLIGHT);
        //requestParams.put("speedlowflag", isSpeedAboveMin);
        requestParams.put("isLimitReached", isLimitReached);
        requestParams.put("flightid", flightNumber);
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", Props.SessionProp.pIsDebug);

        new AsyncHttpClient().post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getNewFlightID OnSuccess", 'd');
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
                        FontLog.appendLog(TAG + "getNewFlightID onFailure: " + flightNumber, 'd');

                    }
                    @Override
                    public void onFinish() {
                        set_flightState(FSTATE.CLOSED);
                    }
                }
        );
    }

    void replaceFlightNumber(String fnew) {
        if (sqlHelper.updateTempFlightNum(flightNumber, fnew) > 0) {
            FontLog.appendLog(TAG + "replaceFlightNumber: " + flightNumber+"->" +fnew, 'd');
        }
        flightNumber = fnew;
    }

    int getLocationFlightCount() {
         return sqlHelper.getLocationFlightCount(flightNumber);
    }
}
