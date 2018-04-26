package com.flightontrack.shared;

public abstract class Const {

    public static final int APPBOOT_DELAY_MILLISEC = 30000;

    public static final int REQUEST_LOCATION_UPDATE = 1;
    public static final int REQUEST_FLIGHT_NUMBER = 2;
    public static final int REQUEST_STOP_FLIGHT = 4;
    public static final int REQUEST_PSW = 6;
    public static final int REQUEST_IS_CLOCK_ON = 7;

    public static final String RESPONSE_TYPE_DATA_WITHLOAD = "0";
    public static final String RESPONSE_TYPE_NOTIF_WITHLOAD = "2";
    public static final String RESPONSE_TYPE_DATA_PSW = "aP";



    public static final String COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN = "TerminateFlightLowSpeed";
    public static final String COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED = "TerminateFlightMaxPointsReached";
    public static final int COMMAND_FLIGHT_STATE_PENDING = -7;
    public static final String COMMAND_TERMINATEFLIGHT = "TerminateFlightOnAltitude";

    public static final long DISTANCE_CHANGE_FOR_UPDATES_MIN = 0; //20; //  meters
    public static final long DISTANCE_CHANGE_FOR_UPDATES_ZERO = 0; //  meters

    public static final long ZERO_DISTANCE_CHANGE_FOR_UPDATES = 0;
    public static final long MIN_TIME_BW_GPS_UPDATES = 3000;
    public static final int  MIN_TIME_BW_GPS_UPDATES_SEC = (int)MIN_TIME_BW_GPS_UPDATES/1000;
    public static final int  SPEEDLOW_TIME_BW_GPS_UPDATES_SEC = 5;
    public static final int  ALARM_TIME_SEC = 600;

    public static final long DEFAULT_TIME_BW_GPS_UPDATES = 10000; //... sec
    public static final long DEFAULT_DISTANCE_CHANGE_FOR_UPDATES = 10; //  meters

    public static final int TIME_RESERVE =150;

    public static final int DEFAULT_SPEED_SPINNER_POS = 0;
    public static final int DEFAULT_URL_SPINNER_POS = 0;
    public static final int DEFAULT_INTERVAL_SELECTED_ITEM = 6;

    public static final String FLIGHT_NUMBER_DEFAULT = "00";
    public static final String ROUTE_NUMBER_DEFAULT = "00";

    public static final int COMM_BATCH_SIZE_MAX = 100;
    public static final int COMM_BATCH_SIZE_MIN = 1;
    public static final String GLOBALTAG="FLIGHT_ON_TRACK";

    public static final String SPACE=" ";
    public static final String FLIGHT_TIME_ZERO ="00:00";

    public static final int WAY_POINT_HARD_LIMIT = 1200;
    public static final int LEG_COUNT_HARD_LIMIT = 3;
    public static final int ELEVATIONCHECK_FLIGHT_TIME_SEC = 70; //20;

    public static final String PACKAGE_NAME= "com.flightontrack";
    //public static final String FONT_RECEIVER_FILTER= "com.flightontrack.START_FONT_ACTIVITY";
    public static final String FONT_RECEIVER_FILTER= PACKAGE_NAME.concat(".START_FONT_ACTIVITY");
    //public static final String HEALTHCHECK_BROADCAST_RECEIVER_FILTER = "com.flightontrack.BROADCAST_HEALTHCHECK";
    public static final String HEALTHCHECK_BROADCAST_RECEIVER_FILTER = PACKAGE_NAME.concat(".BROADCAST_HEALTHCHECK");

    public static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    public static final int MY_PERMISSIONS_RITE_EXTERNAL_STORAGE = 3;
    public static final int START_ACTIVITY_RESULT = 1;
    public static final int MAX_FAILURE_COUNT = 10;
    public static final int MAX_JSON_ERROR = 10;

    public static final String SMS_RECEIPIENT_PHONE = "9784295693";
    public static final String SMS_RECEIPIENT_PHONE_CC = "9784295693";
    public static final String SMS_LOWBATTERY_TEXT = "Battery is low, please recharge.";

    public enum ALERT_RESPONSE {
        POS,
        NEG,
        CANCEL}

    public enum RSTATUS {
        PASSIVE,
        ACTIVE}

    public enum FSTATUS {
        PASSIVE,
        ACTIVE}

    public enum MODE {
        CLOCK_LOCATION,
        CLOCK_ONLY}

//    public enum FACTION {
//        REQUEST_FLIGHT,
//        CHANGE_IN_PENDING,
//        CHANGE_IN_WAIT_TO_CLOSEFLIGHT,
//        CHANGE_IN_FLIGHT,
//        CHANGESTATE_SPEED_BELOW_MIN,
//        CHANGESTATE_COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED,
//        GET_LOCATIONSERVICE,
//        CLOSE_FLIGHT_IF_ZERO_LOCATIONS,
//        TERMINATE_FLIGHT,
//        CLOSED,
//        CLOSED_FAILURE,
//        FLIGHTTIME_UPDATE,
//        REQUEST_FLIGHTNUMBER,
//        ON_FLIGHTGET_FINISH,
//        ON_SERVER_N0TIF}

    public enum BUTTONREQUEST{
        BUTTON_STATE_RED,
        BUTTON_STATE_GETFLIGHTID,
        BUTTON_STATE_YELLOW,
        BUTTON_STATE_GREEN,
        BUTTON_STATE_STOPPING;

        public static BUTTONREQUEST toMyEnum (String myEnumString) {
                return valueOf(myEnumString);
        }
    }

    public enum APPTYPE {
        PUBLIC,
        PRIVATE}


}
