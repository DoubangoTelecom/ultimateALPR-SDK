/* Copyright (C) 2016-2019 Doubango Telecom <https://www.doubango.org>
 * File author: Mamadou DIOP (Doubango Telecom, France).
 * License: GPLv3. For commercial license please contact us.
 * Source code: https://github.com/DoubangoTelecom/compv
 * WebSite: http://compv.org
 */
package org.doubango.ultimateAlpr.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import org.doubango.ultimateAlpr.Sdk.UltAlprSdkResult;

import java.util.List;

public class AlprPlateView extends View {

    static final String TAG = AlprPlateView.class.getCanonicalName();

    static final float TEXT_NUMBER_SIZE_DIP = 20;
    static final float TEXT_CONFIDENCE_SIZE_DIP = 15;
    static final float TEXT_INFERENCE_TIME_SIZE_DIP = 10;
    static final int STROKE_WIDTH = 10;

    private final Paint mPaintTextNumber;
    private final Paint mPaintTextNumberBackground;
    private final Paint mPaintTextConfidence;
    private final Paint mPaintTextConfidenceBackground;
    private final Paint mPaintBorder;
    private final Paint mPaintTextDurationTime;
    private final Paint mPaintTextDurationTimeBackground;
    private final Paint mPaintDetectROI;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private int mOrientation = 0;

    private long mDurationTimeMillis;

    private Size mImageSize;
    private List<AlprUtils.Plate> mPlates = null;
    private RectF mDetectROI;

    /**
     *
     * @param context
     * @param attrs
     */
    public AlprPlateView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final Typeface fontALPR = Typeface.createFromAsset(context.getAssets(), "GlNummernschildEng-XgWd.ttf");

        mPaintTextNumber = new Paint();
        mPaintTextNumber.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_NUMBER_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintTextNumber.setColor(Color.BLACK);
        mPaintTextNumber.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextNumber.setTypeface(Typeface.create(fontALPR, Typeface.BOLD));

        mPaintTextNumberBackground = new Paint();
        mPaintTextNumberBackground.setColor(Color.YELLOW);
        mPaintTextNumberBackground.setStrokeWidth(STROKE_WIDTH);
        mPaintTextNumberBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintTextConfidence = new Paint();
        mPaintTextConfidence.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_CONFIDENCE_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintTextConfidence.setColor(Color.BLUE);
        mPaintTextConfidence.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextConfidence.setTypeface(Typeface.create(fontALPR, Typeface.BOLD));

        mPaintTextConfidenceBackground = new Paint();
        mPaintTextConfidenceBackground.setColor(Color.YELLOW);
        mPaintTextConfidenceBackground.setStrokeWidth(STROKE_WIDTH);
        mPaintTextConfidenceBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintBorder = new Paint();
        mPaintBorder.setStrokeWidth(STROKE_WIDTH);
        mPaintBorder.setPathEffect(null);
        mPaintBorder.setColor(Color.YELLOW);
        mPaintBorder.setStyle(Paint.Style.STROKE);

