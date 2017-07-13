package com.flightontrack.flight;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.flightontrack.activity.MainActivity;
import com.flightontrack.R;
import com.flightontrack.ui.ShowAlertClass;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Util;
import com.flightontrack.communication.SvcComm;
import com.flightontrack.mysql.DBSchema;
import com.flightontrack.mysql.SQLHelper;

import java.util.ArrayList;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Statics.*;

public class Route {
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    private static final String TAG = "Route:";
    //static Context ctx;
//    private static SharedPreferences sharedPreferences;
//    private static SharedPreferences.Editor editor;

    static  String          routeNumber     = null;
            int             _legCount =0;
    public  static  int             dbLocationRecCount = 0;
    public static FlightInstance activeFlight;
    public static  RSTATUS         _routeStatus =RSTATUS.PASSIVE;
    public static  BUTTONREQUEST    trackingButtonState=BUTTONREQUEST.BUTTON_STATE_RED;
    public static  boolean          _isRoad = false; //change isRoad() too in two places
    public static  Route            instanceRoute = null;
    public static SQLHelper sqlHelper;

    public static ArrayList<FlightInstance> currentFlights = new ArrayList<>();

    public Route(){
        instanceRoute =this;
        //Route.ctx = ctx;
        //sharedPreferences = ctxApp.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        //editor = sharedPreferences.edit();
        sqlHelper = new SQLHelper();
        SessionProp.clear();
    }

    public static Boolean isRouteExist(){
        return instanceRoute != null;
    }

    public void set_RouteRequest(ROUTEREQUEST request){
        Util.appendLog(TAG + "set_ROUTEREQUEST:" + request, 'd');
        switch (request){
            case OPEN_NEW_ROUTE:
                set_routeStatus(RSTATUS.ACTIVE);
                /// start SvcLocationClock which is a clock at the same time
                //ctx.startService(new Intent(ctx, SvcLocationClock.class));
                currentFlights.add(new FlightInstance(this));
                break;
            case SWITCH_TO_PENDING:
                if(!SvcLocationClock.isInstanceCreated())ctxApp.startService(new Intent(ctxApp, SvcLocationClock.class));
                set_ActiveFlightID(currentFlights.get(currentFlights.size()-1));
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSACTIVE);
                break;
            case ON_FLIGHTTIME_CHANGED:
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GREEN);
                break;
            case CLOSE_SPEED_BELOW_MIN:
                activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
                if (MainActivity.AppProp.pIsMultileg&&(_legCount<LEG_COUNT_HARD_LIMIT)) {
                    /// ignore request to close route
                    currentFlights.add(new FlightInstance(this));
                    setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                }
                else {
                    SvcLocationClock.stopLocationUpdates();
                    routeNumber =null;
                    _legCount=0;
                    set_routeStatus(RSTATUS.PASSIVE);
                    SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
                }
                break;
            case CLOSE_SPEED_BELOW_MIN_SERVER_REQUEST:
                ///rethrow
                //activeFlight.set_ChangeFlightState_if_isSpeedAboveMinChanged(false);
                activeFlight._isSpeedAboveMin = false;
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_POINTS_LIMIT_REACHED:
                ///rethrow
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_FLIGHT_CANCELED:
                ///rethrow
                set_RouteRequest(ROUTEREQUEST.CLOSE_SPEED_BELOW_MIN);
                break;
            case CLOSE_FLIGHT_DELETE_ALL_POINTS:
                closeFlightDeleteAllPoints();
                break;
            case CLOSE_BUTTON_STOP_PRESSED:
                closeFlightDeleteAllPoints();
                break;
            case CLOSEAPP_BUTTON_BACK_PRESSED:
                if(dbLocationRecCount>0) {
                    new ShowAlertClass(MainActivity.instanceThis).showUnsentPointsAlert(dbLocationRecCount);
                    Util.appendLog(TAG + " PointsUnsent: " + dbLocationRecCount, 'd');
                }
                else {
                    closeFlightDeleteAllPoints();
                    MainActivity.instanceThis.finishActivity();
                }
                break;
            case CLOSE_RECEIVEFLIGHT_FAILED:
                currentFlights.remove(currentFlights.size()-1);  /// remove the latest flight added
                ///rethrow
                //set_RouteRequest(ROUTEREQUEST.CLOSE_BUTTON_STOP_PRESSED);
                if(!(SvcLocationClock.instance==null))SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
                set_routeStatus(RSTATUS.PASSIVE);

