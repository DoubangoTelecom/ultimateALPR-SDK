/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */

/* Copyright (C) 2016-2019 Doubango Telecom <https://www.doubango.org>
 * File author: Mamadou DIOP (Doubango Telecom, France).
 * License: GPLv3. For commercial license please contact us.
 * Source code: https://github.com/DoubangoTelecom/compv
 * WebSite: http://compv.org
 */

/*
 * Most of the code from this file comes from CompV project: https://github.com/DoubangoTelecom/compv/blob/master/gl/compv_gl_renderer.cxx
 * The GL surface view allows displaying the YUV data without CPU-based conversion to RGB. As everything is done on GPU
 */

package org.doubango.ultimateAlpr.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.PixelFormat;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * GL surface view
 */
public class AlprGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG = AlprGLSurfaceView.class.getCanonicalName();

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private static final float[] TRIANGLE_VERTICES_DATA_0 = {
            1, -1, 0, 1, 1,     // 0: bottom/right
            1, 1, 0, 1, 0,      // 1: top/right
            -1, 1, 0, 0, 0,     // 2: top/left
            -1, -1, 0, 0, 1     // 3: bottom/left
    };
    private static final short[] INDICES_DATA_0 = {
            0, 1, 2,    // triangle #1: bottom/right, top/right, top/left
            2, 3, 0   // triangle #2: top/left, bottom/left, bottom/right
    };

    private static final float[] TRIANGLE_VERTICES_DATA_90 = {
            1, -1, 0, 1, 0,
            1, 1, 0, 0, 0,
            -1, 1, 0, 0, 1,
            -1, -1, 0, 1, 1,
    };
    private static final short[] INDICES_DATA_90 = {
            3, 0, 1,
            1, 2, 3
    };

    private static final float[] TRIANGLE_VERTICES_DATA_180 = {
            1, -1, 0, 0, 0,
            1, 1, 0, 0, 1,
            -1, 1, 0, 1, 1,
            -1, -1, 0, 1, 0,
    };
    private static final short[] INDICES_DATA_180 = {
            2, 3, 0,
            0, 1, 2
    };

    private static final float[] TRIANGLE_VERTICES_DATA_270 = {
            1, -1, 0, 0, 1,
            1, 1, 0, 1, 1,
            -1, 1, 0, 1, 0,
            -1, -1, 0, 0, 0,
    };
    private static final short[] INDICES_DATA_270 = {
            1, 2, 3,
            3, 0, 1
    };

    private FloatBuffer mTriangleVertices;
    private ShortBuffer mIndices;
    private int mJpegOrientation = 0;
    private boolean mJpegOrientationChanged = false;

    private static final String VERTEX_SHADER_SOURCE = "precision mediump float;" +
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = aPosition;\n" +
            "  vTextureCoord = aTextureCoord;\n" +
            "}\n";

    private static final String FRAGMENT_SHADER_SOURCE = "precision mediump float;" +
            "varying vec2 vTextureCoord;" +
            "" +
            "uniform sampler2D SamplerY; " +
            "uniform sampler2D SamplerU;" +
            "uniform sampler2D SamplerV;" +
            "" +
            "const mat3 yuv2rgb = mat3(1.164, 0, 1.596, 1.164, -0.391, -0.813, 1.164, 2.018, 0);" +
            "" +
            "void main() {    " +
            "    vec3 yuv = vec3(1.1643 * (texture2D(SamplerY, vTextureCoord).r - 0.06274)," +
            "                    texture2D(SamplerU, vTextureCoord).r - 0.5019," +
            "                    texture2D(SamplerV, vTextureCoord).r - 0.5019);" +
            "    vec3 rgb = yuv * yuv2rgb;    " +
            "    gl_FragColor = vec4(rgb, 1.0);" +
            "} ";

    private int mProgram;
    private int maPositionHandle;
    private int maTextureHandle;
    private int muSamplerYHandle;
    private int muSamplerUHandle;
    private int muSamplerVHandle;
    private int[] mTextureY = new int[1];
    private int[] mTextureU = new int[1];
    private int[] mTextureV = new int[1];

    private boolean mSurfaceCreated;

    private Image mImage = null;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AlprGLSurfaceView(android.content.Context context) {
        super(context);
        initGL();
    }

    public AlprGLSurfaceView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        initGL();
    }

    private void initGL() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mTriangleVertices = ByteBuffer.allocateDirect(TRIANGLE_VERTICES_DATA_0.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(TRIANGLE_VERTICES_DATA_0).position(0);

        mIndices = ByteBuffer.allocateDirect(INDICES_DATA_0.length
                * SHORT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(INDICES_DATA_0).position(0);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    /**
     *
     * @param
     */
    public void setImage(final Image image, final int jpegOrientation){
        if (!isReady()) {
            Log.i(TAG, "Not ready");
            image.close();
            return;
        }
        if (mImage != null) {
            Log.i(TAG, "Already rendering previous image");
            image.close();
            return;
        }

        // We need to save the image as the rendering is asynchronous
        mImage = image;

        if (mJpegOrientation != jpegOrientation) {
            Log.i(TAG, "Orientation changed: " + mJpegOrientation + " -> " + jpegOrientation);
            mJpegOrientation = jpegOrientation;
            mJpegOrientationChanged = true;
        }

        // Signal the surface as dirty to force redrawing
        requestRender();
    }

    public boolean isReady(){
        return mSurfaceCreated;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        mSurfaceCreated = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
        if (mImage != null) {
            mImage.close();
            mImage = null;
        }
        super.surfaceDestroyed(holder);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        if (mImage == null) {
            return;
        }

        if (mJpegOrientationChanged) {
            updateVertices();
            mJpegOrientationChanged = false;
        }

        final boolean swapSize = (mJpegOrientation % 180) != 0;
        final int imageWidth = mImage.getWidth();
        final int imageHeight = mImage.getHeight();

        final AlprUtils.AlprTransformationInfo tInfo = new AlprUtils.AlprTransformationInfo(swapSize ? imageHeight : imageWidth, swapSize ? imageWidth : imageHeight, getWidth(), getHeight());
        GLES20.glViewport(tInfo.getXOffset(), tInfo.getYOffset(), tInfo.getWidth(), tInfo.getHeight());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT /*| GLES20.GL_DEPTH_BUFFER_BIT*/);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        final Image.Plane[] planes = mImage.getPlanes();

        final ByteBuffer bufferY = planes[0].getBuffer();
        final ByteBuffer bufferU = planes[1].getBuffer();
        final ByteBuffer bufferV = planes[2].getBuffer();

        final int uvPixelStride = planes[1].getPixelStride();

        final int bufferWidthY = planes[0].getRowStride();
        final int bufferHeightY = imageHeight;
        final int bufferWidthUV = (planes[1].getRowStride() >> (uvPixelStride - 1));
        final int bufferHeightUV = (bufferHeightY >> 1); // Always YUV420_888 -> half-height

        final int uvFormat = uvPixelStride == 1 ? GLES20.GL_LUMINANCE : GLES20.GL_LUMINANCE_ALPHA; // Interleaved UV

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, bufferWidthY, bufferHeightY, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bufferY);
        GLES20.glUniform1i(muSamplerYHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, uvFormat, bufferWidthUV, bufferHeightUV, 0, uvFormat, GLES20.GL_UNSIGNED_BYTE, bufferU);
        GLES20.glUniform1i(muSamplerUHandle, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, uvFormat, bufferWidthUV, bufferHeightUV, 0, uvFormat, GLES20.GL_UNSIGNED_BYTE, bufferV);
        GLES20.glUniform1i(muSamplerVHandle, 2);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICES_DATA_0.length, GLES20.GL_UNSIGNED_SHORT, mIndices);

        mImage.close();
        mImage = null;
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // GLU.gluPerspective(glUnused, 45.0f, (float)width/(float)height, 0.1f, 100.0f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);
        GLES20.glDisable(GL10.GL_DITHER);

        String extensions = GLES20.glGetString(GL10.GL_EXTENSIONS);
        Log.d(TAG, "OpenGL extensions=" +extensions);

        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        mProgram = createProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muSamplerYHandle = GLES20.glGetUniformLocation(mProgram, "SamplerY");
        if (muSamplerYHandle == -1) {
            throw new RuntimeException("Could not get uniform location for SamplerY");
        }
        muSamplerUHandle = GLES20.glGetUniformLocation(mProgram, "SamplerU");
        if (muSamplerUHandle == -1) {
            throw new RuntimeException("Could not get uniform location for SamplerU");
        }
        muSamplerVHandle = GLES20.glGetUniformLocation(mProgram, "SamplerV");
        if (muSamplerVHandle == -1) {
            throw new RuntimeException("Could not get uniform location for SamplerV");
        }

        updateVertices();

        GLES20.glGenTextures(1, mTextureY, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glGenTextures(1, mTextureU, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glGenTextures(1, mTextureV, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void updateVertices() {
        mTriangleVertices.rewind();
        mIndices.rewind();

        switch (mJpegOrientation) {
            case 90:
                mTriangleVertices.put(TRIANGLE_VERTICES_DATA_90).position(0);
                mIndices.put(INDICES_DATA_90).position(0);
                break;
            case 180:
                mTriangleVertices.put(TRIANGLE_VERTICES_DATA_180).position(0);
                mIndices.put(INDICES_DATA_180).position(0);
                break;
            case 270:
                mTriangleVertices.put(TRIANGLE_VERTICES_DATA_270).position(0);
                mIndices.put(INDICES_DATA_270).position(0);
                break;
            case 0:
                mTriangleVertices.put(TRIANGLE_VERTICES_DATA_0).position(0);
                mIndices.put(INDICES_DATA_0).position(0);
                break;
            default:
                throw new RuntimeException("Invalid orientation:" + mJpegOrientation);
        }

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}