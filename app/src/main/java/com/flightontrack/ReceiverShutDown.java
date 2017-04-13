package com.flightontrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import static com.flightontrack.Const.*;

import java.util.Map;

public class ReceiverShutDown extends BroadcastReceiver {
    private static final String TAG = "ReceiverShutDown:";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " FONT:ReceiverShutDown Started ");
        Intent i = new Intent(context,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        }
    }