                break;
            case ON_COMMUNICATION_SUCCESS:
                break;
            case START_COMMUNICATION:
                if (Util.isNetworkAvailable()) {
                    if(Route.dbLocationRecCount>0) {
                        startLocationCommService();
                    }
                    checkIfAnyFlightNeedClose();
                    }
                else {
                    Util.appendLog(TAG + "Connectivity unavailable, cant send location", 'd');
                    Toast.makeText(ctxApp, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
            case ON_CLOSE_FLIGHT:
                /// to avoid ConcurrentModificationException making copy of the currentFlights
                Util.appendLog(TAG + "ON_CLOSE_FLIGHT: currentFlights: size before: " + currentFlights.size(), 'd');
                for (FlightInstance f : new ArrayList<>(currentFlights) ) {
                    if (f.flightState == FLIGHTREQUEST.CLOSED) {
                        currentFlights.remove(f);
                        if(activeFlight==f) activeFlight=null;
                    }
                }

                break;
//            case ON_CLOSE_FLIGHT_SUCCESS:
//                /// to avoid ConcurrentModificationException making copy of the currentFlights
//                Util.appendLog(TAG + "ON_CLOSE_FLIGHT_SUCCESS: currentFlights: size before: " + currentFlights.size(), 'd');
//                for (FlightInstance f : new ArrayList<>(currentFlights) ) {
//                    if (f.flightState == FLIGHTREQUEST.CLOSED) {
//                        Util.appendLog(TAG + "ON_CLOSE_FLIGHT_SUCCESS: flight to remove: " + f.flightNumber, 'd');
//                        currentFlights.remove(f);
//                        if(activeFlight==f) activeFlight=null;
//                    }
//                }
//                Util.appendLog(TAG + "ON_CLOSE_FLIGHT_SUCCESS: currentFlights: size after: " + currentFlights.size(), 'd');
//                break;
//            case ON_CLOSE_FLIGHT_FAILURE:
//                /// to avoid ConcurrentModificationException making copy of the currentFlights
//                Util.appendLog(TAG + "ON_CLOSE_FLIGHT_FAILURE: currentFlights: size before: " + currentFlights.size(), 'd');
//                for (FlightInstance f : new ArrayList<>(currentFlights) ) {
//                    if (f.flightState == FLIGHTREQUEST.CLOSED_FAILURE) {
//                        currentFlights.remove(f);
//                        if(activeFlight==f) activeFlight=null;
//                    }
//                }
//                Util.appendLog(TAG + "ON_CLOSE_FLIGHT_FAILURE: currentFlights: size after: " + currentFlights.size(), 'd');
//                break;
        }
    }
    public static void setTrackingButtonState(BUTTONREQUEST request) {
        //Util.appendLog(TAG+"trackingButtonState request:" +request,'d');
        switch (request) {
            case BUTTON_STATE_RED:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(setTextRed());
                break;
            case BUTTON_STATE_YELLOW:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                MainActivity.trackingButton.setText("Flight " + (Route.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff));
                //editor.putInt("trackingButtonState", BUTTON_STATE_YELLOW);
                break;
            case BUTTON_STATE_GREEN:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_green);
                MainActivity.trackingButton.setText(setTextGreen());
                //editor.putInt("trackingButtonState", BUTTON_STATE_GREEN);
                break;
            case BUTTON_STATE_GETFLIGHTID:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(ctxApp.getString(R.string.tracking_gettingflight));
                break;
            case BUTTON_STATE_STOPPING:
                //appendLog(LOGTAG+"BUTTON_STATE_STOPPING");
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                //MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_stopping));
                break;
            default:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                // MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_is_off));
        }
        trackingButtonState = request;
    }
    void set_ActiveFlightID(FlightInstance f) {
        //_iCurrentFlight =currentFlights.indexOf(f);
        if (!(f.flightNumber == null)) {
            activeFlight =f;
            routeNumber =(routeNumber ==null?f.flightNumber: routeNumber);
            Util.appendLog(TAG + "set_ActiveFlightID: routeNumber "+routeNumber,'d');
        }
    }

