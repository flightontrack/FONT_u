package com.flightontrack.flight;

import android.content.Intent;
import java.util.ArrayList;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Util;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.flight.Session.*;

public class Route{
    private static final String TAG = "Route:";

    public Flight activeFlight;
    public static boolean _isRoad = false; //change isRoad() too in two places
    public static ArrayList<Flight> flightList = new ArrayList<>();
    static String routeNumber = null;
    int _legCount = 0;

    public Route() {
        activeRoute = this;
        //sqlHelper = new SQLHelper();
        SessionProp.clear();
        set_RouteRequest(ROUTEREQUEST.OPEN_NEW_ROUTE);
    }

    public static Boolean isRouteExist() {
        return activeRoute != null;
    }

    public static void set_isRoad(boolean b) {
        _isRoad = b;
        ///hard code true
        //_isRoad =true;
    }

//    private String setTextRed() {
//        String fid = SessionProp.pTextRed;
//        String fTime = "";
//
//        if (activeFlight == null) {
//            Util.appendLog(TAG + " setTextRed: flightId IS NULL", 'd');
//        } else {
//            String flightId = activeFlight.flightNumber;
//            fid = "Flight " + flightId + '\n' + "Stopped"; // + '\n';
//            fTime = activeFlight.flightTimeString.equals(FLIGHT_TIME_ZERO) ? ctxApp.getString(R.string.time) + SPACE + Util.getTimeLocal() : ctxApp.getString(R.string.tracking_flight_time) + SPACE + activeFlight.flightTimeString;
//        }
//        SessionProp.pTextRed = fid + fTime;
//        return SessionProp.pTextRed;
//    }
//
//    private String setTextGreen() {
//        SessionProp.pTextGreen = "Flight: " + (activeFlight.flightNumber) + '\n' +
//                "Point: " + activeFlight._wayPointsCount +
//                ctxApp.getString(R.string.tracking_flight_time) + SPACE + activeFlight.flightTimeString + '\n'
//                + "Alt: " + activeFlight.lastAltitudeFt + " ft";
//        return SessionProp.pTextGreen;
//    }

    public void set_RouteRequest(ROUTEREQUEST request) {
        Util.appendLog(TAG + "set_ROUTEREQUEST:" + request, 'd');
        switch (request) {
            case OPEN_NEW_ROUTE:
                //set_routeStatus(RSTATUS.ACTIVE);
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                flightList.add(new Flight(activeRoute));
                break;
            case SWITCH_TO_PENDING:
                if (!SvcLocationClock.isInstanceCreated())
                    ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                set_ActiveFlightID(flightList.get(flightList.size() - 1));
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE);
                break;
            case ON_FLIGHTTIME_CHANGED:
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GREEN);
                break;
            case CLOSE_SPEED_BELOW_MIN:
                activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
                if (MainActivity.AppProp.pIsMultileg && (_legCount < LEG_COUNT_HARD_LIMIT)) {
                    /// ignore request to close route
                    flightList.add(new Flight(activeRoute));
                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                } else {
                    SvcLocationClock.stopLocationUpdates();
                    routeNumber = null;
                    _legCount = 0;
                    //set_routeStatus(RSTATUS.PASSIVE);
                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                    SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
                }
                break;
            case CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST:
                ///rethrow
                //activeFlight.set_ChangeFlightState_if_isSpeedAboveMinChanged(false);
                activeFlight._isSpeedAboveMin = false;
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_POINTS_LIMIT_REACHED:
                ///rethrow
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_FLIGHT_CANCELED:
                ///rethrow
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_FLIGHT_DELETE_ALL_POINTS:
                closeFlightDeleteAllPoints();
                //activeRoute =null;
                break;
            case CLOSE_BUTTON_STOP_PRESSED:
                closeFlightDeleteAllPoints();
                //activeRoute =null;
                break;
//            case CLOSEAPP_BUTTON_BACK_PRESSED:
//                if (dbLocationRecCount > 0) {
//                    new ShowAlertClass(mainactivityInstance).showUnsentPointsAlert(dbLocationRecCount);
//                    Util.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
//                } else {
//                    closeFlightDeleteAllPoints();
//                    activeRoute =null;
//                    mainactivityInstance.finishActivity();
//                }
//                break;
            case CLOSE_RECEIVEFLIGHT_FAILED:
                flightList.remove(flightList.size() - 1);  /// remove the latest flight added
                ///rethrow
                //set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                if (!(SvcLocationClock.instance == null))
                    SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
                    //set_routeStatus(RSTATUS.PASSIVE);
                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                    //activeRoute =null;
                break;
            case ON_CLOSE_FLIGHT:
                /// to avoid ConcurrentModificationException making copy of the flightList
                Util.appendLog(TAG + "ON_CLOSE_FLIGHT: flightList: size before: " + flightList.size(), 'd');
                for (Flight f : new ArrayList<>(flightList)) {
                    if (f.flightState == FLIGHTREQUEST.CLOSED) {

                        if (activeFlight == f) activeFlight = null;
                        flightList.remove(f);
                    }
                    if (flightList.isEmpty()) {
                        if (activeRoute == this) activeRoute = null;
                        routeList.remove(this);
                    }
                }

                break;
            case CHECK_IFANYFLIGHT_NEED_CLOSE:
                Util.appendLog(TAG + "ROUTE.CHECK_IFANYFLIGHT_NEED_CLOSE", 'd');
                checkIfAnyFlightNeedClose();
                break;
        }
    }

    private void set_ActiveFlightID(Flight f) {
        //_iCurrentFlight =flightList.indexOf(f);
        if (!(f.flightNumber == null)) {
            activeFlight = f;
            routeNumber = (routeNumber == null ? f.flightNumber : routeNumber);
            Util.appendLog(TAG + "set_ActiveFlightID: routeNumber " + routeNumber, 'd');
        }
    }

    private void checkIfAnyFlightNeedClose() {
        try {
            for (Flight f : flightList) {
                if (f.flightState == FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE) {
                    f.set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                }
                //String flights ="-";
                //flights=flights+f.flightNumber+"-"+f.flightState+"-";
            }
        } catch (Exception e) {
            Util.appendLog(TAG + "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
        }
    }

    private void closeFlightDeleteAllPoints() {
        //set_routeStatus(RSTATUS.PASSIVE);
        setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
        if (!(activeFlight == null))
            //activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
            activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
        int i = sqlHelper.allLocationsDelete();
        Util.appendLog(TAG + "Deleted from database: " + i + " all locations", 'd');
        if (!(SvcLocationClock.instance == null))
            SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
    }

//    private void set_routeStatus(RSTATUS status) {
//        _routeStatus = status;
//        switch (status) {
//            case ACTIVE:
//                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
//                break;
//            case PASSIVE:
//                routeNumber = null;
//                //set_isRoad(false);
//                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
//                break;
//        }
//    }

//    public static class SessionProp {
//        static String pTextRed;
//        static String pTextGreen;
//
//        public static void save() {
//            editor.putString("pTextRed", pTextRed);
//            editor.commit();
//        }
//
//        public static void get() {
//            pTextRed = sharedPreferences.getString("pTextRed", ctxApp.getString(R.string.start_flight));
//        }
//
//        public static void clear() {
//            editor.remove("pTextRed").commit();
//        }
//    }
}
