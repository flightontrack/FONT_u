package com.flightontrack.receiver;

import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.flightontrack.log.FontLog;
import com.flightontrack.other.AlarmManagerCtrl;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.other.MyApplication;
import com.flightontrack.R;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Util;
import com.flightontrack.communication.Response;
import com.flightontrack.pilot.MyPhone;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.AppConfig.pIsAppTypePublic;

public class ReceiverHealthCheckAlarm extends WakefulBroadcastReceiver {
    private static final String TAG = "ReceiverHealthCheckAlarm:";
    public static boolean alarmDisable = false;
    public static boolean isRestart = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isRestart = false;
        if(!MainActivity.isMainActivityExist()){
            FontLog.appendLog(TAG+ "!!!! MainActivity is killed ...... returning",'d');
            return;
        }

        if(alarmDisable) {
            FontLog.appendLog(TAG+ "!!!! Alarm Disabled",'d');
            return;
        }

        if(!alarmDisable && !pIsAppTypePublic) {
            healthCheckComm(context);

            if (!SvcLocationClock.isInstanceCreated()) {
                FontLog.appendLog(TAG+ "Restarting : performClick()",'d');
                SessionProp.set_isMultileg(true);
                mainactivityInstance.trackingButton.performClick();
                isRestart = true;
                healthCheckComm(context);
            }

            AlarmManagerCtrl.setAlarm();

            return;
        }

    }
    void healthCheckComm(Context ctx) {
        FontLog.appendLog(TAG+ "getCloseFlight",'d');
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_IS_CLOCK_ON);
        requestParams.put("isrestart", isRestart);
        requestParams.put("phonenumber", MyPhone._myPhoneId);
        requestParams.put("deviceid", MyPhone._myDeviceId);
        requestParams.put("isClockOn", SvcLocationClock.isInstanceCreated());
        //requestParams.put("flightid", activeRoute.activeFlight==null?FLIGHT_NUMBER_DEFAULT : activeRoute.activeFlight.flightNumber);
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", SessionProp.pIsDebug);
        requestParams.put("battery", ReceiverBatteryLevel.getBattery());

        new AsyncHttpClient().post(Util.getTrackingURL() + ctx.getString(R.string.aspx_communication), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "healthCheckComm OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));

                        if (response.responseAckn != null) {
                            FontLog.appendLog(TAG + "onSuccess|HealthCheck: "+response.responseAckn,'d');
                        }
                        if (response.responseNotif != null) {
                            FontLog.appendLog(TAG + "onSuccess|HealthCheck|RESPONSE_TYPE_NOTIF:" +response.responseNotif,'d');
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        FontLog.appendLog(TAG + "onFailure|HealthCheck: " ,'d');

                    }
                    public void onFinish() {

                    }
                }
        );

    }
}
