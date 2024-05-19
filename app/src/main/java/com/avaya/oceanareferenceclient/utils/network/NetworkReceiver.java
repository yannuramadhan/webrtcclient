/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.avaya.oceanareferenceclient.utils.Logger;

/**
 * This class alerts the user to any changes in network connectivity
 */
public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkReceiver.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    Context mContext;

    @Override
    public void onReceive(Context context, final Intent intent) {
        try {
            this.mContext = context;
            String status = NetworkUtil.getConnectivityStatusString(context);
            mLogger.i(TAG + "Network connection changed: " + status);
            Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            mLogger.e("Error while receiving network update", e);
        }
    }
}
