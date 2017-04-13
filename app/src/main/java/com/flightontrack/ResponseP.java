package com.flightontrack;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static com.flightontrack.Const.*;

class ResponseP {

    private static final String TAG = "ResponseP:";
    String responseType;
    String responseTypeLoad;
    //String responseDataType;
    //String responseDataLoad;
    private JSONObject responseJsonData;
    private JSONArray responseJsonArray;
    //private String response;

    ResponseP(String responseBody) {
        //String response = responseBody;
        //String responseData = responseBody.substring(2, responseBody.length());
        Log.w("RESPONSE: ",responseBody);
        try {
            responseJsonArray = new JSONArray(responseBody);
            ///getting response type and load on it
            responseJsonData = responseJsonArray.getJSONObject(0);
            JSONArray ja = responseJsonData.names();
            responseType = (String) ja.get(0);
            responseTypeLoad = responseJsonData.getString(responseType);

            ///getting response data
            responseJsonData = responseJsonArray.getJSONObject(1);
            ja = responseJsonData.names();
            String responseDataType = (String) ja.get(0);
            String responseDataLoad = responseJsonData.getString(responseDataType);

        }
        catch (JSONException e)
        {
            Log.e(GLOBALTAG,TAG+ "Couldn't parse JSON: ");
        }
    }
    public String getValue (String key){

        try {
            JSONObject rjd = responseJsonArray.getJSONObject(1);
            if(rjd.has(key)) {
                return rjd.getString(key);
            }
            else return null;
        }
        catch (JSONException e){
            Log.e(GLOBALTAG,TAG+ "JSONException");
            return null;
        }
    }
    public String getKey (int i){
            try {
                JSONArray ja = responseJsonData.names();
                String s = (String) ja.get(i);
                return s;
            }
            catch (JSONException e){
                Log.e(GLOBALTAG,TAG+ "JSONException");
                return null;
            }
}
}




