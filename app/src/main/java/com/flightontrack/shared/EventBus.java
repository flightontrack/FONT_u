package com.flightontrack.shared;
import com.flightontrack.flight.Route;
import com.flightontrack.flight.Session;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLog;
import com.flightontrack.mysql.SQLHelper;

import java.util.ArrayList;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public interface EventBus {
    enum EVENT {
        MACT_BIGBUTTON_ONCLICK_START,
        MACT_BIGBUTTON_ONCLICK_STOP,
        MACT_BACKBUTTON_ONCLICK,
        MACT_MULTILEG_ONCLICK,
        FLIGHT_GETNEWFLIGHT_STARTED,
        FLIGHT_GETNEWFLIGHT_COMPLETED,
        FLIGHT_FLIGHTTIME_STARTED,
        FLIGHT_FLIGHTTIME_UPDATE_COMPLETED,
        FLIGHT_CLOSEFLIGHT_COMPLETED,
        CLOCK_SERVICESTARTED_MODELOCATION,
        CLOCK_ONTICK,
        CLOCK_MODECLOCK_ONLY,
        PROP_CHANGED_MULTILEG,
        ROUTE_ONNEW,
        SVCCOMM_ONSUCCESS_NOTIFICATION,
        SETTINGACT_BUTTONCLEARCACHE_CLICKED,
        ALERT_SENTPOINTS,
        ALERT_STOPAPP
    }
//    enum REQUEST {
//        SEND_STORED_LOCATIONS_ON_YES,
//        CLOSEAPP_NO_CACHE_CHECK,
//        CHECK_CACHE_FIRST,
//    }
    String TAG = "Bus:";

    static void distribute(EventMessage eventMessage){
        ArrayList<EventBus> interfaceList = new ArrayList();
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + ev, 'd');
        switch(ev){
            case MACT_BIGBUTTON_ONCLICK_START:
                interfaceList.add(Route.activeRoute);
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                interfaceList.add(Props.getInstance());
                interfaceList.add(Route.activeRoute);
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case PROP_CHANGED_MULTILEG:
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_GETNEWFLIGHT_STARTED:
                interfaceList.add(Props.getInstance());
                break;
            case SVCCOMM_ONSUCCESS_NOTIFICATION:
                interfaceList.add(Props.getInstance());
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
                SQLHelper.eventReceiver(eventMessage);
                break;
            case MACT_MULTILEG_ONCLICK:
                interfaceList.add(Props.getInstance());
                break;
            case CLOCK_MODECLOCK_ONLY:
                interfaceList.add(mainactivityInstance);
                break;
            case CLOCK_SERVICESTARTED_MODELOCATION:
                interfaceList.add(mainactivityInstance);
                break;
            case CLOCK_ONTICK:
                if(eventMessage.eventMessageValueClockMode==MODE.CLOCK_LOCATION) {
                    interfaceList.add(Route.activeRoute.activeFlight);
                }
                interfaceList.add(Route.activeRoute); /// passing acive route which calling static mrthod
                interfaceList.add(Session.getInstance());
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                interfaceList.add(mainactivityInstance);
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
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                interfaceList.add(Route.activeRoute);
                break;

        }
        for( EventBus i : interfaceList) {
            i.eventReceiver(eventMessage);
        }
    }

    default void eventReceiver(EventMessage eventMessage){};
}


