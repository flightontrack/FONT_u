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

    private final String TAG = "RouteBase:";
    public static Route activeRoute;
    public static ArrayList<Route> routeList = new ArrayList<>();
    String routeNumber=ROUTE_NUMBER_DEFAULT;

    public static Flight activeFlight;
    public static ArrayList<FlightBase> flightList = new ArrayList<>();

    public RouteBase() {
    }

     public static FlightBase get_FlightInstanceByNumber(String flightNumber){
        for (RouteBase r : RouteBase.routeList) {
            for (FlightBase f : r.flightList) {
                if (f.flightNumber.equals(flightNumber)) {
                    return f;
                }
            }
        }
        return RouteBase.activeRoute.activeFlight;
    }
    void setToNull(){
        for(RouteBase r:new ArrayList<>(routeList)) {
            for (FlightBase f : new ArrayList<>(r.flightList)) {
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
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if (routeNumber == ROUTE_NUMBER_DEFAULT) routeNumber =eventMessage.eventMessageValueString;
                break;
            case CLOCK_ONTICK:
                break;
            case FLIGHT_ONSPEEDLOW:
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                break;
            case CLOCK_SERVICESELFSTOPPED:
                break;

        }
    }
}
