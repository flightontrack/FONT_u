package com.flightontrack.flight;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Const.*;


public class Route extends RouteBase implements EventBus{
    private final String TAG = "Route";

    int _legCount = 0;
    String routeNumber=ROUTE_NUMBER_DEFAULT;

    public Route() {
        activeRoute = this;
    }

    void set_rAction(RACTION request) {
        //new FontLogAsync().execute(new LogMessage(TAG, "reaction:" + request, 'd');
        switch (request) {
            case OPEN_NEW_FLIGHT:
                flightList.add(new Flight(this));
                break;
            case SWITCH_TO_PENDING:
                break;
            case RESTART_NEW_FLIGHT:
                if (Props.SessionProp.pIsMultileg && (_legCount < LEG_COUNT_HARD_LIMIT)) {
                    /// ignore request to close route
                    flightList.add(new Flight(this));
                } else {
                    EventBus.distribute(new EventMessage(EVENT.ROUTE_ONRESTART).setEventMessageValueBool(false));
                }
                break;
//            case CLOSE_RECEIVEFLIGHT_FAILED:
//                flightList.remove(flightList.size() - 1);  /// remove the latest flight added
//                if (!(SvcLocationClock.instanceSvcLocationClock == null))
//                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
//                break;
//            case RECEIVEFLIGHT_FAILED_GET_TEMPFLIGHTNUMBER:
////                _legCount++;
////                int tempFlight = sqlHelper.getNewTempFlightNum();
////
////
////                if (!(SvcLocationClock.instanceSvcLocationClock == null))
////                    SvcLocationClock.instanceSvcLocationClock.set_mode(MODE.CLOCK_ONLY);
////                    //set_routeStatus(RSTATUS.PASSIVE);
////                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
////                    //activeRoute =null;
//                break;
        }
    }

//    static void checkIfAnyFlightNeedClose() {
//        try {
//            for (Route r : Route.routeList) {
//                for (Flight f : r.flightList) {
//                    if (f.lastAction == FACTION.CHANGE_IN_WAIT_TO_CLOSEFLIGHT) {
//                        f.set_fAction(FACTION.CLOSE_FLIGHT_IF_ZERO_LOCATIONS);
//                    }
//                    //String flights ="-";
//                    //flights=flights+f.flightNumber+"-"+f.lastAction+"-";
//                }
//            }
//        } catch (Exception e) {
//            //new FontLogAsync().execute(new LogMessage(TAG, "checkIfAnyFlightNeedClose: " + e.getMessage() + "\n" + e.getCause(), 'e');
//        }
//    }


@Override
public void eventReceiver(EventMessage eventMessage){
    EVENT ev = eventMessage.event;
    new FontLogAsync().execute(new EntityLogMessage(TAG, routeNumber+" :eventReceiver:"+ev+":eventString:"+eventMessage.eventMessageValueString, 'd'));
    switch(ev){
                case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                    if (routeNumber == ROUTE_NUMBER_DEFAULT) routeNumber =eventMessage.eventMessageValueString;
                    break;
                case MACT_BIGBUTTON_ONCLICK_START:
                    set_rAction(RACTION.OPEN_NEW_FLIGHT);
                    break;
                case FLIGHT_ONSPEEDLOW:
                    set_rAction(RACTION.RESTART_NEW_FLIGHT);
                break;

        }
    }
}
