package com.flightontrack.flight;

import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;

import java.util.ArrayList;

import static com.flightontrack.flight.Flight.FACTION;
import static com.flightontrack.shared.Const.LEG_COUNT_HARD_LIMIT;
import static com.flightontrack.shared.Const.MODE;
import static com.flightontrack.shared.Const.ROUTE_NUMBER_DEFAULT;

public class RouteBase implements EventBus{
    public enum RACTION {
        OPEN_NEW_FLIGHT,
        SWITCH_TO_PENDING,
        CLOSE_RECEIVEFLIGHT_FAILED,
        RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER,
        RESTART_NEW_FLIGHT,
        REMOVE_FLIGHT_IF_CLOSED,
    }

    final String TAG = "RouteBase:";
    //public static ArrayList<Route> routeList = new ArrayList<>();
    static String routeNumber=ROUTE_NUMBER_DEFAULT;
    static RouteBase routeBaseInstance = null;
    public static Route activeRoute;
    public static Flight activeFlight;
    public static ArrayList<FlightBase> flightList = new ArrayList<>();

    public static RouteBase getInstance() {
        if(routeBaseInstance == null) {
            routeBaseInstance = new RouteBase();
        }
        return routeBaseInstance;
    }

     public static FlightBase get_FlightInstanceByNumber(String flightNumber){
        //for (RouteBase r : RouteBase.routeList) {
            for (FlightBase f : flightList) {
                if (f.flightNumber.equals(flightNumber)) {
                    return f;
                }
            }
        //}
        return RouteBase.activeRoute.activeFlight;
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
                /// to avoid ConcurrentModificationException making copy of the flightList
                //FontLog.appendLog(TAG + "REMOVE_FLIGHT_IF_CLOSED: flightList: size before: " + flightList.size(), 'd');
                    for (FlightBase f : new ArrayList<>(flightList)) {
                        FontLog.appendLog(TAG + "f:" + f.flightNumber + ":" + f.lastAction, 'd');
                        if (f.lastAction == FACTION.CLOSED || f.lastAction == FACTION.TERMINATE_GETFLIGHTNUM) {
                            //if (activeFlight == f) activeFlight = null;
                            FontLog.appendLog(TAG + "reaction:" + request+":f:"+f, 'd');
                            flightList.remove(f);
                        }
                        if (flightList.isEmpty()) {
                            //if (activeRoute == this) activeRoute = null;
                            FontLog.appendLog(TAG + "reaction:" + request+":r:"+routeNumber, 'd');
                            RouteBase.routeNumber =  ROUTE_NUMBER_DEFAULT;
                            RouteBase.activeFlight = null;
                            RouteBase.activeRoute = null;
                            EventBus.distribute(new EventMessage(EVENT.ROUTE_NOACTIVEROUTE));
                        }
                    }
                break;
        }
    }
@Override
public void eventReceiver(EventMessage eventMessage){
    EVENT ev = eventMessage.event;
    FontLog.appendLog(TAG + routeNumber+" :eventReceiver:"+ev, 'd');
    switch(ev){
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if (routeNumber == ROUTE_NUMBER_DEFAULT) routeNumber =eventMessage.eventMessageValueString;
                break;
            case CLOCK_ONTICK:
                set_rAction(RACTION.REMOVE_FLIGHT_IF_CLOSED);
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
