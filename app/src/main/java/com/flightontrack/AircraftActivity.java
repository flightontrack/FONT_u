package com.flightontrack;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import static com.flightontrack.Const.*;

import org.json.JSONException;
import org.json.JSONObject;

public class AircraftActivity extends Activity {
    private static final String TAG = "AircraftActivity:";
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    static TextView txtBlueText;
    static TextView txtAcftMake;
    static TextView txtAcftModel;
    static TextView txtAcftSeries;
    static EditText txtAcftRegNum;
    static EditText txtAcftName;
    static TextView txtAcftTagId;
    static TextView txtUserName;
    static Button doneButton;
    static Button cancelButton;
    static Button clearButton;
    static Switch nfcSwitch;
    ShowAlertClass showAlertClass;
    protected static NfcAdapter nfcAdapter;
    IntentFilter tagDetected;
    IntentFilter ndefDetected;
    IntentFilter[] nfcFilters;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.appendLog(TAG + "AircraftActivity onCreate", 'd');
        //try{
        sharedPreferences = getSharedPreferences("com.flightontrack", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setContentView(MainActivity.isNFCcapable? R.layout.activity_acraft: R.layout.activity_acraft_no_nfc);
//        txtAcftMake = (EditText) findViewById(R.id.txtAcftMake);
//        txtAcftModel = (EditText) findViewById(R.id.txtAcftModel);
//        txtAcftSeries = (EditText) findViewById(R.id.txtAcftSeries);
//        txtAcftRegNum = (EditText) findViewById(R.id.txtAcftRegNum);
//        txtAcftName = (EditText) findViewById(R.id.txtAcftName);
//        txtAcftTagId = (EditText) findViewById(R.id.txtAcftTagId);
//        doneButton = (Button) findViewById(R.id.btn_acft_done);
//        cancelButton = (Button) findViewById(R.id.btn_acft_cancel);
//        clearButton = (Button) findViewById(R.id.btn_acft_clear);
//        nfcSwitch= (Switch)findViewById(R.id.switch_nfc);
//        txtBlueText=(TextView)findViewById(R.id.txtBlueText);
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        //showAlertClass = new ShowAlertClass(this);
//        enableNfcForegroundMode();
//        init_listeners();
//        setAcft(getAcft());
    //}
//        catch (NullPointerException nullPointer){
//            //Log.d(GLOBALTAG,TAG+ nullPointer.toString());
//        }
//        catch (Exception e){
//            //Log.d(GLOBALTAG,TAG+e.toString());
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.acraft, menu);
        return true;
    }
   @Override
    public void onResume() {
       Util.appendLog(TAG + "AircraftActivity onResume", 'd');
       txtAcftMake = (TextView) findViewById(R.id.txtAcftMake);
       txtAcftModel = (TextView) findViewById(R.id.txtAcftModel);
       txtAcftSeries = (TextView) findViewById(R.id.txtAcftSeries);
       txtAcftRegNum = (EditText) findViewById(R.id.txtAcftRegNum);
       txtAcftName = (EditText) findViewById(R.id.txtAcftName);
       txtAcftTagId = (TextView) findViewById(R.id.txtAcftTagId);
       doneButton = (Button) findViewById(R.id.btn_acft_done);
       cancelButton = (Button) findViewById(R.id.btn_acft_cancel);
       clearButton = (Button) findViewById(R.id.btn_acft_clear);
       nfcSwitch= (Switch)findViewById(R.id.switch_nfc);
       txtBlueText=(TextView)findViewById(R.id.txtBlueText);
       nfcAdapter = NfcAdapter.getDefaultAdapter(this);
       txtUserName = (TextView) findViewById(R.id.txtUserName);
       //showAlertClass = new ShowAlertClass(this);
       //if (MainActivity.isNFCcapable) enableNfcForegroundMode();
       init_listeners();
       txtUserName.setText(Util.getUserName());
       setAcft(getAcft());

       super.onResume();

//       nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return (id == R.id.action_settings) || super.onOptionsItemSelected(item);
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
    }

    void init_listeners(){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json  = new JSONObject();
                try
                {
                    json.put("AcftMake",txtAcftMake.getText().toString());
                    json.put("AcftModel",txtAcftModel.getText().toString());
                    json.put("AcftSeries", txtAcftSeries.getText().toString());
                    json.put("AcftRegNum",txtAcftRegNum.getText().toString());
                    json.put("AcftTagId",txtAcftTagId.getText().toString());
                    json.put("AcftName",txtAcftName.getText().toString());
                } catch (JSONException e)
                {
                    //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
                }
                setAcft(json);
                Util.setUserName(txtUserName.getText().toString());
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.clearAcftPreferences();
                setAcft(getAcft());
                //finish();
            }
        });
        if (MainActivity.isNFCcapable) {
            enableNfcForegroundMode();
            nfcSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.appendLog(TAG + "AircraftActivity onCheckedChanged", 'd');
                    setTagNFCState(buttonView.isChecked());
                }
            });
        }
