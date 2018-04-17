package com.flightontrack.entities;

import android.app.ProgressDialog;
import android.content.Context;

public class EntityProgressBarGetPsw  extends ProgressDialog implements AutoCloseable{

    public EntityProgressBarGetPsw(Context context){
        super(context);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setMessage("Getting password");
        setIndeterminate(true);
        setCancelable(true);
        setMax(100);
        setProgress(100);
    }
    @Override
    public void close() throws Exception {
        //new FontLogAsync().execute(new EntityLogMessage(TAG," From Close -  AutoCloseable  ", 'd'));
    }
}
