package com.avaya.oceanareferenceclient.utils.view;

import android.app.Activity;
import android.content.Context;
import android.widget.Spinner;

import com.avaya.clientservices.media.capture.VideoCaptureController;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.ocs.Services.Device.Video.Enums.VideoCaptureOrientation;
import com.avaya.ocs.Services.Device.Video.Enums.VideoCapturePreference;

import java.util.ArrayList;

public class OrientationSpinner extends GenericSpinner {

    private static final String TAG = OrientationSpinner.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private Context context;
    private Activity activity;

    private VideoCaptureOrientation orientation;
    private ArrayList<String> orientationList;
    private Spinner mSpinner;

    public OrientationSpinner(Context context, Activity activity, Spinner spinner) {
           super(context,activity,spinner);
    }



    public void setup() {
        try {
            mLogger.d("entering setup spinner()");
            orientationList = new ArrayList<String>();
            for(VideoCaptureOrientation orientation : VideoCaptureOrientation.values()){
                orientationList.add(orientation.getName());
            }

            super.setup(orientationList);

        } catch (Exception e) {
            mLogger.e("Exception occurred in setupOrientations(): " + e.getClass() + ": " + e.getMessage(), e);
        }
    }
    @Override
    public void setValue(String value) {
        for(VideoCaptureOrientation orientation : VideoCaptureOrientation.values()){
            if(value.equals(orientation.getName()))
                this.orientation = orientation;
        }
    }

    public VideoCaptureOrientation getSelected() {
        return orientation;
    }


}
