package com.flightontrack.communication;

import android.util.Log;

import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.log.FontLogAsync;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ResponseJsonObj implements AutoCloseable{
    static final String TAG = "ResponseJsonObj";
    public String  responseCurrentFlightNum;
    public String  responseNewFlightNum;
    public String  responseCommand;
    public String  responseException;
    public String  responseAckn;
    public String  responsePsw;
    //public int     iresponseCommand;
    public boolean isException = false;

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
                case "Wsp":
                    responsePsw= getValue(jsonObject,jkey);
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
                case "Command":
                    responseCommand= getValue(jsonObject,jkey);
                    //iresponseCommand=Integer.parseInt(responseCommand);
                    break;
                case "Exception":
                    responseException = getValue(jsonObject,jkey);
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
            isException = true;
            return null;
        }
    }
    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}




