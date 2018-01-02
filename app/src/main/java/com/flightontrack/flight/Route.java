package com.flightontrack.flight;

import android.content.Intent;
import java.util.ArrayList;

import com.flightontrack.locationclock.SvcLocationClock;
//import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class Route implements EventBus{
    public enum ROUTEREQUEST{
        OPEN_NEW_ROUTE,
        SWITCH_TO_PENDING,
        ON_FLIGHTTIME_CHANGED,
        CLOSE_BUTTON_STOP_PRESSED,
        CLOSE_RECEIVEFLIGHT_FAILED,
        RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER,
        CHECK_IF_ROUTE_MULTILEG,
        CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST,
        CLOSE_POINTS_LIMIT_REACHED,
        CLOSE_FLIGHT_CANCELED,
        ON_CLOSE_FLIGHT,
        SET_FLIGHT_PASIVE,
        SET_FLIGHT_PASIVE_TIMER_CLOCKONLY,
        SET_ACTIVEFLIGHT_TOPASSIVE,
        CHECK_IFANYFLIGHT_NEED_CLOSE
    }

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
            case SET_FLIGHT_PASIVE:
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
                //FontLog.appendLog(TAG + "ROUTE.CHECK_IFANYFLIGHT_NEED_CLOSE", 'd');
                if (Util.isNetworkAvailable()) {
                    checkIfAnyFlightNeedClose();
                }
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

    static void checkIfAnyFlightNeedClose() {
        try {
            for (Route r : Route.routeList) {
                for (Flight f : r.flightList) {
                    if (f.flightState == FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT) {
                        f.set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                    }
                    //String flights ="-";
                    //flights=flights+f.flightNumber+"-"+f.flightState+"-";
                }
            }
        } catch (Exception e) {
            //FontLog.appendLog(TAG + "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
        }
    }

    private void setFlightPassive() {
        if (!(activeFlight == null))
        activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE_AND_CLOSEFLIGHT);
//        if (!(SvcLocationClock.instanceSvcLocationClock == null))
//            SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
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
@Override
public void eventReceiver(EventMessage eventMessage){
    FontLog.appendLog(TAG + " eventReceiver Interface is called on Route", 'd');
    EVENT ev = eventMessage.event;
            switch(ev){
            case MACT_BIGBUTTON_ONCLICK_START:
                routeList.add(new Route());
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                activeRoute.set_RouteRequest(ROUTEREQUEST.SET_FLIGHT_PASIVE);
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                //check if success/failure
                //get temp flight if fail
                //start clock service in clock mode?
                //command flight to start receiving locations FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE
                break;
            case CLOCK_ONTICK:
                set_RouteRequest(ROUTEREQUEST.CHECK_IFANYFLIGHT_NEED_CLOSE);
            break;

        }
    }
}
