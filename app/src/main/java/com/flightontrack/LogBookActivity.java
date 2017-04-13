package com.flightontrack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Arrays;
import java.util.List;


public class LogBookActivity extends Activity {
    private ProgressDialog progressBar;
    private WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_book);

        myWebView = (WebView) findViewById(R.id.logbookwebview);
        myWebView.setInitialScale(0);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        myWebView.addJavascriptInterface(new WebAppInterface(this,myWebView),"Android");
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //view.loadUrl(url);
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        List<String> spinnerUrls = Arrays.asList(this.getResources().getStringArray(R.array.url_array));
        String url ="/Member/PilotLogBookMobile?pilotUserName=" + MainActivity._userId;
        switch (spinnerUrls.indexOf(Util.getTrackingURL())){
            case 0:
                url= "http://flightontrack.azurewebsites.net"+url;
                break;
            case 1:
                url= "http://flightontrack-test.azurewebsites.net"+url;
                break;
            case 2:
                url= "http://192.168.1.3/FlightOnTrack/Member/PilotLogBookMobile?pilotUserName=9784295693.0993";
                //url= Util.getTrackingURL().substring(0,18)+"/FlightOnTrack"+url;
                break;
            default:
                url= "http://flightontrack.azurewebsites.net"+url;
        }
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage(getString(R.string.load_logbook));
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.setMax(100);
        progressBar.setProgress(100);
        progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
                //myWebView.setVisibility(View.GONE);
                finish();
            }
        });
        myWebView.loadUrl(url);
        progressBar.show();
    }

    @Override
    public void onResume() {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onResume();
    }
    public final class WebAppInterface {
        Context mContext;
        WebView myWebView;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c,WebView myWebView) {
            mContext = c;
            this.myWebView=myWebView;
        }

        @JavascriptInterface
        public void reloadMobileLogBook() {
            myWebView.post(new Runnable() {
                @Override
                public void run() {
                    String url= "http://192.168.1.2/FlightOnTrack/Flight/PilotLogBookMobile?pilotUserName=9784295693.0993";
                    myWebView.loadUrl(url);
                }
            });
        }
    }
}
