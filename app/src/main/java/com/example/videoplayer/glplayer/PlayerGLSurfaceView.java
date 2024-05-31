package com.example.videoplayer.glplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.TimedValueQueue;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.video.VideoFrameMetadataListener;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;


@OptIn(markerClass = UnstableApi.class)
public final class PlayerGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "PlayerGLSurfaceView";

    public interface VideoProcessor {

        void initialize();

        void setSurfaceSize(int width, int height);

        void draw(int frameTexture, long frameTimestampUs, float[] transformMatrix);

        void release();
    }

    private static final int EGL_PROTECTED_CONTENT_EXT = 0x32C0;

    private final VideoRenderer renderer;
    private final Handler mainHandler;

    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private ExoPlayer player;

    public PlayerGLSurfaceView(Context context, boolean requireSecureContext, VideoProcessor videoProcessor) {
        super(context);
        renderer = new VideoRenderer(videoProcessor);
        mainHandler = new Handler(Looper.getMainLooper());
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);

        setEGLContextFactory(new EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
                int[] paramsList = requireSecureContext
                        ? new int[] { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL_PROTECTED_CONTENT_EXT, EGL14.EGL_TRUE, EGL14.EGL_NONE }
                        : new int[] { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
                return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, paramsList);
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                egl.eglDestroyContext(display, context);
            }
        });

        setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory() {
            @Override
            public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
                int[] paramsList = requireSecureContext
                                ? new int[] {EGL_PROTECTED_CONTENT_EXT, EGL14.EGL_TRUE, EGL10.EGL_NONE}
                                : new int[] {EGL10.EGL_NONE};
                return egl.eglCreateWindowSurface(display, config, nativeWindow, paramsList);
            }

            @Override
            public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                egl.eglDestroySurface(display, surface);
            }
        });

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setPlayer(ExoPlayer player) {
        if (player == this.player) {
            return;
        }
        if (this.player != null) {
            if (surface != null) {
                this.player.clearVideoSurface(surface);
            }
            this.player.clearVideoFrameMetadataListener(renderer);
        }
        this.player = player;
        if (this.player != null) {
            this.player.setVideoFrameMetadataListener(renderer);
            this.player.setVideoSurface(surface);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mainHandler.post(() -> {
            if (surface != null) {
                if (player != null) {
                    player.setVideoSurface(null);
                }
                releaseSurface(surfaceTexture, surface);
                surfaceTexture = null;
                surface = null;
            }
        });
    }

    private void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
        mainHandler.post(() -> {
            SurfaceTexture oldSurfaceTexture = this.surfaceTexture;
            Surface oldSurface = PlayerGLSurfaceView.this.surface;
            this.surfaceTexture = surfaceTexture;
            this.surface = new Surface(surfaceTexture);
            releaseSurface(oldSurfaceTexture, oldSurface);
            if (player != null) {
                player.setVideoSurface(surface);
            }
        });
    }

    private static void releaseSurface(SurfaceTexture oldSurfaceTexture, Surface oldSurface) {
        if (oldSurfaceTexture != null) {
            oldSurfaceTexture.release();
        }
        if (oldSurface != null) {
            oldSurface.release();
        }
    }

    @UnstableApi
    private final class VideoRenderer implements GLSurfaceView.Renderer, VideoFrameMetadataListener {

        private final VideoProcessor videoProcessor;
        private final AtomicBoolean frameAvailable;
        private final TimedValueQueue<Long> sampleTimestampQueue;
        private final float[] transformMatrix;

        private int texture;
        private SurfaceTexture surfaceTexture;

        private boolean initialized;
        private int width;
        private int height;
        private long frameTimestampUs;

        public VideoRenderer(VideoProcessor videoProcessor) {
            this.videoProcessor = videoProcessor;
            frameAvailable = new AtomicBoolean();
            sampleTimestampQueue = new TimedValueQueue<>();
            width = -1;
            height = -1;
            frameTimestampUs = C.TIME_UNSET;
            transformMatrix = new float[16];
        }

        @Override
        public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
            try {
                texture = GlUtil.createExternalTexture();
            } catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to create an external texture", e);
            }
            surfaceTexture = new SurfaceTexture(texture);
            surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
                    frameAvailable.set(true);
                    requestRender();
            });
            onSurfaceTextureAvailable(surfaceTexture);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            this.width = width;
            this.height = height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (videoProcessor == null) {
                return;
            }
            if (!initialized) {
                videoProcessor.initialize();
                initialized = true;
            }
            if (width != -1 && height != -1) {
                videoProcessor.setSurfaceSize(width, height);
                width = -1;
                height = -1;
            }
            if (frameAvailable.compareAndSet(true, false)) {
                SurfaceTexture surfaceTexture = Assertions.checkNotNull(this.surfaceTexture);
                surfaceTexture.updateTexImage();
                long lastFrameTimestampNs = surfaceTexture.getTimestamp();
                @Nullable Long frameTimestampUs = sampleTimestampQueue.poll(lastFrameTimestampNs);
                if (frameTimestampUs != null) {
                    this.frameTimestampUs = frameTimestampUs;
                }
                surfaceTexture.getTransformMatrix(transformMatrix);
            }
            videoProcessor.draw(texture, frameTimestampUs, transformMatrix);
        }

        @Override
        public void onVideoFrameAboutToBeRendered(long presentationTimeUs, long releaseTimeNs, Format format, MediaFormat mediaFormat) {
            sampleTimestampQueue.add(releaseTimeNs, presentationTimeUs);
        }
    }
}
