package com.flightontrack;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class PhoneListener extends PhoneStateListener
{

    public PhoneListener(Context ctx){
        PhoneListener.ctx=ctx;
    }
    static Context ctx;
    private static final String TAG = " PhoneListener";
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);
        //Util.appendLog(TAG + " onSignalStrengthsChanged: " + signalStrength,'d');
        enableSignalStrengthListen(false);

        if (signalStrength.isGsm()) {
            Util.setSignalStregth("gsmsignalstrength", signalStrength.getGsmSignalStrength());
//            Log.i(TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
//                    + signalStrength.getGsmBitErrorRate());
//            Log.i(TAG, "onSignalStrengthsChanged: getGsmSignalStrength "
//                    + signalStrength.getGsmSignalStrength());
        } else if (signalStrength.getCdmaDbm() > 0) {
            Util.setSignalStregth("cdmasignalstrength", signalStrength.getCdmaDbm());
//            Log.i(TAG, "onSignalStrengthsChanged: getCdmaDbm "
//                    + signalStrength.getCdmaDbm());
//            Log.i(TAG, "onSignalStrengthsChanged: getCdmaEcio "
//                    + signalStrength.getCdmaEcio());
        } else {
            Util.setSignalStregth("cdmasignalstrength", signalStrength.getEvdoDbm());
//            Log.i(TAG, "onSignalStrengthsChanged: getEvdoDbm "
//                    + signalStrength.getEvdoDbm());
//            Log.i(TAG, "onSignalStrengthsChanged: getEvdoEcio "
//                    + signalStrength.getEvdoEcio());
//            Log.i(TAG, "onSignalStrengthsChanged: getEvdoSnr "
//                    + signalStrength.getEvdoSnr());
        }
    }

    public static void enableSignalStrengthListen(boolean start){
        if (start) {
            ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).listen(SvcLocationClock.phStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).listen(SvcLocationClock.phStateListener, PhoneStateListener.LISTEN_NONE);
        }

    }

}

