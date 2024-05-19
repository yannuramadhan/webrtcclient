/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.interactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.application.ForegroundService;
import com.avaya.oceanareferenceclient.application.OceanaApplication;
import com.avaya.oceanareferenceclient.click2call.CallsStatisticsFragment;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.GeneralDialogFragment;
import com.avaya.ocs.Exceptions.AuthorizationTokenException;
import com.avaya.ocs.Services.Work.Enums.AudioDeviceError;
import com.avaya.ocs.Services.Work.Enums.AudioDeviceType;
import com.avaya.ocs.Services.Work.Enums.DTMFTone;
import com.avaya.ocs.Services.Work.Enums.InteractionError;
import com.avaya.ocs.Services.Work.Enums.InteractionState;
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction;
import com.avaya.ocs.Services.Work.Interactions.Interaction;
import com.avaya.ocs.Services.Work.Interactions.Listeners.AudioInteractionListener;
import com.avaya.ocs.Services.Work.Interactions.Listeners.ConnectionListener;
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener;

import java.util.ArrayList;
import java.util.List;

import com.avaya.ocs.Services.Device.Video.Enums.CallQuality;
public abstract class AbstractInteractionActivity extends FragmentActivity implements AudioInteractionListener,
        ConnectionListener, OnAudioDeviceChangeListener {

    public static final int INTERACTION_AUDIO = 1;
    public static final int INTERACTION_VIDEO = 2;

    private static final String TAG = AbstractInteractionActivity.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    // reference common application elements
    protected OceanaApplication app;

    // reference common UI elements
    private TextView callProgressState;
    private PopupWindow dtmfPopupWindow = null;

    protected String token;

    // Call timer implementation
    private Handler mTimerHandler;

    private boolean canHangup = true;

    private AudioDeviceType currentAudioDevice;

    private ImageButton holdButton;

    protected TextView callTimeTextView;
    protected ImageButton audioDeviceButton;

    protected TextView textViewCallQuality;
    protected ImageView ivCallQualityRating;

    protected Interaction mInteraction;


    // abstract methods that implementing interaction classes should implement.
    protected abstract int view();

    public abstract AbstractInteraction getInteraction();

    public abstract int getInteractionType();

    protected abstract void updateCallState();

    protected abstract void sendDtmf(DTMFTone tone);

    public abstract void hangup();

    protected boolean canHungup() {
        return canHangup;
    }

    protected void setCanHungup(boolean canHangup) {
        this.canHangup = canHangup;
    }

    private Runnable mCallTimeChecker = new Runnable() {
        @Override
        public void run() {
            updateCallTime();
            mTimerHandler.postDelayed(mCallTimeChecker, Constants.TIMER_INTERVAL);
        }
    };

    protected void handleClickToCall() {
        try {
            mInteraction = createInteraction(this);
            mInteraction.setAuthorizationToken(token);
            registerListener();
            mInteraction.registerConnectionListener(this);
            try {
                mInteraction.start();
                setCanHungup(true);
                attachEventHandlers();
            } catch (AuthorizationTokenException e) {
                mLogger.e("Exception starting Audio Interaction: " + e.getMessage(), e);
                showToast(this, "Error with Authorization token!", Toast.LENGTH_LONG);
            }
        } catch (Exception ex) {
            mLogger.e("Exception in handleClickToCall " + ex.getMessage(), ex);
        }
    }

    protected abstract void attachEventHandlers();

    protected abstract void registerListener();

    protected abstract Interaction createInteraction(OnAudioDeviceChangeListener listener);

    protected void startCallTimer() {
        stopCallTimer();
        mLogger.d("starting call timer");
        try {
            mCallTimeChecker.run();
        } catch (Exception e) {
            mLogger.e("Exception in startCallTimer", e);
        }
    }

    protected void stopCallTimer() {
        mLogger.d("stopping call timer");
        try {
            mTimerHandler.removeCallbacks(mCallTimeChecker);
        } catch (Exception e) {
            mLogger.e("Exception in stopCallTimer", e);
        }
    }
    // End of call timer implementation

    // Android lifecycle implementation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view());
        initToolbar();
        token = getIntent().getExtras().getString(Constants.DATA_KEY_TOKEN);

        // Delegate to the OceanaApplication class to handle uncaught exceptions that could cause app crashes
        app = (OceanaApplication) getApplication();
        app.setCurrentActivity(this);

        callProgressState = findViewById(R.id.textViewCallState);
        ImageButton mDtmfButton = findViewById(R.id.imageButtonLaunchNumberPad);
        mDtmfButton.setOnClickListener(view -> {
            mLogger.d("DTMF pressed");

            if (getDtmfPopupWindow() == null) {
                launchDtmf(view);
                mDtmfButton.setImageResource(R.drawable.ic_activecall_dtmf_active);
            } else {
                dismissDtmf();
                mDtmfButton.setImageResource(R.drawable.ic_activecall_dtmf);
            }
        });
        ImageButton mEndCallButton = findViewById(R.id.imageButtonEndCall);
        mEndCallButton.setOnClickListener(view -> {
            mLogger.d("end pressed");
            hangup();
        });
        holdButton = findViewById(R.id.holdButton);
        holdButton.setOnClickListener(view -> {
            mLogger.d("hold/resume is pressed");
            if (mInteraction.getInteractionState() == InteractionState.HELD) {
                mInteraction.unhold();
            } else {
                mInteraction.hold();
            }
        });



        // create a handler for the call timer
        mTimerHandler = new Handler();

        getElementReferences();
        audioDeviceButton.setEnabled(false);
        audioDeviceButton.setOnClickListener(view -> handleChangeAudioDevice());

        handleClickToCall();
        //If this is not implemented then after phone is put on sleep mode, the call remains active however audio transmission to other side stops after a few seconds  until you wake up the screen again.
        startService(new Intent(this, ForegroundService.class));

    }

    // Video Interaction callbacks
    @Override
    public void onInteractionInitiating() {
        mLogger.d("AbstractInteraction:onInteractionInitiating");
        updateCallState();
    }

    @Override
    public void onInteractionRemoteAlerting() {
        mLogger.d("AbstractInteraction:onInteractionRemoteAlerting");
        runOnUiThread(this::startCallTimer);
        updateCallState();
    }


    @Override
    public void onInteractionActive() {
        mLogger.d("AbstractInteraction:onInteractionActive");
        updateCallState();
    }

    @Override
    public void onInteractionEnded() {
        mLogger.d("AbstractInteraction:onInteractionEnded");
        updateCallState();
        hangup();
    }

    @Override
    public void onInteractionHeld() {
        holdButton.setEnabled(!mInteraction.isHeldRemotely());
        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold_active);
        updateCallState();
    }

    @Override
    public void onInteractionUnheld() {
        holdButton.setImageResource(R.drawable.ic_activecall_advctrl_hold);
        updateCallState();
    }

    @Override
    public void onInteractionHeldRemotely() {
        //Take appropriate action
    }

    @Override
    public void onInteractionUnheldRemotely() {
        //Take appropriate action
    }


    @Override
    public void onInteractionFailed(InteractionError error) {
        mLogger.d("Interaction failed, error - " + error);
        updateCallState();
        displayMessage(error.toString(), true);
    }

    @Override
    public void onDiscardComplete() {
        mLogger.d("AbstractInteraction:onInteractionEnded");
        finish();
    }

    protected void updateCallTime() {
        try {
            long callTimeElapsed = getInteractionTimeElapsed();
            int callTimeSeconds;
            if (callTimeElapsed == -1L) {
                callTimeSeconds = 0;
            } else {
                callTimeSeconds = (int) (callTimeElapsed / 1000);
            }

            int seconds = callTimeSeconds % 60;
            int minutes = ((callTimeSeconds - seconds) / 60);

            final String time = Constants.CALL_TIME_ELAPSED_SEPARATOR
                    + String.format(Constants.CALL_TIME_ELAPSED_FORMAT, minutes, seconds)
                    + Constants.CALL_TIME_ELAPSED_END;
            runOnUiThread(() -> callTimeTextView.setText(time));


        } catch (Exception e) {
            mLogger.e("Exception in updateCallTime", e);
        }
    }

    protected abstract long getInteractionTimeElapsed();

    protected abstract void getElementReferences();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLogger.d("onBackPressed()");
        hangup();
    }

    @Override
    public void finish() {
        super.finish();
        runOnUiThread(this::stopCallTimer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLogger.d("onStop()");
        dismissDtmf();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLogger.d("onDestroy()");
        hangup();
        stopService(new Intent(this, ForegroundService.class));

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLogger.d("onConfigurationChanged()");
    }

    // UI helper methods
    protected void updateCallState(final String state) {
        runOnUiThread(() -> callProgressState.setText(state));
    }

    protected void showToast(Activity activity, String message, int length) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(activity.getApplicationContext(), message, length);
            toast.show();
        });
    }

    /**
     * Display a message
     *
     * @param message message to display
     */
    protected void displayMessage(final String message) {
        displayMessage(message, false);
    }

    /**
     * Display a message and finishes the current activity
     *
     * @param message message to display
     */
    protected void displayMessage(final String message, final boolean finishActivity) {
        mLogger.d("Display message: " + message);
        final FragmentActivity activity = this;

        try {
            runOnUiThread(() -> GeneralDialogFragment.displayMessage(activity, message, finishActivity));

        } catch (Exception e) {
            mLogger.e("Exception in displayMessage", e);
        }
    }
    // End of UI helper methods

    // DTMF keypad implementation
    protected PopupWindow getDtmfPopupWindow() {
        return dtmfPopupWindow;
    }

    protected void launchDtmf(View v) {
        mLogger.d("Pop a DTMF window");

        try {
            if (dtmfPopupWindow == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.dtmf_popup, null);
                dtmfPopupWindow = new PopupWindow(
                        popupView,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                dtmfPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);//-v.getHeight(), v.getWidth());
            }
        } catch (Exception e) {
            mLogger.e("Exception in launchDtmf", e);
            displayMessage("Launch DTMF exception: " + e.getMessage());
        }

    }

    protected void dismissDtmf() {
        if (dtmfPopupWindow != null) {
            dtmfPopupWindow.dismiss();
        }

        dtmfPopupWindow = null;
    }

    public void dtmf(View v) {
        try {
            String tone = (String) v.getTag();
            mLogger.d("DTMF: " + tone);
            sendDtmf(DTMFTone.get(tone));
        } catch (Exception e) {
            mLogger.e("Exception in dtmf", e);
            displayMessage("DTMF exception: " + e.getMessage());
        }
    }

    @Override
    public void onInteractionServiceConnecting() {
        mLogger.d("onInteractionServiceConnecting - Call signalling interrupted");
    }

    @Override
    public void onInteractionServiceConnected() {
        mLogger.d("onInteractionServiceConnected - Call signalling available");
    }

    @Override
    public void onInteractionServiceDisconnected(InteractionError interactionError) {
        mLogger.d("onInteractionServiceDisconnected with error [" + interactionError + "] - Call signalling not recoverable");
    }

    @Override
    public void onAudioDeviceListChanged(List<AudioDeviceType> devices) {
        mLogger.d("onAudioDeviceListChanged");
    }

    @Override
    public void onAudioDeviceChanged(AudioDeviceType audioDeviceType) {
        mLogger.d("onAudioDeviceChanged " + audioDeviceType);
        audioDeviceButton.setEnabled(true);
        currentAudioDevice = audioDeviceType;
        audioDeviceButton.setImageResource(getAudioDeviceIcon(audioDeviceType));
    }

    @Override
    public void onAudioDeviceError(AudioDeviceError audioDeviceError) {
        mLogger.d("onAudioDeviceError " + audioDeviceError);
    }

    private void handleChangeAudioDevice() {
        List<AudioDeviceType> switchList = getDevicesToSwitch(mInteraction.getAvailableAudioDevices(), currentAudioDevice);
        if (switchList != null && !switchList.isEmpty()) {
            if (switchList.size() == 1) {
                mInteraction.setAudioDevice(switchList.get(0));
            } else {
                showSelectAudioDeviceDialog(switchList);
            }
        } else {
            Toast.makeText(this, R.string.no_device_to_switch, Toast.LENGTH_SHORT).show();
        }
    }

    private List<AudioDeviceType> getDevicesToSwitch(List<AudioDeviceType> availableDevices, AudioDeviceType currentDevice) {
        List<AudioDeviceType> toSwitch = new ArrayList<>();
        for (AudioDeviceType device : availableDevices) {
            if (device != currentDevice) toSwitch.add(device);
        }
        return toSwitch;
    }

    private void showSelectAudioDeviceDialog(List<AudioDeviceType> devices) {
        List<String> labels = new ArrayList<>();
        for (AudioDeviceType device : devices) {
            labels.add(getAudioDeviceLabel(device));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_audio_device);
        builder.setItems(labels.toArray(new String[0]), (dialog, index) -> mInteraction.setAudioDevice(devices.get(index)));
        builder.show();
    }

    private String getAudioDeviceLabel(AudioDeviceType type) {
        switch (type) {
            case WIRED_HEADSET:
                return getString(R.string.headset);
            case BLUETOOTH_HEADSET:
                return getString(R.string.bt_headset);
            case HANDSET:
                return getString(R.string.ear_speaker);
            case SPEAKER:
                return getString(R.string.speaker);
            default:
                return "";
        }
    }

    private @DrawableRes
    int getAudioDeviceIcon(AudioDeviceType type) {
        switch (type) {
            case WIRED_HEADSET:
                return R.drawable.ic_persistent_audioselect_headset;
            case BLUETOOTH_HEADSET:
                return R.drawable.ic_persistent_audioselect_bluetooth;
            case HANDSET:
                return R.drawable.ic_persistent_audioselect_handset;
            default:
                return R.drawable.ic_activecall_speaker_off;
        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.in_call_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_call_statistics) {
                    showStatsDialog();
                }
                return false;
            }
        });
    }

    private void showStatsDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new CallsStatisticsFragment();
        dialog.show(getSupportFragmentManager(), "CallStatisticsFragment");
    }




    protected void renderCallQuality(CallQuality quality) {
        int callQualityRatingResource;
        int callQualityText;
        if (quality == CallQuality.EXCELLENT) {
            callQualityRatingResource = R.drawable.ic_networkquality_5;
            callQualityText = R.string.Excellent;
        } else if (quality == CallQuality.GOOD) {
            callQualityRatingResource = R.drawable.ic_networkquality_4;
            callQualityText = R.string.Good;
        } else if (quality == CallQuality.FAIR) {
            callQualityRatingResource = R.drawable.ic_networkquality_3;
            callQualityText = R.string.Fair;
        } else if (quality == CallQuality.POOR) {
            callQualityRatingResource = R.drawable.ic_networkquality_2;
            callQualityText = R.string.Poor;
        } else if (quality == CallQuality.BAD) {
            callQualityRatingResource = R.drawable.ic_networkquality_1;
            callQualityText = R.string.Bad;
        } else {
            callQualityText = R.string.No_Network;
            callQualityRatingResource = R.drawable.ic_networkquality_0;
        }

        ivCallQualityRating.setImageResource(callQualityRatingResource);

        textViewCallQuality.setText(callQualityText);

    }


}
