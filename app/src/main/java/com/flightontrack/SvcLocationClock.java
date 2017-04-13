package com.flightontrack;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Date;

import static com.flightontrack.Const.*;


public class SvcLocationClock extends Service implements LocationListener {
    private static final String TAG = "SvcLocationClock:";
    private static Context ctx;
    static LocationManager locationManager;
    static PhoneListener phStateListener;
    static SvcLocationClock instance = null;
    private static boolean isBound = false;
    private static int counter = 0;
    static MODE _mode;
    static int _intervalClockSecCurrent = MIN_TIME_BW_GPS_UPDATES_SEC;
    static int intervalClockSecPrev = _intervalClockSecCurrent;
    static long  alarmNextTimeUTCmsec;

    public SvcLocationClock() {
    }

    public static boolean isInstanceCreated() {
        return instance != null;
    }

    //    public static SvcLocationClock getInstance() {
//        return instance;
//    }
    public static boolean isBound() {
        return isBound;
    }

    public static void stopLocationUpdates() {
        Util.appendLog(TAG + "stopLocationUpdates : instance = " + instance, 'd');
        try {
            locationManager.removeUpdates(instance);
        }
        catch(SecurityException e ){
            Util.appendLog(TAG + e, 'e');
        }
    }

    public void requestLocationUpdate(int timeSec, long distance) {

        Util.appendLog(TAG + "requestLocationUpdate: interval: " + timeSec + " dist: " + distance, 'd');
        SvcLocationClock.stopLocationUpdates();
        set_intervalClockSecCurrent(timeSec);
        setClockNextTimeLocalMsec(0);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _intervalClockSecCurrent * 1000, distance, this);
        }
        catch(SecurityException e ){
            Util.appendLog(TAG + e, 'e');
        }
    }
    void set_mode(MODE m){
        _mode = m;
        switch (_mode){
            case CLOCK_ONLY:
                requestLocationUpdate(MIN_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_ZERO);
                break;
            case CLOCK_LOCATION:
                requestLocationUpdate(MIN_TIME_BW_GPS_UPDATES_SEC, DISTANCE_CHANGE_FOR_UPDATES_MIN);
                break;
        }
    }
    public static void set_intervalClockSecCurrent(int timeSec){
        intervalClockSecPrev = _intervalClockSecCurrent;
        _intervalClockSecCurrent = timeSec;
        //Util.appendLog(TAG + "set_intervalClockSecCurrent: "+_intervalClockSecCurrent, 'd');
    }
    @Override
    public void onLocationChanged(final Location location) {
        counter++;
        if(_mode==MODE.CLOCK_ONLY&&Route.dbLocationRecCount<1&&Route.currentFlights.size()==0){
            stopServiceSelf();
            return;
        }
        else {
            long currTime = Util.getTimeGMT();
            Util.appendLog(TAG + "___TIMER-onLocationChanged :  Counter:" + counter, 'd');

            if (currTime + TIME_RESERVE >= alarmNextTimeUTCmsec) {
                //Util.appendLog(TAG + "isClockTimeReached: ", 'd');
                /// it is a protection
                setClockNextTimeLocalMsec(_intervalClockSecCurrent);
                if (_mode == MODE.CLOCK_LOCATION) {
                    Route.activeFlight.onClock(location);
                }

                Util.appendLog(TAG + "onLocationChanged: Route.dbLocationRecCount:" + Route.dbLocationRecCount + " Route._openFlightsCount:" + Route.currentFlights.size() + " _mode:" + _mode, 'd');
                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.START_COMMUNICATION);
            }
        }
    }

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {}
    @Override
    public IBinder onBind(Intent intent) {
        isBound = false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return true; // ensures onRebind is called
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        setSignalStrengthListener(true);
        _mode = MODE.CLOCK_LOCATION;
        requestLocationUpdate((int)MIN_TIME_BW_GPS_UPDATES/1000, DISTANCE_CHANGE_FOR_UPDATES_MIN);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!MainActivity.isMainActivityExist()){
            stopSelf();
            return;
        }
        Util.appendLog(TAG + "onCreate",'d');
        instance=this;
        _mode = MODE.CLOCK_LOCATION;
        ctx = getApplicationContext();
        alarmNextTimeUTCmsec = Util.getTimeGMT();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //ReceiverHealthCheckAlarm.isRestart = true;

    }

    public static void setSignalStrengthListener(Boolean start) {
        if (start) {
            phStateListener = new PhoneListener(ctx);
            ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).listen(phStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d(Const.GLOBALTAG,TAG+ "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(ctx, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Util.appendLog(TAG + "onDestroy", 'd');
        setToNull();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        setSignalStrengthListener(false);
        Util.appendLog(TAG + "onTaskRemoved: ",'d');
        if(!(instance==null)){
            stopLocationUpdates();
            setToNull();
        }
        stopSelf();
        //Util.getLogcat();
    }
    public void stopServiceSelf() {
        Util.appendLog(TAG + "stopServiceSelf",'d');
        setSignalStrengthListener(false);
        if(!(instance==null)){
            stopLocationUpdates();
            setToNull();
        }
        stopSelf();
    }
    private void setToNull(){
        instance=null;
        ctx=null;
        phStateListener=null;
    }

    static void setClockNextTimeLocalMsec(int intervalSec) {
        alarmNextTimeUTCmsec = Util.getTimeGMT()+ intervalSec*1000;
    }
}
