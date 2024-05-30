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
import com.example.videoplayer.databinding.ActivityPlayFiveOnHuaweiBinding;
import com.example.videoplayer.databinding.ActivityPlayFiveSameBinding;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class PlayFiveSameActivity extends AppCompatActivity {

    private static final String TAG = "PlayFiveSameActivity";

    private ActivityPlayFiveSameBinding dataBinding;
    private boolean isNullPlayer = true;
    private List<ExoPlayer> playerList = new ArrayList<>();
    private ProgressiveMediaSource.Factory factory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_play_five_same);

        factory = new ProgressiveMediaSource.Factory(new DefaultDataSource.Factory(this))
                .setDrmSessionManagerProvider(unusedMediaItem -> DrmSessionManager.DRM_UNSUPPORTED);

        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNullPlayer) {
            Log.d(TAG, "onResume: initPlayerList");
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_5.mp4")), dataBinding.surfaceF1, 0));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_1.mp4")), dataBinding.surfaceF2, 1));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_2.mp4")), dataBinding.surfaceF3, 1));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_3.mp4")), dataBinding.surfaceF4, 1));
            playerList.add(initSurfaceViewPlayer(MediaItem.fromUri(Uri.parse("asset:///video5_4.mp4")), dataBinding.surfaceF5, 1));
            isNullPlayer = false;
        }
    }

    private ExoPlayer initSurfaceViewPlayer(MediaItem mediaItem, SurfaceView surfaceView, int isVolume) {
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        if (isVolume == 1) {
            player.setTrackSelectionParameters(
                    player.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                            .build());
        }

        MediaSource mediaSource = factory.createMediaSource(mediaItem);

        player.setMediaSource(mediaSource);
        player.setVideoSurfaceView(surfaceView);
        player.prepare();

        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        return player;
    }

    private void initListener() {
        dataBinding.playFbtn.setOnClickListener(v -> {
            for (ExoPlayer exoPlayer : playerList) {
                if (exoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) {
                    if (exoPlayer.isPlaying()) {
                        exoPlayer.pause();
                        dataBinding.playFbtn.setText("PLAY");
                    } else {
                        Log.d(TAG, "initListener: liuxin play start");
                        exoPlayer.play();
                        dataBinding.playFbtn.setText("PAUSE");
                        Log.d(TAG, "initListener: liuxin play end");
                    }
                }
            }
        });

        dataBinding.backFbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
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