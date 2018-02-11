package com.flightontrack.flight;

import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;

import java.util.ArrayList;

import static com.flightontrack.shared.Const.ROUTE_NUMBER_DEFAULT;

public class RouteBase implements EventBus{
    public enum RACTION {
        OPEN_NEW_FLIGHT,
        SWITCH_TO_PENDING,
        CLOSE_RECEIVEFLIGHT_FAILED,
        RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT_IF_CLOSED,
        ADD_OR_UPDATE_FLIGHT
    }

    final String TAG = "RouteBase:";
    //public static ArrayList<Route> routeList = new ArrayList<>();
    static String routeNumber=ROUTE_NUMBER_DEFAULT;
    static RouteBase routeBaseInstance = null;
    public static Route activeRoute;
    public static Flight activeFlight;
    public static ArrayList<FlightBase> flightList = new ArrayList<>();
    EventMessage eventMessage;
    EVENT ev;

    public static RouteBase getInstance() {
        if(routeBaseInstance == null) {
            routeBaseInstance = new RouteBase();
        }
        return routeBaseInstance;
    }

     public static FlightBase get_FlightInstanceByNumber(String flightNumber){
        for (FlightBase f : flightList) {
            if (f.flightNumber.equals(flightNumber)) {
                return f;
            }
        }
        return RouteBase.activeRoute.activeFlight;
    }
    public static boolean isFlightNumberInList(String flightNumber){
        for (FlightBase f : flightList) {
            if (f.flightNumber.equals(flightNumber)) {
                return true;
            }
        }
        return false;
    }
    void setToNull(){
            for (FlightBase f : new ArrayList<>(flightList)) {
                flightList.remove(f);
            }
        activeFlight = null;
        activeRoute = null;
    }
    void set_rAction(RACTION request) {
        //FontLog.appendLog(TAG + "reaction:" + request, 'd');
        switch (request) {
            case REMOVE_FLIGHT_IF_CLOSED:
                FontLog.appendLog(TAG + "REMOVE_FLIGHT_IF_CLOSED: flightList: size : " + flightList.size(), 'd');
                    for (FlightBase f : new ArrayList<>(flightList)) {
                        FontLog.appendLog(TAG + "f:" + f.flightNumber + ":" + request, 'd');
                        if (f.flightState == FlightBase.FSTATE.CLOSED) {
                            //if (activeFlight == f) activeFlight = null;
                            FontLog.appendLog(TAG + "reaction:" + request+":f:"+f, 'd');
                            if (f==activeFlight) activeFlight =null;
                            flightList.remove(f);
                        }
                        if (flightList.isEmpty()) {
                            //if (activeRoute == this) activeRoute = null;
                            FontLog.appendLog(TAG + "flightList.isEmpty()"+":r:"+routeNumber, 'd');
                            RouteBase.routeNumber =  ROUTE_NUMBER_DEFAULT;
                            RouteBase.activeFlight = null;
                            RouteBase.activeRoute = null;
                            EventBus.distribute(new EventMessage(EVENT.ROUTE_NOACTIVEROUTE));
                        }
                    }
                break;
            case ADD_OR_UPDATE_FLIGHT:
                FlightBase fb = (FlightBase) eventMessage.eventMessageValueObject;
                if (flightList.contains(fb)) break;
                else {
                    for (FlightBase f : new ArrayList<> (flightList)) {
                        if (f.flightNumberTemp == fb.flightNumberTemp) {
                            f.set_flightNumber(eventMessage.eventMessageValueString);
                            eventMessage = null; /// kill temp flight
                        }
                        else flightList.add((FlightBase) eventMessage.eventMessageValueObject);
                    }
                    break;
                }
        }
    }
@Override
public void eventReceiver(EventMessage eventMessage){
    ev = eventMessage.event;
    this.eventMessage = eventMessage;
    FontLog.appendLog(TAG + routeNumber+" :eventReceiver:"+ev, 'd');
    switch(ev){
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if (routeNumber == ROUTE_NUMBER_DEFAULT) routeNumber =eventMessage.eventMessageValueString;
                break;
            case CLOCK_ONTICK:
                for (FlightBase f : flightList) {
                    if (f.flightState == FlightBase.FSTATE.CLOSED) {
                        set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                        break;
                    }
                }
                break;
            case FLIGHT_STATECHANGEDTO_READYTOSEND:
                set_rAction(RACTION.ADD_OR_UPDATE_FLIGHT);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setToNull();
                break;
        }
    }
}
