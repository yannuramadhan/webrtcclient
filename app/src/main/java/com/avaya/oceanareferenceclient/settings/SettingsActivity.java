/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.settings;

import android.app.Activity;
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

public class SettingsActivity extends AppCompatActivity {
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
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_amc_server));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_user_name));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_display_name));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_number_to_call));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_context));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_topic));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_email_address));
            onSharedPreferenceChanged(sharedPreferences, getString((R.string.preference_aawg_server)));
            onSharedPreferenceChanged(sharedPreferences, getString((R.string.preference_aawg_port)));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_aawg_url_path));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_amc_port));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_log_file_name));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_priority));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_locale));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_strategy));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_source_name));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_resource_id));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_amc_url_path));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_server));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_port));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.preference_token_url_path));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_key_a));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_value_a));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_key_b));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_value_b));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_key_c));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.attribute_value_c));
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Added to avoid crash which might come when Activity is not yet added but this callback is invoked and getString function gets invoked.
            if(getActivity() == null || !isAdded())
                return;
            // Configure logger depending on keys
            if (key.equals(getString(R.string.preference_log_to_device)) || key.equals(getString(R.string.preference_log_file_name))) {
                boolean logToDisk = sharedPreferences.getBoolean(getString(R.string.preference_log_to_device), false);
                String logFileName = sharedPreferences.getString(getString(R.string.preference_log_file_name), "");
                Logger.configure(logToDisk, logFileName);
            }

            // Only show summary for string|numeric values, not booleans
            if (!key.equals(getString(R.string.preference_secure_login)) && !key.equals(getString(R.string.preference_log_to_device))
                    && !key.equals(getString(R.string.preference_token_secure))) {
                Preference preference = findPreference(key);
                preference.setSummary(sharedPreferences.getString(key, ""));
            }



        }
    }
}



