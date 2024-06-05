package com.example.videoplayer.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import androidx.annotation.Nullable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MediaGLView extends GLSurfaceView
        implements MediaGLRender.RendererListener, SurfaceTexture.OnFrameAvailableListener {

    public interface SurfaceListener {
        void onSurfaceCreated(Surface surface);
    }

    private MediaGLRender mRenderer;
    @Nullable
    private SurfaceTexture surfaceTexture;
    @Nullable
    private Surface videoSurface;
    private boolean isSurfaceUpdated = false;
    @Nullable
    private SurfaceListener listener = null;

    public MediaGLView(Context context) {
        super(context);
        initRender(context);
    }

    public MediaGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRender(context);
    }

    private void initRender(Context context){
        setEGLContextClientVersion(2);
        mRenderer = new MediaGLRender(context, this);
        setRenderer(mRenderer);
    }

    public void setSurfaceListener(SurfaceListener listener) {
        this.listener = listener;
    }

    public void releaseSurface() {
        if (videoSurface != null) {
            videoSurface.release();
        }
    }

    public void releaseAll() {
        if (videoSurface != null) {
            videoSurface.release();
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
        if (mRenderer != null) {
            mRenderer.release();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig, int textureId) {
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
        videoSurface = new Surface(surfaceTexture);
        if (listener != null) {
            listener.onSurfaceCreated(videoSurface);
        }
        synchronized(this) {
            isSurfaceUpdated = false;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        synchronized (this) {
            if (isSurfaceUpdated && surfaceTexture != null) {
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(mRenderer.getSTMatrix());
                isSurfaceUpdated = false;
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        synchronized (this) {
            isSurfaceUpdated = true;
        }
    }
}
