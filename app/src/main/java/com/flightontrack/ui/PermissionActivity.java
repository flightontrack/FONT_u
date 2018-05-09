package com.flightontrack.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.flight.Route;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.entities.EntityLogMessage;

import static com.flightontrack.shared.Const.*;

public class PermissionActivity extends Activity {
    private static final String TAG = "PermissionActivity";

    private static Activity thisAct = null;
    protected Route route;
    protected static boolean autostart = false;
    
    //public static boolean productionRelease = false;
    //public static boolean productionRelease = true;

    public PermissionActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String permissionType = getIntent().getExtras().getString("PERMISSIONTYPE");

        super.onCreate(savedInstanceState);
        try {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "onCreate", 'd'));
            thisAct = this;
            set_Permissions(permissionType);
        }
        catch (Exception e) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, e.toString(), 'e'));
        }
    }
    @Override
    public void onResume() {
        new FontLogAsync().execute(new EntityLogMessage(TAG, "onResume", 'd'));
        super.onResume();
    }

    protected void set_Permissions(String permissionType) {

        if (permissionType.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showMessageOKCancel(getString(R.string.permisslocation_ask), getString(R.string.permiss_grantyes), getString(R.string.permiss_grantno),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(thisAct, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_LOCATION);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
            );
            return;
        }

        if (permissionType.equals(Manifest.permission.READ_PHONE_STATE)) {
            showMessageOKCancel(getString(R.string.permiss_ask), getString(R.string.permiss_grantyes), getString(R.string.permiss_grantno),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(thisAct, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
            );
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("MyStringInstanceState", "Activity Recreation");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Util.appendLog(TAG + "OnDestroy",'d');
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //_phoneNumber = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
                    //_myDeviceId = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(thisAct, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_LOCATION);
                    }
                    else{
                        //startActivity(new Intent(this, MainActivity.class));
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    Toast.makeText(this,R.string.toast_permiss_declined_1, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            case MY_PERMISSIONS_REQUEST_READ_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startActivity(new Intent(this, MainActivity.class));
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this,R.string.toast_permiss_declined_1, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
                return;
        }
    }
    private void showMessageOKCancel(String message,String posBtnTxt, String negBtnTxt, DialogInterface.OnClickListener okListener,DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(thisAct)
                .setMessage(message)
                .setPositiveButton(posBtnTxt, okListener)
                .setNegativeButton(negBtnTxt, cancelListener)
                .create()
                .show();
    }
}

