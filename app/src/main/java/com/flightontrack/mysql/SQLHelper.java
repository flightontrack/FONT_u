package com.flightontrack.mysql;

import android.content.ContentValues;
//import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.flightontrack.log.FontLog;
import com.flightontrack.shared.EventMessage;

import static com.flightontrack.shared.Const.COMMAND_TERMINATEFLIGHT;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.GetTime;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class SQLHelper extends SQLiteOpenHelper implements EventBus,GetTime {

    private static final String TAG = "SQLHelper:";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FONTLOCATION.dbw";
    public SQLiteDatabase dbw;
    public static Cursor cl;

    public SQLHelper() {
        super(ctxApp, DATABASE_NAME, null, DATABASE_VERSION);
        FontLog.appendLog(TAG + "SQLHelper:SQLHelper", 'd');
        dbw = getWritableDatabase();
        dbw.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        dbw.execSQL(DBSchema.SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
        dbw.execSQL(DBSchema.SQL_CREATE_TABLE_FLIGHTNUM_IF_NOT_EXISTS);
        dbLocationRecCountTotal = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
        if (dbLocationRecCountTotal == 0) {
            dbw.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
            dbw.execSQL(DBSchema.SQL_DROP_TABLE_FLIGHT_NUMBER);
            dbw.execSQL(DBSchema.SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
            dbw.execSQL(DBSchema.SQL_CREATE_TABLE_FLIGHTNUM_IF_NOT_EXISTS);
        }
        dbTempFlightRecCount = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_FLIGHTNUMBER);
        FontLog.appendLog(TAG + "Unsent Locations from Previous Session :  " + dbLocationRecCountTotal, 'd');
        FontLog.appendLog(TAG + "Temp Flights Previous Session :  " + dbTempFlightRecCount, 'd');
        dbw.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FontLog.appendLog(TAG + "SQLHelper:onCreate", 'd');
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        onCreate(db);
    }
    void dropCreateDb(){
        dbw = getWritableDatabase();
        int lcount = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
        dbw.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        dbw.execSQL(DBSchema.SQL_DROP_TABLE_FLIGHT_NUMBER);
        dbw.execSQL(DBSchema.SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
        dbw.execSQL(DBSchema.SQL_CREATE_TABLE_FLIGHTNUM_IF_NOT_EXISTS);
        dbLocationRecCountTotal = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
        dbTempFlightRecCount = (int) DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_FLIGHTNUMBER);
        if(dbLocationRecCountTotal ==0){
            Toast.makeText(ctxApp,"Deleted "+lcount+" location points",Toast.LENGTH_LONG).show();
        }
        dbw.close();
    }
    public void rowLocationDelete(int id, String flightId) {
        String selection = DBSchema.LOC_wpntnum + "= ? AND "+DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs = {String.valueOf(id),flightId};

        try{
        dbw = getWritableDatabase();
        dbw.delete(
                DBSchema.TABLE_LOCATION,
                selection,
                selectionArgs
        );
        dbw.close();
        } catch (Exception e) {
            FontLog.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCountTotal = getLocationTableCountTotal();
    }
    public void flightLocationsDelete(String flightId) {
        String selection = DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs = {flightId};

        try{
            dbw = getWritableDatabase();
            dbw.delete(
                    DBSchema.TABLE_LOCATION,
                    selection,
                    selectionArgs
            );
            dbw.close();
        } catch (Exception e) {
            FontLog.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCountTotal = getLocationTableCountTotal();
    }
    public int allLocationsDelete() {
        int i =0;
        try{
            dbw = getWritableDatabase();
            i = dbw.delete(
                    DBSchema.TABLE_LOCATION,"1",null
            );
            dbw.close();
        } catch (Exception e) {
            FontLog.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCountTotal = 0;
        return i;
    }
    public long  rowLocationInsert(ContentValues values) {
        long r = 0;
        try {
            dbw = getWritableDatabase();
            r = dbw.insert(DBSchema.TABLE_LOCATION,
                    null,
                    values);
            dbw.close();
            //if (r>0)Route.isDbRecord=true;
        } catch (Exception e) {
            FontLog.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCountTotal = getLocationTableCountTotal();
        dbLocationRecCountTemp = getLocationTableCountTemp();
        return r;
    }

    public void setCursorDataLocation() {

        String[] projection = {
                DBSchema._ID,
                DBSchema.COLUMN_NAME_COL1,
                DBSchema.LOC_flightid,
                DBSchema.LOC_isTempFlight,
                DBSchema.LOC_speedlowflag,
                DBSchema.COLUMN_NAME_COL4,
                DBSchema.COLUMN_NAME_COL6,
                DBSchema.COLUMN_NAME_COL7,
                DBSchema.COLUMN_NAME_COL8,
                DBSchema.COLUMN_NAME_COL9,
                DBSchema.LOC_wpntnum,
                DBSchema.COLUMN_NAME_COL11,
                DBSchema.LOC_date,
                DBSchema.COLUMN_NAME_COL13
        };
        String sortOrder = DBSchema._ID;
        //String selection = null; //DBSchema._ID + "= ?";
        String selection = DBSchema.LOC_isTempFlight + "= ?";
        //String[] selectionArgs = null; // { String.valueOf(newRowId) };
        String[] selectionArgs = {"0"}; // { String.valueOf(newRowId) };
        dbw = getWritableDatabase();
        cl = dbw.query(
                DBSchema.TABLE_LOCATION,  // The table to query
                projection,                               // The columns to return
                selection,                               // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        cl.moveToFirst();
        dbw.close();
    }

    public static int getCursorCountLocation() {
        return cl.getCount();
    }
//    public int getCursorCountFlight() {
//        return cf.getCount();
//    }

    int getLocationTableCountTotal() {
        dbw = getWritableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION);
        dbw.close();
        return (int) numRows;
    }
    int getLocationTableCountTemp() {
        dbw = getWritableDatabase();
        Cursor c = dbw.rawQuery("select _id from Location where istempflightnum =1" ,new String[]{});
        c.moveToFirst();
        int numRows = c.getCount();
        dbw.close();
        return (int) numRows;
    }
    int getTempFlightTableCount() {
        dbw = getWritableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_FLIGHTNUMBER);
        dbw.close();
        return (int) numRows;
    }
    public int getLocationFlightCount(String flightId) {
        dbw = getWritableDatabase();
        String selection = DBSchema.LOC_flightid +"= ?";
        String[] selectionArgs = {flightId};
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_LOCATION,selection,selectionArgs);
        dbw.close();

        return (int) numRows;
    }
    public String getNewTempFlightNum(String dateTime){
        dbw = getWritableDatabase();

        Cursor c = dbw.rawQuery("select max(ifnull(flightNumber,0)) from FlightNumber" ,new String[]{});
        c.moveToFirst();
        int f= c.getCount()<=0?1:c.getInt(0)+1;
        ContentValues values = new ContentValues();
        values.put(DBSchema.FLIGHTNUM_FlightNumber, f); //flightid
        values.put(DBSchema.FLIGHTNUM_RouteNumber, 0);
        values.put(DBSchema.FLIGHTNUM_FlightTimeStart, dateTime); //date

        long r = 0;
        try {
            r = dbw.insert(DBSchema.TABLE_FLIGHTNUMBER,
                    null,
                    values);
        } catch (Exception e) {
            FontLog.appendLog(TAG + e.getMessage(), 'e');
        }
        finally {
            dbw.close();
        }

        if (r > 0) {
            dbTempFlightRecCount = f;
            FontLog.appendLog(TAG + "getNewTempFlightNum: dbTempFlightRecCount: " + dbTempFlightRecCount, 'd');
        }
        return (String.valueOf(f));
    }
    public String getMinTempFlightNum(){
        return "1";
    }
    public void insertTempFlight(SQLiteDatabase db,String flightNumber,String routeNumber, String dateTime){
//        ContentValues values = new ContentValues();
//        values.put(DBSchema.FLIGHT_COLUMN_NAME_COL1, flightNumber); //flightid
//        values.put(DBSchema.FLIGHT_COLUMN_NAME_COL1, routeNumber); //flightid
//        values.put(DBSchema.FLIGHT_COLUMN_NAME_COL2, dateTime); //date
//        long r = sqlHelper.rowLocationInsert(values);
//        if (r > 0) {
//            FontLog.appendLog(TAG + "saveLocation: dbLocationRecCountTotal: " + Props.SessionProp.dbLocationRecCountTotal, 'd');
//        }
//        Cursor c = db.rawQuery("select max(ifnull(flightNumber,0)) from FlightNumber",new String[]{"0"});

    }
    @Override
    public void eventReceiver(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
        switch(ev){
            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
                sqlHelper.dropCreateDb();
                break;
            case SVCCOMM_ONSUCCESS_COMMAND:
                if (eventMessage.eventMessageValueInt==COMMAND_TERMINATEFLIGHT) flightLocationsDelete(eventMessage.eventMessageValueString);
                break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
                if(!eventMessage.eventMessageValueBool)
                    try {
                        String dt = URLEncoder.encode(getDateTimeNow(), "UTF-8");
                        EventBus.distribute(new EventMessage(EVENT.SQL_TEMPFLIGHTNUM_ALLOCATED).setEventMessageValueString(getNewTempFlightNum(dt)));

                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                break;
        }
    }
}
