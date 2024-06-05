package com.example.videoplayer.clip.oepngl;

import android.opengl.EGLContext;
import android.opengl.EGLSurface;

import androidx.annotation.Nullable;

public class EGLSurfaceHolder {
    private final String TAG = "EGLSurfaceHolder";
    private EGLCore mEGLCore;
    private EGLSurface mEGLSurface;

    public final void init(@Nullable EGLContext shareContext, int flags) {
        this.mEGLCore = new EGLCore();
        EGLCore var10000 = this.mEGLCore;
        var10000.init(shareContext, flags);
    }

    public final void createEGLSurface(@Nullable Object surface, int width, int height) {
        EGLCore var10001;
        EGLSurface var4;
        if (surface != null) {
            var10001 = this.mEGLCore;

            var4 = var10001.createWindowSurface(surface);
        } else {
            var10001 = this.mEGLCore;

            var4 = var10001.createOffScreenSurface(width, height);
        }

        this.mEGLSurface = var4;
    }

    public final void makeCurrent() {
        if (this.mEGLSurface != null) {
            EGLCore var10000 = this.mEGLCore;

            EGLSurface var10001 = this.mEGLSurface;

            var10000.makeCurrent(var10001);
        }

    }

    public final void swapBuffers() {
        if (this.mEGLSurface != null) {
            EGLCore var10000 = this.mEGLCore;

            EGLSurface var10001 = this.mEGLSurface;

            var10000.swapBuffers(var10001);
        }

    }

    public final void setTimestamp(long timeMs) {
        if (this.mEGLSurface != null) {
            mEGLCore.setPt(mEGLSurface, timeMs * (long) 1000);
        }

    }

    public final void destroyEGLSurface() {
        if (this.mEGLSurface != null) {
            EGLCore var10000 = this.mEGLCore;
            if (var10000 == null) {
            }

            EGLSurface var10001 = this.mEGLSurface;
            if (var10001 == null) {
            }

            var10000.destroySurface(var10001);
            this.mEGLSurface = (EGLSurface) null;
        }

    }

    public final void release() {
        EGLCore var10000 = this.mEGLCore;
        if (var10000 == null) {
        }

        var10000.release();
    }
}
