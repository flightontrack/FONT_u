package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.Props;
import com.loopj.android.http.RequestParams;

/**
 * Created by hotvk on 4/12/2018.
 */

public class EntityRequestPostLocation   extends RequestParams implements AutoCloseable{
    static final String TAG = "EntityRequestPostLocation";

    public EntityRequestPostLocation(EntityLocation l) {
        put("rcode", Const.REQUEST_LOCATION_UPDATE);
        put("isdebug", Props.SessionProp.pIsDebug);
        put("speedlowflag", l.sl == 1);
        put("rcode", l.rc);
        put("latitude", l.la);
        put("longitude", l.lo);
        put("flightid", l.ft);
        put("accuracy", l.ac);
        put("extrainfo", l.al);
        put("wpntnum", l.wp);
        put("gsmsignal", l.sg);
        put("speed", l.sd);
        put("dtime", l.dt);
        put("elevcheck", l.irch == 1);
    }

    public EntityRequestPostLocation set(String k, String v){
        put(k, v);
        return this;
    }

    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
