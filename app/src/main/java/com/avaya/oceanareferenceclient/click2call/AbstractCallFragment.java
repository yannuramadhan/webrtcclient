package com.avaya.oceanareferenceclient.click2call;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import com.avaya.oceanareferenceclient.application.OceanaApplication;
import com.avaya.oceanareferenceclient.authorization.AuthorizationResponse;
import com.avaya.oceanareferenceclient.authorization.ResponseListener;
import com.avaya.oceanareferenceclient.authorization.RetrieveAuthTokenTask;
import com.avaya.oceanareferenceclient.settings.AbstractSettingsService;
import com.avaya.oceanareferenceclient.interactions.AudioInteractionActivity;
import com.avaya.oceanareferenceclient.interactions.VideoInteractionActivity;
import com.avaya.oceanareferenceclient.utils.Constants;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.view.ViewHandler;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCallFragment extends Fragment implements ViewHandler {

    private static final String TAG = AbstractCallFragment.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    protected Button clickToCallBtn;
    private int targetSdkVersion;

    private boolean skipOnePermissionCheck = false;
    protected FloatingActionButton fabVoice;
    protected FloatingActionButton fabVideo;
    protected FloatingActionMenu fabMenu;

    private boolean bVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Context context = getContext();
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            mLogger.e("Unable to get target SDK version", e);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getElementReferences();
        attachEventHandlers();
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: This is messy, it should probably be in the activtiy
        if (!skipOnePermissionCheck) {
            checkPermissions();
        } else {
            skipOnePermissionCheck = false;
        }

        fabMenu.setEnabled(true);
    }

    private ResponseListener<AuthorizationResponse> callback = new ResponseListener<AuthorizationResponse>() {
        @Override
        public void done(AuthorizationResponse authorizationResponse) {
            if (authorizationResponse.isSuccessful()) {
                moveToInCallActivity(authorizationResponse.getData());
            } else {
                mLogger.w("Authorization request failed, not proceeding with click to call");
                if (isAdded()) { // Make sure this fragment is still active
                    showToast(getActivity(), "Authorization request failed", Toast.LENGTH_LONG);
                }
                fabMenu.setEnabled(true);
            }
        }
    };

    public void checkPermissions() {
        String[] requiredPermissions = getRequiredPermissions();
        List<String> requestPermissions = new ArrayList<String>(requiredPermissions.length);

        boolean granted = true;

        for (String permission : requiredPermissions) {
            if (!isPermissionAvailable(permission)) {
                mLogger.e("Missing permission: " + permission);
                granted = false;
                requestPermissions.add(permission);
            }
        }

        if (granted) {
            mLogger.d("Permissions Granted");
            showUIElements();
        } else {
            hideUIElements();
            if (Build.VERSION.SDK_INT >= 23) {
                mLogger.d("Requesting required permissions");
                requestPermissions(requestPermissions.toArray(new String[requestPermissions.size()]), 1);
                skipOnePermissionCheck = true;
            } else {
                showToast(getActivity(), "Unable to make call: missing permissions", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if (granted) {
            checkPermissions();
        }
    }

    private boolean isPermissionAvailable(String permission) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                result = (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), permission));
            } else {
                result = (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(getContext(), permission));
            }
        }

        return result;
    }


    protected void retrieveTokenWithCallback() {
        try {
            AbstractSettingsService settingsService = ((OceanaApplication) getActivity().getApplication()).getSettingsService(getContext());
            RetrieveAuthTokenTask retrieveAuthTokenTask = new RetrieveAuthTokenTask(settingsService, callback);
            retrieveAuthTokenTask.execute();
        } catch (Exception e) {
            mLogger.e("Error in video click to call: " + e.getMessage(), e);
        }
    }

    @Override
    public void attachEventHandlers() {
//        clickToCallBtn.setOnClickListener(v -> {
//            clickToCallBtn.setEnabled(false);
//            mLogger.d("Attempting click to call");
//            retrieveTokenWithCallback();
//        });

        fabMenu.setOnMenuButtonClickListener(view -> {

            fabMenu.toggle(true);
        });
        fabVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bVideo = false;
                fabMenu.toggle(true);
                retrieveTokenWithCallback();
            }
        });
        fabVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bVideo = true;
                fabMenu.toggle(true);
                retrieveTokenWithCallback();
            }
        });
    }

    protected void moveToInCallActivity(String token) {
        try {
            mLogger.d("Moving to interaction activity");
            Intent intent = getInCallActivtyIntent();
            intent.putExtra(Constants.DATA_KEY_TOKEN, token);
            addExtraIntent(intent);
            getContext().startActivity(intent);
        } catch (Exception ex) {
            fabMenu.setEnabled(true);
            mLogger.e("Error moving to interaction activity " + ex.getMessage(), ex);
        }
    }

    private Intent getInCallActivtyIntent() {
        if (bVideo)
            return new Intent(getContext(), VideoInteractionActivity.class);
        else
            return new Intent(getContext(), AudioInteractionActivity.class);
    }

    protected void showToast(Activity activity, String message, int length) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(activity.getApplicationContext(), message, length);
            toast.show();
        });
    }

    protected void addExtraIntent(Intent intent) {
    }

    protected abstract void showUIElements();

    protected abstract void hideUIElements();

    protected abstract String[] getRequiredPermissions();

}
