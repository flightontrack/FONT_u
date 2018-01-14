package com.flightontrack.flight;

import java.util.ArrayList;

import com.flightontrack.locationclock.SvcLocationClock;
//import com.flightontrack.mysql.SQLHelper;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.flight.Flight.*;

public class Route implements EventBus{
    public enum rACTION {
        OPEN_NEW_FLIGHT,
        SWITCH_TO_PENDING,
        ON_FLIGHTTIME_CHANGED,
        CLOSE_BUTTON_STOP_PRESSED,
        CLOSE_RECEIVEFLIGHT_FAILED,
        RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT,
    }

    private final String TAG = "Route:";
    public static Route activeRoute;
    public static ArrayList<Route> routeList = new ArrayList<>();

    public static Flight activeFlight;
    public ArrayList<Flight> flightList = new ArrayList<>();
    String routeNumber;
    int _legCount = 0;

    public Route() {
        routeList.add(activeRoute = this);
        //activeRoute = this;
    }

    public static Boolean isRouteExist() {
        return activeRoute != null;
    }

    void set_rAction(rACTION request) {
        FontLog.appendLog(TAG + "set_ROUTEREQUEST:" + request, 'd');
        switch (request) {
            case OPEN_NEW_FLIGHT:
                flightList.add(new Flight(this));
                break;
            case SWITCH_TO_PENDING:
//                if (!SvcLocationClock.isInstanceCreated())
//                    ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                //set_ActiveFlightID(flightList.get(flightList.size() - 1));
                //setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                //activeFlight.set_fAction(F_ACTION.CHANGE_IN_PENDING);
                ////break;
//            case ON_FLIGHTTIME_CHANGED:
//                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GREEN);
//                break;
            case RESTART_NEW_FLIGHT:
                //activeFlight.set_fAction(F_ACTION.CHANGE_IN_WAIT_TO_CLOSEFLIGHT);
                if (Props.SessionProp.pIsMultileg && (_legCount < LEG_COUNT_HARD_LIMIT)) {
                //if (_legCount < LEG_COUNT_HARD_LIMIT) {
                    /// ignore request to close route
                    flightList.add(new Flight(this));
                    ////setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                } else {
                    ////SvcLocationClock.stopLocationUpdates();
                    /////setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                    //SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);

                }
                break;
//            case SET_FLIGHT_PASIVE_TIMER_CLOCKONLY:
//                setFlightPassive();
//                break;
//            case SET_FLIGHT_PASIVE:
//                setFlightPassive();
//                break;
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
            case REMOVE_FLIGHT:
                /// to avoid ConcurrentModificationException making copy of the flightList
                FontLog.appendLog(TAG + "REMOVE_FLIGHT: flightList: size before: " + flightList.size(), 'd');
                for (Flight f : new ArrayList<>(flightList)) {
                    if (f.lastAction == F_ACTION.CLOSED||f.lastAction == F_ACTION.TERMINATE_GETFLIGHTNUM) {
                        if (activeFlight == f) activeFlight = null;
                        flightList.remove(f);
                    }
//                    if (flightList.isEmpty()) {
//                        if (activeRoute == this) activeRoute = null;
//                        routeList.remove(this);
//                    }
                }

                break;
//            case CHECK_IFANYFLIGHT_NEED_CLOSE:
//                //FontLog.appendLog(TAG + "ROUTE.CHECK_IFANYFLIGHT_NEED_CLOSE", 'd');
//                if (Util.isNetworkAvailable()) {
//                    checkIfAnyFlightNeedClose();
//                }
//                break;
        }
    }

//    static void checkIfAnyFlightNeedClose() {
//        try {
//            for (Route r : Route.routeList) {
//                for (Flight f : r.flightList) {
//                    if (f.lastAction == F_ACTION.CHANGE_IN_WAIT_TO_CLOSEFLIGHT) {
//                        f.set_fAction(F_ACTION.CLOSE_FLIGHT);
//                    }
//                    //String flights ="-";
//                    //flights=flights+f.flightNumber+"-"+f.lastAction+"-";
//                }
//            }
//        } catch (Exception e) {
//            //FontLog.appendLog(TAG + "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
//        }
//    }

//    private void setFlightPassive() {
//        if (!(activeFlight == null))
//        activeFlight.set_fAction(F_ACTION.CHANGE_IN_WAIT_TO_CLOSEFLIGHT);
////        if (!(SvcLocationClock.instanceSvcLocationClock == null))
////            SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
//    }
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
    void setToNull(){
        for (Route r : Route.routeList) {
            for (Flight f : r.flightList) {
                flightList.remove(f);
                if (flightList.isEmpty()) routeList.remove(r);
            }
        }
        activeFlight = null;
        activeRoute = null;
    }
@Override
public void eventReceiver(EventMessage eventMessage){
    EVENT ev = eventMessage.event;
    FontLog.appendLog(TAG + routeNumber+" :eventReceiver:"+ev, 'd');
            switch(ev){
            case MACT_BIGBUTTON_ONCLICK_START:
                //routeList.add(new Route());
//                routeList.add(this);
//                activeRoute = this;
                set_rAction(rACTION.OPEN_NEW_FLIGHT);
                break;
//            case MACT_BIGBUTTON_ONCLICK_STOP:
//                activeRoute.set_rAction(rACTION.SET_FLIGHT_PASIVE);
//                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if (routeNumber == null) routeNumber =eventMessage.eventMessageValueString;
                //if(eventMessage.eventMessageValueBool) set_ActiveFlightID(flightList.get(flightList.size() - 1)); //TODO flight number is passed in the message - get flight from the number
                break;
            case CLOCK_ONTICK:
                set_rAction(rACTION.REMOVE_FLIGHT);
                break;
            case FLIGHT_ONSPEEDLOW:
                set_rAction(rACTION.RESTART_NEW_FLIGHT);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                set_rAction(rACTION.REMOVE_FLIGHT);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setToNull();
                break;

        }
    }
}
