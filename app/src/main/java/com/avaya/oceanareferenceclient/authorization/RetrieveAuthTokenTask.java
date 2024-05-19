/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.authorization;

import android.content.Context;
import android.os.AsyncTask;

import com.avaya.oceanareferenceclient.application.OceanaApplication;
import com.avaya.oceanareferenceclient.settings.AbstractSettingsService;
import com.avaya.oceanareferenceclient.settings.SettingsService;
import com.avaya.oceanareferenceclient.utils.Logger;

public class RetrieveAuthTokenTask extends AsyncTask<Void, Void, AuthorizationResponse> {

    private static final String TAG = RetrieveAuthTokenTask.class.getSimpleName();
    private final AbstractSettingsService settingsService;
    private Logger mLogger = Logger.getLogger(TAG);

    private Context context;
    private ResponseListener<AuthorizationResponse> callback;

    /*
     * When the end user requests 'click-to-call', the app request a Authorization token from the test token service
     * provided on the AAWG. In a production environment, the test token service should be removed from the AAWG.
     * The Authorization token request should be delegated to a customer developed Web Service which can authorization the end user.
     */

    public RetrieveAuthTokenTask(AbstractSettingsService settingsService, ResponseListener<AuthorizationResponse> callback) {
        this.settingsService = settingsService;
        this.callback = callback;
    }

    @Override
    protected AuthorizationResponse doInBackground(Void... params) {
        mLogger.d("Attempting to retrieve Authorization token");
        AuthorizationHandler authorizationService = new AuthorizationHandler(settingsService);
        return authorizationService.getToken();
    }

    @Override
    protected void onPostExecute(AuthorizationResponse response) {
        callback.done(response);
    }
}
