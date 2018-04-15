package com.flightontrack.entities;

import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;


public class EntityRequestNewFlightOffline  extends RequestParams {

    public EntityRequestNewFlightOffline() {
        put("rcode", Const.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlightOffline set(String k, String v){
        put(k, v);
        return this;
    }

}
