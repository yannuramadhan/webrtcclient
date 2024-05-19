/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings;

import android.content.Context;

import com.avaya.oceanareferenceclient.settings.pojos.AvayaPlatformPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.CustomerPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.LoggingPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.TokenServicePreferences;
import com.avaya.oceanareferenceclient.settings.pojos.WebGatewayPreferences;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Work.Attributes.Attribute;
import com.avaya.ocs.Services.Work.Enums.PlatformType;
import com.avaya.ocs.Services.Work.Schema.Attributes;

import java.util.List;

public abstract class AbstractSettingsService {

    private static final String TAG = AbstractSettingsService.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);
    public static final int TYPE_OCEANA =0;
    public static final int TYPE_ELITE =1;
    private Context context;

    public AbstractSettingsService(Context context) {
        this.context = context;
    }

    public abstract AvayaPlatformPreferences retrievePreferences();

    public abstract List<Attribute> retrieveOceanaRoutingAttributes() ;

    public abstract Attributes retrieveServiceMapAttributes();

    public abstract CustomerPreferences retrieveAuthorizationPreferences();

    public abstract WebGatewayPreferences retrieveWebGatewayPreferences() ;


    public abstract TokenServicePreferences retrieveTokenServicePreferences();


    public abstract LoggingPreferences retrieveLoggingPreferences();

    public abstract PlatformType getType();

    Boolean validate(String... stringValues) {
        Boolean isValid = true;

        for (String stringValue : stringValues) {
            if (stringValue == null || stringValue.isEmpty()) {
                isValid = false;
            }
        }

        return isValid;
    }

    protected boolean isPreferenceConfigured(String key) {
        boolean configured = true;
        if (key.isEmpty() || key == null) {
            configured = false;
        }
        return configured;
    }

    protected String[] trimValues(String[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

}
