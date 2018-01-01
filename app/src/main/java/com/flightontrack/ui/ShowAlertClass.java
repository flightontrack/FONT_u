package com.flightontrack.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.activity.AircraftActivity;
import com.flightontrack.activity.MainActivity;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;

import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.flight.Session.SESSIONREQUEST;

public class ShowAlertClass{

    //MainActivity ctxActivity;
    Context ctx;
    Activity ctxActivity;
    private static final String TAG = "ShowAlertClass";

//    ShowAlertClass(MainActivity mainact_this){
//        ctxActivity=mainact_this;
//    }
    public ShowAlertClass(Context c){
        ctx=c;
    }
    public ShowAlertClass(Activity c){
        ctxActivity=c;
    }
    public void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        alertDialogBuilder.setMessage("GPS is disabled on your device")
                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                ctxActivity.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showNFCDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage("NFC is disabled in your device")
                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable NFC",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callNFCIntent = new Intent((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)? Settings.ACTION_NFC_SETTINGS:Settings.ACTION_WIRELESS_SETTINGS);
                                ctxActivity.startActivity(callNFCIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        //appController.setTagNFCState(Boolean.FALSE);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showNetworkDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        alertDialogBuilder.setMessage("You are not connected to network")

                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable Connectivity",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                ctxActivity.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    void showPemissionsDisabledAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        alertDialogBuilder.setMessage("You are not connected to network")

                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable Connectivity",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                ctxActivity.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showAircraftIsEmptyAlert(){
        //Log.d(TAG, "0=" + ctxActivity);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        alertDialogBuilder.setMessage(ctxActivity.getString(R.string.acft_dialog))
                .setCancelable(false)
                .setPositiveButton(ctxActivity.getString(R.string.acft_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Util.setIsEmptyAcftOk(true);
                                SessionProp.pIsEmptyAcftOk=true;
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(ctxActivity.getString(R.string.acft_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(ctxActivity, AircraftActivity.class);
                        ctxActivity.startActivity(intent);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showUnsentPointsAlert(int n){
//        Log.d(TAG, "0=" + ctxActivity);
//        Util.appendLog(TAG+ "showUnsentPointsAlert",'d');
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        alertDialogBuilder.setMessage(Integer.toString(n)+' '+ctxActivity.getString(R.string.unsentrecords_dialog))
                .setCancelable(false)
                .setPositiveButton(ctxActivity.getString(R.string.unsentrecords_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                EventBus.distribute(new EventMessage(EVENT.DIALOG_ONCLICK).setEventMessageValueSessionRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS_ON_YES));
                                //set_SessionRequest(SESSIONREQUEST.SEND_STORED_LOCATIONS);
                                if(dbLocationRecCount>0) Toast.makeText(ctxActivity, R.string.unsentrecords_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
        alertDialogBuilder.setNegativeButton(ctxActivity.getString(R.string.unsentrecords_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //Util.setPointsUnsent(0);
                        //int j = sqlHelper.allLocationsDelete();
                        //Util.appendLog(TAG + "Deleted from database: " + j + " all locations", 'd');
                        //set_SessionRequest(SESSIONREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED_NO_CACHE_CHECK);
                        EventBus.distribute(new EventMessage(EVENT.DIALOG_ONCLICK).setEventMessageValueSessionRequest(SESSIONREQUEST.CLOSEAPP_NO_CACHE_CHECK));

                        //ctxActivity.onBackPressed();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showBackPressed(){
        //MainActivity.dialogOn = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctxActivity);
        //alertDialogBuilder.setTitle("Something");

        alertDialogBuilder.setMessage(ctxActivity.getString(R.string.backpressed_dialog))
                .setCancelable(true)
                .setPositiveButton(ctxActivity.getString(R.string.backpressed_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                EventBus.distribute(new EventMessage(EVENT.DIALOG_ONCLICK).setEventMessageValueSessionRequest(SESSIONREQUEST.CHECK_CACHE_FIRST));
                                //set_SessionRequest(SESSIONREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED_WITH_CACHE_CHECK);
                            }
                        });
        alertDialogBuilder.setNegativeButton(ctxActivity.getString(R.string.backpressed_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.isToDestroy = false;
                        ctxActivity.onBackPressed();
                        dialog.cancel();
                    }
                });
//        //Button Three : Neutral
//        alertDialogBuilder.setNeutralButton("Can't Say!", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(MainActivity.this, "Neutral button Clicked!", Toast.LENGTH_LONG).show();
//                        dialog.cancel();
//                    }
//                }
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showFBisNotEnabled(String alertMessage){
        new AlertDialog.Builder(ctxActivity)
                //.setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.fb_ok, null)
                .show();
    }
}