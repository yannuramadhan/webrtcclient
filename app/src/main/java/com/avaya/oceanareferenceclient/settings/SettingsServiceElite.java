/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.settings.pojos.CustomerPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.ElitePreferences;
import com.avaya.oceanareferenceclient.settings.pojos.LoggingPreferences;
import com.avaya.oceanareferenceclient.settings.pojos.TokenServicePreferences;
import com.avaya.oceanareferenceclient.settings.pojos.WebGatewayPreferences;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Work.Attributes.Attribute;
import com.avaya.ocs.Services.Work.Enums.PlatformType;
import com.avaya.ocs.Services.Work.Schema.Attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsServiceElite extends AbstractSettingsService {

    private static final String TAG = SettingsServiceElite.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private Context context;

    public SettingsServiceElite(Context context) {
        super(context);
        this.context = context;
    }

    public ElitePreferences retrievePreferences() {
        mLogger.d("Reading elite preferences");
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final ElitePreferences preferences = new ElitePreferences();
        try {

            String mDestination = sharedPref.getString(context.getString(R.string.preference_number_to_call_elite), "").trim();
            String mContext = sharedPref.getString(context.getString(R.string.preference_context_elite), "").trim();
            String mTopic = sharedPref.getString(context.getString(R.string.preference_topic_elite), "").trim();

            String mPriority = sharedPref.getString(context.getString(R.string.preference_priority_elite), "").trim();
            String mLocale = sharedPref.getString(context.getString(R.string.preference_locale_elite), "").trim();
            String mStrategy = sharedPref.getString(context.getString(R.string.preference_strategy_elite), "").trim();
            String mSourceName = sharedPref.getString(context.getString(R.string.preference_source_name_elite), "").trim();
            String mResourceId = sharedPref.getString(context.getString(R.string.preference_resource_id_elite), "").trim();

            boolean wasReadSuccessful = validate(mDestination);
            preferences.setAvailable(wasReadSuccessful);

            if (wasReadSuccessful) {
                preferences.setDestination(mDestination);
                preferences.setContext(mContext);
                preferences.setTopic(mTopic);
                preferences.setPriority(mPriority);
                preferences.setLocale(mLocale);
                preferences.setStrategy(mStrategy);
                preferences.setSourceName(mSourceName);
                preferences.setResourceId(mResourceId);

            }

        } catch (Exception e) {
            preferences.setAvailable(false);
            mLogger.e("Exception reading elite preferences: " + e.getMessage(), e);
        }
        mLogger.d("Read elite preferences successfully: " + preferences.isAvailable());
        return preferences;
    }

    public List<Attribute> retrieveOceanaRoutingAttributes() {
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mLogger.d("Reading routing attributes");

        int[] preferenceKeys = {R.string.attribute_key_a, R.string.attribute_key_b, R.string.attribute_key_c};
        int[] preferenceValues = {R.string.attribute_value_a, R.string.attribute_value_b, R.string.attribute_value_c};
        List<Attribute> attributes = new ArrayList<>();

        try {
            for (int i = 0; i < preferenceKeys.length; i++) {
                String key = sharedPref.getString(context.getString(preferenceKeys[i]), "");
                String value = sharedPref.getString(context.getString(preferenceValues[i]), "");

                if (isPreferenceConfigured(key) && isPreferenceConfigured(value)) {
                    Attribute attribute = new Attribute()
                            .withName(key.trim())
                            .withValues(Arrays.asList(trimValues(value.split(","))));
                    attributes.add(attribute);
                }
            }
        } catch (Exception e) {
            mLogger.e("Exception reading routing attributes: " + e.getMessage(), e);
        }

        return attributes;
    }

    public Attributes retrieveServiceMapAttributes() {
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mLogger.d("Retrieving ServiceMap attributes");

        int[] preferenceKeys = {R.string.attribute_key_a, R.string.attribute_key_b, R.string.attribute_key_c};
        int[] preferenceValues = {R.string.attribute_value_a, R.string.attribute_value_b, R.string.attribute_value_c};
        Attributes attributes = new Attributes();

        try {
            for (int i = 0; i < preferenceKeys.length; i++) {
                String key = sharedPref.getString(context.getString(preferenceKeys[i]), "");
                String value = sharedPref.getString(context.getString(preferenceValues[i]), "");

                if (isPreferenceConfigured(key) && isPreferenceConfigured(value)) {
                    attributes.add(key.trim(), Arrays.asList(trimValues(value.split(","))));
                }
            }
        } catch (Exception e) {
            mLogger.e("Exception reading routing attributes: " + e.getMessage(), e);
        }
        return attributes;
    }

    public CustomerPreferences retrieveAuthorizationPreferences() {
        mLogger.d("Reading authorization preferences");
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final CustomerPreferences preferences = new CustomerPreferences();
        try {
            String mDisplayName = sharedPref.getString(context.getString(R.string.preference_display_name_elite), "").trim();
            String mFromAddress = sharedPref.getString(context.getString(R.string.preference_user_name_elite), "").trim();

            boolean wasReadSuccessful = validate(mDisplayName, mFromAddress);
            preferences.setAvailable(wasReadSuccessful);

            if (wasReadSuccessful) {
                preferences.setDisplayName(mDisplayName);
                preferences.setFromAddress(mFromAddress);
            }

        } catch (Exception e) {
            preferences.setAvailable(false);
            mLogger.e("Exception reading authorization preferences: " + e.getMessage(), e);
        }
        mLogger.d("Read authorization preferences successfully: " + preferences.isAvailable());
        return preferences;
    }


    public WebGatewayPreferences retrieveWebGatewayPreferences() {
        mLogger.d("Reading web gateway preferences");
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final WebGatewayPreferences preferences = new WebGatewayPreferences();
        try {
            String mAawgServer = sharedPref.getString(context.getString(R.string.preference_aawg_server_elite), "").trim();
            int mAawgPort = Integer.parseInt(sharedPref.getString(context.getString(R.string.preference_aawg_port_elite), "443").trim());
            boolean aawgIsSecure = sharedPref.getBoolean(context.getString(R.string.preference_aawg_secure_elite), true);
            String mAawgUrlPath = sharedPref.getString(context.getString(R.string.preference_aawg_url_path_elite), "").trim();

            boolean wasReadSuccessful = validate(mAawgServer);
            preferences.setAvailable(wasReadSuccessful);

            if (wasReadSuccessful) {
                preferences.setAawgServer(mAawgServer);
                preferences.setAawgPort(mAawgPort);
                preferences.setSecure(aawgIsSecure);
                preferences.setAawgUrlPath(mAawgUrlPath);
            }

        } catch (Exception e) {
            preferences.setAvailable(false);
            mLogger.e("Exception reading web gateway preferences: " + e.getMessage(), e);
        }
        mLogger.d("Read web gateway preferences successfully: " + preferences.isAvailable());
        return preferences;
    }


    public TokenServicePreferences retrieveTokenServicePreferences() {
        mLogger.d("Reading token service preferences");
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final TokenServicePreferences preferences = new TokenServicePreferences();
        try {
            String tokenServer = sharedPref.getString(context.getString(R.string.preference_token_server_elite), "").trim();
            int tokenPort = Integer.parseInt(sharedPref.getString(context.getString(R.string.preference_token_port_elite), "443").trim());
            boolean tokenIsSecure = sharedPref.getBoolean(context.getString(R.string.preference_token_secure_elite), true);
            String tokenUrlPath = sharedPref.getString(context.getString(R.string.preference_token_url_path_elite), "").trim();

            boolean wasReadSuccessful = validate(tokenServer, tokenUrlPath);
            preferences.setAvailable(wasReadSuccessful);

            if (wasReadSuccessful) {
                preferences.setTokenServer(tokenServer);
                preferences.setTokenPort(tokenPort);
                preferences.setSecure(tokenIsSecure);
                preferences.setRestUrlPath(tokenUrlPath);
            }

        } catch (Exception e) {
            preferences.setAvailable(false);
            mLogger.e("Exception reading web gateway preferences: " + e.getMessage(), e);
        }
        mLogger.d("Read web gateway preferences successfully: " + preferences.isAvailable());
        return preferences;
    }

    public LoggingPreferences retrieveLoggingPreferences() {
        mLogger.d("Retrieving logging preferences");
        //SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.elite_preferences), Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final LoggingPreferences preferences = new LoggingPreferences();

        boolean logToDisk = sharedPref.getBoolean(context.getString(R.string.preference_log_to_device_elite), false);
        preferences.setLogToDiskEnabled(logToDisk);

        String logFileName = sharedPref.getString(context.getString(R.string.preference_log_file_name_elite), "");
        preferences.setLogFileName(logFileName);

        return preferences;
    }

    @Override
    public PlatformType getType() {
        return PlatformType.ELITE;
    }


}
