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
    void healthCheckComm(Context ctx){

        try (
                HttpJsonClient client = new HttpJsonClient(new EntityRequestHealthCheck())
        )
        {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm", 'd'));
            client.post(
            new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess", 'd'));
                                ResponseJsonObj response = new ResponseJsonObj(jsonObject);

                                if (response.isException= true) {
                                    new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onSuccess|Exception|" + response.responseException, 'd'));
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                                new FontLogAsync().execute(new EntityLogMessage(TAG, "healthCheckComm onFailure", 'd'));
                            }

                            public void onFinish() {

                            }
                        }
            );
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
