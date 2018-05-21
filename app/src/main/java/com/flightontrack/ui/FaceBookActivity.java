package com.flightontrack.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import com.flightontrack.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.flightontrack.flight.RouteBase;

public class FaceBookActivity extends FragmentActivity{
    private CallbackManager callbackManager;
    private boolean canPresentShareDialog;
    private ShareDialog shareDialog;
    private static final String TAG = "<-----";

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d(TAG+"Sharer", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG+"Sharer", String.format("Error: %s", error.toString()));
            String title = getString(R.string.fb_error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d(TAG+"Sharer", "Success!" +result.getPostId());
            if (result.getPostId() != null) {
                Log.d(TAG+"Sharer", "result.getPostId() != null");
                String title = getString(R.string.fb_success);
                String id = result.getPostId();
                String alertMessage = getString(R.string.fb_successfully_posted_post, id);
                //showResult(title, alertMessage);
            }
        }
        private void showResult(String title, String alertMessage) {
            Log.d(TAG+"Sharer showResult", "Success!");
            new AlertDialog.Builder(FaceBookActivity.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.fb_ok, null)
                    .show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_book);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG+"Login", "Success!");
                        //handlePendingAction();
                        //updateUI();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG+"Login", "Cancel!");
//                        if (pendingAction != PendingAction.NONE) {
//                            showAlert();
//                            pendingAction = PendingAction.NONE;
//                        }
//                        updateUI();
                    }
                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG+"Login", "Exception!");
//                        if (pendingAction != PendingAction.NONE && exception instanceof FacebookAuthorizationException) {
//                            showAlert();
//                            pendingAction = PendingAction.NONE;
//                        }
                        //updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(FaceBookActivity.this)
                                .setTitle(R.string.fb_cancelled)
                                .setMessage(R.string.fb_permission_not_granted)
                                .setPositiveButton(R.string.fb_ok, null)
                                .show();
                    }
                });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);
        canPresentShareDialog = ShareDialog.canShow(ShareLinkContent.class);
        boolean hasPublishPermission = hasPublishPermission();
        postStatusUpdate();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void postStatusUpdate() {
        //Profile profile = Profile.getCurrentProfile();
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                //.setContentTitle("My Flight " +Route.activeFlight.flightNumber)
                //.setContentDescription("Watch my flight on the link below")
                .setQuote("Watch my flight on the link below")
                //.setContentUrl(Uri.parse("http://flightontrack-test.azurewebsites.net/Flight/DisplayMyFlightMovingMap/"+ RouteBase.activeFlight.flightNumber+"?FlightOrRoute=Route"))
                .setContentUrl(Uri.parse("http://flightontrack-test.azurewebsites.net"))
                .build();
        if (canPresentShareDialog) {
            shareDialog.show(linkContent);
//        } else if (profile != null && hasPublishPermission()) {
//            ShareApi.share(linkContent, shareCallback);
//        }
// else {
//            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.d(TAG, "accessToken:" +accessToken);
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }
}
