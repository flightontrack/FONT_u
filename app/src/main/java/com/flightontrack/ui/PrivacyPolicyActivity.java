package com.flightontrack.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.flightontrack.R;


public class PrivacyPolicyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privpol_page);
        WebView myWebView = (WebView) findViewById(R.id.privpolwebview);
        myWebView.loadUrl("file:///android_asset/privacypolicy.html");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.help_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_email:
//                sendEmail();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
