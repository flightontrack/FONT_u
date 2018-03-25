package com.flightontrack.shared;
import com.flightontrack.activity.SimpleSettingsActivity;
import com.flightontrack.flight.FlightBase;
import com.flightontrack.flight.Route;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.flight.Session;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.log.FontLog;

import java.util.ArrayList;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;

public interface EventBus extends Events{

    String TAG = "Bus:";

    static void distribute(EventMessage eventMessage){
        ArrayList<EventBus> interfaceList = new ArrayList();
        ArrayList<EventBus> onClockList = new ArrayList();

        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + ev+":eventString:"+eventMessage.eventMessageValueString+":eventObject:"+eventMessage.eventMessageValueObject, 'd');
        switch(ev){
            case MACT_BIGBUTTON_ONCLICK_START:
                interfaceList.add(new Route());
                break;
            case MACT_BIGBUTTON_ONCLICK_STOP:
                interfaceList.add(Props.getInstance());
                interfaceList.add(RouteBase.activeRoute.activeFlight); // close flight of set the pending flight to fail
                //interfaceList.add(Session.getInstance());
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
//                    interfaceList.add(RouteBase.getInstance()); // set route number
//                    interfaceList.add(new SvcLocationClock()); //start clock service in location mode
//                    interfaceList.add(mainactivityInstance);
                }
                else
                {
                    interfaceList.add(sqlHelper);
                }
                break;
            case FLIGHTBASE_GETFLIGHTNUM:
                if(!eventMessage.eventMessageValueBool){
                    interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                }
                else
                {
                }
                    //interfaceList.add(mainactivityInstance);
                break;

            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                interfaceList.add(RouteBase.getInstance()); /// remove flight
                break;
            case FLIGHT_ONSPEEDLOW:
                if(!SessionProp.pIsMultileg) interfaceList.add(SvcLocationClock.getInstance());//TODO doing nothing
                interfaceList.add(RouteBase.activeRoute); /// restart new flight in the route
                break;
            case FLIGHT_ONSPEEDABOVEMIN:
                interfaceList.add(SvcLocationClock.getInstance()); ///
                break;
            case FLIGHT_ONPOINTSLIMITREACHED:
                ///TODO
                break;
            case ROUTE_ONLEGLIMITREACHED:
                ///TODO
                break;
            case ROUTE_NOACTIVEROUTE:
                if (SvcLocationClock.getInstance()!=null) interfaceList.add(SvcLocationClock.getInstance());
                else interfaceList.add(mainactivityInstance);
                break;
            case ROUTE_ONRESTART:
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SVCCOMM_ONSUCCESS_NOTIF:
                interfaceList.add(Props.getInstance());
                interfaceList.add(SvcLocationClock.getInstance());
                break;
            case SVCCOMM_ONSUCCESS_COMMAND:
                interfaceList.add(Route.get_FlightInstanceByNumber(eventMessage.eventMessageValueString));
                switch (eventMessage.eventMessageValueInt){
                    case COMMAND_TERMINATEFLIGHT:
                        interfaceList.add(Props.getInstance()); //set multileg to false
                        //interfaceList.add(sqlHelper); // delete all locations on the flight
                        interfaceList.add(RouteBase.activeFlight); // stop active flight
                        interfaceList.add(SvcLocationClock.getInstance()); //swithch to clockonly
                        break;
                    case COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN:
                        interfaceList.add(RouteBase.activeRoute); //initiate a new flight if multileg
                        break;
                    case COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED:
                        interfaceList.add(RouteBase.activeRoute); //initiate a new flight if multileg
                        break;
                }
            case SVCCOMM_ONDESTROY:
                if(SimpleSettingsActivity.simpleSettingsActivityInstance!=null) interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                //interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                //interfaceList.add(Session.getInstance());
                break;
            case SVCCOMM_LOCRECCOUNT_NOTZERO:
                interfaceList.add(Session.getInstance());
                break;
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
                interfaceList.add(RouteBase.getInstance());
                break;
            case CLOCK_SERVICESELFSTOPPED:
                interfaceList.add(RouteBase.getInstance());
                interfaceList.add(Session.getInstance());
                //interfaceList.add(mainactivityInstance);
                break;
            case CLOCK_ONTICK:
                onClockList.add(RouteBase.getInstance()); /// delete closed flights from flightlist
                for (FlightBase f : Route.flightList) {
                    onClockList.add(f);                   /// check if any of them need to be replace temp flight num                     /// check if any of them need to be closed
                }
                onClockList.add(Session.getInstance());   /// start communication service
                break;
            case ALERT_SENTPOINTS:
                interfaceList.add(RouteBase.activeRoute);
                interfaceList.add(Session.getInstance());
                break;
            case MACT_BACKBUTTON_ONCLICK:
                interfaceList.add(Session.getInstance());
                break;
            case ALERT_STOPAPP:
                interfaceList.add(Session.getInstance());
                break;
            case SQL_TEMPFLIGHTNUM_ALLOCATED:
                interfaceList.add(Route.activeFlight);
                break;
            case SQL_ONCLEARCACHE_COMPLETED:
                interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                break;
            case SQL_FLIGHTRECORDCOUNT_ZERO:
                interfaceList.add((FlightBase)eventMessage.eventMessageValueObject);
                break;
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                interfaceList.add(RouteBase.activeRoute); // set route number
                interfaceList.add(new SvcLocationClock()); //start clock service in location mode
                interfaceList.add(mainactivityInstance);
                break;
            case FLIGHT_REMOTENUMBER_RECEIVED:
                interfaceList.add(RouteBase.getInstance()); /// add the base flight to flightlist if it is not in
                interfaceList.add(Session.getInstance());   /// start send locations for the flights with replaced flight number
                break;
            case FLIGHT_ONSENDCACHECOMPLETED:
                if (SimpleSettingsActivity.simpleSettingsActivityInstance!=null) interfaceList.add(SimpleSettingsActivity.simpleSettingsActivityInstance);
                break;
        }
        for( EventBus i : interfaceList) {
            if(!(null==i))i.eventReceiver(eventMessage);
            else  FontLog.appendLog(TAG + " null interface ", 'd');
        }
        for( EventBus i : onClockList) {
            if(!(null==i))i.onClock(eventMessage);
            else  FontLog.appendLog(TAG + " null interface ", 'd');
        }
    }

    default void eventReceiver(EventMessage eventMessage){}
    default void onClock(EventMessage eventMessage){}
}


