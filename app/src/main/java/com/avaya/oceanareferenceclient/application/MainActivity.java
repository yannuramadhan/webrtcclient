package com.avaya.oceanareferenceclient.application;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.avaya.oceanareferenceclient.R;
import com.avaya.oceanareferenceclient.click2call.CallFragment;
import com.avaya.oceanareferenceclient.settings.SettingsActivity;
import com.avaya.oceanareferenceclient.settings.SettingsActivityElite;
import com.avaya.oceanareferenceclient.settings.SettingsService;
import com.avaya.oceanareferenceclient.settings.SettingsServiceElite;
import com.avaya.oceanareferenceclient.utils.Logger;
import com.avaya.oceanareferenceclient.utils.network.NetworkUtil;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Logger mLogger = Logger.getLogger(TAG);

    protected OceanaApplication app;

    // index to identify current nav tool_bar_menu item
    public static int navItemIndex = 1;

    // tags used to attach the fragments
    private static final String TAG_AUDIO = "audio";
    private static final String TAG_VIDEO = "video";
    public static String CURRENT_TAG = TAG_VIDEO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delegate to the OceanaApplication class to handle uncaught exceptions that could cause app crashes
        app = (OceanaApplication) getApplication();
        app.setCurrentActivity(this);

        Toolbar toolbar = setupToolbar();
        showNetworkConnectionType();

        loadFragment();

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_report_issue) {
            mLogger.i("Report issue");
            IssueReporter issueReporter = new IssueReporter(this, getString(R.string.app_version));
            issueReporter.reportIssue();
        } else if (id == R.id.action_settings) {
            mLogger.d("Settings selected");
            // Langsung arahkan ke SettingsActivityElite tanpa dialog
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.platformType), getString(R.string.platformTypeElite));
            editor.commit();

            // Initialise application services again to get reconfigured with the new Settings
            app.initServices();

            // Launch SettingsActivityElite
            Intent intent = new Intent(getApplicationContext(), SettingsActivityElite.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private void loadFragment() {


        Fragment fragment = getFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public Fragment getFragment() {

        return new CallFragment();

    }

    public void showPlatformSelectionAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose platform");
        String[] platforms = {getString(R.string.platformTypeOceana), getString(R.string.platformTypeElite)};
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String platformType = sharedPref.getString(getString(R.string.platformType), getString(R.string.platformTypeElite)).trim();
        int checkedItem = 1;

        builder.setSingleChoiceItems(platforms, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent ;
                SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString(getString(R.string.platformType),getString(R.string.platformTypeElite));
                    intent = new Intent(getApplicationContext(), SettingsActivityElite.class);


                editor.commit();
                // Initialise application services again to get reconfigured with the nw Settings
                app.initServices();
                //Launch corresponding settings
                startActivity(intent);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Toolbar setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        return toolbar;
    }

    private void showNetworkConnectionType() {
        String networkStatus = NetworkUtil.getConnectivityStatusString(this);
        mLogger.i("Network connection: " + networkStatus);
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(this, networkStatus, Toast.LENGTH_SHORT);
            toast.show();
        });
    }
}
