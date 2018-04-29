package com.flightontrack.entities;

import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;

public class EntityRequestCloseFlight   extends RequestParams implements AutoCloseable{
    final String TAG = "EntityRequestCloseFlight";
    final int rcode = Const.REQUEST_STOP_FLIGHT;


    public EntityRequestCloseFlight() {
        put("rcode", rcode);
    }

    public EntityRequestCloseFlight set(String k, String v) {
        put(k, v);
        return this;
    }
    public EntityRequestCloseFlight set(String k, boolean v) {
        put(k, v);
        return this;
    }
    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}

