package com.flightontrack.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flightontrack.log.FontLog;
import com.flightontrack.other.AlarmManagerCtrl;
import com.flightontrack.R;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.ui.ShowAlertClass;
import com.flightontrack.locationclock.SvcLocationClock;
import com.flightontrack.shared.Util;
import com.flightontrack.flight.Route;
import com.flightontrack.pilot.MyPhone;
import com.flightontrack.pilot.Pilot;
import com.flightontrack.receiver.ReceiverHealthCheckAlarm;
import com.flightontrack.receiver.ReceiverBatteryLevel;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static com.flightontrack.shared.Const.*;
import static com.flightontrack.shared.Props.*;
import static com.flightontrack.shared.Props.SessionProp.*;
import static com.flightontrack.flight.Session.*;

public class MainActivity extends AppCompatActivity implements EventBus{
    private static final String TAG = "MainActivity:";
    //public static Context ctxApp;
    static TextView txtUserName;
    public static TextView txtAcftNum ;
    public static Spinner spinnerUpdFreq;
    public static Spinner spinnerMinSpeed;
    public static CheckBox chBoxIsMultiLeg;
    public static Button trackingButton;
    static Toolbar toolbarTop;
    static Toolbar toolbarBottom;
    static ActionMenuView amvMenu;
    static View cardLayout1;
    //public static MainActivity instanceThis = null;
    //public Route route;
    public static boolean isToDestroy = true;
//    public static SharedPreferences sharedPreferences;
//    public static SharedPreferences.Editor editor;
    ReceiverHealthCheckAlarm alarmReceiver;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //Log.d(TAG, "MainActivityThread:" + Thread.currentThread().getId());
            //instanceThis = this;
            // Session c = new Session(getApplicationContext(),this);
            //MainActivity.ctxApp = getApplicationContext();
            initProp(getApplicationContext(),this);

            setContentView(R.layout.activity_main);
            AppConfig.pMainActivityLayout = findViewById(R.id.TagView).getTag().toString();
            if(AppConfig.pMainActivityLayout.equals("full")) {
                //AppConfig.pIsOnRebootCheckBoxEnabled = true;
                toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
                amvMenu = (ActionMenuView) toolbarBottom.findViewById(R.id.amvMenu);
                amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return onOptionsItemSelected(menuItem);
                    }
                });
                setSupportActionBar(toolbarBottom);
            }
            cardLayout1 = findViewById(R.id.cardLayoutId1);
            cardLayout1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //FontLog.appendLog(TAG + "Method1", 'd');
                    Intent intent = new Intent(ctxApp, AircraftActivity.class);
                    startActivity(intent);
                }

            });
            toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
            setSupportActionBar(toolbarTop);
            getSupportActionBar().setTitle(getString(R.string.app_label) + " " + AppConfig.pAppRelease + AppConfig.pAppReleaseSuffix);
            txtAcftNum = (TextView) findViewById(R.id.txtAcftNum);
            txtUserName = (TextView) findViewById(R.id.txtUserName);
            chBoxIsMultiLeg = (CheckBox) findViewById(R.id.patternCheckBox);
            spinnerUpdFreq = (Spinner) findViewById(R.id.spinnerId);
            spinnerMinSpeed = (Spinner) findViewById(R.id.spinnerMinSpeedId);
            trackingButton = (Button) findViewById(R.id.btnTracking);
            //clearAll();
            AppConfig.get();
            AppConfig.pIsNFCcapable = AppConfig.pIsNFCEnabled &&isNFCcapable();
            SessionProp.get();

//            if (!getApplicationContext().toString().equals(Util.getCurrAppContext())) {
//                FontLog.appendLog(TAG + "New App Context", 'd');
//                Util.setCurrAppContext(ctxApp.toString());
//                Route.activeRoute = null;
//            }

            if (!AppConfig.pIsAppTypePublic) {
                IntentFilter filter = new IntentFilter(HEALTHCHECK_BROADCAST_RECEIVER_FILTER);
                alarmReceiver = new ReceiverHealthCheckAlarm();
                registerReceiver(alarmReceiver, filter);
                AlarmManagerCtrl.initAlarm();
                AlarmManagerCtrl.setAlarm();
                ReceiverBatteryLevel receiverBatteryLevel = new ReceiverBatteryLevel();
                registerReceiver(receiverBatteryLevel, new IntentFilter("android.intent.action.BATTERY_LOW"));
            }
            updFreqSpinnerSetup();
            minSpeedSpinnerSetup();
            SessionProp.set_isMultileg(true);
            SessionProp.save();
        }
