package com.flightontrack;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.flightontrack.Const.GLOBALTAG;

class Response {
    char    responseType;
    //String responseData;
    String  responseFlightNum;
    String  responseDataLoad;
    String  responseCommand;
    String  responseNotif;
    String  responseAckn;
    int     iresponseData;
    int     iresponseAckn;
    int     iresponseCommand;
    int jsonErrorCount  = 0;

    Response(String responseBody) {
        Log.w("RESPONSE: ", responseBody);
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




