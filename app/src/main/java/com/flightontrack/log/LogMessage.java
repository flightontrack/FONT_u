package com.flightontrack.log;

/**
 * Created by hotvk on 3/29/2018.
 */

public class LogMessage {
    public String tag;
    public String msg;
    public char msgType;

    public LogMessage(String p1, String p2, char p3){
        tag= p1;
        msg =p2;
        msgType=p3;
    }
}
