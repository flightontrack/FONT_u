package com.flightontrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import static com.flightontrack.Const.*;

class ShowAlertClass {

    MainActivity mainact_ctx;
    Context ctx;
    private static final String TAG = "ShowAlertClass";
    ShowAlertClass(MainActivity mainact_this){
        mainact_ctx=mainact_this;
    }
    public ShowAlertClass(Context c){
        ctx=c;
    }
    void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        alertDialogBuilder.setMessage("GPS is disabled on your device")
                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mainact_ctx.startActivity(callGPSSettingIntent);
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
    void showNFCDisabledAlertToUser(Context ctx){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage("NFC is disabled in your device")
                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable NFC",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callNFCIntent = new Intent((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)? Settings.ACTION_NFC_SETTINGS:Settings.ACTION_WIRELESS_SETTINGS);
                                mainact_ctx.startActivity(callNFCIntent);
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
    void showNetworkDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        alertDialogBuilder.setMessage("You are not connected to network")

                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable Connectivity",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                mainact_ctx.startActivity(callGPSSettingIntent);
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

    public void showPemissionsDisabledAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        alertDialogBuilder.setMessage("You are not connected to network")

                .setCancelable(false)
                .setPositiveButton("Goto Settings To Enable Connectivity",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                mainact_ctx.startActivity(callGPSSettingIntent);
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
    void showAircraftIsEmptyAlert(){
        //Log.d(TAG, "0=" + mainact_ctx);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        alertDialogBuilder.setMessage(mainact_ctx.getString(R.string.acft_dialog))
                .setCancelable(false)
                .setPositiveButton(mainact_ctx.getString(R.string.acft_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Util.setIsEmptyAcftOk(true);
                                MainActivity.AppProp.pIsEmptyAcftOk=true;
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(mainact_ctx.getString(R.string.acft_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(mainact_ctx, AircraftActivity.class);
                        mainact_ctx.startActivity(intent);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    void showUnsentPointsAlert(int n){
//        Log.d(TAG, "0=" + mainact_ctx);
//        Util.appendLog(TAG+ "showUnsentPointsAlert",'d');
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        alertDialogBuilder.setMessage(Integer.toString(n)+' '+mainact_ctx.getString(R.string.unsentrecords_dialog))
                .setCancelable(false)
                .setPositiveButton(mainact_ctx.getString(R.string.unsentrecords_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                int MAX_count = 5;
                                SvcComm.commBatchSize=Route.dbLocationRecCount;
                                int counter =0;
                                while (Route.dbLocationRecCount>0){
                                    if (counter >MAX_count) break;
                                    try {
                                        Thread.sleep(1000);
                                    } catch(InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                    counter++;
                                    Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.START_COMMUNICATION);
                                }
                                if(Route.dbLocationRecCount>0) Toast.makeText(mainact_ctx, R.string.unsentrecords_failed, Toast.LENGTH_SHORT).show();
                                int j = Route.sqlHelper.allLocationsDelete();
                                Util.appendLog(TAG + "Deleted from database: " + j + " all locations", 'd');
                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED);
                            }
                        });
        alertDialogBuilder.setNegativeButton(mainact_ctx.getString(R.string.unsentrecords_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Util.setPointsUnsent(0);
                        int j = Route.sqlHelper.allLocationsDelete();
                        //Util.appendLog(TAG + "Deleted from database: " + j + " all locations", 'd');
                        Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED);
                        mainact_ctx.onBackPressed();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    void showBackPressed(){
        //MainActivity.dialogOn = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainact_ctx);
        //alertDialogBuilder.setTitle("Something");

        alertDialogBuilder.setMessage(mainact_ctx.getString(R.string.backpressed_dialog))
                .setCancelable(true)
                .setPositiveButton(mainact_ctx.getString(R.string.backpressed_dialog_pos),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Route.instanceRoute.set_RouteRequest(ROUTEREQUEST.CLOSEAPP_BUTTON_BACK_PRESSED);
                            }
                        });
        alertDialogBuilder.setNegativeButton(mainact_ctx.getString(R.string.backpressed_dialog_neg),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainact_ctx.isToDestroy = false;
                        mainact_ctx.onBackPressed();
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
        new AlertDialog.Builder(mainact_ctx)
                //.setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.fb_ok, null)
                .show();
    }
}