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

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

//import static com.flightontrack.flight.Flight.FACTION.CHANGE_IN_PENDING;
//import static com.flightontrack.flight.Flight.FACTION.RAISE_EVENT;
//import static com.flightontrack.flight.FlightBase.FACTION.CHANGE_IN_PENDING;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public class Flight extends FlightBase implements GetTime,EventBus {
//    public static enum FACTION {
//        DEFAULT_REQUEST,
//        REQUEST_FLIGHT,
//        CHANGE_IN_PENDING,
//        CHANGE_IN_FLIGHT,
//        //CHANGE_IN_WAIT_TO_CLOSEFLIGHT,
//        TERMINATE_GETFLIGHTNUM,
//        CLOSE_FLIGHT_IF_ZERO_LOCATIONS,
//        TERMINATE_FLIGHT,
//        CLOSED,
//        REQUEST_FLIGHTNUMBER
//    }
    //public static final String FLIGHT_NUMBER_DEFAULT = "00";
    private static final String TAG = "Flight:";
    public String flightNumber;
    public boolean isGetFlightNumber = true;
    //public FSTATUS fStatus = FSTATUS.PASSIVE;
    FACTION lastAction = FACTION.DEFAULT_REQUEST;
    EVENT lastEvent =EVENT.DEFAULT_EVENT;
    boolean isSpeedAboveMin=false;
    public String flightTimeString;
    public int lastAltitudeFt;
    public int _wayPointsCount;
    private Route route;
    private float _speedCurrent = 0;
    private float speedPrev = 0;
    private boolean isLimitReached = false;
    private long _flightStartTimeGMT;
    private int _flightTimeSec;
    //private int flightRequestCounter;
    private boolean isElevationCheckDone;
    private double cutoffSpeed;
    boolean isGetFlightCallSuccess = false;
    boolean isTempFlightNum = false;

    public Flight(){}

    public Flight(Route r) {
        route = r;
        flightTimeString = FLIGHT_TIME_ZERO;
        flightNumber = FLIGHT_NUMBER_DEFAULT;
        isElevationCheckDone = false;
        r.activeFlight=this;
        set_fAction(FACTION.REQUEST_FLIGHT);
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
            // FontLog.appendLog(TAG + "set_speedCurrent: Reported speed is ZERO", 'd');
        }
        // FontLog.appendLog(TAG + "set_speedCurrent: " + _speedCurrent, 'd');
    }

    boolean isDoubleSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (_speedCurrent >= cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev >= cutoffSpeed);
        //FontLog.appendLog(TAG + "isDoubleSpeedAboveMin: cutoffSpeed: " + cutoffSpeed, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
        else if (Route.activeRoute.activeFlight.lastAction == FACTION.CHANGE_IN_FLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
            FontLog.appendLog(TAG + "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd');
            if (isPrevSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin)
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

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
        if (route.routeNumber != ROUTE_NUMBER_DEFAULT) requestParams.put("routeid", route.routeNumber);
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
                                flightState = FSTATE.READY_TOSENDLOCATIONS;
                                isGetFlightCallSuccess = true;
                                route._legCount++;
                            } else {
                                //replaceFlightNumber(response.responseFlightNum);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        //flightRequestCounter++;
                        //FontLog.appendLog(TAG + "getNewFlightID onFailure:" + flightRequestCounter, 'd');
                        //if (mainactivityInstance!=null) Toast.makeText(mainactivityInstance, R.string.reachability_error, Toast.LENGTH_LONG).show();
                        if (mainactivityInstance!=null) Toast.makeText(mainactivityInstance, R.string.temp_flight_alloc, Toast.LENGTH_LONG).show();
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
                        if(lastAction!= FACTION.TERMINATE_GETFLIGHTNUM) set_fAction(FACTION.CHANGE_IN_PENDING);
//                        EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
//                            .setEventMessageValueBool(isGetFlightCallSuccess)
//                            .setEventMessageValueString(flightNumber));
//                        route._legCount++;
                        //set_FlightNumber(flightNumber);
//                        if (flightNumber == null)
//                            //it is never null now;
//                            route.set_rAction(RACTION.RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER);
//                        else
//                            route.set_rAction(RACTION.SWITCH_TO_PENDING);
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
                        set_fAction(FACTION.CLOSED);
                    }
                }
        );
        requestParams = null;
    }

    public void saveLocCheckSpeed(final Location location) {

        float speedCurrent = location.getSpeed();
        FontLog.appendLog(TAG + "saveLocCheckSpeed: reported speed: " + speedCurrent, 'd');
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = isDoubleSpeedAboveMin();
        switch (lastAction) {
            case CHANGE_IN_PENDING:
                if (isSpeedAboveMin) set_fAction(FACTION.CHANGE_IN_FLIGHT);
                break;
            case CHANGE_IN_FLIGHT:
                if (!isElevationCheckDone) {
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                //set_fAction(FACTION.FLIGHTTIME_UPDATE);
                set_flightTimeSec();
                //if (!isSpeedAboveMin) route.set_rAction(RACTION.RESTART_NEW_FLIGHT);
                if (!isSpeedAboveMin) {
                    set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
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
            values.put(DBSchema.LOC_isTempFlight, isTempFlightNum); //istempflightnum
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
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                FontLog.appendLog(TAG + "saveLocation: dbLocationRecCountNormal: " + SessionProp.dbLocationRecCountNormal, 'd');
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
        return SessionProp.pSpinnerMinSpeed * (Route.activeRoute.activeFlight.lastAction == FACTION.CHANGE_IN_FLIGHT ? 0.75 : 1.0);
    }

    void set_fAction(FACTION request) {
        FontLog.appendLog(TAG + flightNumber+":fACTION :" + request, 'd');
        lastAction = request;
//        switch (fStatus) {
//            case ACTIVE:
        switch (request) {
            case CHANGE_IN_PENDING:
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_GETNEWFLIGHT_COMPLETED)
                        .setEventMessageValueBool(isGetFlightCallSuccess)
                        .setEventMessageValueString(flightNumber));
                break;
            case TERMINATE_GETFLIGHTNUM:
                flightState = FSTATE.CLOSED;
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
                break;
            case CHANGE_IN_FLIGHT:
                /// reset Timer 1 to slower rate
                _flightStartTimeGMT = getTimeGMT();
                SvcLocationClock.instanceSvcLocationClock.requestLocationUpdate(SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                //route.set_rAction(RACTION.ON_FLIGHTTIME_CHANGED);
                break;
            case TERMINATE_FLIGHT:
                //TODO
//                        Toast.makeText(mainactivityInstance, R.string.driving, Toast.LENGTH_LONG).show();
//                        //fStatus = FSTATUS.PASSIVE;
//                        lastAction = request;
//                        //sqlHelper.flightLocationsDelete(flightNumber);
//                        //SessionProp.set_isMultileg(false);
//                        //route.set_rAction(RACTION.RESTART_NEW_FLIGHT);
                break;
            case REQUEST_FLIGHTNUMBER:
                /// request flight number if the flight on temp number
                if (isGetFlightNumber) getNewFlightID();
                break;
            case REQUEST_FLIGHT:
                getNewFlightID();
                break;
            case CLOSE_FLIGHT_IF_ZERO_LOCATIONS:
                if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                    flightState = FSTATE.READY_TOBECLOSED;
                    getCloseFlight();
                }
                break;
            case CLOSED:
                flightState = FSTATE.CLOSED;
                EventBus.distribute(new EventMessage(EVENT.FLIGHT_CLOSEFLIGHT_COMPLETED).setEventMessageValueString(flightNumber));
                break;
        }
    }
    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + flightNumber+":eventReceiver:"+ev, 'd');
        switch (ev) {
            case CLOCK_ONTICK:
                if (route.activeFlight==this && lastAction!= FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS && eventMessage.eventMessageValueLocation!=null) {
//                    String s = Arrays.toString(Thread.currentThread().getStackTrace());
//                    FontLog.appendLog(TAG + "StackTrace: "+s,'d');
                    saveLocCheckSpeed(eventMessage.eventMessageValueLocation);
                }
                if (Util.isNetworkAvailable()) {
                    if (lastAction == FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS) {
                        /// try close again, previouse attempt did not work
                        set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
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
                        set_fAction(FACTION.TERMINATE_FLIGHT);
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        /// the request from server normally coming when the flight in CLOSE_FLIGHT_IF_ZERO_LOCATIONS already
                        if(lastAction!= FACTION.CLOSED) {
                            isSpeedAboveMin = false;
                            set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                        }
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        isLimitReached = true;
                        set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                        break;
                }
            break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                if(lastAction == FACTION.REQUEST_FLIGHT) set_fAction(FACTION.TERMINATE_GETFLIGHTNUM);
                else set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
                break;
            case SQL_TEMPFLIGHTNUM_ALLOCATED:
                flightNumber=eventMessage.eventMessageValueString;
                tempFlightNumber=flightNumber;
                isGetFlightCallSuccess=true;
                isTempFlightNum =true;
                route._legCount++;
                set_fAction(FACTION.CHANGE_IN_PENDING);
                break;
        }
        lastEvent=ev;
    }
}
