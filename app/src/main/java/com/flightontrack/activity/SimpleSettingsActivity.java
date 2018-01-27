package com.flightontrack.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flightontrack.R;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Util;
import com.flightontrack.pilot.Pilot;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.sqlHelper;

public class SimpleSettingsActivity extends Activity implements AdapterView.OnItemSelectedListener,EventBus {

    TextView txtUser;
    public static TextView txtPsw;
    public static TextView txtBuild;
    TextView txtCached;
    Button resetButton;
    Button clearCacheButton;
    Button sendCacheButton;
    Button getPswButton;
    Spinner spinnerUrls;
    Spinner spinnerTextTo;
    CheckBox chBoxIsDebug;
    CheckBox chBoxIsOnReboot;
    CheckBox chBoxIsRoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_settings);
        txtBuild= (TextView) findViewById((R.id.txtBuild));
        txtBuild.setText((getString(R.string.app_label)+" "+ AppConfig.pAppRelease+ AppConfig.pAppReleaseSuffix));
        resetButton = (Button) findViewById(R.id.btnReset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionProp.resetSessionProp();
                updateUI();
                //spinnerUrls.setSelection(SessionProp.pSpinnerUrlsPos);
                Util.setPsw(null);
                getPswButton.setText(R.string.label_btnpsw_get);
                //MainActivity.spinnerMinSpeed.setSelection(Util.getSpinnerSpeedPos());
            }
        });
        clearCacheButton = (Button) findViewById(R.id.btnClearCache);
        clearCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.distribute(new EventMessage(EVENT.SETTINGACT_BUTTONCLEARCACHE_CLICKED));
            }
        });
        sendCacheButton = (Button) findViewById(R.id.btnSendCache);
        sendCacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.distribute(new EventMessage(EVENT.SETTINGACT_BUTTONSENDCACHE_CLICKED));
            }
        });
        getPswButton = (Button) findViewById(R.id.btnGetPsw);
        getPswButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Util.getPsw()==null) {
                    Util.setCloudPsw(view);
                }
                txtPsw.setVisibility(View.VISIBLE);
            }
        });
        if(!AppConfig.pIsAppTypePublic) {
            chBoxIsOnReboot = (CheckBox) findViewById(R.id.isOnRebootCheckBox);
            if (null!=chBoxIsOnReboot) {
                chBoxIsOnReboot.setChecked(SessionProp.pIsOnReboot);
                chBoxIsOnReboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SessionProp.pIsOnReboot = b;
                        //Util.setIsOnBoot(b);
                    }
                });
            }
            spinnerTextTo = (Spinner) findViewById(R.id.spinnerTextTo);
            ArrayAdapter<CharSequence> adapterTextTo = ArrayAdapter.createFromResource(this,R.array.textto_array, android.R.layout.simple_spinner_item);
            adapterTextTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTextTo.setAdapter(adapterTextTo);
            spinnerTextTo.setOnItemSelectedListener(this);

        }
        else{
            findViewById(R.id.layoutTextTo).setVisibility(View.INVISIBLE);
            findViewById(R.id.layoutStartOnReboot).setVisibility(View.INVISIBLE);
        }
        chBoxIsDebug = (CheckBox) findViewById(R.id.isDebugCheckBoxCheckBox);
        //chBoxIsDebug.setChecked(SessionProp.pIsDebug);
        chBoxIsDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                                        if(b) set_writePermissions();
                                                        //Util.setIsDebug(b);
                                                        SessionProp.pIsDebug=b;
                                                    }
                                                });
        chBoxIsRoad = (CheckBox) findViewById(R.id.isRoadCheckBox);
        //chBoxIsRoad.setChecked(SessionProp.pIsRoad);
        chBoxIsRoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SessionProp.pIsRoad=b;
                //Route.set_isRoad(b);
            }
        });
        txtUser= (TextView) findViewById((R.id.txtWebsiteUser));
        txtUser.setText(Pilot.getUserID());
        txtPsw= (TextView) findViewById((R.id.txtWebsitePsw));
        txtPsw.setText(Util.getPsw());
        //chBoxIsDebug.setChecked(Util.getIsDebug());
        spinnerUrls = (Spinner) findViewById(R.id.spinnerUrlId);
        ArrayAdapter<CharSequence> adapterUrl = ArrayAdapter.createFromResource(this,R.array.url_array, android.R.layout.simple_spinner_item);
        adapterUrl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrls.setAdapter(adapterUrl);
        spinnerUrls.setOnItemSelectedListener(this);
        txtCached= (TextView) findViewById((R.id.txtCached));
        txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));

        updateUI();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed(); //done to get to recreate (not to create) activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
                super.onBackPressed(); //done to get to recreate (not to create) activity
    }

    public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
        if (parent.getId()==R.id.spinnerUrlId) {
            SessionProp.pSpinnerUrlsPos=pos;
        }
        if (parent.getId()==R.id.spinnerTextTo) {
            SessionProp.pSpinnerTextToPos=pos;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    protected void set_writePermissions() {
        final int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionWrite == PackageManager.PERMISSION_DENIED) {
            chBoxIsDebug.setChecked(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_RITE_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Util.setIsDebug(true);
                    SessionProp.pIsDebug=true;
                    chBoxIsDebug.setChecked(true);
                } else {
                    //Util.setIsDebug(false);
                    //chBoxIsDebug.setChecked(Util.getIsDebug());
                    Toast.makeText(this,R.string.toast_permiss_declined_2, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionProp.save();
    }
    @Override
    public void onStop() {
        super.onStop();
        SessionProp.save();
    }
    @Override
    public void onPause() {
        super.onPause();
        SessionProp.save();
    }

    private void updateUI(){
        spinnerUrls.setSelection(SessionProp.pSpinnerUrlsPos);
        if(!AppConfig.pIsAppTypePublic) spinnerTextTo.setSelection(SessionProp.pSpinnerTextToPos);
        chBoxIsDebug.setChecked(SessionProp.pIsDebug);
        chBoxIsRoad.setChecked(SessionProp.pIsRoad);
    }
@Override
    public void eventReceiver(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
//        switch(ev){
//            case SETTINGACT_BUTTONCLEARCACHE_CLICKED:
//                sqlHelper.dropCreateDb();
//                break;
//        }
    }
}
