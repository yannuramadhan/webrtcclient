package com.avaya.oceanareferenceclient.application;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CallNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Finish activty at top of backstack to land onto the call activity
        finish();
    }
}