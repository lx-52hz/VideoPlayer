package com.example.videoplayer.clip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;

import com.example.videoplayer.R;;
import com.example.videoplayer.clip.media.decoder.Frame;
import com.example.videoplayer.clip.media.decoder.AudioDecoder;
import com.example.videoplayer.clip.media.decoder.BaseDecoder;
import com.example.videoplayer.clip.media.decoder.IDecoderStateListener;
import com.example.videoplayer.clip.media.decoder.VideoDecoder;
import com.example.videoplayer.clip.oepngl.CustomerGLRenderer;
import com.example.videoplayer.clip.oepngl.VideoDrawer;
import com.example.videoplayer.databinding.ActivityClipBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClipActivity extends AppCompatActivity {

    private ActivityClipBinding dataBinding;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video2.mp4";
    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    private CustomerGLRenderer render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_clip);

        render = new CustomerGLRenderer();
        initVideo();
        render.setSurface(dataBinding.clipSurf);
    }

    private void initVideo() {
        VideoDrawer drawer = new VideoDrawer();
        drawer.setAlpha(1.f);
        drawer.setVideoSize(1920, 1080);
        render.addDrawer(drawer);
        drawer.getSurfaceTexture(new VideoDrawer.OnSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureCreate(SurfaceTexture texture) {
                initPlayer(texture);
            }
        });
    }


    private void initPlayer(SurfaceTexture texture) {
        VideoDecoder decoder = new VideoDecoder(path, null, new Surface(texture));
        threadPool.submit(decoder);
        decoder.goOn();
        decoder.setStateListener(new IDecoderStateListener() {
            @Override
            public void decoderPrepare(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decoderReady(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decoderRunning(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decoderPause(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decodeOneFrame(@Nullable BaseDecoder decoder, @NonNull Frame frame) {
                render.notifySwap(frame.getBufferInfo().presentationTimeUs);
            }

            @Override
            public void decoderFinish(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decoderDestroy(@Nullable BaseDecoder decoder) {

            }

            @Override
            public void decoderError(@Nullable BaseDecoder decoder, @NonNull String msg) {

            }
        });

        AudioDecoder audioDecoder = new AudioDecoder(path);
        threadPool.submit(audioDecoder);
        audioDecoder.goOn();
    }
}