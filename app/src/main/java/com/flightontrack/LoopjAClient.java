package com.flightontrack;

import com.loopj.android.http.*;

public class LoopjAClient extends AsyncHttpClient{
    private int startID;
    LoopjAClient(int id){startID=id;}

    public int getStartID() {
        return startID;
    }
    //private static AsyncHttpClient client = new AsyncHttpClient();
//    public static int timeOut = 20000;
//    public static int retriestimeOut = 3000;
//    public static int maxRetries = 5;
    //public static boolean siteAvailable = false;
//    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
//        client.get(url, params, responseHandler);
//    }

//    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler) {
//      //client.setResponseTimeout(timeOut);
////        client.setTimeout(timeOut);
////        client.setMaxRetriesAndTimeout(maxRetries,retriestimeOut);
////        client.setTimeout(retriestimeOut);
//        client.post(url, requestParams, responseHandler);
//    }
}
