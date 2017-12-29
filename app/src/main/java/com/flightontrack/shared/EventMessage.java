package com.flightontrack.shared;
import static com.flightontrack.shared.Const.*;
/**
 * Created by hotvk on 12/28/2017.
 */

public class EventMessage {
public EVENT event;
public boolean eventMessageValueBool;
public String eventMessageValue;

public  EventMessage(EVENT eventMessage ){
    this.event = eventMessage;
    //return this;
}
public EventMessage setEventMessageValueBool(Boolean val){
    this.eventMessageValueBool = val;
    return this;
}

}