//                txtUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        txtUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String input;
                EditText editText;
                if (!hasFocus) {
                    editText = (EditText) v;
                    input = editText.getText().toString();
                    Util.setUserName(input);
                }
            }
        });
    }
    //void setAcft(String AcftMake,String AcftModel,String AcftSeries,String AcftRegNum,String AcftTagId){
        void setAcft(JSONObject json){
            try
            {
                Util.appendLog(TAG+ json.toString(),'d');
                String AcftMake = json.getString("AcftMake");
                String AcftModel = json.getString("AcftModel");
                String AcftSeries = json.getString("AcftSeries");
                String AcftRegNum = json.getString("AcftRegNum");
                String AcftTagId = json.getString("AcftTagId");
                String AcftName = json.getString("AcftName");
                editor.putString("AcftMake", AcftMake.trim());
                editor.putString("AcftModel", AcftModel.trim());
                editor.putString("AcftSeries", AcftSeries.trim());
                editor.putString("AcftRegNum", AcftRegNum.trim());
                editor.putString("AcftTagId", AcftTagId.trim());
                editor.putString("AcftName", AcftName.trim());
                editor.commit();

                txtAcftMake.setText(AcftMake);
                txtAcftModel.setText(AcftModel);
                txtAcftSeries.setText(AcftSeries);
                txtAcftRegNum.setText(AcftRegNum);
                txtAcftTagId.setText(AcftTagId);
                txtAcftName.setText(AcftName);

//                Editable FieldAcftMake = txtAcftMake.getText();
//                FieldAcftMake.clear();
//                FieldAcftMake.append(AcftMake);
//
//                Editable FieldAcftModel = txtAcftModel.getText();
//                FieldAcftModel.clear();
//                FieldAcftModel.append(AcftModel);
//
//                Editable FieldAcftSeries = txtAcftSeries.getText();
//                FieldAcftSeries.clear();
//                FieldAcftSeries.append(AcftSeries);
//
//                Editable FieldAcftRegNum = txtAcftRegNum.getText();
//                FieldAcftRegNum.clear();
//                FieldAcftRegNum.append(AcftRegNum);
//
//                Editable FieldAcftTagId = txtAcftTagId.getText();
//                FieldAcftTagId.clear();
//                FieldAcftTagId.append(AcftTagId);
//
//                Editable FieldAcftName = txtAcftName.getText();
//                FieldAcftName.clear();
//                FieldAcftName.append(AcftName);

            } catch (JSONException e)
            {
                Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ");
            }
//        editor.putString("AcftMake", AcftMake.trim());
//        editor.putString("AcftModel", AcftModel.trim());
//        editor.putString("AcftSeries", AcftSeries.trim());
//        editor.putString("AcftRegNum", AcftRegNum.trim());
//        editor.putString("AcftTagId", AcftTagId.trim());
//        editor.commit();
//
//        Editable FieldAcftMake = txtAcftMake.getText();
//        FieldAcftMake.clear();
//        FieldAcftMake.append(AcftMake);
//
//        Editable FieldAcftModel = txtAcftModel.getText();
//        FieldAcftModel.clear();
//        FieldAcftModel.append(AcftModel);
//
//        Editable FieldAcftSeries = txtAcftSeries.getText();
//        FieldAcftSeries.clear();
//        FieldAcftSeries.append(AcftSeries);
//
//        Editable FieldAcftRegNum = txtAcftRegNum.getText();
//        FieldAcftRegNum.clear();
//        FieldAcftRegNum.append(AcftRegNum);
//
//        Editable FieldAcftTagId = txtAcftTagId.getText();
//        FieldAcftTagId.clear();
//        FieldAcftTagId.append(AcftTagId);
        //MainActivity.txtAcftNum.setText(a1);
     }

    JSONObject getAcft(){
//        txtAcftMake.setText(sharedPreferences.getString("AcftMake",""));
//        txtAcftModel.setText(sharedPreferences.getString("AcftModel",""));
//        txtAcftSeries.setText(sharedPreferences.getString("AcftSeries",""));
//        txtAcftRegNum.setText(sharedPreferences.getString("AcftRegNum",getString(R.string.default_acft_N)));
//        txtAcftRegNum.setText(sharedPreferences.getString("AcftTagId",""));
        JSONObject json  = new JSONObject();
        try
        {
            json.put("AcftMake",sharedPreferences.getString("AcftMake",""));
            json.put("AcftModel",sharedPreferences.getString("AcftModel",""));
            json.put("AcftSeries",sharedPreferences.getString("AcftSeries",""));
            json.put("AcftRegNum",sharedPreferences.getString("AcftRegNum",getString(R.string.default_acft_N)));
            json.put("AcftTagId",sharedPreferences.getString("AcftTagId",""));
            json.put("AcftName",sharedPreferences.getString("AcftName",""));
        } catch (JSONException e)
        {
            //Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ", e);
        }
        return json;
    }
    public void setTagNFCState(Boolean tagstate){
    if(tagstate&&!nfcAdapter.isEnabled()) {
        showAlertClass.showNFCDisabledAlertToUser(this);
        tagstate = false;
        nfcSwitch.setChecked(tagstate);
    }
        editor.putBoolean("nfctagstate", tagstate).commit();
        txtBlueText.setText(getTagNFCState()?R.string.instructions1:R.string.instructions2);
    }
    public static Boolean getTagNFCState(){
        return sharedPreferences.getBoolean("nfctagstate", false);
    }
    public void enableNfcForegroundMode() {
        //Util.appendLog(TAG+ "AircraftActivity enableForegroundMode");
        // foreground mode gives the current active application priority for reading scanned tags
        tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); // filter for tags
        ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try
        {
            ndefDetected.addDataType("application/com.flightontrack");
        } catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("Could not add MIME type.", e);
        }
        nfcFilters = new IntentFilter[]{tagDetected,ndefDetected};
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }
    NdefMessage[] getNdefMessagesFromIntent(Intent intent)
    {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else
            {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
    NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
    msgs = new NdefMessage[] { msg };
}
} else
        {
        //Log.e(GLOBALTAG,TAG+ "Unknown intent.");
        finish();
        }
        return msgs;
        }
