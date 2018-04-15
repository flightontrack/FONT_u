package com.flightontrack.flight;

import android.content.ContentValues;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.HttpJsonClient;
import com.flightontrack.communication.Response;
import com.flightontrack.communication.ResponseJsonObj;
import com.flightontrack.entities.EntityRequestCloseFlight;
import com.flightontrack.entities.EntityRequestNewFlight;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.GetTime;
import com.flightontrack.shared.Props;
import com.flightontrack.shared.Util;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.flight.FlightOffline.FLIGHTNUMBER_SRC.REMOTE_DEFAULT;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class FlightOnline extends FlightOffline implements GetTime, EventBus {
    static final String TAG = "Flight";

    public boolean isGetFlightNumber = true;
    public String flightTimeString;
    public int lastAltitudeFt;
    public int _wayPointsCount;
    Route route;
    float _speedCurrent = 0;
    float speedPrev = 0;
    int _flightTimeSec;
    private long _flightStartTimeGMT;
    boolean isElevationCheckDone;
    double cutoffSpeed;
    boolean isGettingFlight = false;
    boolean isGetFlightCallSuccess = false;
    List<Integer> dbIdList = new ArrayList<>();

    public FlightOnline(Route r) {
        route = r;
        flightTimeString = FLIGHT_TIME_ZERO;
        isElevationCheckDone = false;
        r.activeFlight = this;
        set_flightState(FLIGHT_STATE.GETTINGFLIGHT);
    }

    public void set_flightNumber(String fn) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, " set_flightNumber " + fn + " flightNumStatus " + flightNumStatus, 'd'));
        replaceFlightNumber(fn);
        switch (flightNumStatus) {
            case REMOTE_DEFAULT:
                set_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
            case LOCAL:
                set_flightNumStatus(REMOTE_DEFAULT);
                break;
        }
    }

    void set_wayPointsCount(int pointsCount) {
        _wayPointsCount = pointsCount;
        if (pointsCount >= Util.getWayPointLimit()) {
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONPOINTSLIMITREACHED));
            //route.set_rAction(RACTION.CLOSE_POINTS_LIMIT_REACHED);
        }
    }

    void set_speedCurrent(float speed) {
        /// gps can report speed equal 0 in flight  which should be ignored.
        if ((speed > 0.0) | SessionProp.pIsDebug) {
            speedPrev = _speedCurrent;
            /// this 0.1 is needed to start flight whe Flight min speed set to 0;
            _speedCurrent = speed + (float) 0.01;
        } else {
            /// this condition never happen when writing a log file because SessionProp.pIsDebug == true
            // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: Reported speed is ZERO", 'd');
        }
        // new FontLogAsync().execute(new LogMessage(TAG, "set_speedCurrent: " + _speedCurrent, 'd');
    }


    boolean isDoubleSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (_speedCurrent >= cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev >= cutoffSpeed);
        //new FontLogAsync().execute(new LogMessage(TAG, "isDoubleSpeedAboveMin: cutoffSpeed: " + cutoffSpeed, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
            //else if (RouteBase.activeFlight.lastAction == FACTION.CHANGE_IN_FLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
        else if (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd'));
            if (isPrevSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

    void getNewFlightID() {
        isGettingFlight = true;
            EntityRequestNewFlight entityRequestNewFlight = new EntityRequestNewFlight()
            .set("phonenumber", MyPhone._myPhoneId)
            .set("username", Pilot.getPilotUserName())
            .set("userid", Pilot.getUserID())
            .set("deviceid", MyPhone._myDeviceId)
            .set("aid", MyPhone.getMyAndroidID())
            .set("versioncode", String.valueOf(MyPhone.getVersionCode()))
            .set("AcftNum", Util.getAcftNum(4))
            .set("AcftTagId", Util.getAcftNum(5))
            .set("AcftName", Util.getAcftNum(6))
            .set("isFlyingPattern", String.valueOf(Props.SessionProp.pIsMultileg))
            .set("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec))
            .set("speed_thresh", String.valueOf(Math.round(SessionProp.pSpinnerMinSpeed)))
            .set("isdebug", String.valueOf(SessionProp.pIsDebug))
            .set("routeid", route.routeNumber.equals(ROUTE_NUMBER_DEFAULT)?null:route.routeNumber);
            try(HttpJsonClient client = new HttpJsonClient(entityRequestNewFlight)) {
                client.post(new JsonHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        Log.i(TAG, "oonStart " + client.urlLink   );
                    }

                    @Override
                    public void onSuccess(int code, Header[] headers, JSONObject jsonObject) {
                        //Log.i("TAG", "onSuccessjsonObject: " + jsonObject);
                        ResponseJsonObj response = new ResponseJsonObj(jsonObject);
                        if (response.responseException != null) {
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "RESPONSE_TYPE_NOTIF: " + response.responseException, 'd'));
                            Toast.makeText(mainactivityInstance, R.string.cloud_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.responseNewFlightNum != null) {
                            isGetFlightCallSuccess = true;
                            route._legCount++;
                            set_flightNumber(response.responseNewFlightNum);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure; startId= " + response, 'd'));
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: " + e, 'd'));
                        if (flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
                            Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
                            EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                    .setEventMessageValueBool(isGetFlightCallSuccess)
                                    .setEventMessageValueString(flightNumber));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String s, Throwable e) {
                        Log.i(TAG, "onFailure: " + e.getMessage());
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFailure e: " + e, 'd'));
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewFlightID onRetry:" + retryNo, 'd'));
                    }
                });
            }
            catch(Exception e) {
                new FontLogAsync().execute(new EntityLogMessage(TAG, "onException e: ", 'e'));
            }
        }
//        new FontLogAsync().execute(new EntityLogMessage(TAG, "Flight - getNewFlightID:  " + flightNumber, 'd'));
//        RequestParams requestParams = new RequestParams();
//
//        requestParams.put("rcode", Const.REQUEST_FLIGHT_NUMBER);
//        requestParams.put("phonenumber", MyPhone._myPhoneId); // Util.getMyPhoneID());
//        requestParams.put("username", Pilot.getPilotUserName());
//        requestParams.put("userid", Pilot.getUserID());
//        requestParams.put("deviceid", MyPhone._myDeviceId);
//        requestParams.put("aid", MyPhone.getMyAndroidID());
//        requestParams.put("versioncode", String.valueOf(MyPhone.versionCode));
//        requestParams.put("AcftNum", Util.getAcftNum(4));
//        requestParams.put("AcftTagId", Util.getAcftNum(5));
//        requestParams.put("AcftName", Util.getAcftNum(6));
//        requestParams.put("isFlyingPattern", Props.SessionProp.pIsMultileg);
//        requestParams.put("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec));
//        long speed_thresh = Math.round(SessionProp.pSpinnerMinSpeed);
//        requestParams.put("speed_thresh", String.valueOf(speed_thresh));
//        requestParams.put("isdebug", SessionProp.pIsDebug);
//        if (route.routeNumber != ROUTE_NUMBER_DEFAULT) requestParams.put("routeid", route.routeNumber);
//        isGetFlightNumber = false;
////        requestParams.setUseJsonStreamer(true);
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.setMaxRetriesAndTimeout(2, 2000);
//        client.post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        //String responseText = new String(responseBody);
//                        Response response = new Response(new String(responseBody));
//                        //char responseType = response.responseType;
//
//                        if (response.responseException != null) {
//                            new FontLogAsync().execute(new EntityLogMessage(TAG, "RESPONSE_TYPE_NOTIF: " + response.responseException, 'd'));
//                            Toast.makeText(mainactivityInstance, R.string.cloud_error, Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (response.responseFlightNum != null) {
//
//                            isGetFlightCallSuccess = true;
//                            route._legCount++;
//
//                            set_flightNumber(response.responseFlightNum);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                        //new FontLogAsync().execute(new LogMessage(TAG, "getNewFlightID onFailure:" + flightRequestCounter, 'd');
//                        //if (mainactivityInstance!=null) Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
//                        //if (!isTempFlightNum) if (mainactivityInstance != null) {
//                        if (flightNumStatus == REMOTE_DEFAULT) if (mainactivityInstance != null) {
//                            Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
//                            raiseEventGetFlightComleted();
//                        }
//
//                        //set_flightState(FSTATE.LOCAL);
////                        if (flightNumber == null) {
//////                            try {
//////                                String dt = URLEncoder.encode(getDateTimeNow(), "UTF-8");
//////                                //flightNumber = sqlHelper.getNewTempFlightNum(flightNumber, route.routeNumber, dt);
//////                            } catch (UnsupportedEncodingException e1) {
//////                                e1.printStackTrace();
//////                            }
////                        }
//                        //if(SvcLocationClock.isInstanceCreated()) ctxApp.stopService(new Intent(ctxApp, SvcLocationClock.class));
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "onFinish: FlightNumber: " + flightNumber, 'd'));
//                        isGettingFlight = false;
//                    }
//
//                    @Override
//                    public void onRetry(int retryNo) {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getNewFlightID onRetry:" + retryNo, 'd'));
//                    }
//                }
//        );
//        client = null;
//        requestParams = null;
//    }

