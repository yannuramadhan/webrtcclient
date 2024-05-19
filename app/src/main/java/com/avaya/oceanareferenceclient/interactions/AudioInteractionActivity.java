/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.interactions;

import android.widget.ImageButton;
import android.widget.Toast;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.ViewHandler;
import com.avaya.ocs.Services.Device.Video.Enums.CallQuality;
import com.avaya.ocs.Services.Work.Enums.DTMFTone;
import com.avaya.ocs.Services.Work.Enums.InteractionState;
import com.avaya.ocs.Services.Work.Interactions.AbstractInteraction;
import com.avaya.ocs.Services.Work.Interactions.AudioInteraction;
import com.avaya.ocs.Services.Work.Interactions.Interaction;
import com.avaya.ocs.Services.Work.Interactions.Listeners.OnAudioDeviceChangeListener;

public class AudioInteractionActivity extends AbstractInteractionActivity implements ViewHandler {

    private static final String TAG = AudioInteractionActivity.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private AudioInteraction mAudioInteraction;

    private ImageButton mMicrophoneButton;

    @Override
    public int view() {
        return R.layout.activity_audio_call;
    }

    @Override
    public int getInteractionType() {
        return INTERACTION_AUDIO;
    }

    @Override
    public AbstractInteraction getInteraction() {
        return mAudioInteraction;
    }


    protected void registerListener() {
        mAudioInteraction.registerListener(this);
    }

    @Override
    protected Interaction createInteraction(OnAudioDeviceChangeListener listener) {
        mAudioInteraction = InteractionService.getInstance().createAudioInteraction(listener);
        return mAudioInteraction;
    }

    @Override
    protected void updateCallState() {
        try {
            if (mAudioInteraction != null) {
                InteractionState state = mAudioInteraction.getInteractionState();
                updateCallState(state.toString());
            }
        } catch (Exception e) {
            mLogger.e("Exception getting interaction state, " + e.getMessage(), e);
        }
    }

    @Override
    protected void sendDtmf(DTMFTone tone) {
        if (mAudioInteraction != null) {
            mAudioInteraction.sendDtmf(tone);
        }
    }

    @Override
    public void hangup() {
        if (canHungup()) {
            mLogger.d("hanging up");
            setCanHungup(false);

            try {
                dismissDtmf();
                if (mAudioInteraction != null) {
                    mAudioInteraction.end();
                    mAudioInteraction.discard();
                } else {
                    mLogger.w("Audio Interaction is null");
                }
            } catch (Exception e) {
                mLogger.e("Exception in hangup " + e.getMessage(), e);
                showToast(this, "Incomplete call cleanup sequence", Toast.LENGTH_LONG);
            }
        }
    }


    @Override
    public void finish() {
        try {
            if (mAudioInteraction != null) {
                mAudioInteraction.unregisterListener(this);
                mAudioInteraction.unregisterConnectionListener(this);
            } else {
                mLogger.w("Audio Interaction is null - cannot unregister listeners");
            }
        } catch (Exception e) {
            mLogger.e("Exception in finish " + e.getMessage(), e);
            showToast(this, "Incomplete listener cleanup sequence", Toast.LENGTH_LONG);
        }

        super.finish();
    }

    protected void configureAudioMuteStatus(Boolean state) {
        mLogger.d("entering configureAudioMuteStatus - state: " + state);
        if (mAudioInteraction != null) {
            mAudioInteraction.muteAudio(state);
        }
    }

    @Override
    public void onInteractionAudioMuteStatusChanged(boolean state) {
        mLogger.d("AudioInteraction:onInteractionAudioMuteStatusChanged, state - " + state);
        if (state) {
            mMicrophoneButton.setImageResource(R.drawable.ic_activecall_mute_active);
        } else {
            mMicrophoneButton.setImageResource(R.drawable.ic_activecall_mute);
        }
    }

    @Override
    public void onInteractionQualityChanged(CallQuality callQuality) {
        AudioInteractionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderCallQuality(callQuality);
            }
        });
    }
    // End of interaction callbacks

    @Override
    public void getElementReferences() {
        mMicrophoneButton = findViewById(R.id.imageButtonMuteMic);
        callTimeTextView = findViewById(R.id.textViewTimer);
        audioDeviceButton = findViewById(R.id.audioDeviceButton);
        textViewCallQuality = findViewById(R.id.textViewCallQuality);
        ivCallQualityRating = findViewById(R.id.ivCallQualityRating);
    }

    @Override
    public void attachEventHandlers() {
        mMicrophoneButton.setOnClickListener(view -> {
            mLogger.d("Mute pressed");

            if (mAudioInteraction != null) {
                Boolean isMuted = mAudioInteraction.isAudioMuted();
                mLogger.i("Is audio muted: " + isMuted);
                configureAudioMuteStatus(!isMuted);
            } else {
                mLogger.w("Audio Interaction is null");
            }
        });
    }

    @Override
    protected long getInteractionTimeElapsed() {
        return mAudioInteraction.getInteractionTimeElapsed();
    }


}
