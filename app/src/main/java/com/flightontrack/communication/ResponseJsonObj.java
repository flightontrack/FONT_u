package com.flightontrack.communication;

import android.util.Log;

import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ResponseJsonObj {
    static final String TAG = "ResponseJsonObj";
    public String responseCurrentFlightNum;
    public String  responseNewFlightNum;
    public String  responseDataLoad;
    public String  responseCommand;
    public String  responseNotif;
    public String  responseAckn;
    public int     iresponseData;
    public int     iresponseAckn;
    public int     iresponseCommand;
    public int     jsonErrorCount  = 0;

    public ResponseJsonObj(JSONObject jsonObject) {

        new FontLogAsync().execute(new EntityLogMessage(TAG, jsonObject.toString(), 'd'));
        Iterator<?> keys = jsonObject.keys();
        while(keys.hasNext() ) {
            String jkey = (String)keys.next();

            switch (jkey) {
                case "f":
                    responseCurrentFlightNum = getValue(jsonObject,jkey);
                    break;
                case "FlightID":
                    responseNewFlightNum= getValue(jsonObject,jkey);
                    break;
                case "Ackn":
                    responseAckn= getValue(jsonObject,jkey);
//                    try {
//                        //iresponseAckn = Integer.parseInt(responseAckn);
//                    }
//                    catch(Exception e){
//                        Log.e(GLOBALTAG, "Couldn't Int.Parse JSON responseAckn: " + responseAckn);
//                    }
                    break;
                case "c":
                    responseCommand= getValue(jsonObject,jkey);
                    iresponseCommand=Integer.parseInt(responseCommand);
                    break;
                case "n":
                    responseNotif= getValue(jsonObject,jkey);
                    break;
            }
        }
    }
    public String getValue (JSONObject jo,String key){
        try {
            if(jo.has(key)) {
                return jo.getString(key);
            }
            else return null;
        }
        catch (JSONException e){
            Log.e(TAG,"JSONException");
            return null;
        }
    }
}




