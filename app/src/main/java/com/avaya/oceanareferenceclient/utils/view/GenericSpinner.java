package com.avaya.oceanareferenceclient.utils.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.avaya.oceanareferenceclient.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericSpinner implements AdapterView.OnItemSelectedListener {

    private static final String TAG = GenericSpinner.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    private Context context;
    private Activity activity;
    private List<String> values;
    private Spinner mSpinner;

    public GenericSpinner(Context context, Activity activity, Spinner spinner) {
        this.context = context;
        this.activity = activity;
        this.mSpinner = spinner;
    }

    public void setup(ArrayList<String> values) {
        try {
            mLogger.d("entering setup spinner()");
            this.values = values;
            final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, this.values);

            final AdapterView.OnItemSelectedListener onItemSelectedListener = this;
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    updateSpinner(dataAdapter, onItemSelectedListener);
                }
            });

        } catch (Exception e) {
            mLogger.e("Exception occurred in setupResolutions(): " + e.getClass() + ": " + e.getMessage(), e);
        }
    }

    public void updateSpinner(final ArrayAdapter<String> dataAdapter, final AdapterView.OnItemSelectedListener onItemSelectedListener) {
        mSpinner.setAdapter(dataAdapter);
        mSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void setSpinnerValue(Object item) {
        mLogger.d("Update camera capture resolution");

        if (item != null) {
            try {
                String itemValue = (String) item;
                setValue(itemValue);

            } catch (Exception e) {
                mLogger.e("Exception: " + e.getMessage(), e);
            }
        }
    }

    public abstract void setValue(String key);

    public abstract Object getSelected();

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        setSpinnerValue(item);
        View resolutionText = parent.getChildAt(0);
        if (resolutionText != null && resolutionText instanceof TextView) {
            ((TextView) resolutionText).setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Unused
    }
}
