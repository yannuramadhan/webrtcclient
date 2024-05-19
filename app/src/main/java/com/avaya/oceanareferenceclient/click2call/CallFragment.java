package com.avaya.oceanareferenceclient.click2call;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.OrientationSpinner;
import com.avaya.oceanareferenceclient.utils.view.ResolutionPreferenceSpinner;

public class CallFragment extends AbstractCallFragment {

    private static final String TAG = CallFragment.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);


    private TextView availableResolutionsText;
    private Spinner resolutionSpinnerView;
    private Spinner orientationSpinnerView;
    private ResolutionPreferenceSpinner resolutionsSpinner;
    private OrientationSpinner orientationSpinner;

    private View fragmentView;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupSpinners();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String platformType = sharedPref.getString(getString(R.string.platformType), getString(R.string.platformTypeOceana)).trim();
        if(platformType.equals(getString(R.string.platformTypeElite))) {
            ((TextView)this.fragmentView.findViewById(R.id.textViewTitle)).setText(R.string.menu_background_text_elite);
        }else{
            ((TextView)this.fragmentView.findViewById(R.id.textViewTitle)).setText(R.string.menu_background_text_oceana);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_video, container, false);
        return fragmentView;
    }



    public void setupSpinners() {
        try {
            resolutionsSpinner = new ResolutionPreferenceSpinner(getContext(), getActivity(), resolutionSpinnerView);
            resolutionsSpinner.setup();
            orientationSpinner = new OrientationSpinner(getContext(),getActivity(),orientationSpinnerView);
            orientationSpinner.setup();
        } catch (Exception e) {
            mLogger.e("Exception while setting up spinner", e);
        }
    }

    @Override
    public void getElementReferences() {
        resolutionSpinnerView = getActivity().findViewById(R.id.resolution_spinner);
//        orientationSpinnerView = getActivity().findViewById(R.id.orientation_spinner);

        availableResolutionsText = getActivity().findViewById(R.id.textViewResolutions);
        fabMenu = getActivity().findViewById(R.id.fabMenu);

        fabVoice = fabMenu.findViewById(R.id.fabVoice);
        fabVideo = fabMenu.findViewById(R.id.fabVideo);
    }

    protected void showUIElements() {
        resolutionSpinnerView.setVisibility(View.VISIBLE);
        availableResolutionsText.setVisibility(View.VISIBLE);
    }

    protected void hideUIElements() {
        resolutionSpinnerView.setVisibility(View.INVISIBLE);
        availableResolutionsText.setVisibility(View.INVISIBLE);
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
    }

    @Override
    protected void addExtraIntent(Intent intent) {
        intent.putExtra(Constants.DATA_KEY_VIDEO_RESOLUTION, resolutionsSpinner.getSelected());
        intent.putExtra(Constants.DATA_KEY_VIDEO_ORIENTATION, orientationSpinner.getSelected());

    }
}
