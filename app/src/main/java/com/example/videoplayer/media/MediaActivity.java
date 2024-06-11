package com.example.videoplayer.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.videoplayer.MainActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityMediaBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaActivity extends AppCompatActivity {

    private static final String TAG = "MediaActivity";
    private ActivityMediaBinding binding;

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private List<String> playList = new ArrayList<>();
    private MediaPlayManger playerManger;
    private StringBuilder playProgress = new StringBuilder("00:00");
    private int currentProcess = 0;
    private int durationProcess = 0;
    private boolean isFullScreen = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentIndex = 0;

    private final MediaPlayManger.PlayerMangerListener listener = new MediaPlayManger.PlayerMangerListener() {
        @Override
        public void onVideoDurationInitialized(int millDuration) {
            runOnUiThread(() -> {
                durationProcess = millDuration;
                binding.totalProcess.setText(MediaPlayManger.millTimeToClock(millDuration, true));
            });
        }

        @Override
        public void onVideoCurrentPositionChanged(int millPosition) {
            runOnUiThread(() -> {
                playProgress.setLength(0);
                playProgress.append(MediaPlayManger.millTimeToClock(millPosition, false));
                binding.currentProcess.setText(playProgress);
                binding.seekProcess.setProgress(millPosition * 1000 / durationProcess);
            });
        }

        @Override
        public void onVideoPlayedCompletion() {
            handler.postDelayed(() -> {
                if (playerManger.isVideoPlaying()) {
                    binding.mediaPlayBtn.setText("STOP");
                } else {
                    binding.mediaPlayBtn.setText("PLAY");
                }
            }, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_media);

        initListener();
        initData();

        playerManger = MediaPlayManger.getInstance();
        playerManger.initPlayer(getApplication(), binding.mediaSurfaceView, 800, listener);
        playerManger.prepareVideo(getPath(0), false);

//        try {
//            AssetFileDescriptor afd1 = getApplication().getAssets().openFd("video/video1.mp4");
//            playerManger.prepareVideo(afd1, true);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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

    private void initListener() {
        binding.mediaPlayBtn.setOnClickListener(view -> {
            if (playerManger.isVideoPlaying()) {
                playerManger.pauseVideo();
            } else {
                playerManger.playVideo();
            }

            handler.postDelayed(() -> {
                if (playerManger.isVideoPlaying()) {
                    binding.mediaPlayBtn.setText("STOP");
                } else {
                    binding.mediaPlayBtn.setText("PLAY");
                }
            }, 50);
        });

        binding.mediaBackBtn.setOnClickListener(v -> startActivity(new Intent(MediaActivity.this, MainActivity.class)));

        binding.mediaFaBtn.setOnClickListener(v -> playerManger.updatePlayProgress(MediaPlayManger.SeekMode.FORWARD_10));

        binding.mediaReBtn.setOnClickListener(v -> playerManger.updatePlayProgress(MediaPlayManger.SeekMode.REWIND_10));

        binding.mediaPreBtn.setOnClickListener(v -> {
            boolean b = playerManger.prepareVideo(getPath(1), true);
            if(!b) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            }
        });

        binding.mediaNextBtn.setOnClickListener(v -> {
            boolean b = playerManger.prepareVideo(getPath(2), true);
            if(!b) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            }
        });

        binding.mediaFullBtn.setOnClickListener(v -> {
            if (isFullScreen) {
                playerManger.fullScreen(false);
                isFullScreen = false;
            } else {
                playerManger.fullScreen(true);
                isFullScreen = true;
            }
        });

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
                playerManger.seekToPlay(currentProcess * durationProcess / 1000);
            }
        });
    }

    private void initData() {
        playList.add(ROOT_PATH + "/video5.mp4");
        playList.add(ROOT_PATH + "/video3.mp4");
        playList.add(ROOT_PATH + "/video4.mp4");
    }

    private String getPath(int mode) {
        int index = 0;
        if (mode == 1) {
            // 获取上一个路径
            if (currentIndex == 0) {
                index = playList.size() - 1;
            } else {
                index = currentIndex - 1;
            }
        } else if (mode == 2) {
            // 获取下一个路径
            if (currentIndex != (playList.size() - 1)) {
                index = currentIndex + 1;
            }
        }
        currentIndex = index;
        return playList.get(index);
    }
}