/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.application;

import android.app.Activity;
import android.content.Context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.fragment.app.FragmentActivity;
import androidx.multidex.MultiDexApplication;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.interactions.InteractionService;
import com.avaya.oceanareferenceclient.settings.AbstractSettingsService;
import com.avaya.oceanareferenceclient.settings.SettingsService;
import com.avaya.oceanareferenceclient.settings.SettingsServiceElite;
import com.avaya.oceanareferenceclient.settings.pojos.LoggingPreferences;
import com.avaya.oceanareferenceclient.utils.Logger;

public class OceanaApplication extends MultiDexApplication {
    private static final String TAG = OceanaApplication.class.getSimpleName();
    private static Context appContext;
    private Logger mLogger = Logger.getLogger(TAG);

    private FragmentActivity mCurrentActivity = null;

    private AbstractSettingsService settingsService;

    public FragmentActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(FragmentActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public void onCreate() {
        super.onCreate();
        mLogger.d("onCreate()");

        initServices();

        // set up handler for uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    public void initServices() {
        Context context = getApplicationContext();
        settingsService = getSettingsService(context);
        Logger.setApplicationContext(context);
        configureLogger(settingsService);
        InteractionService.getInstance().init(this, settingsService);
    }

    public AbstractSettingsService getSettingsService(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String platformType = sharedPref.getString(context.getString(R.string.platformType), context.getString(R.string.platformTypeOceana)).trim();
        if (platformType.equals(context.getString(R.string.platformTypeOceana))) {
            return new SettingsService(context);
        } else {
            return new SettingsServiceElite(context);
        }
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        mLogger.e("Uncaught exception thrown", e);

        launchReportIssue();

        System.exit(1); // kill off the crashed app
    }

    public void launchReportIssue() {
        mLogger.d("launchReportIssue");
        try {
            if (mCurrentActivity != null) {
                mLogger.i("Report issue");
                IssueReporter issueReporter = new IssueReporter(mCurrentActivity, getString(R.string.app_name), true);
                issueReporter.reportIssue();
            }
        } catch (Exception e) {
            mLogger.e("Exception in launchReportIssue", e);
        }
    }

    public void configureLogger(AbstractSettingsService settingsService) {
        mLogger.d("configureLogger");
        LoggingPreferences loggingPreferences = settingsService.retrieveLoggingPreferences();

        boolean logToDisk = loggingPreferences.isLogToDiskEnabled();
        String logFileName = loggingPreferences.getLogFileName();

        Logger.configure(logToDisk, logFileName);
    }

}
