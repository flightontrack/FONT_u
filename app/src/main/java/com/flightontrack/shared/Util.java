package com.flightontrack.shared;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.activity.SimpleSettingsActivity;
import com.flightontrack.communication.ResponseP;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.Entities.EntityLogMessage;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class Util {
    private static final String TAG = "Util";

    public Util() {
    }

     public static String getTrackingURL() {
        String[] spinnerUrls = ctxApp.getResources().getStringArray(R.array.url_array);
        return "http://"+spinnerUrls[SessionProp.pSpinnerUrlsPos].trim();
    }

    public static int getWayPointLimit() {
        return sharedPreferences.getInt("wayPointLimit", WAY_POINT_HARD_LIMIT);
    }

    public static void setWayPointLimit(int wp_limit) {
        editor.putInt("wayPointLimit", WAY_POINT_HARD_LIMIT > wp_limit ? WAY_POINT_HARD_LIMIT : wp_limit).commit();
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

    public static String getPsw() {
        return sharedPreferences.getString("cloudpsw",null);
    }
    public static void setPsw(String psw) {
        editor.putString("cloudpsw", psw).commit();
        SimpleSettingsActivity.txtPsw.setText(psw);
    }

    public static void setSignalStregth(String name, int value) {
        try {
            editor.putInt(name, value).commit();
        }
        catch (Exception e) {Log.e(TAG,"!!!!!!!!!!!!!!"+e.getMessage());}
    }

    public static int getSignalStregth() {
        return sharedPreferences.getInt("gsmsignalstrength", -1);
    }

    public static Boolean getIsOnBoot() {
        return sharedPreferences.getBoolean("a_isOnBoot", false);
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctxApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Boolean isNetworkAvailable = activeNetworkInfo != null&&activeNetworkInfo.isConnected();
        if (!isNetworkAvailable) Toast.makeText(mainactivityInstance, R.string.toast_noconnectivity, Toast.LENGTH_SHORT).show();
        return isNetworkAvailable ;
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

        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloudPsw Started", 'd'));
        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_PSW);
        requestParams.put("userid", Pilot.getUserID());
        requestParams.put("phonenumber", MyPhone._myPhoneId);
        requestParams.put("deviceid", MyPhone._myDeviceId);
        new AsyncHttpClient().post(getTrackingURL() + ctxApp.getString(R.string.aspx_requestpage), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloudPsw OnSuccess", 'd'));
                        ResponseP response = new ResponseP(new String(responseBody));
                        if (response.responseType.equals(RESPONSE_TYPE_DATA_WITHLOAD) && response.responseTypeLoad.equals(RESPONSE_TYPE_DATA_PSW)) {
                            //SimpleSettingsActivity.progressBar.isShowing();
                            progressBar.dismiss();
                            String psw = response.getValue(RESPONSE_TYPE_DATA_PSW);
                            new FontLogAsync().execute(new EntityLogMessage(TAG, "ap="+psw, 'd'));
                            setPsw(psw);
                        }
                        if (response.responseType.equals(RESPONSE_TYPE_NOTIF_WITHLOAD)) {
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        new FontLogAsync().execute(new EntityLogMessage(TAG, "getCloudPsw onFailure:", 'd'));
                        progressBar.dismiss();
                    }
                }

        );
    }
}
