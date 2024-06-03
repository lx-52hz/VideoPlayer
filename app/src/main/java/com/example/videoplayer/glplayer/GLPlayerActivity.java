package com.example.videoplayer.glplayer;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.util.EventLogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.example.videoplayer.MainActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityGlplayerBinding;

@OptIn(markerClass = UnstableApi.class)
public class GLPlayerActivity extends AppCompatActivity {

    private static final String TAG = "GLPlayerActivity";

    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video5.mp4";

    private ActivityGlplayerBinding dataBinding;
    private ProgressiveMediaSource.Factory sourceFactory;
    private ExoPlayer player;
    private PlayerGLSurfaceView playerGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_glplayer);

        sourceFactory = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(this))
                .setDrmSessionManagerProvider(unusedMediaItem -> DrmSessionManager.DRM_UNSUPPORTED);

        playerGLSurfaceView = new PlayerGLSurfaceView(getApplicationContext(),
                false, new BitmapVideoProcessor(getApplicationContext()));

        dataBinding.glPlayerFrameLayout.addView(playerGLSurfaceView);
        dataBinding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(GLPlayerActivity.this, MainActivity.class));
            finish();
        });

        initializePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void initializePlayer() {
        MediaSource mediaSource = sourceFactory.createMediaSource(MediaItem.fromUri(Uri.parse(path)));
        player = new ExoPlayer.Builder(getApplicationContext()).build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setMediaSource(mediaSource);
        player.prepare();

        playerGLSurfaceView.setPlayer(player);
        dataBinding.playerControlView.setPlayer(player);
        player.addAnalyticsListener(new EventLogger());
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
        }
    }
}