package com.flightontrack.shared;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.flight.Route;
import com.flightontrack.flight.Session;
import com.flightontrack.locationclock.SvcLocationClock;

import static com.flightontrack.shared.Const.*;

/**
 * Created by hotvk on 12/28/2017.
 */

public class EventBus {
    public static void distribute(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
        switch(ev){
            case MACT_BIGBUTTON_CLICKED_START:
                Route.eventReceiver(EVENT.MACT_BIGBUTTON_CLICKED_START);
                break;
            case MACT_BIGBUTTON_CLICKED_STOP:
                Props.eventReceiver(EVENT.MACT_BIGBUTTON_CLICKED_STOP);
                Session.eventReceiver(EVENT.MACT_BIGBUTTON_CLICKED_STOP);
                Route.eventReceiver(EVENT.MACT_BIGBUTTON_CLICKED_STOP);
                break;
            case PROP_CHANGED_MULTILEG:
                MainActivity.eventReceiver(eventMessage);
            case FLIGHT_GETNEWFLIGHT_STARTED:
                MainActivity.eventReceiver(eventMessage);
            case SVCCOMM_ONSUCCESS_NOTIFICATION:
                MainActivity.eventReceiver(eventMessage);
                SvcLocationClock.eventReceiver(eventMessage);
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:

        }
    }
}


