package com.flightontrack;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import static com.flightontrack.MainActivity.ctxApp;

public class MyPhone {

    //static Context  ctx;
    static int      versionCode;
    static String   deviceMmnufacturer = "unknown";
    static String   deviceBrand = "unknown";
    static String   deviceProduct = "unknown";
    static String   deviceModel = "unknown";
    static String   codeName = "unknown";
    static String   codeRelease = "unknown";
    static int      codeSDK;

    static String _myDeviceId = null;
    static String _myPhoneId = null;
    static String _phoneNumber = null;

    MyPhone() {
        //ctx = MainActivity.ctxApp;
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

    int getVersionCode() {
        //ctx = ctxApp;
        try {
            versionCode = ctxApp.getPackageManager().getPackageInfo(ctxApp.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = -1;
        }
        return versionCode;
    }

    static void getMyPhoneID() {
        _phoneNumber = ((TelephonyManager) ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        _myDeviceId = ((TelephonyManager) ctxApp.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        String strId = (_phoneNumber == null||_phoneNumber.isEmpty()) ? _myDeviceId : _phoneNumber;
        _myPhoneId = strId.substring(strId.length() - 10); /// 10 digits number
    }

    static String getMyAndroidID() {
        return Settings.Secure.getString(ctxApp.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    String   getMyAndroidVersion() {
        return  codeName +' ' +codeRelease+' ' +codeSDK;
    }
}
