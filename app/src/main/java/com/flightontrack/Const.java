package com.flightontrack;

abstract  class Const {

    static final int APPBOOT_DELAY_MILLISEC = 30000;

    static final int REQUEST_LOCATION_UPDATE = 1;
//    static final int REQUEST_LAST_POINT = 3;
    static final int REQUEST_FLIGHT_NUMBER = 2;
    static final int REQUEST_STOP_FLIGHT = 4;
//    static final int REQUEST_STOP_FLIGHTON_LIMIT_REACHED = 5;
    static final int REQUEST_PSW = 6;
    static final int REQUEST_IS_CLOCK_ON = 7;

//    static final char RESPONSE_TYPE_COMMAND = '1';
//    static final char RESPONSE_TYPE_DATA = '0';
//    static final char RESPONSE_TYPE_NOTIF = '2';
//    static final char RESPONSE_TYPE_ACKN = '3';

    static final String RESPONSE_TYPE_DATA_WITHLOAD = "0";
    static final String RESPONSE_TYPE_NOTIF_WITHLOAD = "2";
    static final String RESPONSE_TYPE_DATA_PSW = "aP";

    static final int COMMAND_STOP_FLIGHT_SPEED_BELOW_MIN = -10;
    static final int COMMAND_STOP_FLIGHT_ON_LIMIT_REACHED = -9;
    static final int COMMAND_FLIGHT_STATE_PENDING = -7;
    static final int COMMAND_CANCELFLIGHT = -6;

//    static final int NOTIF_UNKNOWN_SERVER_ERROR = -1;
//    static final int NOTIF_UNKNOWN_REQUEST = 1;
//    static final int NOTIF_DB_ERROR = 2;

    static final int LOCATION_UPDATE_INTERVAL_SEC_DEFAULT = 3;
    static final int MINSPEED_UPDATE_INTERVAL_SEC_DEFAULT = 1; //sec
    static final int LOCATION_ACCURACY = 150;

    static final int FLIGHT_CALLS_NUM = 2;

    static final long DISTANCE_CHANGE_FOR_UPDATES_MIN = 0; //20; //  meters
    static final long DISTANCE_CHANGE_FOR_UPDATES_ZERO = 0; //  meters

    static final long ZERO_DISTANCE_CHANGE_FOR_UPDATES = 0;
    static final long MIN_TIME_BW_GPS_UPDATES = 3000;
    static final int  MIN_TIME_BW_GPS_UPDATES_SEC = (int)MIN_TIME_BW_GPS_UPDATES/1000;
    static final int  SPEEDLOW_TIME_BW_GPS_UPDATES_SEC = 5;
    static final int  ALARM_TIME_SEC = 10; //600;

    static final long DEFAULT_TIME_BW_GPS_UPDATES = 10000; //... sec
    static final long DEFAULT_DISTANCE_CHANGE_FOR_UPDATES = 10; //  meters

    static final int TIME_RESERVE =150;

    static final int DEFAULT_SPEED_SPINNER_POS = 0;
    static final int DEFAULT_URL_SPINNER_POS = 0;
    static final int DEFAULT_INTERVAL_SELECTED_ITEM = 6;

    static final String FLIGHT_NUMBER_DEFAULT = "00";
    static final String ROUTE_NUMBER_DEFAULT = "00";

    static final int COMM_BATCH_SIZE_MAX = 10;
    static final int COMM_BATCH_SIZE_MIN = 1;
    static final int START_TRACKPOINT_COUNTER = 0;
    static final String GLOBALTAG="FLIGHT_ON_TRACK";

    static final String SPACE=" ";
    static final String FLIGHT_TIME_ZERO ="00:00";

    static final int WAY_POINT_HARD_LIMIT = 1200;
    static final int LEG_COUNT_HARD_LIMIT = 15;
    static final int ELEVATIONCHECK_FLIGHT_TIME_SEC = 70; //20;

    static final String FONT_RECEIVER_FILTER= "com.flightontrack.START_FONT_ACTIVITY";
    static final String HEALTHCHECK_BROADCAST_RECEIVER_FILTER = "com.flightontrack.BROADCAST_HEALTHCHECK";

    static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;
    static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    static final int MY_PERMISSIONS_RITE_EXTERNAL_STORAGE = 3;
    static final int START_ACTIVITY_RESULT = 1;
    static final int MAX_FAILURE_COUNT = 10;
    static final int MAX_JSON_ERROR = 10;

    enum ROUTEREQUEST{
        OPEN_NEW_ROUTE,
        SWITCH_TO_PENDING,
        ON_FLIGHTTIME_CHANGED,
        CLOSE_BUTTON_STOP_PRESSED,
        CLOSEAPP_BUTTON_BACK_PRESSED,
        CLOSE_RECEIVEFLIGHT_FAILED,
        CLOSE_SPEED_BELOW_MIN,
        CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST,
        CLOSE_POINTS_LIMIT_REACHED,
        CLOSE_FLIGHT_CANCELED,
        START_COMMUNICATION,
        ON_COMMUNICATION_SUCCESS,
        ON_CLOSE_FLIGHT,
//        ON_CLOSE_FLIGHT_SUCCESS,
//        ON_CLOSE_FLIGHT_FAILURE,
        CLOSE_FLIGHT_DELETE_ALL_POINTS
    }
    enum RSTATUS {
        PASSIVE,
        ACTIVE}
    enum FSTATUS {
        PASSIVE,
        ACTIVE}
    enum MODE {
        CLOCK_LOCATION,
        CLOCK_ONLY}
    enum FLIGHTREQUEST {
        CHANGESTATE_REQUEST_FLIGHT,
        CHANGESTATE_STATUSACTIVE,
        CHANGESTATE_STATUSPASSIVE,
        CHANGESTATE_INFLIGHT,
        CHANGESTATE_SPEED_BELOW_MIN,
        GET_LOCATIONSERVICE,
        CLOSE_FLIGHT,
        CLOSED,
        CLOSED_FAILURE,
        FLIGHTTIME_UPDATE,
        ON_FLIGHTGET_FINISH}
    enum BUTTONREQUEST{
        BUTTON_STATE_RED,
        BUTTON_STATE_GETFLIGHTID,
        BUTTON_STATE_YELLOW,
        BUTTON_STATE_GREEN,
        BUTTON_STATE_STOPPING;
        public static BUTTONREQUEST toMyEnum (String myEnumString) {
                return valueOf(myEnumString);
        }
    }

    enum APPTYPE {
        PUBLIC,
        PRIVATE}
enum COMMAND{
    COMMAND_CANCELFLIGHT
}
}
