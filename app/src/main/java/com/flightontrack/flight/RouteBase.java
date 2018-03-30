package com.flightontrack.flight;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.log.LogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;

import java.util.ArrayList;

public class RouteBase implements EventBus{
    final String TAG = "RouteBase";

    public enum RACTION {
        OPEN_NEW_FLIGHT,
        SWITCH_TO_PENDING,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT_IF_CLOSED,
        ADD_OR_UPDATE_FLIGHT

    }

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
        RouteBase.activeFlight = null;
        RouteBase.activeRoute = null;
        EventBus.distribute(new EventMessage(EVENT.ROUTE_NOACTIVEROUTE));
    }
    void set_rAction(RACTION request) {
        new FontLogAsync().execute(new LogMessage(TAG, "reaction:" + request, 'd'));
        switch (request) {
            case REMOVE_FLIGHT_IF_CLOSED:
                new FontLogAsync().execute(new LogMessage(TAG, "REMOVE_FLIGHT_IF_CLOSED: flightList: size : " + flightList.size(), 'd'));
                    for (FlightBase f : new ArrayList<>(flightList)) {
                        new FontLogAsync().execute(new LogMessage(TAG, "f:" + f.flightNumber + ":" + request, 'd'));
                        if (f.flightState.equals(FlightBase.FLIGHT_STATE.CLOSED)) {
                            //if (activeFlight == f) activeFlight = null;
                            new FontLogAsync().execute(new LogMessage(TAG, "reaction:" + request+":f:"+f, 'd'));
                            if (f==activeFlight) activeFlight =null;
                            flightList.remove(f);
                        }
                        if (flightList.isEmpty()) {
                            //if (activeRoute == this) activeRoute = null;
                            new FontLogAsync().execute(new LogMessage(TAG, "flightList isEmpty", 'd'));
                            setToNull();
                        }
                    }
                break;
            case ADD_OR_UPDATE_FLIGHT:
                FlightBase fb = (FlightBase) eventMessage.eventMessageValueObject;
                new FontLogAsync().execute(new LogMessage(TAG, "fb.fn"+fb.flightNumber, 'd'));
                //new FontLogAsync().execute(new LogMessage(TAG, "fb.fnt"+fb.flightNumberTemp, 'd');
                if (flightList.contains(fb)) break;
                else {
                    flightList.add((FlightBase) eventMessage.eventMessageValueObject);
                    break;
                }
        }
    }
    @Override
    public void onClock(EventMessage eventMessage) {
        for (FlightBase f : flightList) {
            if (f.flightState == FlightBase.FLIGHT_STATE.CLOSED) {
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                break;
            }
        }
    }
@Override
public void eventReceiver(EventMessage eventMessage){
    ev = eventMessage.event;
    this.eventMessage = eventMessage;
    new FontLogAsync().execute(new LogMessage(TAG,"eventReceiver:"+ev+":eventString:"+eventMessage.eventMessageValueString, 'd'));
    switch(ev){
            case FLIGHT_REMOTENUMBER_RECEIVED:
                set_rAction(RACTION.ADD_OR_UPDATE_FLIGHT);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setToNull();
                break;
            case CLOCK_MODECLOCK_ONLY:
                activeFlight = null;
                break;
        }
    }
}
