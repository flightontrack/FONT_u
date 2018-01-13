package com.flightontrack.flight;

import android.content.ContentValues;
import android.location.Location;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.communication.Response;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLog;
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
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import static com.flightontrack.flight.Flight.F_ACTION.RAISE_EVENT;
import static com.flightontrack.flight.Flight.F_ACTION.TERMINATE_FLIGHT;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class Flight implements GetTime,EventBus {
    public static enum F_ACTION {
        DEFAULT_REQUEST,
        REQUEST_FLIGHT,
        CHANGESTATE_STATUSACTIVE,
        CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT,
        CHANGESTATE_INFLIGHT,
        TERMINATE_GETFLIGHTNUM,
        CLOSE_FLIGHT,
        TERMINATE_FLIGHT,
        CLOSED,
        REQUEST_FLIGHTNUMBER,
        RAISE_EVENT
    }
    //public static final String FLIGHT_NUMBER_DEFAULT = "00";
    private static final String TAG = "Flight:";
    public String flightNumber;
    public boolean isGetFlightNumber = true;
    //public FSTATUS fStatus = FSTATUS.PASSIVE;
    F_ACTION lastAction = F_ACTION.DEFAULT_REQUEST;
    EVENT lastEvent =EVENT.DEFAULT_EVENT;
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

    public Flight(Route r) {
        route = r;
        flightTimeString = FLIGHT_TIME_ZERO;
        flightNumber = FLIGHT_NUMBER_DEFAULT;
        isElevationCheckDone = false;
        r.activeFlight=this;
        set_fAction(F_ACTION.REQUEST_FLIGHT);
    }

    public void set_fAction(F_ACTION request) {
        FontLog.appendLog(TAG + flightNumber+":fACTION :" + request, 'd');
//        switch (fStatus) {
//            case ACTIVE:
                switch (request) {
                    case RAISE_EVENT:
                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                .setEventMessageValueBool(isGetFlightCallSuccess)
                                .setEventMessageValueString(flightNumber));
                        route._legCount++;
                        break;
                    case TERMINATE_GETFLIGHTNUM:
                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                                .setEventMessageValueBool(isGetFlightCallSuccess=false));
                        break;
                    case CHANGESTATE_INFLIGHT:
                        /// reset Timer 1 to slower rate
                        _flightStartTimeGMT = getTimeGMT();
                        SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                        //route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
                        //lastAction = request;
                        break;
//                    case FLIGHTTIME_UPDATE:
//                        set_flightTimeSec();
//                        //route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
//                        break;
//                    case CHANGESTATE_SPEED_BELOW_MIN:
//                        isSpeedAboveMin = false;
//                        //route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_LEG_LIMIT_REACHED);
//                        set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
//                        break;
//                    case CHANGESTATE_COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
//                        isLimitReached = true;
//                        //route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_LEG_LIMIT_REACHED);
//                        set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
//                        break;
//                    case TERMINATE_FLIGHT:
//                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
//                        //fStatus = FSTATUS.PASSIVE;
//                        lastAction = request;
//                        //sqlHelper.flightLocationsDelete(flightNumber);
//                        //SessionProp.set_isMultileg(false);
//                        //route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_LEG_LIMIT_REACHED);
//                        break;
                    case CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT:
                        //fStatus = FSTATUS.PASSIVE;
                        //lastAction = request;
                        //setFlightClosed();
                        ///rethrow and close flight if no locations left
                        set_fAction(F_ACTION.CLOSE_FLIGHT);
                        break;
                    case REQUEST_FLIGHTNUMBER:
                        /// request flight number if the flight on temp number
                        if (isGetFlightNumber) getNewFlightID();
                        break;
                    //case ON_SERVER_N0TIF:
                    //route.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE_TIMER_CLOCKONLY);
                    // break;
//                }
//                break;
            //case PASSIVE:
                //switch (request) {
                    case REQUEST_FLIGHT:

                        getNewFlightID();
                        break;
                    case CHANGESTATE_STATUSACTIVE:
                        //fStatus = FSTATUS.ACTIVE;
                        //lastAction = request;
//                        if (SvcLocationClock.isInstanceCreated()) {
//                            SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_LOCATION);
//                        }
                        break;
                    case CLOSE_FLIGHT:
                        if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                            //lastAction = request;
                            getCloseFlight();
                        }
                        //setFlightClosed(request);
                        break;
                    case CLOSED:
                        //lastAction = request;
                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED));
                        //route.set_RouteRequest(ROUTEREQUEST.ON_CLOSE_FLIGHT);
                        break;

