/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.application;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.FileUtils;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.GeneralDialogFragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IssueReporter {

    private static final String TAG = IssueReporter.class.getSimpleName();
    private static final String FILE_PREFIX = "file://";
    private Logger mLogger = Logger.getLogger(TAG);

    private FragmentActivity mActivity;
    private String mEmailAddress;
    private String mLogFileName;
    private String mCrashLogFileName;
    private String mIntentTitle;
    private String mAppVersion;

    public IssueReporter(FragmentActivity activity, String appVersion) {
        this(activity, appVersion, false);
    }

    public IssueReporter(FragmentActivity activity, String appVersion, boolean uncaughtException) {
        mActivity = activity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String platformType = sharedPref.getString(mActivity.getString(R.string.platformType), mActivity.getString(R.string.platformTypeOceana)).trim();
        if(platformType.equals(mActivity.getString(R.string.platformTypeElite))) {
            mCrashLogFileName = activity.getResources().getString(R.string.issue_log_file_name_elite);
            mEmailAddress = sharedPref.getString(activity.getResources().getString(R.string.preference_email_address_elite), "");
            mLogFileName = sharedPref.getString(activity.getResources().getString(R.string.preference_log_file_name_elite), "");
        }else{
            mCrashLogFileName = activity.getResources().getString(R.string.issue_log_file_name);
            mEmailAddress = sharedPref.getString(activity.getResources().getString(R.string.preference_email_address), "");
            mLogFileName = sharedPref.getString(activity.getResources().getString(R.string.preference_log_file_name), "");
        }
        mAppVersion = (appVersion == null ? "" : appVersion);

        if (uncaughtException) {
            mIntentTitle = mActivity.getResources().getString(R.string.uncaught_exception_email_log_files);
        } else {
            mIntentTitle = mActivity.getResources().getString(R.string.email_log_files);
        }
    }

    public void reportIssue() {
        try {
            mLogger.d("Report issue");

            boolean sendEmail = true;

            Intent emailIntent;

            String idTag = "[" + UUID.randomUUID() + "]";
            String subject = Constants.REPORT_ISSUE_SUBJECT.replace(Constants.PLACEHOLDER1, idTag);

            String regularLogFile = FileUtils.getExternalStoragePathFile(mActivity) + File.separator + mLogFileName + ".%d.log";
            boolean regularLogFileExists = false;

            String crashLog = extractLogToFile();

            File logFile = new File(regularLogFile);
            if (logFile.exists()) {
                regularLogFileExists = true;
            }

            if (crashLog != null) {
                List<String> filePaths = new ArrayList<String>();

                if (regularLogFileExists) {
                    mLogger.d("Send regular App and SDK log files");
                    filePaths.add(regularLogFile);
                } else {
                    mLogger.i("Regular App log file doesn't exist, send SDK logs only");
                }

                filePaths.add(crashLog);

                emailIntent = new Intent(
                        Intent.ACTION_SEND_MULTIPLE);

                ArrayList<Uri> uris = new ArrayList<Uri>();

                for (String file : filePaths) {
                    File fileIn = new File(file);
                    Uri uri = FileProvider.getUriForFile(mActivity, "com.avaya.oceanareferenceclient.provider", fileIn);
                    uris.add(uri);
                }
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                mLogger.i("Can't send crash logs, send regular app logs only");
                emailIntent = new Intent(
                        Intent.ACTION_SEND);

                if (regularLogFileExists) {
                    Uri contentUri = FileProvider.getUriForFile(mActivity, "com.avaya.oceanareferenceclient.provider", logFile);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(FILE_PREFIX + regularLogFile));
                } else {
                    mLogger.w("No log files to send");
                    sendEmail = false;
                }
            }

            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{mEmailAddress});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                    subject);

            if (sendEmail) {
                mActivity.startActivity(Intent.createChooser(emailIntent, mIntentTitle));
            } else {
                mLogger.i("Neither log file could be found or generated, nothing to send");
                GeneralDialogFragment.displayMessage(mActivity, R.string.no_log_files_found);
            }
        } catch (Throwable t) {
            mLogger.e("reportIssue exception", t);

            mActivity.runOnUiThread(() -> {
                Toast.makeText(mActivity,"Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
            });
        }
    }

    protected String extractLogToFile() {
        String fullName;
        InputStreamReader reader = null;
        FileWriter writer = null;

        try {
            fullName = FileUtils.getExternalStoragePathFile(mActivity) + File.separator + mCrashLogFileName;
            File file = new File(fullName);

            String cmd = "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            addDeviceInformation(writer);

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);
        } catch (IOException e) {
            mLogger.e("IOException extractLogToFile", e);
            fullName = null;
        } catch (Exception e) {
            mLogger.e("Exception extractLogToFile", e);
            fullName = null;
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    mLogger.w("Exception closing resource");
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                    mLogger.w("Exception closing resource");
                }
        }

        return fullName;
    }

    private void addDeviceInformation(FileWriter writer) throws IOException, PackageManager.NameNotFoundException {
        String model = Build.MODEL;

        if (!model.startsWith(Build.MANUFACTURER)) {
            model = Build.MANUFACTURER + " " + model;
        }

        writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
        writer.write("Device: " + model + "\n");
        writer.write("App version: " + mAppVersion + "\n");
    }
}