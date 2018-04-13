package com.flightontrack.communication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.flightontrack.shared.Const.GLOBALTAG;

public class Response {

    public String  responseFlightNum;
    public String  responseDataLoad;
    public String  responseCommand;
    public String  responseNotif;
    public String  responseAckn;
    public int     iresponseData;
    public int     iresponseAckn;
    public int     iresponseCommand;
    public int     jsonErrorCount  = 0;

    public Response(String responseBody) {
        Log.w("ALERT_RESPONSE: ", responseBody);
        ResponseJson(responseBody);
    }

    private void ResponseJson(String responseBody) {

        try {
            JSONArray responseJsonArray = new JSONArray(responseBody);
            for(int i=0;i<responseJsonArray.length();i++){
                JSONObject jo = responseJsonArray.getJSONObject(i);
                JSONArray jaNames = jo.names();
                String jaKey = (String) jaNames.get(0);
                switch (jaKey) {
                    case "f":
                        responseFlightNum= jo.getString(jaKey);
                        break;
                    case "d":
                        responseDataLoad= jo.getString(jaKey);
                        //iresponseData = Integer.parseInt(responseDataLoad);
                        break;
                    case "a":
                        responseAckn= jo.getString(jaKey);
                        try {
                            iresponseAckn = Integer.parseInt(responseAckn);
                        }
                        catch(Exception e){
                            Log.e(GLOBALTAG, "Couldn't Int.Parse JSON responseAckn: " + responseAckn);
                        }
                        break;
                    case "c":
                        responseCommand= jo.getString(jaKey);
                        iresponseCommand=Integer.parseInt(responseCommand);
                        break;
                    case "n":
                        responseNotif= jo.getString(jaKey);
                        break;
                }
            }

        } catch (JSONException e) {
            Log.e(GLOBALTAG, "Couldn't parse JSON: ");
            jsonErrorCount++;
        }
    }
}




