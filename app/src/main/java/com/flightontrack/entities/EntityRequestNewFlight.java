package com.flightontrack.entities;

import com.flightontrack.shared.Const;
import com.loopj.android.http.RequestParams;

/**
 * Created by Tikhomirov on 3/7/2018.
 */

public class EntityRequestNewFlight  extends RequestParams {
//    public int rcode            = Const.REQUEST_FLIGHT_NUMBER;
//    public String phonenumber   ="9784295693";
//    public String username      ="Myusername";
//    public String userid ="9784295693.0993";
//    public String deviceid="356204066730993";
//    public String aid="3fc58d5bbfbfc796";
//    public String versioncode="2.0";
//    public String AcftNum="3072S";
//    public String AcftTagId;
//    public String AcftName="MyAcrft";
//    public String isFlyingPattern="0";
//    public String freq="3";
//    public String speed_thresh="0";
//    public String isdebug="0";
//    public String routeid="100";
//    public String speed="10";
//    public String speedlowflag="0";

    public EntityRequestNewFlight() {
        put("rcode", Const.REQUEST_FLIGHT_NUMBER);
    }

    public EntityRequestNewFlight set(String k, String v){
        put(k, v);
        return this;
    }
}
