package com.flightontrack;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class MyPhone {

    static Context ctx;
    static int versionCode;
    static String deviceMmnufacturer = "unknown";
    static String deviceBrand = "unknown";
    static String deviceProduct = "unknown";
    static String deviceModel = "unknown";
    static String codeName = "unknown";
    static String codeRelease = "unknown";
    static int codeSDK;

    static String _myDeviceId = null;
    static String _myPhoneId = null;
    static String _phoneNumber = null;
    static String _userId = null;
    static String _userName = null;

    MyPhone(Context ctxApp) {
        ctx = ctxApp;
        getBuldProp();
        getMyPhoneID();
    }

    static void getBuldProp(){
        deviceMmnufacturer = Build.MANUFACTURER;
        deviceBrand        = Build.BRAND;
        deviceProduct      = Build.PRODUCT;
        deviceModel        = Build.MODEL;

        codeName            = Build.VERSION.CODENAME;
        codeRelease         = Build.VERSION.RELEASE;
        codeSDK             = Build.VERSION.SDK_INT;
    }

    int getVersionCode(Context ctxApp) {
        ctx = ctxApp;
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = -1;
        }
        return versionCode;
    }

    static void getMyPhoneID() {
        _phoneNumber = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        _myDeviceId = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        String strId = (_phoneNumber == null||_phoneNumber.isEmpty()) ? _myDeviceId : _phoneNumber;
        _myPhoneId = strId.substring(strId.length() - 10); /// 10 digits number
    }

    static String getMyUserID() {
        getMyPhoneID();
        _userId = _myPhoneId + "." + _myDeviceId.substring(_myDeviceId.length() - 4); //combination of phone num. 4 digits of deviceid
        return _userId;
    }

    String getMyUserName() {
        _userName = _myPhoneId.substring(0,3)+deviceBrand.substring(0,4)+_myPhoneId.substring(8);
        return _userName;
    }

    static String getMyAndroidID() {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    String   getMyAndroidVersion() {
        return  codeName +' ' +codeRelease+' ' +codeSDK;
    }


}