@Override
public void onNewIntent(Intent intent) {
    Util.appendLog(TAG+ "AircraftActivity onNewIntent",'d');
    //Util.appendLog(TAG+ intent.getAction());
    if ((intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)
            || intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) && getTagNFCState()) {
        NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
        NdefRecord record = msgs[0].getRecords()[0];
        byte[] payload = record.getPayload();
        //String jsonString = new String(payload);
        try {
            JSONObject j = new JSONObject(new String(payload));
            j.put("AcftName",txtAcftName.getText().toString());
            setAcft(j);
        } catch (JSONException e) {
            Util.appendLog(TAG+ "Couldn't create json from NFC: "+e.getMessage(),'e');
        }

//        else if (intent.getAction().equals(
//        NfcAdapter.ACTION_TAG_DISCOVERED))
//        {
//        Toast.makeText(this,
//        "This NFC tag currently has no inventory NDEF data.",
//        Toast.LENGTH_LONG).show();
//        }
    }
}
//    private void setAcftFieldValues(String jsonString)
//    {
//        JSONObject nfcJsonRec;
//        String AcftMake = "";
//        String AcftModel = "";
//        String AcftSeries = "";
//        String AcftRegNum = "";
//        String AcftTagId = "";
//        try
//        {
//            nfcJsonRec = new JSONObject(jsonString);
//            AcftMake = nfcJsonRec.getString("AcftMake");
//            AcftModel = nfcJsonRec.getString("AcftModel");
//            AcftSeries = nfcJsonRec.getString("AcftSeries");
//            AcftRegNum = nfcJsonRec.getString("AcftRegNum");
//            AcftTagId = nfcJsonRec.getString("AcftTagId");
//        } catch (JSONException e)
//        {
//            //Util.appendLog(TAG+ "Couldn't parse JSON: ", e);
//        }
//        setAcft(AcftMake,AcftModel,AcftSeries,AcftRegNum,AcftTagId);
//    }
}
