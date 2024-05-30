package com.example.videoplayer.fivevideo;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.example.videoplayer.MainActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityPlayFiveVideosBinding;

import java.util.ArrayList;
import java.util.List;

public class PlayFiveVideosActivity extends AppCompatActivity {

    private static final String TAG = "PlayFiveVideosActivity";

    private ActivityPlayFiveVideosBinding dataBinding;
    private boolean isNullPlayer = true;
    private List<ExoPlayer> playerList = new ArrayList<>();
    private ProgressiveMediaSource.Factory factory;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBinding = DataBindingUtil.setContentView(PlayFiveVideosActivity.this, R.layout.activity_play_five_videos);

        initListener();

        factory = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(this)).setDrmSessionManagerProvider(unusedMediaItem -> DrmSessionManager.DRM_UNSUPPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNullPlayer) {
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_5.mp4")), dataBinding.surface5, false));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_1.mp4")), dataBinding.surface1, true));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_2.mp4")), dataBinding.surface2, true));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_3.mp4")), dataBinding.surface3, true));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_4.mp4")), dataBinding.surface4, true));
            isNullPlayer = false;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "liuxin_onResume: " + dataBinding.parentView.getWidth() + " " + dataBinding.parentView.getHeight());
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer initSurfaceViewPlayer(MediaItem mediaItem, SurfaceView surfaceView, boolean isVolume) {
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();

        if (isVolume) {
            player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true).build());
        }

        MediaSource mediaSource = factory.createMediaSource(mediaItem);

        player.setMediaSource(mediaSource);
        player.setVideoSurfaceView(surfaceView);
        player.prepare();

        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        return player;
    }

    private void initListener() {
        dataBinding.playBtn.setOnClickListener(v -> {
            for (ExoPlayer exoPlayer : playerList) {
                if (exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
                    if (exoPlayer.isPlaying()) {
                        exoPlayer.pause();
                        dataBinding.playBtn.setText("PLAY");
                    } else {
                        exoPlayer.play();
                        dataBinding.playBtn.setText("PAUSE");
                    }
                }
            }
        });

        dataBinding.backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PlayFiveVideosActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            if (!playerList.isEmpty() || !isNullPlayer) {
                for (ExoPlayer exoPlayer : playerList) {
                    if (exoPlayer != null) {
                        exoPlayer.release();
                    }
                }
                playerList.clear();
            }
        }
    }

}