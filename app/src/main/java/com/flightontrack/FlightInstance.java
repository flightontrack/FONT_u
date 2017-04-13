package com.flightontrack;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import static com.flightontrack.Const.*;

class FlightInstance {
    private static final    String          TAG = "FlightInstance:";
    private static          Context         ctx;
    private                 Route           route;
    FLIGHTREQUEST                           flightState;
    boolean                                 _isSpeedAboveMin;
    float                                   _speedCurrent;
    float                                   speedPrev;
    boolean                                 _isLimitReached;
    String                                  flightNumber;
    private                 long            _flightStartTimeGMT;
    int                                     _flightTimeSec;
    String                                  flightTimeString;
    int                                     lastAltitudeFt;
    int                                     _wayPointsCount;
    FSTATUS                                 fStatus = FSTATUS.PASSIVE;
    int                                     flightRequestCounter;
    boolean                                 isElevationCheckDone;

    FlightInstance(Context ctx, Route r){
        route = r;
        FlightInstance.ctx = ctx;
        flightTimeString = FLIGHT_TIME_ZERO;
        isElevationCheckDone =false;
        set_flightRequest(FLIGHTREQUEST.CHANGESTATE_REQUEST_FLIGHT);
    }

    void set_flightRequest(FLIGHTREQUEST request) {
        Util.appendLog(TAG + "set_FLIGHTREQUEST:" + request, 'd');
        switch (fStatus) {
            case ACTIVE:
            switch (request) {
                case CHANGESTATE_INFLIGHT:
                    /// reset Timer 1 to slower rate
                    _flightStartTimeGMT = Util.getTimeGMT();
                    SvcLocationClock.instance.requestLocationUpdate(MainActivity.AppProp.pIntervalLocationUpdateSec, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                    route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
                    flightState =request;
                    break;
                case FLIGHTTIME_UPDATE:
                    set_flightTimeSec();
                    route.set_RouteRequest(ROUTEREQUEST.ON_FLIGHTTIME_CHANGED);
                    break;
                case CHANGESTATE_STATUSPASSIVE:
                    fStatus = FSTATUS.PASSIVE;
                    flightState =request;
                    //setFlightClosed();
                    ///rethrow and close flight if no locations left
                    //set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                    break;
            }
                break;
            case PASSIVE:
                switch (request) {
                    case CHANGESTATE_REQUEST_FLIGHT:
                        getNewFlightID();
                        flightState =request;
                        break;
                    case CHANGESTATE_STATUSACTIVE:
                        fStatus = FSTATUS.ACTIVE;
                        flightState =request;
                        if (SvcLocationClock.isInstanceCreated()) {
                            SvcLocationClock.instance.set_mode(MODE.CLOCK_LOCATION);
                        }
                        break;
                    case CLOSE_FLIGHT:
                        if (Route.sqlHelper.getLocationFlightCount(flightNumber)==0){
                            flightState =request;
                            getCloseFlight();
                        }
                        //setFlightClosed(request);
                        break;
                    case CLOSED:
                        flightState =request;
                        route.set_RouteRequest(ROUTEREQUEST.ON_CLOSE_FLIGHT);
                        break;
                }
                break;
        }
    }
    void set_wayPointsCount(int pointsCount){
        _wayPointsCount=pointsCount;
        if (pointsCount>=Util.getWayPointLimit()){
            route.set_RouteRequest(ROUTEREQUEST.CLOSE_POINTS_LIMIT_REACHED);
        }
    }

    void set_speedCurrent(float speed){
        //Util.appendLog(TAG + "set_speedCurrent: speed:" + speed, 'd');
        speedPrev = _speedCurrent;

        //Util.appendLog(TAG + "set_speedCurrent: speedPrev:" + speedPrev+" _speedCurrent:"+_speedCurrent, 'd');
        _speedCurrent = speed+(float)0.01;
    }
    boolean isDoubleSpeedAboveMin() {
        double multiplier = Route.activeFlight.flightState== FLIGHTREQUEST.CHANGESTATE_INFLIGHT ?0.75:1.0;
        double cutoffSpeed = Util.getTrackingSpeedIntMeterSec()*multiplier;
        boolean isCurrSpeedAboveMin = (_speedCurrent > cutoffSpeed);
        boolean isPrevSpeedAboveMin = (speedPrev > cutoffSpeed);
        //Util.appendLog(TAG + "cutoffSpeed:" + cutoffSpeed, 'd');
        Util.appendLog(TAG + "isCurrSpeedAboveMin:" + isCurrSpeedAboveMin+" isPrevSpeedAboveMin:"+isPrevSpeedAboveMin, 'd');
        if(isCurrSpeedAboveMin && isPrevSpeedAboveMin) return true;
        else if(Route.activeFlight.flightState== FLIGHTREQUEST.CHANGESTATE_INFLIGHT && (isCurrSpeedAboveMin^isPrevSpeedAboveMin)) {
            if (isPrevSpeedAboveMin) SvcLocationClock.instance.requestLocationUpdate(SPEEDLOW_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            else if (isCurrSpeedAboveMin) SvcLocationClock.instance.requestLocationUpdate(SvcLocationClock.intervalClockSecPrev, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
            return true;
        }
        return false;
    }

    boolean isCurrentSpeedAboveMin() {
        double multiplier = Route.activeFlight.flightState== FLIGHTREQUEST.CHANGESTATE_INFLIGHT ?0.75:1.0;
        double cutoffSpeed = Util.getTrackingSpeedIntMeterSec()*multiplier;
        Util.appendLog(TAG + "isCurrSpeedAboveMin:" + (_speedCurrent > cutoffSpeed), 'd');
        return _speedCurrent > cutoffSpeed;
    }

    public void getNewFlightID() {
        Util.appendLog(TAG + "getNewFlightID", 'd');
        RequestParams requestParams = new RequestParams();

        requestParams.put("rcode", Const.REQUEST_FLIGHT_NUMBER);
        requestParams.put("phonenumber", MainActivity._myPhoneId); // Util.getMyPhoneID());
        requestParams.put("username", Util.getUserName());
        requestParams.put("userid", MainActivity._userId);
        requestParams.put("deviceid", MainActivity._myDeviceId);
        requestParams.put("aid", Util.getMyAndroidID());
        requestParams.put("versioncode", String.valueOf(Util.getVersionCode()));
        requestParams.put("AcftNum", Util.getAcftNum(4));
        requestParams.put("AcftTagId", Util.getAcftNum(5));
        requestParams.put("AcftName", Util.getAcftNum(6));
        requestParams.put("isFlyingPattern", MainActivity.AppProp.pIsMultileg);
        requestParams.put("freq", Integer.toString(MainActivity.AppProp.pIntervalLocationUpdateSec));
        long speed_thresh = Math.round(Util.getTrackingSpeedIntMeterSec());
        requestParams.put("speed_thresh", String.valueOf(speed_thresh));
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", MainActivity.AppProp.pIsDebug);
        if (!(Route.routeNumber ==null)) requestParams.put("routeid", Route.routeNumber);
//        requestParams.setUseJsonStreamer(true);
        new AsyncHttpClient().post(Util.getTrackingURL() + ctx.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Util.appendLog(TAG + "getNewFlightID OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));
                        //char responseType = response.responseType;

                        if (response.responseNotif != null) {
                            Util.appendLog(TAG+"RESPONSE_TYPE_NOTIF: " +response.responseNotif,'d');
                            Toast.makeText(ctx, "Cant get flight number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.responseFlightNum != null) {
                            flightNumber = response.responseFlightNum;
                            route._legCount++;
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Util.appendLog(TAG + "getNewFlightID onFailure:" + flightRequestCounter, 'd');
                        Toast.makeText(ctx, R.string.reachability_error, Toast.LENGTH_LONG).show();
                        //if(SvcLocationClock.isInstanceCreated()) ctx.stopService(new Intent(ctx, SvcLocationClock.class));
                    }
                    @Override
                    public void onFinish() {
                        Util.appendLog(TAG + "onFinish: FlightNumber: "+flightNumber, 'd');
                        //set_FlightNumber(flightNumber);
                        if(flightNumber==null) route.set_RouteRequest(ROUTEREQUEST.CLOSE_RECEIVEFLIGHT_FAILED);
                        else route.set_RouteRequest(ROUTEREQUEST.SWITCH_TO_PENDING); //set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE);
                        //set_flightRequest(FLIGHTREQUEST.ON_FLIGHTGET_FINISH);
                    }
                    @Override
                    public void onRetry(int retryNo) {
                        Util.appendLog(TAG + "getNewFlightID onRetry:"+retryNo, 'd');
                    }
                }
        );
        requestParams = null;
    }

    void getCloseFlight() {
        Util.appendLog(TAG+ "getCloseFlight",'d');
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_STOP_FLIGHT);
        requestParams.put("speedlowflag", _isSpeedAboveMin);
        requestParams.put("isLimitReached", _isLimitReached);
        requestParams.put("flightid", flightNumber);
        //requestParams.put("isdebug", Util.getIsDebug());
        requestParams.put("isdebug", MainActivity.AppProp.pIsDebug);

        new AsyncHttpClient().post(Util.getTrackingURL() + ctx.getString(R.string.aspx_rootpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Util.appendLog(TAG + "getCloseFlight OnSuccess", 'd');
                        //String responseText = new String(responseBody);
                        Response response = new Response(new String(responseBody));

                        if (response.responseAckn != null) {
                            Util.appendLog(TAG + "onSuccess|Flight closed: "+flightNumber,'d');
                        }
                        if (response.responseNotif != null) {
                            Util.appendLog(TAG + "onSuccess|RESPONSE_TYPE_NOTIF:" +response.responseNotif,'d');
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Util.appendLog(TAG + "getCloseFlight onFailure: " + flightNumber,'d');

                    }
                    public void onFinish() {
                        set_flightRequest(FLIGHTREQUEST.CLOSED);
                    }
                }
        );
        requestParams = null;
    }

    public void onClock(final Location location) {
        Util.appendLog(TAG + "onClock:", 'd');

                float speedCurrent = location.getSpeed();
                set_speedCurrent(speedCurrent);

        _isSpeedAboveMin = isDoubleSpeedAboveMin();
        switch(flightState) {
            case CHANGESTATE_STATUSACTIVE:
                if (_isSpeedAboveMin) set_flightRequest(FLIGHTREQUEST.CHANGESTATE_INFLIGHT);
                break;
            case CHANGESTATE_INFLIGHT:
                if (!isElevationCheckDone){
                    if (_flightTimeSec >= ELEVATIONCHECK_FLIGHT_TIME_SEC) isElevationCheckDone = true;
                    saveLocation(location, isElevationCheckDone);
                }
                else saveLocation(location, false);

                set_flightRequest(FLIGHTREQUEST.FLIGHTTIME_UPDATE);
                if(!_isSpeedAboveMin) route.set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
        }
    }

    private void saveLocation(Location location,boolean iselevecheck) {
        //Util.appendLog(TAG + "_____Timer 3 - saveLocation", 'd');
        try {
            int p = Route.activeFlight._wayPointsCount+1;
            ContentValues values = new ContentValues();
            values.put(DBSchema.COLUMN_NAME_COL1, REQUEST_LOCATION_UPDATE); //rcode
            values.put(DBSchema.COLUMN_NAME_COL2, flightNumber); //flightid
            values.put(DBSchema.COLUMN_NAME_COL3, !isCurrentSpeedAboveMin()); /// speed low
            //values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString(speedCurrentInt)); //speed
            values.put(DBSchema.COLUMN_NAME_COL4, Integer.toString((int)location.getSpeed())); //speed
            values.put(DBSchema.COLUMN_NAME_COL6, Double.toString(location.getLatitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL7, Double.toString(location.getLongitude())); //latitude
            values.put(DBSchema.COLUMN_NAME_COL8, Float.toString(location.getAccuracy())); //accuracy
            values.put(DBSchema.COLUMN_NAME_COL9, Math.round(location.getAltitude())); //extrainfo
            values.put(DBSchema.COLUMN_NAME_COL10, p); //wpntnum
            values.put(DBSchema.COLUMN_NAME_COL11, Integer.toString(Util.getSignalStregth())); //gsmsignal
            values.put(DBSchema.COLUMN_NAME_COL12, URLEncoder.encode(Util.getDateTimeNow(), "UTF-8")); //date
            values.put(DBSchema.COLUMN_NAME_COL13, iselevecheck);
            long  r = Route.sqlHelper.rowLocationInsert(values);
            if (r>0) {
                lastAltitudeFt=(int) (Math.round(location.getAltitude() * 3.281));
                set_wayPointsCount(p);
                Util.appendLog(TAG + "saveLocation: dbLocationRecCount: " + Route.dbLocationRecCount, 'd');
            }
        } catch (Exception e) {
            Util.appendLog(TAG + "SQLite Exception Placeholder", 'e');
        }
    }

    public void set_flightTimeSec() {
        long elapsedTime = Util.getTimeGMT() - _flightStartTimeGMT;
        _flightTimeSec = (int) elapsedTime/1000;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        flightTimeString = dateFormat.format(elapsedTime);
    }
}
