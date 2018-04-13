package com.flightontrack.communication;

import com.flightontrack.entities.EntityRequestCloseFlight;
import com.flightontrack.entities.EntityRequestNewFlight;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpJsonClient  extends AsyncHttpClient{

    //String url = Util.getTrackingURL() + ctxApp.getString(R.string.aspx_rootpage);
    final String url = "http://10.0.2.2/PostngetWebApi/api/router/";
    String controllerMethod;
    String urlLink;
    RequestParams requestParams;

    public HttpJsonClient(EntityRequestNewFlight entity){
        controllerMethod = "PostFlightRequest";
        setMaxRetriesAndTimeout(3,2000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public HttpJsonClient(EntityRequestCloseFlight entity){
        controllerMethod = "PostFlightClose";
        setMaxRetriesAndTimeout(2,2000);
        requestParams = entity;
    }
    public void post(AsyncHttpResponseHandler h) {
        post(urlLink,requestParams,h);
    }
}
