package com.flightontrack.ui;

import com.flightontrack.R;
import com.flightontrack.entities.EntityLogMessage;
import com.flightontrack.flight.RouteBase;
import com.flightontrack.log.FontLogAsync;
import com.flightontrack.shared.Const;
import com.flightontrack.shared.EventBus;
import com.flightontrack.shared.EventMessage;
import com.flightontrack.shared.Props;

import static com.flightontrack.shared.Const.FLIGHT_NUMBER_DEFAULT;
import static com.flightontrack.shared.Const.SPACE;
import static com.flightontrack.shared.Props.SessionProp.trackingButtonState;
import static com.flightontrack.shared.Props.ctxApp;
import static com.flightontrack.shared.Props.mainactivityInstance;


public class BigButton implements EventBus {
    static final String TAG = "BigButton";
    public static BigButton bigButtonInstance = null;

    public static BigButton getInstance() {
        if(bigButtonInstance == null) {
            bigButtonInstance = new BigButton();
        }
        return bigButtonInstance;
    }

    static void setTrackingButton(Const.BUTTONREQUEST request) {
        int backgroundResource;
        switch (request) {
            case BUTTON_STATE_RED:
                backgroundResource = R.drawable.bttn_status_red;
                break;
            case BUTTON_STATE_YELLOW:
                backgroundResource = R.drawable.bttn_status_yellow;
                break;
            case BUTTON_STATE_GREEN:
                backgroundResource = R.drawable.bttn_status_green;
                break;
            case BUTTON_STATE_GETFLIGHTID:
                backgroundResource = R.drawable.bttn_status_red;
                break;
            default:
                backgroundResource = R.drawable.bttn_status_red;

        }
        if (request!= Const.BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID)trackingButtonState = request;
        mainactivityInstance.trackingButton.setText(getButtonText(request));
        mainactivityInstance.trackingButton.setBackgroundResource(backgroundResource);

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

    @Override
    public void eventReceiver(EventMessage eventMessage) {
        EVENT ev = eventMessage.event;
        new FontLogAsync().execute(new EntityLogMessage(TAG, "eventReceiver : " + ev, 'd'));
        //txtCached.setText(String.valueOf(sqlHelper.getLocationTableCountTotal()));
        switch (ev) {
            case FLIGHT_GETNEWFLIGHT_STARTED:
                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_GETFLIGHTID);
                break;
//            case SESSION_ONSUCCESS_EXCEPTION:;
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
//                break;
            case FLIGHT_STATECHANGEDTO_READYTOSAVE:
                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_YELLOW);
                break;
            case CLOCK_MODECLOCK_ONLY:
                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
                break;
//            case CLOCK_SERVICESTARTED_MODELOCATION:
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_YELLOW);
//                break;
//            case CLOCK_SERVICESELFSTOPPED:
//                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
//                break;
            case FLIGHT_FLIGHTTIME_UPDATE_COMPLETED:
                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_GREEN);
                break;
            case FLIGHT_CLOSEFLIGHT_COMPLETED:
                /// swithch to red
                break;
            case ROUTE_NOACTIVEROUTE:
                setTrackingButton(Const.BUTTONREQUEST.BUTTON_STATE_RED);
                break;
        }
    }

}
