package com.flightontrack.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.telephony.SmsManager;

import com.flightontrack.R;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.shared.Util;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;

public class ReceiverBatteryLevel extends BroadcastReceiver {
    private static final String TAG = "ReceiverBatteryLevel";

    @Override
    public void onReceive(Context context, Intent intent) {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onReceive intent: "+intent, 'd'));
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) (level / (float)scale);

        setBattery(String.valueOf(level));

        if (intent.getAction().contains("BATTERY_LOW")) {

            String message =    "Help !!!"+"\n"+
                    SMS_LOWBATTERY_TEXT+"\n"+
                    "Pilot : "+ Pilot.getPilotUserName()+"\n"+
                    "Aircraft : "+Util.getAcftNum(4)+"\n";
            new FontLogAsync().execute(new EntityLogMessage(TAG, "BatteryPct low: Level :"+level+" out of "+scale, 'd'));

            SmsManager smsManager = SmsManager.getDefault();
            String[] spinnerTextTo = ctxApp.getResources().getStringArray(R.array.textto_array);
            smsManager.sendTextMessage(spinnerTextTo[SessionProp.pSpinnerTextToPos], null, message, null, null);
            //smsManager.sendTextMessage(SMS_RECEIPIENT_PHONE, null, message, null, null);
            smsManager.sendTextMessage(SMS_RECEIPIENT_PHONE_CC, null, message, null, null);
        }
        if (intent.getAction().contains("BATTERY_OKAY")) {
            setBattery(String.valueOf(level));
            new FontLogAsync().execute(new EntityLogMessage(TAG, "Battery Restored Level: "+level, 'd'));
        }
    }

    static void setBattery(String text) {
        editor.putString("batteryLevel", text).commit();
    }

    public static String getBattery() {
        return sharedPreferences.getString("batteryLevel","0");
    }
}
