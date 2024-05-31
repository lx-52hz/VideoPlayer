package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.videoplayer.databinding.ActivityPlayFullScreenBinding;

public class PlayFullScreenActivity extends AppCompatActivity {

    private ActivityPlayFullScreenBinding dataBinding;
    private SurfaceView currentOutputView;
    private static SurfaceControl surfaceControl;
    private static ExoPlayer player;
    private static Surface videoSurface;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video5.mp4";
//    private String path = "asset:///video5.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_play_full_screen);

        initListener();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onResume() {
        super.onResume();

        if (player == null) {
            initializePlayer();
        }
        setCurrentOutputView(dataBinding.playVideoSurf);

        dataBinding.playerControlView.setPlayer(player);
        dataBinding.playerControlView.show();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onPause() {
        super.onPause();
        dataBinding.playerControlView.setPlayer(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            if (surfaceControl != null) {
                surfaceControl.release();
                surfaceControl = null;
            }
            if (videoSurface != null) {
                videoSurface.release();
                videoSurface = null;
            }
            if (player != null) {
                player.release();
                player = null;
            }
        }
    }

    private void initListener() {
        attachSurfaceListener(dataBinding.playVideoSurfFull);
        attachSurfaceListener(dataBinding.playVideoSurf);

        dataBinding.surfInFullBtn.setOnClickListener(v -> {
            setCurrentOutputView(dataBinding.playVideoSurfFull);
            dataBinding.playVideoSurfFull.setVisibility(View.VISIBLE);
            dataBinding.playVideoSurf.setVisibility(View.GONE);
        });

        dataBinding.surfOutFullBtn.setOnClickListener(v -> {
            setCurrentOutputView(dataBinding.playVideoSurf);
            dataBinding.playVideoSurfFull.setVisibility(View.GONE);
            dataBinding.playVideoSurf.setVisibility(View.VISIBLE);
        });

        dataBinding.surfBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(unusedMediaItem -> DrmSessionManager.DRM_UNSUPPORTED)
                .createMediaSource(MediaItem.fromUri(Uri.parse(path)));

        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        player.setMediaSource(mediaSource);
        player.prepare();
//        player.play();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        surfaceControl = new SurfaceControl.Builder().setName("surface_demo").setBufferSize(0,0).build();
        videoSurface = new Surface(surfaceControl);
        player.setVideoSurface(videoSurface);
        this.player = player;
    }

    private void attachSurfaceListener(SurfaceView surfaceView) {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if (surfaceView == currentOutputView) {
                    reparent(surfaceView);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {}
        });
    }

    private void setCurrentOutputView(SurfaceView surfaceView) {
        currentOutputView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder().getSurface() != null) {
            reparent(surfaceView);
        }
    }

    private static void reparent(SurfaceView surfaceView) {
        if (surfaceView == null) {
            new SurfaceControl.Transaction()
                    .reparent(surfaceControl, null)
                    .setBufferSize(surfaceControl, 0,0)
                    .setVisibility(surfaceControl, false)
                    .apply();
        } else {
            SurfaceControl newParentSurfaceControl = surfaceView.getSurfaceControl();
            new SurfaceControl.Transaction()
                    .reparent(surfaceControl, newParentSurfaceControl)
                    .setBufferSize(surfaceControl, surfaceView.getWidth(), surfaceView.getHeight())
                    .setVisibility(surfaceControl, true)
                    .apply();
        }
    }
}