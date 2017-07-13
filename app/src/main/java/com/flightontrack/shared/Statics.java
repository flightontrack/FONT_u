package com.flightontrack.shared;

import android.content.Context;
import android.content.SharedPreferences;

import static com.flightontrack.shared.Const.PACKAGE_NAME;

/**
 * Created by hotvk on 7/6/2017.
 */

public class Statics {

    public static Context ctxApp;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public Statics(Context ctx) {
        ctxApp = ctx;
        sharedPreferences =ctxApp.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
    }
}
