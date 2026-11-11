/*
 * Copyright (C) 2016-2020 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr;

import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import org.doubango.ultimateAlpr.sample.videoparallel.R;

import org.doubango.ultimateAlpr.common.AlprActivity;
import org.doubango.ultimateAlpr.common.AlprCameraFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Main activity
 */
public class AlprVideoParallelActivity extends AlprActivity {

    /**
     * TAG used for the debug logs.
     */
    static final String TAG = AlprVideoParallelActivity.class.getCanonicalName();

    /**
     * Preferred size for the video stream. Will select the
     * closest size from the camera capabilities.
     */
    static final Size PREFERRED_SIZE = new Size(1280, 720);

    /**
     * The server url used to activate the license. Please contact us to get the real URL.
     * e.g. https://localhost:3600
     */
    static final String ACTIVATION_SERVER_URL = "";

    /**
     * The master or slave key to use for the activation.
     * You MUST NEVER include your master key in the code or share it with the end user.
     * The master key should be used to generate slaves (one-time activation keys).
     * More information about master/slave keys at https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html.
     */
    static final String ACTIVATION_MASTER_OR_SLAVE_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate " + this);
        super.onCreate(savedInstanceState);

        // At this step, the base class (AlprActivity) already initialized the engine (thanks to "super.onCreate()").
        // Do not try to create the parallel delivery callback in this method. Do it
        // in the constructor or at the declaration (see above). If the engine is initialized without
        // a parallel delivery callback, then it'll run in sequential mode.

        // Add camera fragment to the layout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, AlprCameraFragment.newInstance(PREFERRED_SIZE, this))
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy " + this);
        super.onDestroy();

        // At this step, the engine is already deInitialized (thanks to "super.onDestroy()").
        // Any call using the engine (UltAlprSdkEngine.*) will miserably fail.
    }

    @Override
    protected boolean isParallelDeliveryEnabled() { return true; /* we want to activated parallel instead of sequential delivery */ }

    @Override
    protected String getActivationServerUrl() { return ACTIVATION_SERVER_URL; }

    @Override
    protected String getActivationMasterOrSlaveKey() { return ACTIVATION_MASTER_OR_SLAVE_KEY; }
}
