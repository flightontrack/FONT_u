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

import static com.flightontrack.flight.Session.*;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class Flight {
    private static final String TAG = "Flight:";
    public String flightNumber;
    public FSTATUS fStatus = FSTATUS.PASSIVE;
    FLIGHTREQUEST flightState;
    boolean isSpeedAboveMin;
    String flightTimeString;
    int lastAltitudeFt;
    int _wayPointsCount;
    private Route route;
    private float _speedCurrent=0;
    private float speedPrev=0;
    private boolean isLimitReached;
    private long _flightStartTimeGMT;
    private int _flightTimeSec;
    private int flightRequestCounter;
    private boolean isElevationCheckDone;
    private double cutoffSpeed;

    public Flight(Route r) {
        route = r;
        //Flight.ctx = ctx;
        flightTimeString = FLIGHT_TIME_ZERO;
        isElevationCheckDone = false;
        set_flightRequest(FLIGHTREQUEST.CHANGESTATE_REQUEST_FLIGHT);
    }

    public void set_flightRequest(FLIGHTREQUEST request) {
        FontLog.appendLog(TAG + "set_FLIGHTREQUEST:" + request, 'd');
        switch (fStatus) {
            case ACTIVE:
                switch (request) {
                    case CHANGESTATE_INFLIGHT:
                        /// reset Timer 1 to slower rate
                        _flightStartTimeGMT = Util.getTimeGMT();
                        SvcLocationClock.instance.requestLocationUpdate(SessionProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                        route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
                        flightState = request;
                        break;
                    case FLIGHTTIME_UPDATE:
                        set_flightTimeSec();
                        route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
                        break;
                    case CHANGESTATE_SPEED_BELOW_MIN:
                        isSpeedAboveMin = false;
                        route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_ROUTE_MULTILEG);
                        set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
                        break;
                    case CHANGESTATE_COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        isLimitReached=true;
                        route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_ROUTE_MULTILEG);
                        set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
                        break;
                    case TERMINATE_FLIGHT:
                        fStatus = FSTATUS.PASSIVE;
                        flightState = request;
                        sqlHelper.flightLocationsDelete(flightNumber);
                        SessionProp.set_isMultileg(false);
                        route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_ROUTE_MULTILEG);
                    case CHANGESTATE_STATUSPASSIVE:
                        fStatus = FSTATUS.PASSIVE;
                        flightState = request;
                        //setFlightClosed();
                        ///rethrow and close flight if no locations left
                        set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                        break;
                    case ON_SERVER_N0TIF:
                        route.set_RouteRequest(ROUTEREQUEST.CLOSE_FLIGHT_DELETE_ALL_POINTS);
                        break;
                }
                break;
            case PASSIVE:
                switch (request) {
                    case CHANGESTATE_REQUEST_FLIGHT:
                        getNewFlightID();
                        flightState = request;
                        break;
                    case CHANGESTATE_STATUSACTIVE:
                        fStatus = FSTATUS.ACTIVE;
                        flightState = request;
                        if (SvcLocationClock.isInstanceCreated()) {
                            SvcLocationClock.instance.set_mode(MODE.CLOCK_LOCATION);
                        }
                        break;
                    case CLOSE_FLIGHT:
                        if (sqlHelper.getLocationFlightCount(flightNumber) == 0) {
                            flightState = request;
                            getCloseFlight();
                        }
                        //setFlightClosed(request);
                        break;
                    case CLOSED:
                        flightState = request;
                        route.set_RouteRequest(ROUTEREQUEST.ON_CLOSE_FLIGHT);
                        break;
                }
                break;
        }
    }

    void set_wayPointsCount(int pointsCount) {
        _wayPointsCount = pointsCount;
        if (pointsCount >= Util.getWayPointLimit()) {
            route.set_RouteRequest(ROUTEREQUEST.CLOSE_POINTS_LIMIT_REACHED);
        }
    }

    void set_speedCurrent(float speed) {
        /// gps can report speed equal 0 in flight  which should be ignored.
        if (speed > 0 || SessionProp.pIsDebug) {
            speedPrev = _speedCurrent;
            _speedCurrent = speed + (float) 0.01;
        }
        else {
            FontLog.appendLog(TAG + "set_speedCurrent: speed is ZERO", 'd');
        }
    }

    boolean isDoubleSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        boolean isCurrSpeedAboveMin = (_speedCurrent > cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev > cutoffSpeed);
        //Util.appendLog(TAG + "cutoffSpeed:" + cutoffSpeed, 'd');
        FontLog.appendLog(TAG + "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin + " isPrevSpeedAboveMin:" + isPrevSpeedAboveMin, 'd');
        if (isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
        else if (activeRoute.activeFlight.flightState == FLIGHTREQUEST.CHANGESTATE_INFLIGHT && (isCurrSpeedAboveMin ^ isPrevSpeedAboveMin)) {
            if (isPrevSpeedAboveMin)
                SvcLocationClock.instance.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin)
                SvcLocationClock.instance.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

    boolean isCurrentSpeedAboveMin() {
        cutoffSpeed = get_cutoffSpeed();
        FontLog.appendLog(TAG + "isCurrSpeedAboveMin:" + (_speedCurrent > cutoffSpeed), 'd');
        return _speedCurrent > cutoffSpeed;
    }

    public void getNewFlightID() {
        FontLog.appendLog(TAG + "getNewFlightID", 'd');
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
//        requestParams.setUseJsonStreamer(true);
        new AsyncHttpClient().post(Util.getTrackingURL() + SessionProp.ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getNewFlightID OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));
                        //char responseType = response.responseType;

                        if (response.responseNotif != null) {
                            FontLog.appendLog(TAG + "RESPONSE_TYPE_NOTIF: " + response.responseNotif, 'd');
                            Toast.makeText(SessionProp.ctxApp, "Cant get flight number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.responseFlightNum != null) {
                            flightNumber = response.responseFlightNum;
                            route._legCount++;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        flightRequestCounter++;
                        FontLog.appendLog(TAG + "getNewFlightID onFailure:" + flightRequestCounter, 'd');
                        Toast.makeText(SessionProp.ctxApp, R.string.reachability_error, Toast.LENGTH_LONG).show();
                        //if(SvcLocationClock.isInstanceCreated()) ctxApp.stopService(new Intent(ctxApp, SvcLocationClock.class));
                    }

                    @Override
                    public void onFinish() {
                        FontLog.appendLog(TAG + "onFinish: FlightNumber: " + flightNumber, 'd');
                        //set_FlightNumber(flightNumber);
                        if (flightNumber == null)
                            route.set_RouteRequest(ROUTEREQUEST.CLOSE_RECEIVEFLIGHT_FAILED);
                        else
                            route.set_RouteRequest(ROUTEREQUEST.SWITCH_TO_PENDING); //set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE);
                        //set_flightRequest(FLIGHTREQUEST.ON_FLIGHTGET_FINISH);
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

        new AsyncHttpClient().post(Util.getTrackingURL() + SessionProp.ctxApp.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
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
                        set_flightRequest(FLIGHTREQUEST.CLOSED);
                    }
                }
        );
        requestParams = null;
    }

    public void onClock(final Location location) {
        FontLog.appendLog(TAG + "onClock:", 'd');

        float speedCurrent = location.getSpeed();
        set_speedCurrent(speedCurrent);

        isSpeedAboveMin = isDoubleSpeedAboveMin();
        switch (flightState) {
            case CHANGESTATE_STATUSACTIVE:
                if (isSpeedAboveMin) set_flightRequest(FLIGHTREQUEST.CHANGESTATE_INFLIGHT);
                break;
            case CHANGESTATE_INFLIGHT:
                if (!isElevationCheckDone) {
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC)
                        isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                } else saveLocation(location, false);

                set_flightRequest(FLIGHTREQUEST.FLIGHTTIME_UPDATE);
                if (!isSpeedAboveMin) route.set_RouteRequest(ROUTEREQUEST.CHECK_IF_ROUTE_MULTILEG);
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
            values.put(DBSchema.COLUMN_NAME_COL2, flightNumber); //flightid
            values.put(DBSchema.COLUMN_NAME_COL3, !isCurrentSpeedAboveMin()); /// speed low
            //values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString(speedCurrentInt)); //speed
            values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString((int) location.getSpeed())); //speed
            values.put(DBSchema.COLUMN_NAME_COL6, Double.toString(location.getLatitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL7, Double.toString(location.getLongitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL8, Float.toString(location.getAccuracy())); //accuracy
            values.put(DBSchema.COLUMN_NAME_COL9, Math.round(location.getAltitude())); //extrainfo
            values.put(DBSchema.COLUMN_NAME_COL10, p); //wpntnum
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Util.getSignalStregth())); //gsmsignal
            values.put(DBSchema.COLUMN_NAME_COL12, URLEncoder.encode(Util.getDateTimeNow(), "UTF-8")); //date
            values.put(DBSchema.COLUMN_NAME_COL13, iselevecheck);
            long r = sqlHelper.rowLocationInsert(values);
            if (r > 0) {
                lastAltitudeFt = (int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                FontLog.appendLog(TAG + "saveLocation: dbLocationRecCount: " + dbLocationRecCount, 'd');
            }
        } catch (Exception e) {
            FontLog.appendLog(TAG + "SQLite Exception Placeholder", 'e');
        }
    }

    public void set_flightTimeSec() {
        long elapsedTime = Util.getTimeGMT() - _flightStartTimeGMT;
        _flightTimeSec = (int) elapsedTime / 1000;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
    }

    private double get_cutoffSpeed(){
        return SessionProp.pSpinnerMinSpeed * (activeRoute.activeFlight.flightState == FLIGHTREQUEST.CHANGESTATE_INFLIGHT ? 0.75 : 1.0);
    }
}
