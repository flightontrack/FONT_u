package com.flightontrack.activity;

import android.widget.Button;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Const.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.shared.Const.SPACE;
import static com.flightontrack.shared.Props.SessionProp.trackingButtonState;
import static com.flightontrack.shared.Props.ctxApp;

/**
 * Created by hotvk on 5/5/2018.
 */

public class BigButton {
    static final String TAG = "BigButton";
    public Button trackingButton;

    void setTrackingButton(Const.BUTTONREQUEST request) {
        trackingButton.setText(getButtonText(request));
        switch (request) {
            case BUTTON_STATE_RED:
                trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                break;
            case BUTTON_STATE_YELLOW:
                trackingButton.setBackgroundResource(R.drawable.bttn_status_yellow);
                break;
            case BUTTON_STATE_GREEN:
                trackingButton.setBackgroundResource(R.drawable.bttn_status_green);
                break;
            case BUTTON_STATE_GETFLIGHTID:
                trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
                break;
            default:
                trackingButton.setBackgroundResource(R.drawable.bttn_status_red);
        }
        if (request!= Const.BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID)trackingButtonState = request;
    }

    static String setTextGreen() {
        return "Flight: " + (RouteBase.activeFlight.flightNumber) + '\n' +
                "Point: " + RouteBase.activeFlight._wayPointsCount +
                ctxApp.getString(R.string.tracking_flight_time) + SPACE + RouteBase.activeFlight.flightTimeString + '\n'
                + "Alt: " + RouteBase.activeFlight.lastAltitudeFt + " ft";
    }

    static String setTextRedFlightStopped() {
        String fText;
        String fTime = "";
        String flightN = FLIGHT_NUMBER_DEFAULT;

        if (RouteBase.activeRoute == null) {
            new FontLogAsync().execute(new EntityLogMessage(TAG, "setTextRedFlightStopped: activeRoute == null", 'd'));
            fText = Props.SessionProp.pTrackingButtonText;
        } else {
            if (RouteBase.activeFlight != null) {
                flightN = RouteBase.activeFlight.flightNumber;
                fTime = ctxApp.getString(R.string.tracking_flight_time) + SPACE + RouteBase.activeFlight.flightTimeString;
            }
            fText = "Flight " + flightN + '\n' + "Stopped"; // + '\n';
        }
        return fText + fTime;
    }

    static String getButtonText(Const.BUTTONREQUEST request) {
        switch (request) {
            case BUTTON_STATE_RED:
                Props.SessionProp.pTrackingButtonText=setTextRedFlightStopped();
                break;
            case BUTTON_STATE_YELLOW:
                Props.SessionProp.pTrackingButtonText="Flight " + (RouteBase.activeFlight.flightNumber) + ctxApp.getString(R.string.tracking_ready_to_takeoff);
                break;
            case BUTTON_STATE_GREEN:
                Props.SessionProp.pTrackingButtonText=setTextGreen();
                break;
            case BUTTON_STATE_GETFLIGHTID:
                Props.SessionProp.pTrackingButtonText=ctxApp.getString(R.string.tracking_gettingflight);
                break;
        }
        return Props.SessionProp.pTrackingButtonText;
    }
}
