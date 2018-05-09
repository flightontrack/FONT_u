package com.flightontrack.receiver;

import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityRequestHealthCheck;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.other.AlarmManagerCtrl;
import com.flightontrack.ui.MainActivity;
import com.flightontrack.locationclock.SvcLocationClock;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.AppConfig.pIsAppTypePublic;

public class ReceiverHealthCheckAlarm extends WakefulBroadcastReceiver {
    private static final String TAG = "ReceiverHealthCheckAlarm";
    public static boolean alarmDisable = false;
    public static boolean isRestart = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isRestart = false;
        if(!MainActivity.isMainActivityExist()){
            new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!! MainActivity is killed ...... returning",'d'));
            return;
        }

        if(alarmDisable) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "!!!! Alarm Disabled",'d'));
            return;
        }

        if(!alarmDisable && !pIsAppTypePublic) {
            healthCheckComm(context);

            if (!SvcLocationClock.isInstanceCreated()) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "Restarting : performClick()",'d'));
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
//        RequestParams requestParams = new RequestParams();
//        requestParams.put("rcode", REQUEST_IS_CLOCK_ON);
//        requestParams.put("isrestart", isRestart);
//        requestParams.put("phonenumber", MyPhone._myPhoneId);
//        requestParams.put("deviceid", MyPhone._myDeviceId);
//        requestParams.put("isClockOn", SvcLocationClock.isInstanceCreated());
//        //requestParams.put("flightid", activeRoute.activeFlight==null?FLIGHT_NUMBER_DEFAULT : activeRoute.activeFlight.flightNumber);
//        //requestParams.put("isdebug", Util.getIsDebug());
//        requestParams.put("isdebug", SessionProp.pIsDebug);
//        requestParams.put("battery", ReceiverBatteryLevel.getBattery());

        try (
                FontLogAsync mylog = new FontLogAsync();

                HttpJsonClient client = new HttpJsonClient(new EntityRequestHealthCheck());
        )
        {
            mylog.execute(new EntityLogMessage(TAG, "healthCheckComm", 'd'));
            client.post(
            new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                                mylog.execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess", 'd'));
                                ResponseJsonObj response = new ResponseJsonObj(jsonObject);

                                if (response.isException= true) {
                                    mylog.execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess|Exception|" + response.responseException, 'd'));
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                mylog.execute(new EntityLogMessage(TAG, "healthCheckComm onFailure", 'd'));
                            }

                            public void onFinish() {

                            }
                        }
            );
        }
        catch (Exception e){}

    }
}
