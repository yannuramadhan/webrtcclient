/**
 * AudioInteractionActivity.java <br>
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * MOBILE_VIDEO_SDK_LICENSE_AGREEMENT.txt, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.interactions;

import android.content.res.Configuration;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.ViewHandler;
import com.avaya.ocs.Services.Device.Video.Enums.CallQuality;
import com.avaya.ocs.Services.Device.Video.Enums.CameraType;
import com.avaya.ocs.Services.Device.Video.Enums.VideoCaptureOrientation;
import com.avaya.ocs.Services.Device.Video.Enums.VideoCapturePreference;
import com.avaya.ocs.Services.Device.Video.VideoDevice;
import com.avaya.ocs.Services.Device.Video.VideoSurfaceView;
import com.avaya.ocs.Services.Statistics.AudioDetails;
import com.avaya.ocs.Services.Statistics.Callbacks.AudioDetailsCallback;
import com.avaya.ocs.Services.Statistics.Callbacks.VideoDetailsCallback;
import com.avaya.ocs.Services.Statistics.VideoDetails;
import com.avaya.ocs.Services.Work.Enums.DTMFTone;
import com.avaya.ocs.Services.Work.Enums.InteractionState;
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction;
import com.avaya.ocs.Services.Work.Interactions.Interaction;
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener;
import com.avaya.ocs.Services.Work.Interactions.Listeners.VideoInteractionListener;
import com.avaya.ocs.Services.Work.Interactions.VideoInteraction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VideoInteractionActivity extends AbstractInteractionActivity implements ViewHandler, VideoInteractionListener {

    private static final String TAG = VideoInteractionActivity.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private VideoInteraction mVideoInteraction;
    private VideoDevice mVideoDevice;

    private ImageButton mMicrophoneButton;
    private ImageButton mCameraButton;
    private ImageButton mEnableVideoButton;
    private ImageButton mSwapCamera;
    private ConstraintLayout mMainLayout;
    private VideoSurfaceView mRemoteLayout;
    private VideoSurfaceView mLocalLayout;
    private TextView mVideoDisabledText;
    private ExecutorService executorService;
    private Runnable longRunningTask;
    private long poorQualityStartTime = -1;
    private double previousMOSValue = -1;

    @Override
    public int view() {
        return R.layout.activity_video_call;
    }

    @Override
    public int getInteractionType() {
        return INTERACTION_VIDEO;
    }

    @Override
    public AbstractInteraction getInteraction() {
        return mVideoInteraction;
    }


    @Override
    protected void registerListener() {
        mVideoInteraction.registerListener(this);
    }

    @Override
    protected Interaction createInteraction(OnAudioDeviceChangeListener listener) {
        mVideoInteraction = InteractionService.getInstance().createVideoInteraction(listener);
//        mVideoInteraction.disableVideoInLowBandwidth(true,CallQuality.FAIR,10000l);
        mVideoDevice = mVideoInteraction.getVideoDevice();
        setupVideoDevice();
        executorService = Executors.newSingleThreadExecutor();

        // TODO: For testing pre-call video disable/mute and audio mute
        /**
         configureAudioMuteState(true);
         configureVideoMuteState(true);
         configureVideoEnabled(false);
         */
        setOnTouchListener();
        return mVideoInteraction;
    }


    @Override
    protected void updateCallState() {
        try {
            if (mVideoInteraction != null) {
                InteractionState state = mVideoInteraction.getInteractionState();
                updateCallState(state.toString());
            }
        } catch (Exception e) {
            mLogger.e("Exception getting interaction state, " + e.getMessage(), e);
        }
    }

    @Override
    protected void sendDtmf(DTMFTone tone) {
        if (mVideoInteraction != null) {
            mVideoInteraction.sendDtmf(tone);
        }
    }

    @Override
    public void hangup() {
        if (canHungup()) {
            mLogger.d("hanging up");
            setCanHungup(false);

            try {
                dismissDtmf();
                if (mVideoInteraction != null) {
                    mVideoInteraction.end();
                    mVideoInteraction.discard();
                } else {
                    mLogger.w("Video Interaction is null");
                }
            } catch (Exception e) {
                mLogger.e("Exception in finish " + e.getMessage(), e);
                showToast(this, "Incomplete call cleanup sequence", Toast.LENGTH_LONG);
            }
        }
    }


    @Override
    public void finish() {
        try {
            if (mVideoInteraction != null) {
                mVideoInteraction.unregisterListener(this);
                mVideoInteraction.unregisterConnectionListener(this);
            } else {
                mLogger.w("Video Interaction is null - cannot unregister listeners");
            }
        } catch (Exception e) {
            mLogger.e("Exception in finish " + e.getMessage(), e);
            showToast(this, "Incomplete listener cleanup sequence", Toast.LENGTH_LONG);
        }

        super.finish();
    }

    private void setupVideoDevice() {
        try {
            if (mMainLayout != null) {
                mLogger.d("Creating video surfaces");

                // We need to keep the screen on to prevent it sleeping during video calls
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                mVideoDevice.setVideoSurface(mMainLayout);
            } else {
                mLogger.e("Cannot create video surface - main layout is null");
            }


        } catch (Exception e) {
            mLogger.e("Exception in setupVideoDevice", e);
            displayMessage("Error creating video device: " + e.getMessage());
        }
    }

    @Override
    public void onInteractionRemoteAlerting() {
        super.onInteractionRemoteAlerting();
        //If we set the Resolution and Orientation at the setup phase, CSDK sometimes throws an error because the VideoDevice object is not yet completely initialized and thus setVideoCaptureResolutionWithCaptureOrientation does not work
        try {
            VideoCapturePreference videoCapturePreference = (VideoCapturePreference) getIntent().getExtras().get(Constants.DATA_KEY_VIDEO_RESOLUTION);
            VideoCaptureOrientation videoCaptureOrientation = (VideoCaptureOrientation) getIntent().getExtras().get(Constants.DATA_KEY_VIDEO_ORIENTATION);
            mVideoDevice.setVideoCaptureResolutionWithCaptureOrientation(videoCapturePreference, videoCaptureOrientation);
        } catch (Exception e) {
            mLogger.e("Exception in setVideoCaptureResolutionWithCaptureOrientation", e);
            displayMessage("Error updating video device: " + e.getMessage());
        }
    }

    @Override
    public void onInteractionAudioMuteStatusChanged(boolean state) {
        mLogger.d("VideoInteraction:onInteractionAudioMuteStatusChanged, state - " + state);
        if (state) {
            mMicrophoneButton.setImageResource(R.drawable.ic_activecall_mute_active);

        } else {
            mMicrophoneButton.setImageResource(R.drawable.ic_activecall_mute);
        }

    }

    @Override
    public void onInteractionVideoMuteStatusChanged(boolean state) {
        mLogger.d("VideoInteraction:onInteractionVideoMuteStatusChanged, state - " + state);
        if (state) {
            mCameraButton.setImageResource(R.drawable.ic_activecall_video);
        } else {
            mCameraButton.setImageResource(R.drawable.ic_activecall_video_active);
        }
    }

    @Override
    public void onInteractionVideoEnabledStatusChanged(boolean videoEnabledState) {
        mLogger.d("VideoInteraction:onInteractionVideoEnabledStatusChanged, state - " + videoEnabledState);
        if (videoEnabledState) {
            mEnableVideoButton.setImageResource(R.drawable.ic_videocall_selfview_blockcamera);
            mCameraButton.setVisibility(View.VISIBLE);
            mSwapCamera.setVisibility(View.VISIBLE);
            mLocalLayout.setVisibility(View.VISIBLE);
            mRemoteLayout.setVisibility(View.VISIBLE);
            mVideoDisabledText.setVisibility(View.INVISIBLE);
        } else {
            mEnableVideoButton.setImageResource(R.drawable.ic_videocall_selfview_blockcamera_active);
            mCameraButton.setVisibility(View.INVISIBLE);
            mSwapCamera.setVisibility(View.INVISIBLE);
            mLocalLayout.setVisibility(View.GONE);
            mRemoteLayout.setVisibility(View.GONE);
            mVideoDisabledText.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    public void onInteractionVideoDisabledBelowThreshold(CallQuality callQuality) {
//        mLogger.d("VideoInteraction:onInteractionVideoDisabledBelowThreshold, state - " + callQuality.getValue());
//
//        Toast.makeText(this,R.string.video_disabled_due_to_unacceptable_quality,Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onInteractionQualityChanged(CallQuality callQuality) {
        VideoInteractionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderCallQuality(callQuality);
            }
        });
    }

