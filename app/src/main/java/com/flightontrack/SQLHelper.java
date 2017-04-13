package com.flightontrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.flightontrack.Route.*;

class SQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "SQLHelper:";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FONTLOCATION.dbw";
    SQLiteDatabase dbw;
    static Cursor cl;

    SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Util.appendLog(TAG + "SQLHelper:SQLHelper", 'd');
        dbw = getWritableDatabase();
        dbw.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        dbw.execSQL(DBSchema.SQL_CREATE_TABLE_LOCATION_IF_NOT_EXISTS);
        //dbw.execSQL(DBSchema.SQL_DROP_TABLE_FLIGHT);
        //dbw.execSQL(DBSchema.SQL_CREATE_TABLE_FLIGHT_IF_NOT_EXISTS);
        dbw.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Util.appendLog(TAG + "SQLHelper:onCreate", 'd');
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL(DBSchema.SQL_DROP_TABLE_LOCATION);
        onCreate(db);
    }

    void rowLocationDelete(int id, String flightId) {
        String selection = DBSchema.COLUMN_NAME_COL10 + "= ? AND "+DBSchema.COLUMN_NAME_COL2+"= ?";
        String[] selectionArgs = {String.valueOf(id),flightId};

        try{
        dbw = getWritableDatabase();
        dbw.delete(
                DBSchema.TABLE_NAME_1,
                selection,
                selectionArgs
        );
        dbw.close();
        } catch (Exception e) {
            Util.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCount = getLocationTableCount();
    }
    void flightLocationsDelete(String flightId) {
        String selection = DBSchema.COLUMN_NAME_COL2+"= ?";
        String[] selectionArgs = {flightId};

        try{
            dbw = getWritableDatabase();
            dbw.delete(
                    DBSchema.TABLE_NAME_1,
                    selection,
                    selectionArgs
            );
            dbw.close();
        } catch (Exception e) {
            Util.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCount = getLocationTableCount();
    }
    protected int allLocationsDelete() {
        int i =0;
        try{
            dbw = getWritableDatabase();
            i = dbw.delete(
                    DBSchema.TABLE_NAME_1,"1",null
            );
            dbw.close();
        } catch (Exception e) {
            Util.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCount = 0;
        return i;
    }
    long  rowLocationInsert(ContentValues values) {
        long r = 0;
        try {
            dbw = getWritableDatabase();
            r = dbw.insert(DBSchema.TABLE_NAME_1,
                    null,
                    values);
            dbw.close();
            //if (r>0)Route.isDbRecord=true;
        } catch (Exception e) {
            Util.appendLog(TAG + e.getMessage(), 'e');
        }
        dbLocationRecCount = getLocationTableCount();
        return r;
    }

    void setCursorDataLocation() {

        String[] projection = {
                DBSchema._ID,
                DBSchema.COLUMN_NAME_COL1,
                DBSchema.COLUMN_NAME_COL2,
                DBSchema.COLUMN_NAME_COL3,
                DBSchema.COLUMN_NAME_COL4,
                DBSchema.COLUMN_NAME_COL6,
                DBSchema.COLUMN_NAME_COL7,
                DBSchema.COLUMN_NAME_COL8,
                DBSchema.COLUMN_NAME_COL9,
                DBSchema.COLUMN_NAME_COL10,
                DBSchema.COLUMN_NAME_COL11,
                DBSchema.COLUMN_NAME_COL12,
                DBSchema.COLUMN_NAME_COL13
        };
        String sortOrder = DBSchema._ID;
        String selection = null; //DBSchema._ID + "= ?";
        String[] selectionArgs = null; // { String.valueOf(newRowId) };
        dbw = getWritableDatabase();
        cl = dbw.query(
                DBSchema.TABLE_NAME_1,  // The table to query
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

    static int getCursorCountLocation() {
        return cl.getCount();
    }
//    public int getCursorCountFlight() {
//        return cf.getCount();
//    }

    int getLocationTableCount() {
        dbw = getWritableDatabase();
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_NAME_1);
        dbw.close();
        return (int) numRows;
    }
    int getLocationFlightCount(String flightId) {
        dbw = getWritableDatabase();
        String selection = DBSchema.COLUMN_NAME_COL2+"= ?";
        String[] selectionArgs = {flightId};
        long numRows = DatabaseUtils.queryNumEntries(dbw, DBSchema.TABLE_NAME_1,selection,selectionArgs);
        dbw.close();

        return (int) numRows;
    }

}
