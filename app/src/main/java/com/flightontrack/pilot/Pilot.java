package com.flightontrack.pilot;

import com.flightontrack.shared.Props;

/**
 * Created by hotvk on 5/15/2017.
 */

public class Pilot extends MyPhone {

    static String _userId = null;
    static String _userName = null;

    Pilot(){
    }

    public static String getUserID() {
        getMyPhoneID();
        _userId = _myPhoneId + "." + _myDeviceId.substring(_myDeviceId.length() - 4); //combination of phone num. 4 digits of deviceid
        return _userId;
    }

    String getUserName() {
        _userName = _myPhoneId.substring(0,3)+deviceBrand.substring(0,4)+_myPhoneId.substring(8);
        return _userName;
    }

    public static void setPilotUserName(String un) {
        Props.editor.putString("pilot_UserName", un.trim().replace(" ","")).commit();
        //editor.putString("userName", un.trim()).commit();
        //MainActivity.txtUserName.setText(un);
        //AircraftActivity.txtUserName.setText(un);
    }

    public static String getPilotUserName() {
        getBuldProp();
        getMyPhoneID();
        _userName = _myPhoneId.substring(0,3)+deviceBrand.substring(0,4).toUpperCase()+_myPhoneId.substring(8);
        //String r = sharedPreferences.getString("pilot_UserName", _userName);
        return Props.sharedPreferences.getString("pilot_UserName", _userName);
    }
}
