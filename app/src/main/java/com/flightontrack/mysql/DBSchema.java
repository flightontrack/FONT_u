package com.flightontrack.mysql;

import android.provider.BaseColumns;

public abstract class DBSchema implements BaseColumns {

 private static final String TEXT_TYPE = " TEXT";
 private static final String INT_TYPE = " INTEGER";
 private static final String BOOLEAN_TYPE = " BOOLEAN";
 private static final String COMMA_SEP = ",";
 static final String TABLE_LOCATION = "Location";
 static final String TABLE_FLIGHT = "FLIGHT";
 static final String TABLE_NAME_3 = "LocationExt";


static final String TABLE_FLIGHTNUMBER_ALLOCATION = "FlightNumberAllocation";
static public final String FLIGHTNUM_FlightNumber = "FlightNumber";
static final String FLIGHTNUM_RouteNumber = "RouteNumber";
static final String FLIGHTNUM_FlightTimeStart = "FlightTimeStart";
static final String SQL_CREATE_TABLE_FLIGHTNUM_ALLOC_IF_NOT_EXISTS =    "CREATE TABLE IF NOT EXISTS " + TABLE_FLIGHTNUMBER_ALLOCATION + " (" +
         _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        FLIGHTNUM_FlightNumber + INT_TYPE +COMMA_SEP +
        FLIGHTNUM_RouteNumber + TEXT_TYPE +COMMA_SEP +
        FLIGHTNUM_FlightTimeStart + TEXT_TYPE +
         " )";

     public static final String COLUMN_NAME_ENTRY_ID = "id";
     public static final String COLUMN_NAME_COL1 = "rcode";
     public static final String LOC_flightid = "flightid";
     public static final String LOC_isTempFlight = "istempflightnum";
     public static final String LOC_speedlowflag = "speedlowflag";
     public static final String COLUMN_NAME_COL4 = "speed";
    // public static final String COLUMN_NAME_COL5 = "deviceid";
     public static final String COLUMN_NAME_COL6 = "latitude";
     public static final String COLUMN_NAME_COL7 = "longitude";
     public static final String COLUMN_NAME_COL8 = "accuracy";
     public static final String COLUMN_NAME_COL9 = "extrainfo";
     public static final String LOC_wpntnum = "wpntnum";
     public static final String COLUMN_NAME_COL11 = "gsmsignal";
    public static final String LOC_date = "date";
     public static final String COLUMN_NAME_COL13 = "is_elevetion_check";

     static final String FLIGHT_COLUMN_NAME_COL1 = "state_isAlarmDisabled";
     static final String FLIGHT_COLUMN_NAME_COL2 = "request_StartFlight";
     static final String FLIGHT_COLUMN_NAME_COL3 = "request_StopFlight";
     static final String FLIGHT_COLUMN_NAME_COL4 = "state_isSpeedAboveMin";
     static final String FLIGHT_COLUMN_NAME_COL5 = "state_isDbRecord";
     static final String FLIGHT_COLUMN_NAME_COL6 = "state_isLimitReached";
     static final String FLIGHT_COLUMN_NAME_COL7 = "state_isMultileg";
     static final String FLIGHT_COLUMN_NAME_COL8 = "state_FlightState";
     static final String FLIGHT_COLUMN_NAME_COL9 = "state_ButtonState";
     static final String FLIGHT_COLUMN_NAME_COL10 = "request_AlarmCancel";
     static final String FLIGHT_COLUMN_NAME_COL11 = "request_AlarmDisabled";
     static final String FLIGHT_COLUMN_NAME_COL12 = "request_GetFlight";
     static final String FLIGHT_COLUMN_NAME_COL13 = "state_ActiveFlightID";
     static final String FLIGHT_COLUMN_NAME_COL14 = "request_AlarmStart";


     static final String SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS =    "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            //COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
             LOC_flightid + TEXT_TYPE + COMMA_SEP +
             LOC_isTempFlight + BOOLEAN_TYPE + COMMA_SEP +
             LOC_speedlowflag + BOOLEAN_TYPE + COMMA_SEP +
            COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL7 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL8 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL9 + TEXT_TYPE + COMMA_SEP +
             LOC_wpntnum + INT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL11 + TEXT_TYPE + COMMA_SEP +
             LOC_date + TEXT_TYPE + COMMA_SEP +
             COLUMN_NAME_COL13 + BOOLEAN_TYPE +
             " )";
     static final String SQL_CREATE_TABLE_LOCATIONEXT_IF_NOT_EXISTS =    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_3 + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            //COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL1 + INT_TYPE + COMMA_SEP +
             LOC_flightid + TEXT_TYPE + COMMA_SEP +
             LOC_speedlowflag + BOOLEAN_TYPE + COMMA_SEP +
            COLUMN_NAME_COL4 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL6 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL7 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL8 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL9 + TEXT_TYPE + COMMA_SEP +
             LOC_wpntnum + INT_TYPE + COMMA_SEP +
            COLUMN_NAME_COL11 + TEXT_TYPE + COMMA_SEP +
             LOC_date + TEXT_TYPE +
            " )";

     static final String SQL_CREATE_TABLE_FLIGHT_IF_NOT_EXISTS =    "CREATE TABLE IF NOT EXISTS " + TABLE_FLIGHT + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FLIGHT_COLUMN_NAME_COL1+ INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL2+ INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL3+ INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL4+ INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL5+ INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL6 + INT_TYPE + COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL7 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL8 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL9 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL10 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL11 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL12 + INT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL13 + TEXT_TYPE+ COMMA_SEP +
            FLIGHT_COLUMN_NAME_COL14 + INT_TYPE+
            " )";

     static final String SQL_DROP_TABLE_LOCATION =    "DROP TABLE IF EXISTS " + TABLE_LOCATION;
     static final String SQL_DROP_TABLE_LOCATIONEXT =    "DROP TABLE IF EXISTS " + TABLE_NAME_3;
     static final String SQL_DROP_TABLE_FLIGHTNUMBER_ALLOC =    "DROP TABLE IF EXISTS " + TABLE_FLIGHTNUMBER_ALLOCATION;
     static final String SQL_DROP_TABLE_FLIGHTNUMBER =    "DROP TABLE IF EXISTS FLIGHT_NUMBER";
     static final String SQL_DROP_TABLE_FLIGHT =    "DROP TABLE IF EXISTS " + TABLE_FLIGHT;
}
