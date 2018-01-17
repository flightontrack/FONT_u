package com.flightontrack.shared;
import com.flightontrack.flight.Flight;
import com.flightontrack.flight.Route;
import com.flightontrack.flight.Session;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLog;

import java.util.ArrayList;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public interface EventBus {
    enum EVENT {
        DEFAULT_EVENT,
        MACT_BIGBUTTON_ONCLICK_START,
        MACT_BIGBUTTON_ONCLICK_STOP,
        MACT_BACKBUTTON_ONCLICK,
        MACT_MULTILEG_ONCLICK,

        FLIGHT_GETNEWFLIGHT_STARTED,
        FLIGHT_GETNEWFLIGHT_COMPLETED,
        FLIGHT_FLIGHTTIME_STARTED,
        FLIGHT_FLIGHTTIME_UPDATE_COMPLETED,
        FLIGHT_CLOSEFLIGHT_COMPLETED,
        FLIGHT_ONSPEEDLOW,
        FLIGHT_ONPOINTSLIMITREACHED,

        CLOCK_SERVICESTARTED_MODELOCATION,

        CLOCK_SERVICESELFSTOPPED,
        CLOCK_ONTICK,
        CLOCK_MODECLOCK_ONLY,
        PROP_CHANGED_MULTILEG,

        ROUTE_ONNEW,
        ROUTE_ONLEGLIMITREACHED,
        ROUTE_NOACTIVEROUTE,
        ROUTE_ONRESTART,

        SVCCOMM_ONSUCCESS_NOTIF,
        SVCCOMM_ONSUCCESS_ACKN,
        SVCCOMM_ONSUCCESS_COMMAND,

        SETTINGACT_BUTTONCLEARCACHE_CLICKED,
        SETTINGACT_BUTTONSENDCACHE_CLICKED,

        ALERT_SENTPOINTS,
        ALERT_STOPAPP,

        SQL_TEMPFLIGHTNUM_ALLOCATED,

        FLIGHT_OFFLINE_DBUPDATE_COMPLETED
    }
    String TAG = "Bus:";

    static void distribute(EventMessage eventMessage){
        ArrayList<EventBus> interfaceList = new ArrayList();
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + ev, 'd');
        switch(ev){
            case MACT_BIGBUTTON_ONCLICK_START:
                interfaceList.add(new Route());
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                interfaceList.add(Props.getInstance());
                interfaceList.add(Route.activeRoute.activeFlight); // set the active flight to fail
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case PROP_CHANGED_MULTILEG:
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_GETNEWFLIGHT_STARTED:
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if(eventMessage.eventMessageValueBool){
                    interfaceList.add(Route.activeRoute); // set route number
                    interfaceList.add(new SvcLocationClock()); //start clock service in location mode
                    interfaceList.add(mainactivityInstance);
                }
                else
                {
                    interfaceList.add(sqlHelper);
                }
                    //interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                interfaceList.add(Route.activeRoute); //remove flight
                break;
            case FLIGHT_ONSPEEDLOW:
                if(!SessionProp.pIsMultileg) interfaceList.add(SvcLocationClock.getInstance());//TODO doing nothing
                interfaceList.add(Route.activeRoute);
                break;
            case FLIGHT_ONPOINTSLIMITREACHED:
                ///TODO
                break;
            case ROUTE_ONLEGLIMITREACHED:
                ///TODO
                break;
            case ROUTE_NOACTIVEROUTE:
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case ROUTE_ONRESTART:
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SVCCOMM_ONSUCCESS_NOTIF:
                interfaceList.add(Props.getInstance());
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SVCCOMM_ONSUCCESS_COMMAND:
                interfaceList.add(Route.get_FlightInstanceByNumber(eventMessage.eventMessageValueString)); //change active flight to passive
                switch (eventMessage.eventMessageValueInt){
                    case COMMAND_TERMINATEFLIGHT:
                        interfaceList.add(Props.getInstance()); //set multileg to false
                        interfaceList.add(sqlHelper); // delete all locations on the flight
                        // TODO flight should be deleted from the list of flights on the route
                        interfaceList.add(SvcLocationClock.getInstance()); //swithch to clockonly
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        interfaceList.add(Route.activeRoute); //initiate a new flight if multileg
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        interfaceList.add(Route.activeRoute); //initiate a new flight if multileg
                        break;
                }
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
                interfaceList.add(sqlHelper);
                //SQLHelper.eventReceiver(eventMessage);
                break;
            case SETTINGACT_BUTTONSENDCACHE_CLICKED:
                interfaceList.add(Session.getInstance());
                break;
            case MACT_MULTILEG_ONCLICK:
                interfaceList.add(Props.getInstance());
                break;
            case CLOCK_MODECLOCK_ONLY:
                interfaceList.add(mainactivityInstance);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                interfaceList.add(Route.activeRoute); // set to null flightlist and routelist
                interfaceList.add(mainactivityInstance);
                break;
            case CLOCK_ONTICK:
//                if(eventMessage.eventMessageValueClockMode==MODE.CLOCK_LOCATION) {
//                    interfaceList.add(Route.activeFlight);
//                }
                interfaceList.add(Route.activeRoute); /// passing acive route which calling static mrthod
                for (Route r : Route.routeList) {
                    for (Flight f : r.flightList) {
                        interfaceList.add(f); /// check if any of them need to be closed
                    }
                }
                interfaceList.add(Session.getInstance()); ///start communication service
                //FontLog.appendLog(TAG + interfaceList, 'd');
                break;
            case ALERT_SENTPOINTS:
                interfaceList.add(Route.activeRoute);
                interfaceList.add(Session.getInstance());
                break;
            case MACT_BACKBUTTON_ONCLICK:
                interfaceList.add(Session.getInstance());
                break;
            case ALERT_STOPAPP:
                interfaceList.add(Session.getInstance());
                break;
            case SQL_TEMPFLIGHTNUM_ALLOCATED:
                interfaceList.add(Route.activeRoute.activeFlight);
                break;
            case FLIGHT_OFFLINE_DBUPDATE_COMPLETED:
                interfaceList.add(Session.getInstance());
                break;
        }
        for( EventBus i : interfaceList) {
            if(!(null==i))i.eventReceiver(eventMessage);
        }
    }

    default void eventReceiver(EventMessage eventMessage){};
}