//    @Override
//    public void onInteractionVideoCanBeEnabledThresholdCrossed(CallQuality callQuality) {
//        mLogger.d("VideoInteraction:onInteractionVideoCanBeEnabledThresholdCrossed, state - " + callQuality.getValue());
//
//        Toast.makeText(this,R.string.video_can_be_enabled_threshold_crossed,Toast.LENGTH_SHORT).show();
//        if (mVideoInteraction != null) {
//            mVideoInteraction.enableVideo(true);
//        }
//
//    }

    protected void configureAudioMuteState(boolean muteAudio) {
        mLogger.d("entering configureAudioMuteStatus");
        if (mVideoInteraction != null) {
            mVideoInteraction.muteAudio(muteAudio);
        }
    }

    protected void configureVideoMuteState(Boolean muteVideo) {
        mLogger.d("entering configureVideoMuteStatus");
        if (mVideoInteraction != null) {
            mVideoInteraction.muteVideo(muteVideo);
        }
    }

    protected void configureVideoEnabled(Boolean enableVideo) {
        mLogger.d("entering configureVideoEnabled");
        if (mVideoInteraction != null) {
            mVideoInteraction.enableVideo(enableVideo);
        }
    }

    @Override
    public void getElementReferences() {
        mMicrophoneButton = findViewById(R.id.imageButtonMuteMic);
        mCameraButton = findViewById(R.id.imageButtonMuteCamera);
        mEnableVideoButton = findViewById(R.id.imageButtonEnableCamera);
        mSwapCamera = findViewById(R.id.imageButtonSwapCamera);
        callTimeTextView = findViewById(R.id.textViewTimer);
        mRemoteLayout = findViewById(R.id.remoteLayout);
        mMainLayout = findViewById(R.id.mainLayout);
        mLocalLayout = findViewById(R.id.localLayout);
        mVideoDisabledText = findViewById(R.id.textVideoDisabled);
        audioDeviceButton = findViewById(R.id.audioDeviceButton);
        textViewCallQuality = findViewById(R.id.textViewCallQuality);
        ivCallQualityRating = findViewById(R.id.ivCallQualityRating);

    }

    @Override
    public void attachEventHandlers() {
        mMicrophoneButton.setOnClickListener(view -> {
            mLogger.d("mute audio pressed");

            if (mVideoInteraction != null) {
                Boolean isMuted = mVideoInteraction.isAudioMuted();
                mLogger.i("Is audio muted: " + isMuted);
                configureAudioMuteState(!isMuted);
            } else {
                mLogger.w("Audio Interaction is null");
            }
        });
        mCameraButton.setOnClickListener(view -> {
            mLogger.d("mute video pressed");

            if (mVideoInteraction != null) {
                Boolean isMuted = mVideoInteraction.isVideoMuted();
                mLogger.i("Is video muted: " + isMuted);
                configureVideoMuteState(!isMuted);
            } else {
                mLogger.w("Video Interaction is null");
            }
        });
        mEnableVideoButton.setOnClickListener(v -> {
            mLogger.d("enable video pressed");
            if (mVideoInteraction != null) {
                Boolean isEnabled = mVideoInteraction.isVideoEnabled();
                mLogger.i("Is video enabled: " + isEnabled);
                configureVideoEnabled(!isEnabled);
            } else {
                mLogger.w("Video Interaction is null");
            }
        });
        mSwapCamera.setOnClickListener(v -> {
            mLogger.d("swap camera pressed");
            if (mVideoDevice != null) {
                CameraType selectedCamera = mVideoDevice.getSelectedCamera();
                if (selectedCamera.equals(CameraType.FRONT)) {
                    mVideoDevice.selectCamera(CameraType.BACK);
                } else {
                    mVideoDevice.selectCamera(CameraType.FRONT);
                }
            } else {
                mLogger.w("Video Device is null");
            }
        });
    }

    @Override
    protected long getInteractionTimeElapsed() {
        return mVideoInteraction.getInteractionTimeElapsed();
    }

    private void setOnTouchListener() {
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showBottomControlsWithAnimation();
                return false;
            }
        };
        mLocalLayout.setOnTouchListener(onTouchListener);
        mRemoteLayout.setOnTouchListener(onTouchListener);
        View mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(onTouchListener);

    }

    private void showBottomControlsWithAnimation() {
        View view = findViewById(R.id.call_options_layout);
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);

        }
        removeCallbacks();

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return;
        longRunningTask = new Runnable() {
            @Override
            public void run() {
                View view = findViewById(R.id.call_options_layout);
                view.setVisibility(View.GONE);

            }
        };

        view.postDelayed(longRunningTask, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeCallbacks();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        removeCallbacks();
        showBottomControlsWithAnimation();
    }

    private void removeCallbacks() {
        View view = findViewById(R.id.call_options_layout);
        if (view != null && longRunningTask != null) {
            view.removeCallbacks(longRunningTask);
        }
    }

    private void showBottomControlsView() {
        View view = findViewById(R.id.call_options_layout);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showBottomControlsWithAnimation();
    }



}
