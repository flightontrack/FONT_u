package com.flightontrack;

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

import com.google.android.gms.appindexing.AppIndex;

import static com.flightontrack.Const.MY_PERMISSIONS_RITE_EXTERNAL_STORAGE;

public class SimpleSettingsActivity extends Activity implements AdapterView.OnItemSelectedListener {

    protected static TextView txtUser;
    protected static TextView txtPsw;
    protected static Button resetButton;
    protected static Button getPswButton;
    protected static Spinner spinnerUrls;
    protected static CheckBox chBoxIsDebug;
    protected static CheckBox chBoxIsOnReboot;
    protected static CheckBox chBoxIsRoad;
    //public static int spinnerUrlsPos;
    //public static ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_settings);
        resetButton = (Button) findViewById(R.id.btnClear);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.clearSettingPreferences();
                //spinnerUrls.setSelection(Util.getSpinnerUrlsPos());
                spinnerUrls.setSelection(MainActivity.AppProp.pSpinnerUrlsPos);
                Util.setPsw(null);
                getPswButton.setText(R.string.label_btnpsw_get);
                MainActivity.spinnerMinSpeed.setSelection(Util.getSpinnerSpeedPos());
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
        chBoxIsOnReboot = (CheckBox) findViewById(R.id.isOnRebootCheckBox);
        chBoxIsOnReboot.setChecked(Util.getIsOnBoot());
        chBoxIsOnReboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Util.setIsOnBoot(b);
            }
        });
        chBoxIsDebug = (CheckBox) findViewById(R.id.isDebugCheckBoxCheckBox);
        chBoxIsDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                                        if(b) set_writePermissions();
                                                        //Util.setIsDebug(b);
                                                        MainActivity.AppProp.pIsDebug=b;
                                                    }
                                                });
        chBoxIsRoad = (CheckBox) findViewById(R.id.isRoadCheckBox);
        chBoxIsRoad.setChecked(Route._isRoad);
        chBoxIsRoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Route.set_isRoad(b);
            }
        });
        txtUser= (TextView) findViewById((R.id.txtWebsiteUser));
        txtUser.setText(MainActivity._userId);
        txtPsw= (TextView) findViewById((R.id.txtWebsitePsw));
        txtPsw.setText(Util.getPsw());
        //chBoxIsDebug.setChecked(Util.getIsDebug());
        chBoxIsDebug.setChecked(MainActivity.AppProp.pIsDebug);
        spinnerUrls = (Spinner) findViewById(R.id.spinnerUrlId);
        ArrayAdapter<CharSequence> adapterUrl = ArrayAdapter.createFromResource(this,R.array.url_array, android.R.layout.simple_spinner_item);
        adapterUrl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrls.setAdapter(adapterUrl);
        spinnerUrls.setOnItemSelectedListener(this);
        //spinnerUrls.setSelection(Util.getSpinnerUrlsPos());
        spinnerUrls.setSelection(MainActivity.AppProp.pSpinnerUrlsPos);
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
        //String item_selected = parent.getItemAtPosition(pos).toString();
        if (parent.getId()==R.id.spinnerUrlId) {
            //spinnerUrlsPos=pos;
            //Util.setSpinnerUrlsPos(pos);
            MainActivity.AppProp.pSpinnerUrlsPos=pos;
            //MainActivity.AppProp.save();
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
                    MainActivity.AppProp.pIsDebug=true;
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
        MainActivity.AppProp.save();
    }
    @Override
    public void onStop() {
        super.onStop();
        MainActivity.AppProp.save();
    }
    @Override
    public void onPause() {
        super.onPause();
        MainActivity.AppProp.save();
    }
}
