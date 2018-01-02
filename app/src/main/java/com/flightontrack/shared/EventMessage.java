package com.flightontrack.shared;
import android.location.Location;

import com.flightontrack.flight.Route;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.flight.Session.*;
import static com.flightontrack.flight.Route.*;
/**
 * Created by hotvk on 12/28/2017.
 */

public class EventMessage implements EventBus {
public EVENT event;
public boolean eventMessageValueBool;
public Location eventMessageValueLocation;
public MODE eventMessageValueClockMode;
public SESSIONREQUEST eventMessageValueSessionRequest;
public ROUTEREQUEST eventMessageValueRouteRequest;
public RESPONSE eventMessageValueAlertResponse;
public String eventMessageValue;

public  EventMessage(EVENT eventMessage ){
    this.event = eventMessage;
    //return this;
}
public EventMessage setEventMessageValueBool(Boolean val){
    this.eventMessageValueBool = val;
    return this;
}
public EventMessage setEventMessageValueLocation(Location val){
    this.eventMessageValueLocation = val;
    return this;
}
public EventMessage setEventMessageValueClockMode(MODE val){
        this.eventMessageValueClockMode = val;
        return this;
    }
public EventMessage setEventMessageValueSessionRequest(SESSIONREQUEST val){
    this.eventMessageValueSessionRequest = val;
    return this;
}
public EventMessage setEventMessageValueRouteRequest(ROUTEREQUEST val){
    this.eventMessageValueRouteRequest = val;
    return this;
}
public EventMessage setEventMessageValueAlertResponse(RESPONSE val){
    this.eventMessageValueAlertResponse = val;
    return this;
}
}
