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

import androidx.annotation.NonNull;

import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    static class Car {
        static class Attribute {
            private int mKlass;
            private String mName;
            private float mConfidence;

            public int getKlass() { return mKlass; }
            public String getName() { return mName; }
            public float getConfidence() { return mConfidence; }
        }
        static class MakeModelYear {
            private int mKlass;
            private String mMake;
            private String mModel;
            private String mYear; // Not integer on purpose, could be interval or...
            private float mConfidence;

            public int getKlass() { return mKlass; }
            public String getMake() { return mMake; }
            public String getModel() { return mModel; }
            public String getYear() { return mYear; }
            public float getConfidence() { return mConfidence; }
        }

        private float mConfidence;
        private float mWarpedBox[];
        private List<Car.Attribute> mColors;
        private List<Car.Attribute> mBodyStyles;
        private List<Car.MakeModelYear> mMakesModelsYears;

        public float[] getWarpedBox() { return mWarpedBox; }
        public float getConfidence() { return mConfidence; }
        public List<Car.Attribute> getColors() { return mColors; }
        public List<Car.Attribute> getBodyStyles() { return mBodyStyles; }
        public List<Car.MakeModelYear> getMakesModelsYears() { return mMakesModelsYears; }
    }

    /**
     *
     */
    static class Country {
        private int mKlass;
        private String mCode;
        private String mName;
        private String mState;
        private String mOther;
        private float mConfidence;

        public int getKlass() { return mKlass; }
        public String getCode() { return mCode; }
        public String getName() { return mName; }
        public String getState() { return mState; }
        public String getOther() { return mOther; }
        public float getConfidence() { return mConfidence; }
    }

    /**
     *
     */
    static class Plate {
        private String mNumber;
        private float mDetectionConfidence;
        private float mRecognitionConfidence;
        private float mWarpedBox[];
        private List<Country> mCountries;

        public String getNumber() { return mNumber; }
        public float getDetectionConfidence() { return mDetectionConfidence; }
        public float getRecognitionConfidence() { return mRecognitionConfidence; }
        public float[] getWarpedBox() { return mWarpedBox; }

        public List<Country> getCountries() { return mCountries; }
    }

    static class Block {
        private Car mCar;
        private  Plate mPlate;

        public Car getCar() { return mCar; }
        public Plate getPlate() { return mPlate; }
    }

    static public final List<Block> extractBlocks(String jsonString) {
        final List<Block> blocks = new LinkedList<>();

        if (jsonString == null) { // No plate
            return blocks;
        }

        try {
            final JSONObject jObject = new JSONObject(jsonString);
            if (jObject.has("blocks")) {
                final JSONArray jBlocks = jObject.getJSONArray("blocks");
                for (int i = 0; i < jBlocks.length(); ++i) {
                    final JSONObject jBlock = jBlocks.getJSONObject(i);

                    final Block block = new Block(); // a block contains a "plate" and a "car"

                    // Block::Plate
                    if (jBlock.has("plate")) {
                        // License Plate Recognition [OCR]
                        final JSONObject jPlate = jBlock.getJSONObject("plate");
                        block.mPlate = new Plate();

                        final JSONArray jConfidences = jPlate.getJSONArray("confidences");
                        final JSONArray jWarpedBox = jPlate.getJSONArray("warpedBox");
                        block.mPlate.mNumber = jPlate.getString("text");
                        block.mPlate.mWarpedBox = new float[8];
                        for (int j = 0; j < 8; ++j) {
                            block.mPlate.mWarpedBox[j] = (float) jWarpedBox.getDouble(j);
                        }
                        block.mPlate.mRecognitionConfidence = (float) jConfidences.getDouble(0);
                        block.mPlate.mDetectionConfidence = (float) jConfidences.getDouble(1);

                        // License Plate Country Identification [LPCI]
                        if (jPlate.has("lpci")) {
                            block.mPlate.mCountries = new LinkedList<>();
                            final JSONArray jCountries = jPlate.getJSONArray("lpci");
                            for (int k = 0; k < jCountries.length(); ++k) {
                                final JSONObject jCountry = jCountries.getJSONObject(k);
                                final Country country = new Country();
                                country.mKlass = jCountry.getInt("klass");
                                country.mConfidence = (float) jCountry.getDouble("confidence");
                                country.mCode = jCountry.getString("code"); // ISO-code: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
                                country.mName = jCountry.getString("name"); // Name in English
                                if (jCountry.has("state")) { // optional
                                    country.mState = jCountry.getString("state");
                                }
                                if (jCountry.has("other")) { // optional
                                    country.mOther = jCountry.getString("other");
                                }

                                block.mPlate.mCountries.add(country);
                            }
                        }
                    }

                    // Block::car
                    if (jBlock.has("car")) {
                        final JSONObject jCar = jBlock.getJSONObject("car");
                        final JSONArray jCarWarpedBox = jCar.getJSONArray("warpedBox");
                        block.mCar = new Car();
                        block.mCar.mConfidence = (float) jCar.getDouble("confidence");
                        block.mCar.mWarpedBox = new float[8];
                        for (int j = 0; j < 8; ++j) {
                            block.mCar.mWarpedBox[j] = (float) jCarWarpedBox.getDouble(j);
                        }

                        // Vehicle Color Recognition [VCR]
                        if (jCar.has("vcr")) {
                            block.mCar.mColors = new LinkedList<>();
                            final JSONArray jColors = jCar.getJSONArray("vcr");
                            for (int k = 0; k < jColors.length(); ++k) {
                                final JSONObject jColor = jColors.getJSONObject(k);
                                final Car.Attribute color = new Car.Attribute();
                                color.mKlass = jColor.getInt("klass");
                                color.mConfidence = (float) jColor.getDouble("confidence");
                                color.mName = jColor.getString("name"); // Name in English

                                block.mCar.mColors.add(color);
                            }
                        }

                        // Vehicle Make Model Recognition [VMMR]
                        if (jCar.has("vmmr")) {
                            block.mCar.mMakesModelsYears = new LinkedList<>();
                            final JSONArray jMMYs = jCar.getJSONArray("vmmr");
                            for (int k = 0; k < jMMYs.length(); ++k) {
                                final JSONObject jMMY = jMMYs.getJSONObject(k);
                                final Car.MakeModelYear mmy = new Car.MakeModelYear();
                                mmy.mKlass = jMMY.getInt("klass");
                                mmy.mConfidence = (float) jMMY.getDouble("confidence");
                                mmy.mMake = jMMY.getString("make");
                                mmy.mModel = jMMY.getString("model");
                                mmy.mYear = jMMY.get("year").toString(); // Maybe Integer or String or whatever

                                block.mCar.mMakesModelsYears.add(mmy);
                            }
                        }

                        // Vehicle Body Style Recognition [VBSR]
                        if (jCar.has("vbsr")) {
                            block.mCar.mBodyStyles = new LinkedList<>();
                            final JSONArray jBodyStyles = jCar.getJSONArray("vbsr");
                            for (int k = 0; k < jBodyStyles.length(); ++k) {
                                final JSONObject jBodyStyle = jBodyStyles.getJSONObject(k);
                                final Car.Attribute bodyStyle = new Car.Attribute();
                                bodyStyle.mKlass = jBodyStyle.getInt("klass");
                                bodyStyle.mConfidence = (float) jBodyStyle.getDouble("confidence");
                                bodyStyle.mName = jBodyStyle.getString("name"); // Name in English

                                block.mCar.mBodyStyles.add(bodyStyle);
                            }
                        }
                    }

                    blocks.add(block);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return blocks;
    }

    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
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
        return "code: " + result.code() + ", phrase: " + result.phrase() + ", json: " + result.json();
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