//                }
//                break;
                }
        lastAction = request;
    }

    void set_wayPointsCount(int pointsCount) {
        _wayPointsCount = pointsCount;
        if (pointsCount >= Util.getWayPointLimit()) {
            EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONPOINTSLIMITREACHED));
            //route.set_RouteRequest(ROUTEREQUEST.CLOSE_POINTS_LIMIT_REACHED);
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
            // FontLog.appendLog(TAG + "set_speedCurrent: Reported speed is ZERO", 'd');
        }
        // FontLog.appendLog(TAG + "set_speedCurrent: " + _speedCurrent, 'd');
    }

    boolean isDoubleSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (_speedCurrent >= cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev >= cutoffSpeed);
        //FontLog.appendLog(TAG + "isDoubleSpeedAboveMin: cutoffSpeed: " + cutoffSpeed, 'd');
        FontLog.appendLog(TAG + "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
        else if (Route.activeRoute.activeFlight.lastAction == F_ACTION.CHANGESTATE_INFLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
            if (isPrevSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

//    boolean isCurrentSpeedAboveMin() {
//        cutoffSpeed = get_cutoffSpeed();
//        FontLog.appendLog(TAG + "isCurrSpeedAboveMin:" + (_speedCurrent > cutoffSpeed), 'd');
//        return _speedCurrent > cutoffSpeed;
//    }

    public void getNewFlightID() {

        FontLog.appendLog(TAG + "getNewFlightID", 'd');
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_STARTED));
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
        requestParams.put("freq", Integer.toString(SessionProp.pIntervalLocationUpdateSec));
        long speed_thresh = Math.round(SessionProp.pSpinnerMinSpeed);
        requestParams.put("speed_thresh", String.valueOf(speed_thresh));
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", SessionProp.pIsDebug);
        if (!(route.routeNumber == null)) requestParams.put("routeid", route.routeNumber);
        isGetFlightNumber = false;
//        requestParams.setUseJsonStreamer(true);
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
                            if (flightNumber == FLIGHT_NUMBER_DEFAULT) {
                                flightNumber = response.responseFlightNum;
                                isGetFlightCallSuccess = true;
                                set_fAction(F_ACTION.CHANGESTATE_STATUSACTIVE);
                            } else {
                                replaceFlightNumber(response.responseFlightNum);
                            }
                            //isGetFlightNumber = false;
                            //route._legCount++;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        //flightRequestCounter++;
                        //FontLog.appendLog(TAG + "getNewFlightID onFailure:" + flightRequestCounter, 'd');
                        Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                        if (flightNumber == null) {
//                            try {
//                                String dt = URLEncoder.encode(getDateTimeNow(), "UTF-8");
//                                //flightNumber = sqlHelper.getNewTempFlightNum(flightNumber, route.routeNumber, dt);
//                            } catch (UnsupportedEncodingException e1) {
//                                e1.printStackTrace();
//                            }
                        }
                        //if(SvcLocationClock.isInstanceCreated()) ctxApp.stopService(new Intent(ctxApp, SvcLocationClock.class));
                    }

                    @Override
                    public void onFinish() {

                        FontLog.appendLog(TAG + "onFinish: FlightNumber: " + flightNumber, 'd');
                        set_fAction(RAISE_EVENT);
//                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
//                            .setEventMessageValueBool(isGetFlightCallSuccess)
//                            .setEventMessageValueString(flightNumber));
//                        route._legCount++;
                        //set_FlightNumber(flightNumber);
//                        if (flightNumber == null)
//                            //it is never null now;
//                            route.set_RouteRequest(ROUTEREQUEST.RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER);
//                        else
//                            route.set_RouteRequest(ROUTEREQUEST.SWITCH_TO_PENDING);
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
        FontLog.appendLog(TAG + "getCloseFlight", 'd');
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_STOP_FLIGHT);
        requestParams.put("speedlowflag", isSpeedAboveMin);
        requestParams.put("isLimitReached", isLimitReached);
        requestParams.put("flightid", flightNumber);
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", SessionProp.pIsDebug);

        new AsyncHttpClient().post(Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getCloseFlight OnSuccess", 'd');
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
                        FontLog.appendLog(TAG + "getCloseFlight onFailure: " + flightNumber, 'd');

                    }

                    public void onFinish() {
                        set_fAction(F_ACTION.CLOSED);
                    }
                }
        );
        requestParams = null;
    }

    public void onClock(final Location location) {

        float speedCurrent = location.getSpeed();
        //FontLog.appendLog(TAG + "onClock: reported speed: " + speedCurrent, 'd');
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = isDoubleSpeedAboveMin();
        switch (lastAction) {
            case CHANGESTATE_STATUSACTIVE:
                if (isSpeedAboveMin) set_fAction(F_ACTION.CHANGESTATE_INFLIGHT);
                break;
            case CHANGESTATE_INFLIGHT:
                if (!isElevationCheckDone) {
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                //set_fAction(F_ACTION.FLIGHTTIME_UPDATE);
                set_flightTimeSec();
                //if (!isSpeedAboveMin) route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_LEG_LIMIT_REACHED);
                if (!isSpeedAboveMin) {
                    set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
                    EventBus.distribute(new EventMessage(EVENT.FLIGHT_ONSPEEDLOW));
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
            values.put(DBSchema.LOC_isTempFlight, !isGetFlightCallSuccess); //istempflightnum
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
            values.put(DBSchema.COLUMN_NAME_COL13, iselevecheck);
            long r = sqlHelper.rowLocationInsert(values);
            if (r > 0) {
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                FontLog.appendLog(TAG + "saveLocation: dbLocationRecCount: " + SessionProp.dbLocationRecCount, 'd');
            }
        } catch (Exception e) {
            FontLog.appendLog(TAG + "SQLite Exception Placeholder", 'e');
        }
    }

    public void set_flightTimeSec() {
        long elapsedTime = getTimeGMT() - _flightStartTimeGMT;
        _flightTimeSec = (int) elapsedTime / 1000;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
        EventBus.distribute(new EventMessage(EVENT.FLIGHT_FLIGHTTIME_UPDATE_COMPLETED));
    }

    private double get_cutoffSpeed() {
        return SessionProp.pSpinnerMinSpeed * (Route.activeRoute.activeFlight.lastAction == F_ACTION.CHANGESTATE_INFLIGHT ? 0.75 : 1.0);
    }

    void replaceFlightNumber(String pFlightNum) {

    }

    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + flightNumber+":eventReceiver:"+ev, 'd');
        switch (ev) {
            case CLOCK_ONTICK:
                if (!(eventMessage.eventMessageValueLocation==null && route.activeFlight==this)) onClock(eventMessage.eventMessageValueLocation);
                if (Util.isNetworkAvailable()) {
                    if (lastAction == F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT) {
                        set_fAction(F_ACTION.CLOSE_FLIGHT);
                    }
                }
                break;
            case SVCCOMM_ONSUCCESS_COMMAND:
                int server_command = eventMessage.eventMessageValueInt;
                FontLog.appendLog(TAG + "server_command int: "+server_command, 'd');
                //fStatus = FSTATUS.PASSIVE;
                switch (server_command) {
                    case COMMAND_TERMINATEFLIGHT:
                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
                        lastAction = TERMINATE_FLIGHT;
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        isSpeedAboveMin = false;
                        set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        isLimitReached = true;
                        set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
                        break;
                }
            break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if(lastAction == F_ACTION.REQUEST_FLIGHT) set_fAction(F_ACTION.TERMINATE_GETFLIGHTNUM);
                else set_fAction(F_ACTION.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
                break;
            case SQL_TEMPFLIGHTNUM_ALLOCATED:
                flightNumber=eventMessage.eventMessageValueString;
                isGetFlightCallSuccess=true;
                set_fAction(RAISE_EVENT);
                break;
        }
        lastEvent=ev;
    }
}
