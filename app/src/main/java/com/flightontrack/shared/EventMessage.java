package com.flightontrack.shared;
import android.location.Location;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.flight.Session.*;
import static com.flightontrack.flight.Route.*;

/**
 * Created by hotvk on 12/28/2017.
 */

public class EventMessage implements EventBus {
public EVENT event;
public boolean eventMessageValueBool;
public String eventMessageValueString;
public int eventMessageValueInt;
public Object eventMessageValueObject;
public Location eventMessageValueLocation;
public MODE eventMessageValueClockMode;
public SACTION eventMessageValueSessionRequest;
public RACTION eventMessageValueRouteRequest;
public ALERT_RESPONSE eventMessageValueAlertResponse;
//public SVCCOMM_SUCCESS_TYPE eventMessageValueSvcCommSuccessType;

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
public EventMessage setEventMessageValueSessionRequest(SACTION val){
    this.eventMessageValueSessionRequest = val;
    return this;
}
public EventMessage setEventMessageValueRouteRequest(RACTION val){
    this.eventMessageValueRouteRequest = val;
    return this;
}
public EventMessage setEventMessageValueAlertResponse(ALERT_RESPONSE val){
    this.eventMessageValueAlertResponse = val;
    return this;
}
public EventMessage setEventMessageValueInt(int val){
    this.eventMessageValueInt= val;
    return this;
}
//public EventMessage setEventMessageValueSvcCommSuccessType(SVCCOMM_SUCCESS_TYPE val){
//    this.eventMessageValueSvcCommSuccessType= val;
//    return this;
//}
public EventMessage setEventMessageValueString(String val){
    this.eventMessageValueString= val;
    return this;
}
public EventMessage setEventMessageValueObject(Object val){
    this.eventMessageValueObject= val;
    return this;
}
}
