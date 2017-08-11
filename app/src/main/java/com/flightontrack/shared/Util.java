package com.flightontrack.shared;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;

import com.flightontrack.R;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.activity.SimpleSettingsActivity;
import com.flightontrack.communication.ResponseP;
import com.flightontrack.log.FontLog;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import cz.msebera.android.httpclient.Header;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class Util {
    public Util() {
//        ctx = c;
//        sharedPreferences = ctx.getSharedPreferences("com.flightontrack", Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
    }

//        public static void init(Context c,MainActivity a) {
//        Util.ctx = c;
//        sharedPreferences = ctx.getSharedPreferences("com.flightontrack", Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//    }

    private static final String TAG = "Util:";
//    private static Context ctx;
//    static SharedPreferences sharedPreferences;
//    static SharedPreferences.Editor editor;
//    static int versionCode;
//    static String deviceMmnufacturer = "unknown";
//    static String deviceBrand = "unknown";
//    static String deviceProduct = "unknown";
//    static String deviceModel = "unknown";

//    static boolean isEmptyAcftOk() {
//        return sharedPreferences.getBoolean("a_isEmptyAcftOk", false);
//    }
//
//    static void setIsEmptyAcftOk(boolean isEmptyAcftOk) {
//        editor.putBoolean("a_isEmptyAcftOk", isEmptyAcftOk).commit();
//    }

     public static String getTrackingURL() {
        String[] spinnerUrls = ctxApp.getResources().getStringArray(R.array.url_array);
        FontLog.appendLog(TAG + "getTrackingUR : " + spinnerUrls[SessionProp.pSpinnerUrlsPos].trim(),'d');
        //return sharedPreferences.getString("trackingURL", spinnerUrls[getSpinnerUrlsPos()]).trim();
        return spinnerUrls[SessionProp.pSpinnerUrlsPos].trim();
    }

//    static int getSpinnerUrlsPos() {
//        return sharedPreferences.getInt("spinnerUrlsPos", DEFAULT_URL_SPINNER_POS);
//    }
//
//    static void setSpinnerUrlsPos(int pos) {
//        //SimpleSettingsActivity.spinnerUrls.setSelection(pos);
//        editor.putInt("spinnerUrlsPos", pos).commit();
//    }

    public static int getWayPointLimit() {
        return sharedPreferences.getInt("wayPointLimit", WAY_POINT_HARD_LIMIT);
    }

    public static void setWayPointLimit(int wp_limit) {
        editor.putInt("wayPointLimit", WAY_POINT_HARD_LIMIT > wp_limit ? WAY_POINT_HARD_LIMIT : wp_limit).commit();
    }

    public static void setAcftNum(String an) {
        MainActivity.txtAcftNum.setText(an);
    }

    public static String getAcftNum(int a) {
        String acft;
        switch (a) {
            case 1:
                acft = sharedPreferences.getString("AcftMake", ctxApp.getString(R.string.default_acft_Make));
                break;
            case 2:
                acft = sharedPreferences.getString("AcftModel", ctxApp.getString(R.string.default_acft_Model));
                break;
            case 3:
                acft = sharedPreferences.getString("AcftSeries", ctxApp.getString(R.string.default_acft_Series));
                break;
            case 4:
                acft = sharedPreferences.getString("AcftRegNum", ctxApp.getString(R.string.default_acft_N));
                break;
            case 5:
                acft = sharedPreferences.getString("AcftTagId", "");
                break;
            case 6:
                acft = sharedPreferences.getString("AcftName", "");
                break;
            default:
                acft = sharedPreferences.getString("AcftMake", ctxApp.getString(R.string.default_acft_Make))
                        + " " + sharedPreferences.getString("AcftModel", ctxApp.getString(R.string.default_acft_Model))
                        + " " + sharedPreferences.getString("AcftSeries", ctxApp.getString(R.string.default_acft_Series))
                        + " " + sharedPreferences.getString("AcftRegNum", ctxApp.getString(R.string.default_acft_N));
        }
        return acft;
    }

//    static String getMyAndroidID() {
//        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
//    }
//
//    static String   getMyAndroidVersion() {
//        return  Build.VERSION.CODENAME +' ' +Build.VERSION.RELEASE+' ' +Build.VERSION.SDK_INT;
//    }
//    static int getVersionCode() {
//        try {
//            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            versionCode = -1;
//        }
//        return versionCode;
//    }
//
//    static void getMyDevice() {
//        deviceMmnufacturer = Build.MANUFACTURER;
//        deviceBrand        = Build.BRAND;
//        deviceProduct      = Build.PRODUCT;
//        deviceModel        = Build.MODEL;
//    }
//
//    static String getMyPhoneID() {
//        String strId = (MainActivity._phoneNumber == null||MainActivity._phoneNumber.isEmpty()) ? MainActivity._myDeviceId : MainActivity._phoneNumber;
//        return strId.substring(strId.length() - 10);
//    }

    public static String getPsw() {
        return sharedPreferences.getString("cloudpsw",null);
//        if (psw==null) {
//            getCloudPsw();
//        }
//        else SimpleSettingsActivity.txtPsw.setText(psw);
    }
    public static void setPsw(String psw) {
        editor.putString("cloudpsw", psw).commit();
        SimpleSettingsActivity.txtPsw.setText(psw);
    }
//    public static String getTrackingSpeed() {
//        return sharedPreferences.getString("a_speed_min", ctxApp.getString(R.string.default_tracking_speed));
//    }

//    public static double getTrackingSpeedIntMeterSec() {
//        return Double.parseDouble(getTrackingSpeed()) * 0.44704;
//    }

//    public static void setTrackingSpeed(String speed) {
//        editor.putString("a_speed_min", speed).commit();
//    }

//    public static void setSpinnerSpeedPos(int pos) {
//        editor.putInt("a_spinnerSpeedPos", pos).commit();
//        MainActivity.spinnerMinSpeed.setSelection(pos);
//    }

//    public static int getSpinnerSpeedPos() {
//        return sharedPreferences.getInt("a_spinnerSpeedPos", DEFAULT_SPEED_SPINNER_POS);
//    }

//    static void uiResume() {
//            setAcftNum(getAcftNum(4));
//            MainActivity.set_pIntervalLocationUpdateSecPos(MainActivity.AppProp.pIntervalSelectedItem);
//            setSpinnerSpeedPos(getSpinnerSpeedPos());
//
//        if (!(MainActivity._phoneNumber==null)&&!(MainActivity._myDeviceId==null)) {
//            setUserName(getUserName());
//        }
//        MainActivity.AppProp.get();
//        Route.SessionProp.get();
//        Route.setTrackingButtonState(Route.trackingButtonState);
//    }

//    static void clearSettingPreferences() {
//        //Log.d.d(TAG, "clearPref()");
//
//        //editor.remove("trackingURL").commit();
//        editor.remove("speed_thresh").commit();
//        editor.remove("spinnerSpeedPos").commit();
//        editor.remove("pIsMultileg").commit();
//        editor.remove("pIntervalLocationUpdateSec").commit();
//        editor.remove("pIntervalSelectedItem").commit();
//        editor.remove("pIsMultileg").commit();
//        editor.remove("pIsEmptyAcftOk").commit();
//        editor.remove("pSpinnerUrlsPos").commit();
//        editor.remove("pUserName").commit();
//        editor.remove("cloudpsw").commit();
//        editor.remove("pIsDebug").commit();
//        MainActivity.AppProp.get();
//    }


    public static void setSignalStregth(String name, int value) {
        try {
            editor.putInt(name, value).commit();
        }
        catch (Exception e) {Log.e(TAG,"!!!!!!!!!!!!!!"+e.getMessage());}
    }

    public static int getSignalStregth() {
        return sharedPreferences.getInt("gsmsignalstrength", -1);
    }

    public static String getTimeLocal() {
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }

    public static long getTimeGMT() {
        long currTime = new Date().getTime();
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return currTime;
    }

    public static String getDateTimeNow() {
        long currTime = new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(currTime);
    }

    public static String getCurrAppContext() {
        return sharedPreferences.getString("a_currAppContext","0");
    }

    public static void setCurrAppContext(String appContext) {
        sharedPreferences.edit().putString("a_currAppContext",appContext).commit();
    }

//    static Boolean getIsDebug() {
//        return sharedPreferences.getBoolean("a_isDebug", false);
//    }

//    static void setIsDebug(Boolean isDebug) {
//        editor.putBoolean("a_isDebug", isDebug).commit();
//    }

    public static Boolean getIsOnBoot() {
        return sharedPreferences.getBoolean("a_isOnBoot", false);
    }

//    public static void setIsOnBoot(Boolean isDebug) {
//        editor.putBoolean("a_isOnBoot", isDebug).commit();
//    }
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null&&activeNetworkInfo.isConnected() ;
    }

    public static void setCloudPsw(View view){
        final ProgressDialog progressBar;
        progressBar = new ProgressDialog(view.getContext());
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Getting password");
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.setMax(100);
        progressBar.setProgress(100);
        progressBar.show();

        FontLog.appendLog(TAG + "getCloudPsw Started", 'd');
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_PSW);
        requestParams.put("userid", Pilot.getUserID());
        requestParams.put("phonenumber", MyPhone._myPhoneId);
        requestParams.put("deviceid", MyPhone._myDeviceId);
        new AsyncHttpClient().post(getTrackingURL() + ctxApp.getString(R.string.aspx_requestpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        FontLog.appendLog(TAG + "getCloudPsw OnSuccess", 'd');
                        ResponseP response = new ResponseP(new String(responseBody));
                        if (response.responseType.equals(RESPONSE_TYPE_DATA_WITHLOAD) && response.responseTypeLoad.equals(RESPONSE_TYPE_DATA_PSW)) {
                            //SimpleSettingsActivity.progressBar.isShowing();
                            progressBar.dismiss();
                            String psw = response.getValue(RESPONSE_TYPE_DATA_PSW);
                            FontLog.appendLog(TAG + "ap="+psw, 'd');
                            setPsw(psw);
                        }
                        if (response.responseType.equals(RESPONSE_TYPE_NOTIF_WITHLOAD)) {
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        FontLog.appendLog(TAG + "getCloudPsw onFailure:", 'd');
                        progressBar.dismiss();
                    }
                }

        );
    }
}
