package com.flightontrack.communication;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.entities.EntityRequestCloseFlight;
import com.flightontrack.entities.EntityRequestGetPsw;
import com.flightontrack.entities.EntityRequestHealthCheck;
import com.flightontrack.entities.EntityRequestNewFlight;
import com.flightontrack.entities.EntityRequestNewFlightOffline;
import com.flightontrack.entities.EntityRequestPostLocation;
import com.flightontrack.log.FontLogAsync;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.flightontrack.shared.Util.*;
import static com.flightontrack.shared.Props.*;

public class HttpJsonClient  extends AsyncHttpClient implements AutoCloseable{
    static final String TAG = "HttpJsonClient";
    String url = getTrackingURL() + ctxApp.getString(R.string.webapi_controller);
    //final String url = "http://10.0.2.2/PostngetWebApi/api/router/";
    //final String url = "http://192.168.1.2/PostngetWebApi/api/router/";
    String controllerMethod;
    public String urlLink;
    RequestParams requestParams;
    public boolean isFailed = false;

    public HttpJsonClient(EntityRequestNewFlight entity){
        controllerMethod = "PostFlightRequest";
        setMaxRetriesAndTimeout(2,5000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }
    public HttpJsonClient(EntityRequestNewFlightOffline entity){
        controllerMethod = "PostFlightRequest";
        setMaxRetriesAndTimeout(1,1000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public HttpJsonClient(EntityRequestCloseFlight entity){
        controllerMethod = "PostFlightClose";
        setMaxRetriesAndTimeout(2,2000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public HttpJsonClient(EntityRequestPostLocation entity){
        controllerMethod = "PostLocation";
        setMaxRetriesAndTimeout(2,2000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public HttpJsonClient(EntityRequestGetPsw entity){
        controllerMethod = "PostGetPP";
        setMaxRetriesAndTimeout(2,1000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public HttpJsonClient(EntityRequestHealthCheck entity){
        controllerMethod = "HealthCheck";
        setMaxRetriesAndTimeout(1,1000);
        requestParams = entity;
        urlLink= url+controllerMethod;
    }

    public void post(AsyncHttpResponseHandler h) {
        new FontLogAsync().execute(new EntityLogMessage(TAG,"URL:  "+urlLink, 'd'));
        post(urlLink,requestParams,h);
    }

    @Override
    public void close() throws Exception {
        new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
        //System.out.println(" From Close -  AutoCloseable  ");
    }
}
