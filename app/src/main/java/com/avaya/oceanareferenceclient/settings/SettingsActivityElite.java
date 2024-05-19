/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.utils.Logger;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivityElite extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_elite);

            // Hide specific preferences
            Preference contextPreference = findPreference(getString(R.string.preference_context_elite));
            Preference userNamePreference = findPreference(getString(R.string.preference_user_name_elite));
            Preference displayNamePreference = findPreference(getString(R.string.preference_display_name_elite));
            Preference numberToCallPreference = findPreference(getString(R.string.preference_number_to_call_elite));
            Preference topicPreference = findPreference(getString(R.string.preference_topic_elite));
            Preference emailAddressPreference = findPreference(getString(R.string.preference_email_address_elite));
            Preference aawgServerPreference = findPreference(getString(R.string.preference_aawg_server_elite));
            Preference aawgPortPreference = findPreference(getString(R.string.preference_aawg_port_elite));
            Preference aawgUrlPathPreference = findPreference(getString(R.string.preference_aawg_url_path_elite));
            Preference logFileNamePreference = findPreference(getString(R.string.preference_log_file_name_elite));
            Preference priorityPreference = findPreference(getString(R.string.preference_priority_elite));
            Preference localePreference = findPreference(getString(R.string.preference_locale_elite));
            Preference strategyPreference = findPreference(getString(R.string.preference_strategy_elite));
            Preference sourceNamePreference = findPreference(getString(R.string.preference_source_name_elite));
            Preference resourceIdPreference = findPreference(getString(R.string.preference_resource_id_elite));
            Preference tokenServerPreference = findPreference(getString(R.string.preference_token_server_elite));
            Preference tokenPortPreference = findPreference(getString(R.string.preference_token_port_elite));
            Preference tokenUrlPathPreference = findPreference(getString(R.string.preference_token_url_path_elite));

            contextPreference.setVisible(false);
            userNamePreference.setVisible(true);
            displayNamePreference.setVisible(true);
            numberToCallPreference.setVisible(true);
            topicPreference.setVisible(false);
            emailAddressPreference.setVisible(false);
            aawgServerPreference.setVisible(true);
            aawgPortPreference.setVisible(false);
            aawgUrlPathPreference.setVisible(false);
            logFileNamePreference.setVisible(false);
            priorityPreference.setVisible(false);
            localePreference.setVisible(false);
            strategyPreference.setVisible(false);
            sourceNamePreference.setVisible(false);
            resourceIdPreference.setVisible(false);
            tokenServerPreference.setVisible(false);
            tokenPortPreference.setVisible(false);
            tokenUrlPathPreference.setVisible(false);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_user_name_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_display_name_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_number_to_call_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_context_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_topic_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_email_address_elite));
            onSharedPreferenceChanged(sharedPreferences, getString((R.string.preference_aawg_server_elite)));
            onSharedPreferenceChanged(sharedPreferences, getString((R.string.preference_aawg_port_elite)));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_aawg_url_path_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_log_file_name_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_priority_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_locale_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_strategy_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_source_name_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_resource_id_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_server_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_port_elite));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_url_path_elite));
        }

        @Override
        public void onPause() {
            getActivity().getSharedPreferences(getString(R.string.elite_preferences), Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (getActivity() == null || !isAdded())
                return;

            // Configure logger depending on keys
            if (key.equals(getString(R.string.preference_log_to_device_elite)) || key.equals(getString(R.string.preference_log_file_name_elite))) {
                boolean logToDisk = sharedPreferences.getBoolean(getString(R.string.preference_log_to_device_elite), false);
                String logFileName = sharedPreferences.getString(getString(R.string.preference_log_file_name_elite), "");
                Logger.configure(logToDisk, logFileName);
            }

            // Update token server preference if AAWG server preference changes
            if (key.equals(getString(R.string.preference_aawg_server_elite))) {
                String aawgServer = sharedPreferences.getString(key, "");
                // Update token server preference
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.preference_token_server_elite), aawgServer);
                editor.apply();
            }

            // Only show summary for string|numeric values, not booleans
            if (!key.equals(getString(R.string.preference_log_to_device_elite))
                    && !key.equals(getString(R.string.preference_token_secure_elite))) {
                Preference preference = findPreference(key);
                preference.setSummary(sharedPreferences.getString(key, ""));
            }
        }

    }
}