//        catch (NullPointerException nullPointer){
//            Util.appendLog(TAG + nullPointer.toString(), 'e');
//        }
        catch (Exception e) {
            FontLog.appendLog(TAG + "EXCEPTION!!!!: "+e.toString(), 'e');
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        FontLog.appendLog(TAG + "onResume",'d');
        super.onResume();
//        Intent nfcintent = getIntent();
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(nfcintent.getAction())) {
//            ///TODO
//        }

        SessionProp.get();
        Util.setAcftNum(Util.getAcftNum(4));
        setTrackingButton(trackingButtonState);

        init_listeners();
        int permissionCheckPhone = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheckLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheckLocation == PackageManager.PERMISSION_DENIED) {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra("PERMISSIONTYPE",Manifest.permission.ACCESS_FINE_LOCATION);
            startActivityForResult(intent, START_ACTIVITY_RESULT);
        }
        else if (permissionCheckPhone == PackageManager.PERMISSION_DENIED) {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.putExtra("PERMISSIONTYPE",Manifest.permission.READ_PHONE_STATE);
            startActivityForResult(intent, START_ACTIVITY_RESULT);
        }
        else txtUserName.setText(Pilot.getPilotUserName());

        if (AppConfig.pAutostart) {
            trackingButton.performClick();
            AppConfig.pAutostart = false;
        }
        isToDestroy = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        if(AppConfig.pMainActivityLayout.equals("full")) {
            getMenuInflater().inflate(R.menu.menu_bottom, amvMenu.getMenu());
        }
        toolbarTop.inflateMenu(R.menu.menu_top);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Util.setUserName(txtUserName.getText().toString());
        switch (item.getItemId()) {
            case R.id.action_help:
                helpPage();
                return true;
            case R.id.action_email:
                sendEmail();
                return true;
            case R.id.action_logbook:
                logBookPage();
                return true;
            case R.id.action_settings:
                settingsActivity();
                return true;
            case R.id.action_aircraft:
                acftActivity();
                return true;
            case R.id.action_facebook:
                if (!(Route.activeRoute.activeFlight == null)) facebActivity();
                else
                    Toast.makeText(MainActivity.this, getString(R.string.start_flight_first), Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        FontLog.appendLog(TAG + "isToDestroy :" + isToDestroy, 'd');
        if (isToDestroy) {
            new ShowAlertClass(this).showBackPressed();
        } else {
            /// set this flag to true to stop unregister a healthcheck in onDestroy
            isToDestroy = true;
            super.onBackPressed();
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
        FontLog.appendLog(TAG + "OnDestroy", 'd');
        SessionProp.save();
        SessionProp.clearOnDestroy();
        if (isToDestroy && alarmReceiver!=null) {
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == START_ACTIVITY_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onPause() {
        super.onPause();
        FontLog.appendLog(TAG + "onPause", 'd');
        SessionProp.save();
    }

    @Override
    public void onStop() {
        super.onStop();
        FontLog.appendLog(TAG + "onStop", 'd');
        SessionProp.save();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
    public void onCheckboxClicked(View view) {
        FontLog.appendLog(TAG+ "!!!!! CheckBox clicked !!!!!", 'd');
        EventBus.distribute(new EventMessage(EVENT.MACT_MULTILEG_ONCLICK).setEventMessageValueBool(chBoxIsMultiLeg.isChecked()));

    }
    void init_listeners() {

        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Util.appendLog(TAG + "trackingButton: onClick",'d');
                //if (Route._routeStatus == RSTATUS.PASSIVE) {
                switch (trackingButtonState) {
                    case BUTTON_STATE_RED:
                        Util.setAcftNum(txtAcftNum.getText().toString());
                        /// /// if (!AppConfig.pAutostart && !is_services_available()) return;
                        if (!isAircraftPopulated() && !SessionProp.pIsEmptyAcftOk) {

                            new ShowAlertClass(mainactivityInstance).showAircraftIsEmptyAlert();
                            if (!SessionProp.pIsEmptyAcftOk) return;
                        }
                        EventBus.distribute(new EventMessage(EVENT.MACT_BIGBUTTON_ONCLICK_START));
                        break;
                    default:
                        EventBus.distribute(new EventMessage(EVENT.MACT_BIGBUTTON_ONCLICK_STOP));
//                        SessionProp.set_isMultileg(false);
//                        set_SessionRequest(SESSIONREQUEST.BUTTON_STOP_PRESSED);
                        break;
                }
//                if (activeRoute == null) {
//                    //Util.setUserName(txtUserName.getText().toString());
//                    Util.setAcftNum(txtAcftNum.getText().toString());
//                    set_pIntervalLocationUpdateSecPos(spinnerUpdFreq.getSelectedItemPosition());
//                    if (!AppConfig.pAutostart && !is_services_available()) return;
//                    //if (!isAircraftPopulated() && !Util.isEmptyAcftOk()) {
//                    if (!isAircraftPopulated() && !AppConfig.pIsEmptyAcftOk) {
//
//                        new ShowAlertClass(mainactivityInstance).showAircraftIsEmptyAlert();
//                        if (!AppConfig.pIsEmptyAcftOk) return;
//                    }
//                    routeList.add(new Route());
//                    //activeRoute = new Route();
//
//                } else {
//                    set_isMultileg(false);
//                    activeRoute.set_rAction(RACTION.CLOSE_BUTTON_STOP_PRESSED);
//                }

            }
        });
//        chBoxIsMultiLeg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                /// do nothing for now SessionProp.set_isMultileg(b);
//                FontLog.appendLog(TAG+ "!!!!! onCheckedChanged !!!!!", 'd');
//            }
//        });

//        txtUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                String input;
//                if (actionId == EditorInfo.IME_ACTION_DONE ||
//                        event.getAction() == KeyEvent.ACTION_DOWN &&
//                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER
//                        ) {
//                    input = v.getText().toString();
//                    Util.setUserName(input);
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(txtUserName.getWindowToken(), 0);
//                    return true; // consume.
//                }
//                return false; // pass on to other listeners.
//            }
//        });
//        txtUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                String input;
//                EditText editText;
//                if (!hasFocus) {
//                    editText = (EditText) v;
//                    input = editText.getText().toString();
//                    Util.setUserName(input);
//                }
//            }
//        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private boolean isAircraftPopulated() {
        return !(txtAcftNum.getText().toString().trim().equals(getString(R.string.default_acft_N)));
    }

    private boolean isNFCcapable() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return (nfcAdapter != null);
    }

    public void acftActivity() {
        Intent intent = new Intent(this, AircraftActivity.class);
        startActivity(intent);
    }

    public void facebActivity() {
//f        Intent intent = new Intent(this, FaceBookActivity.class);
//f        startActivity(intent);
    }

    void helpPage() {
        try {
            Intent intent = new Intent(ctxApp, HelpPageActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctxApp, "Can't reach help webpage.", Toast.LENGTH_SHORT).show();
        }
    }

    void logBookPage() {
        try {
            Intent intent = new Intent(ctxApp, LogBookActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Can't start LogBook Activity", Toast.LENGTH_SHORT).show();
        }
    }

    void sendEmail() {

        MyPhone myPhone = new MyPhone();
        String[] TO = {getString(R.string.email_crash)};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Flight On Track issue");
        String emailText = "APP_BUILD : " + myPhone.getVersionCode() + '\n' +
                "ANDROID_VERSION : " + myPhone.getMyAndroidVersion() + '\n' +
                "PHONE_MODEL : " +
                //Util.deviceMmnufacturer.toUpperCase()+'\n'+
                //Util.deviceBrand.toUpperCase()+'\n'+
                myPhone.deviceModel.toUpperCase() + ' ' +
                myPhone.deviceProduct.toUpperCase() + '\n' +
                "USER : " + Pilot.getUserID() + '\n';

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText + '\n' + getString(R.string.email_commment) + '\n');
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, getString(R.string.email_notinstalled), Toast.LENGTH_SHORT).show();
        }
    }

    public void settingsActivity() {
        try {
            Intent intent = new Intent(ctxApp, SimpleSettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(ctxApp,
                    "Can't start Settings.", Toast.LENGTH_SHORT).show();
        }
    }

    void updFreqSpinnerSetup() {
        String[] interval_name = getResources().getStringArray(R.array.intervalname_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, interval_name);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerUpdFreq.setAdapter(adapter);
        spinnerUpdFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                set_pIntervalLocationUpdateSecPos(position);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerUpdFreq.setSelection(SessionProp.pIntervalSelectedItem);
    }

    void minSpeedSpinnerSetup() {
        ArrayAdapter<CharSequence> adapterSpeed = ArrayAdapter.createFromResource(this, R.array.speed_array, android.R.layout.simple_spinner_item);
        adapterSpeed.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerMinSpeed.setAdapter(adapterSpeed);
        spinnerMinSpeed.setSelection(SessionProp.pSpinnerMinSpeedPos);
        //Util.setSpinnerSpeedPos(SessionProp.pSpinnerMinSpeedPos);
        spinnerMinSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Util.setSpinnerSpeedPos(position);
                SessionProp.set_pSpinnerMinSpeedPos(position);
                //Util.setTrackingSpeed(spinnerMinSpeed.getSelectedItem().toString());

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //pMinSpeedArray= getResources().getStringArray(R.array.speed_array);
    }

    public static Boolean isMainActivityExist() {
        return mainactivityInstance != null;
    }

    public void finishActivity() {
        if (SvcLocationClock.isInstanceCreated()) SvcLocationClock.instanceSvcLocationClock.stopServiceSelf();
        if (alarmReceiver!=null) {
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
        ctxApp = null;
        txtUserName = null;
        txtAcftNum = null;
        spinnerUpdFreq = null;
        spinnerMinSpeed = null;
        chBoxIsMultiLeg = null;
        trackingButton = null;
        mainactivityInstance = null;
        finish();
    }

    private boolean is_services_available() {

        if (!isGPSEnabled()) {
            new ShowAlertClass(this).showGPSDisabledAlertToUser();
            return false;
        }
        if (!Util.isNetworkAvailable()) {
            new ShowAlertClass(this).showNetworkDisabledAlertToUser();
            return false;
        }
        return true;
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    //    static void Method1(View v){
//        Util.appendLog(TAG + "Method1", 'd');
//        Intent intent = new Intent(this, AircraftActivity.class);
//        //startActivity(intent);
//        //acftActivity();
//    }
    static void setTrackingButton(BUTTONREQUEST request) {
        //Util.appendLog(TAG+"trackingButtonState request:" +request,'d');
        switch (request) {
            case BUTTON_STATE_RED:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(setTextRed());
                break;
            case BUTTON_STATE_YELLOW:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                MainActivity.trackingButton.setText("Flight " + (Route.activeRoute.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff));
                //editor.putInt("trackingButtonState", BUTTON_STATE_YELLOW);
                break;
            case BUTTON_STATE_GREEN:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_green);
                MainActivity.trackingButton.setText(setTextGreen());
                //editor.putInt("trackingButtonState", BUTTON_STATE_GREEN);
                break;
            case BUTTON_STATE_GETFLIGHTID:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                MainActivity.trackingButton.setText(ctxApp.getString(R.string.tracking_gettingflight));
                break;
            case BUTTON_STATE_STOPPING:
                //appendLog(LOGTAG+"BUTTON_STATE_STOPPING");
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                //MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_stopping));
                break;
            default:
                MainActivity.trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                // MainActivity.trackingButton.setText("Flight " + (Flight.get_ActiveFlightID()) + ctx.getString(R.string.tracking_is_off));
        }
        trackingButtonState = request;
    }
    static String setTextGreen() {
        SessionProp.pTextGreen = "Flight: " + (Route.activeFlight.flightNumber) + '\n' +
                "Point: " + Route.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + Route.activeFlight.flightTimeString + '\n'
                + "Alt: " + Route.activeFlight.lastAltitudeFt + " ft";
        return SessionProp.pTextGreen;
    }
    static String setTextRed() {
        String fText = SessionProp.pTextRed;
        String fTime = "";
        String flightN = FLIGHT_NUMBER_DEFAULT;

        if (Route.activeRoute == null) {
            FontLog.appendLog(TAG + " setTextRed: flightId IS NULL", 'd');
        } else {
            if (Route.activeFlight!=null) {
                flightN = Route.activeFlight.flightNumber;
                fTime = ctxApp.getString(R.string.tracking_flight_time) + SPACE + Route.activeFlight.flightTimeString;
            }
            fText = "Flight " + flightN + '\n' + "Stopped"; // + '\n';
        }
        SessionProp.pTextRed = fText + fTime;
        return SessionProp.pTextRed;
    }
    @Override
    public void eventReceiver(EventMessage eventMessage){
        EVENT ev = eventMessage.event;
        FontLog.appendLog(TAG + " eventReceiver : "+ev, 'd');

        switch(ev){
            case PROP_CHANGED_MULTILEG:
                chBoxIsMultiLeg.setChecked(eventMessage.eventMessageValueBool);
                break;
            case FLIGHT_GETNEWFLIGHT_STARTED:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                break;
            case SVCCOMM_ONSUCCESS_NOTIF:
                Toast.makeText(mainactivityInstance,R.string.toast_server_error, Toast.LENGTH_LONG).show();
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
            case FLIGHT_FLIGHTTIME_STARTED:
            //swithch to green
            break;
            case FLIGHT_GETNEWFLIGHT_COMPLETED:
            if(eventMessage.eventMessageValueBool)setTrackingButton(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                else setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
            break;
            case CLOCK_MODECLOCK_ONLY:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
            case CLOCK_SERVICESTARTED_MODELOCATION:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_YELLOW);
                break;
            case CLOCK_SERVICESELFSTOPPED:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_RED);
                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                setTrackingButton(BUTTONREQUEST.BUTTON_STATE_GREEN);
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                /// swithch to red
                break;
        //break;
        }
    }
}

