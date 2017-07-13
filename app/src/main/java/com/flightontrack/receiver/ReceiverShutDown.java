package com.flightontrack.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.util.Log;

public class ReceiverShutDown extends BroadcastReceiver {
    private static final String TAG = "ReceiverShutDown:";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " FONT:ReceiverShutDown Started ");
//        Intent i = new Intent(context,MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);
        }
    }