//    void getCloseFlight() {
//        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight: " + flightNumber, 'd'));
//        RequestParams requestParams = new RequestParams();
//        requestParams.put("rcode", REQUEST_STOP_FLIGHT);
//        requestParams.put("speedlowflag", isSpeedAboveMin);
//        requestParams.put("isLimitReached", isLimitReached);
//        requestParams.put("flightid", flightNumber);
//        //requestParams.put("isdebug", Util.getIsDebug());
//        requestParams.put("isdebug", SessionProp.pIsDebug);
//
//        new AsyncHttpClient().post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight OnSuccess", 'd'));
//                        //String responseText = new String(responseBody);
//                        Response response = new Response(new String(responseBody));
//
//                        if (response.responseAckn != null) {
//                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|Flight closed: " + flightNumber, 'd'));
//                        }
//                        if (response.responseNotif != null) {
//                            new FontLogAsync().execute(new EntityLogMessage(TAG, "onSuccess|RESPONSE_TYPE_NOTIF:" + response.responseNotif, 'd'));
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloseFlight onFailure: " + flightNumber, 'd'));
//
//                    }
//
//                    public void onFinish() {
//                        set_flightState(FLIGHT_STATE.CLOSED);
//                    }
//                }
//        );
//        requestParams = null;
//    }

    public void saveLocCheckSpeed(final Location location) {

        float speedCurrent = location.getSpeed();
        new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocCheckSpeed: reported speed: " + speedCurrent, 'd'));
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = isDoubleSpeedAboveMin();
        //switch (lastAction) {
        switch (flightState) {
            case READY_TOSAVELOCATIONS:
                if (isSpeedAboveMin) set_flightState(FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN);
                break;
//            case CHANGE_IN_PENDING:
//                if (isSpeedAboveMin) set_fAction(FACTION.CHANGE_IN_FLIGHT);
//                break;
            //case CHANGE_IN_FLIGHT:
            case INFLIGHT_SPEEDABOVEMIN:
                if (!isElevationCheckDone) {
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                //set_fAction(FACTION.FLIGHTTIME_UPDATE);
                set_flightTimeSec();
                //if (!isSpeedAboveMin) route.set_rAction(RACTION.RESTART_NEW_FLIGHT);
                if (!isSpeedAboveMin) {
                    //set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                    set_flightState(FLIGHT_STATE.STOPPED);
                    EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDLOW).setEventMessageValueString(flightNumber));
                }
                break;
        }
    }

    private void saveLocation(Location location, boolean iselevecheck) {
        //Util.appendLog(TAG + "_____Timer 3 - saveLocation", 'd');
        try {
            //int p = activeRoute.activeFlight._wayPointsCount+1;
            int p = _wayPointsCount + 1;
            ContentValues values = new ContentValues();
            values.put(DBSchema.COLUMN_NAME_COL1, REQUEST_LOCATION_UPDATE); //rcode
            values.put(DBSchema.LOC_flightid, flightNumber); //flightid
            values.put(DBSchema.LOC_isTempFlight, flightNumStatus == FLIGHTNUMBER_SRC.LOCAL); //istempflightnum
            values.put(DBSchema.LOC_speedlowflag, !isSpeedAboveMin); /// speed low
            //values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString(speedCurrentInt)); //speed
            values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString((int) location.getSpeed())); //speed
            values.put(DBSchema.COLUMN_NAME_COL6, Double.toString(location.getLatitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL7, Double.toString(location.getLongitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL8, Float.toString(location.getAccuracy())); //accuracy
            values.put(DBSchema.COLUMN_NAME_COL9, Math.round(location.getAltitude())); //extrainfo
            values.put(DBSchema.LOC_wpntnum, p); //wpntnum
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Util.getSignalStregth())); //gsmsignal
            values.put(DBSchema.LOC_date, URLEncoder.encode(getDateTimeNow(), "UTF-8")); //date
            values.put(DBSchema.LOC_is_elevetion_check, iselevecheck);
            long r = sqlHelper.rowLocationInsert(values);
            if (r > 0) {
                dbIdList.add((int) r);
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                new FontLogAsync().execute(new EntityLogMessage(TAG, "saveLocation: dbLocationRecCountNormal: " + SessionProp.dbLocationRecCountNormal, 'd'));
            }
        } catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "SQLite Exception Placeholder", 'e'));
        }
    }

    public void set_flightTimeSec() {
        long elapsedTime = getTimeGMT() - _flightStartTimeGMT;
        _flightTimeSec = (int) elapsedTime / 1000;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_FLIGHTTIME_UPDATE_COMPLETED).setEventMessageValueString(flightNumber));
    }

    double get_cutoffSpeed() {
        return SessionProp.pSpinnerMinSpeed * (RouteBase.activeFlight.flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN ? 0.75 : 1.0);
    }

    void raiseEventGetFlightComleted() {
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                .setEventMessageValueBool(isGetFlightCallSuccess)
                .setEventMessageValueString(flightNumber));
    }

    public void set_flightState(FLIGHT_STATE fs) {
        FlightOnline.super.set_flightState(fs);
        switch (fs) {
            case READY_TOSAVELOCATIONS:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_STATECHANGEDTO_READYTOSAVE).setEventMessageValueString(flightNumber));
                //raiseEventGetFlightComleted();
                break;
            case INFLIGHT_SPEEDABOVEMIN:
                _flightStartTimeGMT = getTimeGMT();
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDABOVEMIN).setEventMessageValueString(flightNumber));
                break;
            case STOPPED:
                //EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDLOW).setEventMessageValueString(flightNumber));
                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                    set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
                }
                break;
        }
    }

    public void set_flightState(FLIGHT_STATE fs, String descr) {
        set_flightState(fs);
        new FontLogAsync().execute(new EntityLogMessage(TAG, "flightState reasoning : " + fs + ' ' + descr, 'd'));
    }

    //    void set_fAction(FACTION request) {
