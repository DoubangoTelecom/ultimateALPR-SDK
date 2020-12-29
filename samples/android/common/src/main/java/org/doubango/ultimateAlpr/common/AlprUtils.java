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
        private Car mCar;

        public String getNumber() { return mNumber; }
        public float getDetectionConfidence() { return mDetectionConfidence; }
        public float getRecognitionConfidence() { return mRecognitionConfidence; }
        public float[] getWarpedBox() { return mWarpedBox; }

        public List<Country> getCountries() { return mCountries; }
        public Car getCar() { return mCar; }
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
        if (!result.isOK() || (result.numPlates() == 0 && result.numCars() == 0)) {
            return plates;
        }
        final String jsonString = result.json();
        //final String jsonString = "{\"frame_id\":178,\"lantency\":0,\"plates\":[{\"car\":{\"color\":[{\"confidence\":59.76562,\"klass\":11,\"name\":\"white\"},{\"confidence\":27.73438,\"klass\":0,\"name\":\"black\"},{\"confidence\":11.32812,\"klass\":9,\"name\":\"silver\"},{\"confidence\":0.390625,\"klass\":4,\"name\":\"gray\"},{\"confidence\":0.390625,\"klass\":5,\"name\":\"green\"}],\"confidence\":89.45312,\"makeModelYear\":[{\"confidence\":5.46875,\"klass\":8072,\"make\":\"nissan\",\"model\":\"nv\",\"year\":2012},{\"confidence\":3.90625,\"klass\":4885,\"make\":\"gmc\",\"model\":\"yukon 1500\",\"year\":2007},{\"confidence\":1.953125,\"klass\":3950,\"make\":\"ford\",\"model\":\"f150\",\"year\":2001},{\"confidence\":1.953125,\"klass\":4401,\"make\":\"ford\",\"model\":\"ranger\",\"year\":2008},{\"confidence\":1.953125,\"klass\":3954,\"make\":\"ford\",\"model\":\"f150\",\"year\":2005}],\"warpedBox\":[37.26704,655.171,253.8487,655.171,253.8487,897.6935,37.26704,897.6935]},\"confidences\":[86.99596,99.60938],\"country\":[{\"code\":\"RUS\",\"confidence\":99.60938,\"klass\":65,\"name\":\"Russian Federation\",\"other\":\"Private vehicle\",\"state\":\"Republic of Karelia\"},{\"code\":\"USA\",\"confidence\":0.0,\"klass\":88,\"name\":\"United States of America\",\"state\":\"Iowa\"},{\"code\":\"USA\",\"confidence\":0.0,\"klass\":80,\"name\":\"United States of America\",\"state\":\"Connecticut\"},{\"code\":\"USA\",\"confidence\":0.0,\"klass\":81,\"name\":\"United States of America\",\"state\":\"Delaware\"},{\"code\":\"USA\",\"confidence\":0.0,\"klass\":82,\"name\":\"United States of America\",\"state\":\"Florida\"}],\"text\":\"K643ET10\",\"warpedBox\":[61.73531,819.796,145.57,819.796,145.57,881.916,61.73531,881.916]}]}";
        if (jsonString == null) { // No plate
            return plates;
        }

        try {
            final JSONObject jObject = new JSONObject(jsonString);
            if (jObject.has("plates")) {
                final JSONArray jPlates = jObject.getJSONArray("plates");
                for (int i = 0; i < jPlates.length(); ++i) {
                    final JSONObject jPlate = jPlates.getJSONObject(i);

                    // The plate itself (backward-compatible with 2.0.0)
                    final Plate plate = new Plate();
                    plate.mWarpedBox = new float[8];
                    if (jPlate.has("text")) { // Starting 3.2 it's possible to have cars without plates when enabled
                        final JSONArray jConfidences = jPlate.getJSONArray("confidences");
                        final JSONArray jWarpedBox = jPlate.getJSONArray("warpedBox");
                        plate.mNumber = jPlate.getString("text");
                        for (int j = 0; j < 8; ++j) {
                            plate.mWarpedBox[j] = (float) jWarpedBox.getDouble(j);
                        }
                        plate.mRecognitionConfidence = (float) jConfidences.getDouble(0);
                        plate.mDetectionConfidence = (float) jConfidences.getDouble(1);
                    }
                    else {
                        plate.mNumber = "";
                        plate.mRecognitionConfidence = 0.f;
                        plate.mDetectionConfidence = 0.f;
                    }

                    // License Plate Country Identification [LPCI] (Added in 3.0.0): https://www.doubango.org/SDKs/anpr/docs/Features.html#license-plate-country-identification-lpci
                    if (jPlate.has("country")) {
                        plate.mCountries = new LinkedList<>();
                        final JSONArray jCountries = jPlate.getJSONArray("country");
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

                            plate.mCountries.add(country);
                        }
                    }

                    // Car (Added in 3.0.0)
                    if (jPlate.has("car")) {
                        final JSONObject jCar = jPlate.getJSONObject("car");
                        final JSONArray jCarWarpedBox = jCar.getJSONArray("warpedBox");
                        plate.mCar = new Car();
                        plate.mCar.mConfidence = (float) jCar.getDouble("confidence");
                        plate.mCar.mWarpedBox = new float[8];
                        for (int j = 0; j < 8; ++j) {
                            plate.mCar.mWarpedBox[j] = (float) jCarWarpedBox.getDouble(j);
                        }

                        // Vehicle Color Recognition [VCR] (added in 3.0.0) : https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-color-recognition-vcr
                        if (jCar.has("color")) {
                            plate.mCar.mColors = new LinkedList<>();
                            final JSONArray jColors = jCar.getJSONArray("color");
                            for (int k = 0; k < jColors.length(); ++k) {
                                final JSONObject jColor = jColors.getJSONObject(k);
                                final Car.Attribute color = new Car.Attribute();
                                color.mKlass = jColor.getInt("klass");
                                color.mConfidence = (float) jColor.getDouble("confidence");
                                color.mName = jColor.getString("name"); // Name in English

                                plate.mCar.mColors.add(color);
                            }
                        }

                        // Vehicle Make Model Recognition [VMMR] (added in 3.0.0): https://www.doubango.org/SDKs/anpr/docs/Features.html#vehicle-make-model-recognition-vmmr
                        if (jCar.has("makeModelYear")) {
                            plate.mCar.mMakesModelsYears = new LinkedList<>();
                            final JSONArray jMMYs = jCar.getJSONArray("makeModelYear");
                            for (int k = 0; k < jMMYs.length(); ++k) {
                                final JSONObject jMMY = jMMYs.getJSONObject(k);
                                final Car.MakeModelYear mmy = new Car.MakeModelYear();
                                mmy.mKlass = jMMY.getInt("klass");
                                mmy.mConfidence = (float) jMMY.getDouble("confidence");
                                mmy.mMake = jMMY.getString("make");
                                mmy.mModel = jMMY.getString("model");
                                mmy.mYear = jMMY.get("year").toString(); // Maybe Integer or String or whatever

                                plate.mCar.mMakesModelsYears.add(mmy);
                            }
                        }

                        // Vehicle Body Style Recognition [VBSR] (added in 3.2.0): https://www.doubango.org/SDKs/anpr/docs/Features.html#features-vehiclebodystylerecognition
                        if (jCar.has("bodyStyle")) {
                            plate.mCar.mBodyStyles = new LinkedList<>();
                            final JSONArray jBodyStyles = jCar.getJSONArray("bodyStyle");
                            for (int k = 0; k < jBodyStyles.length(); ++k) {
                                final JSONObject jBodyStyle = jBodyStyles.getJSONObject(k);
                                final Car.Attribute bodyStyle = new Car.Attribute();
                                bodyStyle.mKlass = jBodyStyle.getInt("klass");
                                bodyStyle.mConfidence = (float) jBodyStyle.getDouble("confidence");
                                bodyStyle.mName = jBodyStyle.getString("name"); // Name in English

                                plate.mCar.mBodyStyles.add(bodyStyle);
                            }
                        }
                    }

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