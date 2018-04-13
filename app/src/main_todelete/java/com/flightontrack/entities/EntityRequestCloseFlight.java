package com.communication.atry.communication;

import com.flightontrack.shared.Const;

import static com.communication.atry.communication.MainActivity.flightId;

/**
 * Created by Tikhomirov on 4/4/2018.
 */

public class EntityRequestCloseFlight {
    public int rcode  = Const.REQUEST_STOP_FLIGHT;
    public String flightid = flightId;
    public Boolean speedlowflag=true;
    public Boolean isLimitReached=false;
    public Boolean isdebug=false;
}
