package com.example.videoplayer.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import androidx.annotation.Nullable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MediaGLView extends GLSurfaceView
        implements MediaGLRender.RendererListener, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "MediaGLView";

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

    private int mWidth, mHeight;

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

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 在 ACTION_DOWN 事件时触发 performClick 方法
                performClick();

                // 判断是否响应点击区域进行裁剪
                clipVideo(event.getX(), event.getY());

                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        this.mWidth = w;
        this.mHeight = h;
        super.onSizeChanged(w, h, oldW, oldH);
    }

    private void clipVideo(float x, float y) {
        int halfWidth = mWidth / 2;
        int halfHeight = mHeight / 2;
        MediaConstants.VerticesType type = MediaConstants.VerticesType.DEFAULT_VERTICES;
        if (x < halfWidth && y < halfHeight) {
            Log.d(TAG, "clipVideo: 点击左上区域");
            type = MediaConstants.VerticesType.LEFT_TOP_VERTICES;
        } else if (x < halfWidth && y >= halfHeight) {
            Log.d(TAG, "clipVideo: 点击左下区域");
            type = MediaConstants.VerticesType.LEFT_BOTTOM_VERTICES;
        } else if (x >= halfWidth && y < halfHeight) {
            Log.d(TAG, "clipVideo: 点击右上区域");
            type = MediaConstants.VerticesType.RIGHT_TOP_VERTICES;
        } else if (x >= halfWidth && y >= halfHeight) {
            Log.d(TAG, "clipVideo: 点击右下区域");
            type = MediaConstants.VerticesType.RIGHT_BOTTOM_VERTICES;
        }
        mRenderer.setTriangleVerticesData(type);
    }
}