//        new FontLogAsync().execute(new LogMessage(TAG, flightNumber + ":fACTION :" + request, 'd');
//        lastAction = request;
////        switch (fStatus) {
////            case ACTIVE:
//        switch (request) {
////            case CHANGE_IN_PENDING:
////                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
////                        .setEventMessageValueBool(isGetFlightCallSuccess)
////                        .setEventMessageValueString(flightNumber));
////                break;
////            case TERMINATE_GETFLIGHTNUM:
////                set_flightState(FSTATE.CLOSED);
////                //EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
////                break;
////            case CHANGE_IN_FLIGHT:
////                /// reset Timer 1 to slower rate
////                _flightStartTimeGMT = getTimeGMT();
////                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
////                //route.set_rAction(RACTION.ON_FLIGHTTIME_CHANGED);
////                break;
//            case TERMINATE_FLIGHT:
//                //TODO
//                break;
////            case CLOSE_FLIGHT_IF_ZERO_LOCATIONS:
////                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
////                    set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
////                }
////                break;
//        }
//    }
    @Override
    public void onClock(EventMessage eventMessage) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, flightNumber + "onClock", 'd'));
        if (route.activeFlight == this
                && (flightState == FLIGHT_STATE.READY_TOSAVELOCATIONS || flightState == FLIGHT_STATE.INFLIGHT_SPEEDABOVEMIN)
                && eventMessage.eventMessageValueLocation != null) {
            //                    String s = Arrays.toString(Thread.currentThread().getStackTrace());
            //                    new FontLogAsync().execute(new LogMessage(TAG, "StackTrace: "+s,'d');
            saveLocCheckSpeed(eventMessage.eventMessageValueLocation);
        }
        if (Util.isNetworkAvailable() && !isGettingFlight) {
            if (flightNumStatus == FLIGHTNUMBER_SRC.LOCAL) getNewFlightID();
        }
        if (flightState == FLIGHT_STATE.STOPPED && sqlHelper.getLocationFlightCount(flightNumber) == 0) {
            set_flightState(FLIGHT_STATE.READY_TOBECLOSED);
        }
    }

    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, flightNumber + ":eventReceiver:" + ev, 'd'));
        super.eventReceiver(eventMessage);
        switch (ev) {
            case SESSION_ONSUCCESS_COMMAND:
                String server_command = eventMessage.eventMessageValueString;
                new FontLogAsync().execute(new EntityLogMessage(TAG, "server_command int: " + server_command, 'd'));
                switch (server_command) {
                    case COMMAND_TERMINATEFLIGHT:
                        isJunkFlight = true;
                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
                        set_flightState(FLIGHT_STATE.STOPPED, "Terminate flight server command");
                        //set_fAction(FACTION.TERMINATE_FLIGHT);
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        /// this server request is disabled for now
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        isLimitReached = true;
                        //set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                        set_flightState(FLIGHT_STATE.STOPPED);
                        break;
                }
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if (flightState == FLIGHT_STATE.GETTINGFLIGHT) set_flightState(FLIGHT_STATE.CLOSED);
                else set_flightState(FLIGHT_STATE.STOPPED);
                //TODO remove flight points
                break;
            case SQL_LOCALFLIGHTNUM_ALLOCATED:
                flightNumber = eventMessage.eventMessageValueString;
                flightNumStatus = FLIGHTNUMBER_SRC.LOCAL;
                isGetFlightCallSuccess = true;
                route._legCount++;
                //set_fAction(FACTION.CHANGE_IN_PENDING);
                set_flightState(FLIGHT_STATE.READY_TOSAVELOCATIONS);
                break;
        }
    }
}