//    static boolean isMultileg() {
//        return sharedPreferences.getBoolean("route_isMultileg", true);
//    }
//    static void set_isMultileg(boolean isMultileg) {
//        MainActivity.chBoxIsMultiLeg.setChecked(isMultileg);
//        //Route.isMultileg=isMultileg;
//        MainActivity.AppProp.pIsMultileg=isMultileg;
//        editor.putBoolean("route_isMultileg", isMultileg).commit();
//        //if (isMultileg) setLegCount(LEG_COUNT_HARD_LIMIT);
//    }

    public static void set_isRoad(boolean b){
        _isRoad =b;
        ///hard code true
        //_isRoad =true;
    }

    private void startLocationCommService(){

        Route.sqlHelper.setCursorDataLocation();
        int count = Route.sqlHelper.getCursorCountLocation();
        //Util.appendLog(TAG+ "getCursorCountLocation :" + count,'d');

        Util.appendLog(TAG+ "SvcComm.commBatchSize :" + SvcComm.commBatchSize,'d');
        if (count>=1) {
            for (int i = 0; i <count; i++) {
                if (i >= SvcComm.commBatchSize) break;
                Intent intentComm = new Intent(ctxApp, SvcComm.class);
                //Intent intentComm = new Intent(context, SvcIntentComm.class);
                Bundle bundle = new Bundle();
                bundle.putLong("itemId", sqlHelper.cl.getLong(sqlHelper.cl.getColumnIndexOrThrow(DBSchema._ID)));
                bundle.putInt("rc", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL1)));
                bundle.putString("ft", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL2)));
                bundle.putBoolean("sl", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL3)) == 1);
                bundle.putString("sd", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL4)));
                bundle.putString("la", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL6)));
                bundle.putString("lo", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL7)));
                bundle.putString("ac", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL8)));
                bundle.putString("al", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL9)));
                bundle.putInt("wp", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL10)));
                bundle.putString("sg", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL11)));
                bundle.putString("dt", sqlHelper.cl.getString(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL12)));
                bundle.putBoolean("irch", sqlHelper.cl.getInt(sqlHelper.cl.getColumnIndexOrThrow(DBSchema.COLUMN_NAME_COL13))==1);

                intentComm.putExtras(bundle);
                //Log.d(TAG, "FlightRouterThread:" + Thread.currentThread().getId());
                ctxApp.startService(intentComm);
                Route.sqlHelper.cl.moveToNext();
            }
            Route.sqlHelper.cl.close();
            //if(!alarmDisable)
        }
    }

    private void checkIfAnyFlightNeedClose(){
        try {
            for (FlightInstance f : currentFlights ) {
                if (f.flightState==FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE) {
                    f.set_flightRequest(FLIGHTREQUEST.CLOSE_FLIGHT);
                }
                //String flights ="-";
                //flights=flights+f.flightNumber+"-"+f.flightState+"-";
            }
        }
        catch (Exception e){
            Util.appendLog(TAG + "checkIfAnyFlightNeedClose: "+e.getMessage()+"\n"+e.getCause(),'e');
        }
    }
    private void closeFlightDeleteAllPoints(){
        set_routeStatus(RSTATUS.PASSIVE);
        if(!(activeFlight==null))activeFlight.set_flightRequest(FLIGHTREQUEST.CHANGESTATE_STATUSPASSIVE);
        int i = sqlHelper.allLocationsDelete();
        Util.appendLog(TAG + "Deleted from database: " + i + " all locations", 'd');
        if(!(SvcLocationClock.instance==null))SvcLocationClock.instance.set_mode(MODE.CLOCK_ONLY);
    }

    private void set_routeStatus(RSTATUS status){
        _routeStatus =status;
        switch(status) {
            case ACTIVE:
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                break;
            case PASSIVE:
                routeNumber =null;
                //set_isRoad(false);
                setTrackingButtonState(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
        }
    }
    static String setTextRed() {
        String fid = SessionProp.pTextRed;
        String fTime = "";

        if (Route.activeFlight==null) {
            Util.appendLog(TAG+ " setTextRed: flightId IS NULL",'d');
        }
        else {
            String flightId = Route.activeFlight.flightNumber;
            fid = "Flight " + flightId + '\n' + "Stopped"; // + '\n';
            fTime = Route.activeFlight.flightTimeString.equals(FLIGHT_TIME_ZERO)? ctxApp.getString(R.string.time)+SPACE+Util.getTimeLocal() : ctxApp.getString(R.string.tracking_flight_time)+SPACE+Route.activeFlight.flightTimeString;
        }
        SessionProp.pTextRed = fid + fTime;
        return SessionProp.pTextRed;
    }
    static String setTextGreen() {
        SessionProp.pTextGreen = "Flight: " + (Route.activeFlight.flightNumber)+'\n'+
                "Point: " +Route.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time)+SPACE+ Route.activeFlight.flightTimeString +'\n'
                +"Alt: "+ Route.activeFlight.lastAltitudeFt+" ft";
        return SessionProp.pTextGreen;
    }
    public static class  SessionProp{
        static String  pTextRed;
        static String  pTextGreen;

        public static void save(){
            editor.putString("pTextRed", pTextRed);
            editor.commit();
        }
        public static void get(){
            pTextRed = sharedPreferences.getString("pTextRed", ctxApp.getString(R.string.start_flight));
        }
        public static void clear(){
            editor.remove("pTextRed").commit();
        }
    }
}
