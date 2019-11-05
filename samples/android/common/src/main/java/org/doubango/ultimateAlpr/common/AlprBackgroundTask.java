/*
 * Copyright (C) 2016-2019 Doubango AI <https://www.doubango.org>
 * License: For non-commercial use only
 * Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
 * WebSite: https://www.doubango.org/webapps/alpr/
 */
package org.doubango.ultimateAlpr.common;


import android.os.Handler;
import android.os.HandlerThread;

public class AlprBackgroundTask {

    private Handler mHandler;
    private HandlerThread mThread;

    public synchronized final Handler getHandler() {
        return mHandler;
    }
    public synchronized final boolean isRunning() { return mHandler != null; }

    public synchronized void start(final String threadName) {
        if (mThread != null) {
            return;
        }
        mThread = new HandlerThread(threadName);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    public synchronized void stop() {
        if (mThread == null) {
            return;
        }
        mThread.quitSafely();
        try {
            mThread.join();
            mThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void post(final Runnable r) {
        if (mHandler != null) {
            mHandler.post(r);
        }
    }
}