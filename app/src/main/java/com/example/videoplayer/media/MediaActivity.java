package com.example.videoplayer.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;

import com.example.videoplayer.MainActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityMediaBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MediaActivity extends AppCompatActivity {

    private static final String TAG = "MediaActivity";

    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video5.mp4";
    private ActivityMediaBinding binding;
    private MediaPlayManger playerManger;
    private StringBuilder playProgress = new StringBuilder("00:00");
    private int currentProcess = 0;

    private final MediaPlayManger.PlayerMangerListener listener = new MediaPlayManger.PlayerMangerListener() {
        @Override
        public void onVideoDurationInitialized(int millDuration) {
            runOnUiThread(() -> binding.totalProcess.setText(MediaPlayManger.millTimeToClock(millDuration, true)));
        }

        @Override
        public void onVideoCurrentPositionChanged(int millPosition) {
            runOnUiThread(() -> {
                playProgress.setLength(0);
                playProgress.append(MediaPlayManger.millTimeToClock(millPosition, false));
                binding.currentProcess.setText(playProgress);
                binding.seekProcess.setProgress(millPosition * 1000 / playerManger.getVideoDuration());
            });
        }

        @Override
        public void onVideoPlayedCompletion() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_media);

        playerManger = MediaPlayManger.getInstance();
        playerManger.initPlayer(binding.mediaSurfaceView, listener);
        playerManger.prepareVideo(path, false);


        binding.mediaPlayBtn.setOnClickListener(view -> {
            if (playerManger.isVideoPlaying()) {
                binding.mediaPlayBtn.setText("PLAY");
                playerManger.pauseVideo();
            } else {
                binding.mediaPlayBtn.setText("STOP");
                playerManger.playVideo();
            }
        });

        binding.mediaBackBtn.setOnClickListener(v -> startActivity(new Intent(MediaActivity.this, MainActivity.class)));

        binding.mediaFaBtn.setOnClickListener(v -> playerManger.updatePlayProgress(MediaPlayManger.SeekMode.FORWARD_10));

        binding.mediaReBtn.setOnClickListener(v -> playerManger.updatePlayProgress(MediaPlayManger.SeekMode.REWIND_10));

        binding.seekProcess.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentProcess = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerManger.seekToPlay(currentProcess * playerManger.getVideoDuration() / 1000);
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        playerManger.pauseVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerManger.releasePlayer();
    }
}