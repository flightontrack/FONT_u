package com.flightontrack.shared;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by hotvk on 8/17/2017.
 */

public interface GetTime {
    default String getTimeLocal() {
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }

    default long getTimeGMT() {
        long currTime = new Date().getTime();
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return currTime;
    }

    default String getDateTimeNow() {
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }
}
