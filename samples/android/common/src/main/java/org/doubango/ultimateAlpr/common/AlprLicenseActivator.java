/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr.common;

import android.content.Context;
import android.util.Log;

import org.doubango.ultimateAlpr.Sdk.UltAlprSdkEngine;
import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * License activator.
 */
public class AlprLicenseActivator {

    /**
     * TAG used for the debug logs.
     */
    static final String TAG = AlprLicenseActivator.class.toString();

    /**
     * Read timeout
     */
    static final int TIMEOUT_READ = 10 * 1000;

    /**
     * Connect timeout
     */
    static final int TIMEOUT_CONNECT = 10 * 1000;

    /**
     * Name of the file containing the license token.
     */
    static final String LICENSE_TOKEN_FILE_NAME = "ultimateALPR-token.lic";

    /**
     * Read data from the file.
     * @return Returns data in file at \ref path if succeed, otherwise empty string.
     */
    public static String tokenData(final String path) {
        StringBuilder text = new StringBuilder();
        try {
            final  BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        }
        return text.toString();
    }

    /**
     * Returns path to the file containing the license token.
     * @param context Activity context.
     * @return Path to the file containing the license token if exist, otherwise empty string.
     */
    public static String tokenFile(final Context context) {
        final File tokenFile = new File(context.getFilesDir(), LICENSE_TOKEN_FILE_NAME);
        return tokenFile.exists() ? tokenFile.getAbsolutePath() : "";
    }

    /**
     * Function used to automatically activate the license on the current device. You must initialize
     * the engine before calling this function. After activation you must initialized the engine
     * again to provide the token info.
     * More information about the activation process can be found at https://www.doubango.org/SDKs/LicenseManager/docs/Jargon.html#activation.
     * @param context Activity context.
     * @param activationUrl Activation HTTPS URL. e.g. https://localhost:3600
     * @param masterOrSlaveKey Master or slave key. You must never ever share your master key
     *                         or include it in your application. Slaves are one-time activation keys
     *                         to be included in your application or sent to the end user for activation.
     * @param force Whether to force the activation even if the license file exists. Should be false to
     *              avoid connecting to the server every time this function is called.
     * @return True if success, otherwise false.
     */
    public static String activate(final Context context, final String activationUrl, final String masterOrSlaveKey, final boolean force) {
        final File tokenFile = new File(context.getFilesDir(), LICENSE_TOKEN_FILE_NAME);
        if (tokenFile.exists()) {
            if (!force) {
                return tokenFile.getAbsolutePath();
            }
            Log.w(TAG, "You should not force the activation. You'll be in big trouble if the server is down or the device offline.");
        }
        try {
            return Executors.newSingleThreadExecutor().submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    if (activationUrl == null || activationUrl.isEmpty() || masterOrSlaveKey == null || masterOrSlaveKey.isEmpty()) {
                        Log.e(TAG, "Activation url and master/slave key must not be null");
                        return "";
                    }

                    String pathToLicenseFile = "";
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    try {
                        url = new URL(activationUrl + (activationUrl.charAt(activationUrl.length() - 1) == '/' ? "" : "/") + "activate");
                        urlConnection = (HttpURLConnection) url.openConnection();

                        urlConnection.setReadTimeout(TIMEOUT_READ);
                        urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        final OutputStream outStream = urlConnection.getOutputStream();
                        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));

                        JSONObject data = new JSONObject();
                        data.put("masterOrSlaveKey", masterOrSlaveKey);
                        final UltAlprSdkResult result = UltAlprSdkEngine.requestRuntimeLicenseKey(true);
                        if (!result.isOK()) {
                            throw new AssertionError("Failed to request runtime key: " + result.phrase());
                        }
                        data.put("runtimeKey", result.json());
                        writer.write(data.toString());

                        writer.flush();
                        writer.close();
                        outStream.close();
                        final int responseCode = urlConnection.getResponseCode();
                        final boolean responseCodeIsSuccess = (200 <= responseCode && responseCode <= 299);
                        String responseContent = "";
                        String responseLine;
                        BufferedReader br = new BufferedReader(new InputStreamReader(responseCodeIsSuccess ? urlConnection.getInputStream() : urlConnection.getErrorStream()));
                        while ((responseLine = br.readLine()) != null) {
                            responseContent += responseLine;
                        }
                        if (responseCodeIsSuccess) {
                            final JSONObject responseJSON = new JSONObject(responseContent);
                            FileOutputStream stream = new FileOutputStream(tokenFile);
                            try {
                                stream.write(responseJSON.getString("token").getBytes());
                                pathToLicenseFile = tokenFile.getAbsolutePath();
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to write token: " + e.toString());
                                e.printStackTrace();
                            } finally {
                                stream.close();
                            }
                        }
                        else {
                            Log.e(TAG, "POST request failed: " + urlConnection.getResponseMessage() + "\n" + responseContent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Activate failed:" + e.toString());
                        e.printStackTrace();
                    }
                    finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                    return pathToLicenseFile;
                }
            }).get();
        } catch (Exception e) {
            Log.e(TAG, "Activate failed:" + e.toString());
            e.printStackTrace();
            return null;
        }
    }

}