        mPaintTextDurationTime = new Paint();
        mPaintTextDurationTime.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_INFERENCE_TIME_SIZE_DIP, getResources().getDisplayMetrics()));
        mPaintTextDurationTime.setColor(Color.BLACK);
        mPaintTextDurationTime.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextDurationTime.setTypeface(Typeface.create(fontALPR, Typeface.BOLD));

        mPaintTextDurationTimeBackground = new Paint();
        mPaintTextDurationTimeBackground.setColor(Color.WHITE);
        mPaintTextDurationTimeBackground.setStrokeWidth(STROKE_WIDTH);
        mPaintTextDurationTimeBackground.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaintDetectROI = new Paint();
        mPaintDetectROI.setColor(Color.RED);
        mPaintDetectROI.setStrokeWidth(STROKE_WIDTH);
        mPaintDetectROI.setStyle(Paint.Style.STROKE);
        mPaintDetectROI.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
    }

    public void setDetectROI(final RectF roi) { mDetectROI = roi; }

    /**
     *
     * @param width
     * @param height
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    /**
     *
     * @param result
     * @param imageSize
     */
    public synchronized void setResult(@NonNull final UltAlprSdkResult result, @NonNull final Size imageSize, @NonNull final long durationTime, @NonNull final int orientation) {
        mPlates = AlprUtils.extractPlates(result);
        mImageSize = imageSize;
        mDurationTimeMillis = durationTime;
        mOrientation = orientation;
        postInvalidate();
    }

    @Override
    public synchronized void draw(final Canvas canvas) {
        super.draw(canvas);

        if (mImageSize == null) {
            Log.i(TAG, "Not initialized yet");
            return;
        }

        // Inference time
        // Landscape faster: https://www.doubango.org/SDKs/anpr/docs/Improving_the_speed.html#landscape-mode
        final String mInferenceTimeMillisString = "Total processing time: " + mDurationTimeMillis + (mOrientation == 0 ? "" : " -> Rotate to landscape to speedup");
        Rect boundsTextmInferenceTimeMillis = new Rect();
        mPaintTextDurationTime.getTextBounds(mInferenceTimeMillisString, 0, mInferenceTimeMillisString.length(), boundsTextmInferenceTimeMillis);
        canvas.drawRect(0, 0, boundsTextmInferenceTimeMillis.width(), boundsTextmInferenceTimeMillis.height(), mPaintTextDurationTimeBackground);
        canvas.drawText(mInferenceTimeMillisString, 0, boundsTextmInferenceTimeMillis.height(), mPaintTextDurationTime);

        // Transformation info
        final AlprUtils.AlprTransformationInfo tInfo = new AlprUtils.AlprTransformationInfo(mImageSize.getWidth(), mImageSize.getHeight(), getWidth(), getHeight());

        // ROI
        if (mDetectROI != null && !mDetectROI.isEmpty()) {
            canvas.drawRect(
                    new RectF(
                            tInfo.transformX(mDetectROI.left),
                            tInfo.transformY(mDetectROI.top),
                            tInfo.transformX(mDetectROI.right),
                            tInfo.transformY(mDetectROI.bottom)
                    ),
                    mPaintDetectROI
            );
        }

        // Plates
        if (mPlates != null && !mPlates.isEmpty()) {
            for (final AlprUtils.Plate plate : mPlates) {
                // Transform corners
                final float[] warpedBox = plate.getWarpedBox();
                final PointF cornerA = new PointF(tInfo.transformX(warpedBox[0]), tInfo.transformY(warpedBox[1]));
                final PointF cornerB = new PointF(tInfo.transformX(warpedBox[2]), tInfo.transformY(warpedBox[3]));
                final PointF cornerC = new PointF(tInfo.transformX(warpedBox[4]), tInfo.transformY(warpedBox[5]));
                final PointF cornerD = new PointF(tInfo.transformX(warpedBox[6]), tInfo.transformY(warpedBox[7]));
                // Draw border
                final Path pathBorder = new Path();
                pathBorder.moveTo(cornerA.x, cornerA.y);
                pathBorder.lineTo(cornerB.x, cornerB.y);
                pathBorder.lineTo(cornerC.x, cornerC.y);
                pathBorder.lineTo(cornerD.x, cornerD.y);
                pathBorder.lineTo(cornerA.x, cornerA.y);
                pathBorder.close();
                canvas.drawPath(pathBorder, mPaintBorder);

                // Draw text number
                final String number = plate.getNumber();
                Rect boundsTextNumber = new Rect();
                mPaintTextNumber.getTextBounds(number, 0, number.length(), boundsTextNumber);
                final RectF rectTextNumber = new RectF(
                        cornerA.x,
                        cornerA.y - boundsTextNumber.height(),
                        cornerA.x + boundsTextNumber.width(),
                        cornerA.y
                );
                final Path pathTextNumber = new Path();
                pathTextNumber.moveTo(cornerA.x, cornerA.y);
                pathTextNumber.lineTo(Math.max(cornerB.x, (cornerA.x + rectTextNumber.width())), cornerB.y);
                pathTextNumber.addRect(rectTextNumber, Path.Direction.CCW);
                pathTextNumber.close();
                canvas.drawPath(pathTextNumber, mPaintTextNumberBackground);
                canvas.drawTextOnPath(number, pathTextNumber, 0, 0, mPaintTextNumber);

                // Draw text confidence
                final String confidence = String.format("%.2f%%", Math.min(plate.getRecognitionConfidence(), plate.getDetectionConfidence()));
                Rect boundsTextConfidence = new Rect();
                mPaintTextConfidence.getTextBounds(confidence, 0, confidence.length(), boundsTextConfidence);
                final RectF rectTextConfidence = new RectF(
                        cornerD.x,
                        cornerD.y,
                        cornerD.x + boundsTextConfidence.width(),
                        cornerD.y + boundsTextConfidence.height()
                );
                final Path pathTextConfidence = new Path();
                final double dx = cornerC.x - cornerD.x;
                final double dy = cornerC.y - cornerD.y;
                final double angle = Math.atan2(dy, dx);
                final double cosT = Math.cos(angle);
                final double sinT = Math.sin(angle);
                final float Cx = cornerD.x + rectTextConfidence.width();
                final float Cy = cornerC.y;
                final PointF cornerCC = new PointF((float)(Cx * cosT - Cy * sinT), (float)(Cy * cosT + Cx * sinT));
                final PointF cornerDD = new PointF((float)(cornerD.x * cosT - cornerD.y * sinT), (float)(cornerD.y * cosT + cornerD.x * sinT));
                pathTextConfidence.moveTo(cornerDD.x, cornerDD.y + boundsTextConfidence.height());
                pathTextConfidence.lineTo(cornerCC.x, cornerCC.y + boundsTextConfidence.height());
                pathTextConfidence.addRect(rectTextConfidence, Path.Direction.CCW);
                pathTextConfidence.close();
                canvas.drawPath(pathTextConfidence, mPaintTextConfidenceBackground);
                canvas.drawTextOnPath(confidence, pathTextConfidence, 0, 0, mPaintTextConfidence);
            }
        }
    }
}