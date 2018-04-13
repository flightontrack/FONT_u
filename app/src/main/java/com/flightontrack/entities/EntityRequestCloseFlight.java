package com.flightontrack.entities;

import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;

public class EntityRequestCloseFlight   extends RequestParams {
    final int rcode = Const.REQUEST_STOP_FLIGHT;

//        public int rcode = Const.REQUEST_STOP_FLIGHT;
//        public String flightid = flightId;
//        public Boolean speedlowflag = true;
//        public Boolean isLimitReached = false;
//        public Boolean isdebug = false;

    public EntityRequestCloseFlight() {
        put("rcode", rcode);
    }

    public EntityRequestCloseFlight set(String k, String v) {
        put(k, v);
        return this;
    }
}

