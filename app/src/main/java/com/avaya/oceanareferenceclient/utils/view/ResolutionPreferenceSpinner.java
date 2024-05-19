package com.avaya.oceanareferenceclient.utils.view;

import android.app.Activity;
import android.content.Context;
import android.widget.Spinner;

import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Device.Video.Enums.VideoCapturePreference;
import com.avaya.ocs.Services.Device.Video.VideoDevice;

import java.util.ArrayList;
import java.util.List;

public class ResolutionPreferenceSpinner extends GenericSpinner {

    private static final String TAG = ResolutionPreferenceSpinner.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private Context context;
    private Activity activity;

    private VideoCapturePreference mVideoCapturePreference;
    private ArrayList<String> resolutionList;
    private Spinner mSpinner;

    public ResolutionPreferenceSpinner(Context context, Activity activity, Spinner spinner) {
           super(context,activity,spinner);
    }



    public void setup() {
        try {
            mLogger.d("entering setup spinner()");
            resolutionList = new ArrayList<String>();
            for(VideoCapturePreference preference : VideoCapturePreference.values()){
                resolutionList.add(preference.getName());
            }

            super.setup(resolutionList);

        } catch (Exception e) {
            mLogger.e("Exception occurred in setupResolutions(): " + e.getClass() + ": " + e.getMessage(), e);
        }
    }
    @Override
    public void setValue(String value) {
        for(VideoCapturePreference preference : VideoCapturePreference.values()){
            if(value.equals(preference.getName()))
                this.mVideoCapturePreference = preference;
        }
    }

    public VideoCapturePreference getSelected() {
        return mVideoCapturePreference;
    }


}
