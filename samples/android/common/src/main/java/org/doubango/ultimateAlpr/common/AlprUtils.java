/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr.common;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.PointF;
import android.util.Log;

import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class
 */
public class AlprUtils {
    static final String TAG = AlprUtils.class.getCanonicalName();
    /**
     *
     */
    public static class AlprTransformationInfo {
        final int mXOffset;
        final int mYOffset;
        final float mRatio;
        final int mWidth;
        final int mHeight;
        public AlprTransformationInfo(final int imageWidth, final int imageHeight, final int canvasWidth, final int canvasHeight) {
            final float xRatio = (float)canvasWidth / (float)imageWidth;
            final float yRatio =  (float)canvasHeight / (float)imageHeight;
            mRatio = Math.min( xRatio, yRatio );
            mWidth = (int)(imageWidth * mRatio);
            mHeight = (int)(imageHeight * mRatio);
            mXOffset = (canvasWidth - mWidth) >> 1;
            mYOffset = (canvasHeight - mHeight) >> 1;
        }
        public float transformX(final float x) { return x * mRatio + mXOffset; }
        public float transformY(final float y) { return y * mRatio + mYOffset; }
        public PointF transform(final PointF p) { return new PointF(transformX(p.x), transformY(p.y)); }
        public int getXOffset() { return mXOffset; }
        public int getYOffset() { return mYOffset; }
        public float getRatio() { return mRatio; }
        public int getWidth() { return mWidth; }
        public int getHeight() { return mHeight; }
    }

    /**
     *
     */
    static class Plate {
        private String mNumber;
        private float mDetectionConfidence;
        private float mRecognitionConfidence;
        private float mWarpedBox[];

        public String getNumber() { return mNumber; }
        public float getDetectionConfidence() { return mDetectionConfidence; }
        public float getRecognitionConfidence() { return mRecognitionConfidence; }
        public float[] getWarpedBox() { return mWarpedBox; }
    }

    static public final long extractFrameId(final UltAlprSdkResult result) {
        final String jsonString = result.json();
        if (jsonString != null) {
            try {
                final JSONObject jObject = new JSONObject(jsonString);
                return jObject.getLong("frame_id");
            }
            catch (JSONException e) { }
        }
        return 0;
    }

    static public final List<Plate> extractPlates(final UltAlprSdkResult result) {
        final List<Plate> plates = new LinkedList<>();
        if (!result.isOK() || result.numPlates() == 0) {
            return plates;
        }
        final String jsonString = result.json();
        if (jsonString == null) { // No plate
            return plates;
        }

        try {
            final JSONObject jObject = new JSONObject(jsonString);
            if (jObject.has("plates")) {
                final JSONArray jPlates = jObject.getJSONArray("plates");
                for (int i = 0; i < jPlates.length(); ++i) {
                    final JSONObject jPlate = jPlates.getJSONObject(i);
                    final JSONArray jConfidences = jPlate.getJSONArray("confidences");
                    final JSONArray jWarpedBox = jPlate.getJSONArray("warpedBox");
                    final Plate plate = new Plate();
                    plate.mNumber = jPlate.getString("text");
                    plate.mWarpedBox = new float[8];
                    for (int j = 0; j < 8; ++j) {
                        plate.mWarpedBox[j] = (float) jWarpedBox.getDouble(j);
                    }
                    plate.mRecognitionConfidence = (float) jConfidences.getDouble(0);
                    plate.mDetectionConfidence = (float) jConfidences.getDouble(1);
                    plates.add(plate);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return plates;
    }

    /**
     * Checks if the returned result is success. An assertion will be raised if it's not the case.
     * In production you should catch the exception and perform the appropriate action.
     * @param result The result to check
     * @return The same result
     */
    static public final UltAlprSdkResult assertIsOk(final UltAlprSdkResult result) {
        if (!result.isOK()) {
            throw new AssertionError("Operation failed: " + result.phrase());
        }
        return result;
    }

    /**
     * Converts the result to String.
     * @param result
     * @return
     */
    static public final String resultToString(final UltAlprSdkResult result) {
        return "code: " + result.code() + ", phrase: " + result.phrase() + ", numPlates: " + result.numPlates() + ", json: " + result.json();
    }

    /**
     *
     * @param fileName
     * @return Must close the returned object
     */
    static public FileChannel readFileFromAssets(final AssetManager assets, final String fileName) {
        FileInputStream inputStream = null;
        try {
            AssetFileDescriptor fileDescriptor = assets.openFd(fileName);
            inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            return inputStream.getChannel();
            // To return DirectByteBuffer: fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return null;
        }
    }
}