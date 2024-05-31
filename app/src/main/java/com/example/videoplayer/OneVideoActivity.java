package com.example.videoplayer;

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
import android.widget.SeekBar;

import com.example.videoplayer.databinding.ActivityOneVideoBinding;

import java.util.Locale;

public class OneVideoActivity extends AppCompatActivity {

    private static final String TAG = "OneVideoActivity";

    private ActivityOneVideoBinding dataBinding;
    private static ExoPlayer player;
    private int playbackState;
    private StringBuilder playProgress = new StringBuilder("00:00");

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video5.mp4";
//    private String path = "asset:///video5.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_one_video);

        if (player == null) {
            initTextureViewPlayer();
        }

        initListener();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initTextureViewPlayer() {
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(unusedMediaItem -> DrmSessionManager.DRM_UNSUPPORTED)
                .createMediaSource(MediaItem.fromUri(Uri.parse(path)));

        player.setMediaSource(mediaSource);
        player.setVideoTextureView(dataBinding.view2);

        player.prepare();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                playbackState = state;
                switch (state) {
                    case ExoPlayer.STATE_IDLE:
                        // 播放器处于空闲状态
                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        // 播放器正在缓冲
                        break;
                    case ExoPlayer.STATE_READY:
                        dataBinding.totalProcess.setText(longTimeToClockTime(player.getDuration()));
                        updatePlayProgress();
                        // 播放器准备就绪
                        break;
                    case ExoPlayer.STATE_ENDED:
                        updatePlayProgress();
                        // 播放结束
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    //切换显示
                    updatePlayProgress();
                    dataBinding.playBtn.setText("Play");
                } else {
                    dataBinding.playBtn.setText("Pause");
                }
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                updatePlayProgress();
            }

        });

        this.player = player;
    }

    private void initListener() {
        dataBinding.backObtn.setOnClickListener(v -> {
            Intent intent = new Intent(OneVideoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dataBinding.playBtn.setOnClickListener(v -> {
            if (playbackState == ExoPlayer.STATE_READY) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });

        dataBinding.fastBtn.setOnClickListener(v -> {
            long currentPosition = player.getCurrentPosition();
            long newPosition = Math.min(player.getDuration(), currentPosition + 10000);
            player.seekTo(newPosition);
        });

        dataBinding.backBtn.setOnClickListener(v -> {
            long currentPosition = player.getCurrentPosition();
            long newPosition = Math.max(0, currentPosition - 10000);
            player.seekTo(newPosition);
        });

        dataBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo((progress / 100) * player.getDuration());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.play();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            if (player != null) {
                player.release();
                player = null;
            }
        }
    }

    public String longTimeToClockTime(long msTime) {
        int time = (int) msTime / 1000;
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void updatePlayProgress() {
        long currentPosition = player.getCurrentPosition();
        playProgress.setLength(0);
        playProgress.append(longTimeToClockTime(currentPosition));
        dataBinding.currentProcess.setText(playProgress);
        dataBinding.seekBar.setProgress((int) (currentPosition * 1000 / player.getDuration()));

        if (player.isPlaying()) {
            // 如果在播放，就循环调用自己，触发进度条更新
            dataBinding.seekBar.postDelayed(this::updatePlayProgress, 100);
        }
    }
}