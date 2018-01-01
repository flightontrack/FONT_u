package com.flightontrack.flight;

import android.content.Intent;
import java.util.ArrayList;

import com.flightontrack.locationclock.SvcLocationClock;
//import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;

import static com.flightontrack.shared.Const.*;
//import static com.flightontrack.shared.Const.EVENT.MACT_BIGBUTTON_CLICKED_START;
import static com.flightontrack.shared.Props.*;

public class Route implements EventBus{

    private final String TAG = "Route:";
    public static Route activeRoute;
    public static ArrayList<Route> routeList = new ArrayList<>();

    public Flight activeFlight;
    public ArrayList<Flight> flightList = new ArrayList<>();
    String routeNumber = null;
    int _legCount = 0;

    public Route() {
        activeRoute = this;
        //sqlHelper = new SQLHelper();
        //SessionProp.clear();
        set_RouteRequest(ROUTEREQUEST.OPEN_NEW_ROUTE);
    }

    public static Boolean isRouteExist() {
        return activeRoute != null;
    }

    public void set_RouteRequest(ROUTEREQUEST request) {
        FontLog.appendLog(TAG + "set_ROUTEREQUEST:" + request, 'd');
        switch (request) {
            case OPEN_NEW_ROUTE:
                //setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                flightList.add(new Flight(activeRoute));
                break;
            case SWITCH_TO_PENDING:
                if (!SvcLocationClock.isInstanceCreated())
                    ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                set_ActiveFlightID(flightList.get(flightList.size() - 1));
                //setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE);
                break;
//            case ON_FLIGHTTIME_CHANGED:
//                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GREEN);
//                break;
            case CHECK_IF_ROUTE_MULTILEG:
                //activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
                if (SessionProp.pIsMultileg && (_legCount < LEG_COUNT_HARD_LIMIT)) {
                    /// ignore request to close route
                    flightList.add(new Flight(this));
                    ////setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                } else {
                    ////SvcLocationClock.stopLocationUpdates();
                    /////setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
                }
                break;
            case SET_FLIGHT_PASIVE_TIMER_CLOCKONLY:
                setFlightPassive();
                break;
            case CLOSE_BUTTON_STOP_PRESSED:
                setFlightPassive();
                break;
            case CLOSE_RECEIVEFLIGHT_FAILED:
                flightList.remove(flightList.size() - 1);  /// remove the latest flight added
                if (!(SvcLocationClock.instanceSvcLocationClock == null))
                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
                    ////setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
            case RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER:
//                _legCount++;
//                int tempFlight = sqlHelper.getNewTempFlightNum();
//
//
//                if (!(SvcLocationClock.instanceSvcLocationClock == null))
//                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
//                    //set_routeStatus(RSTATUS.PASSIVE);
//                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
//                    //activeRoute =null;
                break;
            case ON_CLOSE_FLIGHT:
                /// to avoid ConcurrentModificationException making copy of the flightList
                FontLog.appendLog(TAG + "ON_CLOSE_FLIGHT: flightList: size before: " + flightList.size(), 'd');
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
                FontLog.appendLog(TAG + "ROUTE.CHECK_IFANYFLIGHT_NEED_CLOSE", 'd');
                checkIfAnyFlightNeedClose();
                break;
        }
    }

    private void set_ActiveFlightID(Flight f) {
        //_iCurrentFlight =flightList.indexOf(f);
        if (!(f.flightNumber == null)) {
            activeFlight = f;
            routeNumber = (routeNumber == null ? f.flightNumber : routeNumber);
            FontLog.appendLog(TAG + "set_ActiveFlightID: routeNumber " + routeNumber, 'd');
        }
    }

    private void checkIfAnyFlightNeedClose() {
        try {
            for (Flight f : flightList) {
                if (f.flightState == FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT) {
                    f.set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                }
                //String flights ="-";
                //flights=flights+f.flightNumber+"-"+f.flightState+"-";
            }
        } catch (Exception e) {
            FontLog.appendLog(TAG + "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
        }
    }

    private void setFlightPassive() {
        //setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
        if (!(activeFlight == null))
        activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
//        int i = sqlHelper.allLocationsDelete();
        //FontLog.appendLog(TAG + "Deleted from database: " + i + " all locations", 'd');
        if (!(SvcLocationClock.instanceSvcLocationClock == null))
            SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
    }
    public static Flight get_FlightInstanceByNumber(String flightNumber){
        for (Route r : Route.routeList) {
            for (Flight f : r.flightList) {
                if (f.flightNumber.equals(flightNumber)) {
                    return f;
                }
            }
        }
        return Route.activeRoute.activeFlight;
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
@Override
public void eventReceiver(EventMessage eventMessage){
    FontLog.appendLog(TAG + " eventReceiver Interface is called on Route", 'd');
    EVENT ev = eventMessage.event;
            switch(ev){
            case MACT_BIGBUTTON_CLICKED_START:
                routeList.add(new Route());
                break;
            case MACT_BIGBUTTON_CLICKED_STOP:
                activeRoute.set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                //check if success/failure
                //get temp flight if fail
                //start clock service in clock mode?
                //command flight to start receiving locations FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE
                break;
                case CLOCK_ONTICK:

                break;

        }
    }
}
