package com.example.videoplayer.fivevideo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.example.videoplayer.MainActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityPlayFiveBinding;
import com.example.videoplayer.media.MediaGLView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayFiveActivity extends AppCompatActivity {

    private ActivityPlayFiveBinding binding;
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DVR_VIDEO/";

    private MoreVideoPlayManger playManger;
    private List<String> pathList = new ArrayList<>();
    private List<AssetFileDescriptor> pathAssetsList = new ArrayList<>();
    private List<MediaGLView> viewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_five);

        initData();

        playManger = MoreVideoPlayManger.getInstance();

        playManger.initPlayerList(viewList, 1120, new MoreVideoPlayManger.PlayerMangerListener() {
            @Override
            public void onVideoDurationInitialized(int millDuration) {

            }

            @Override
            public void onVideoCurrentPositionChanged(int millPosition) {

            }

            @Override
            public void onVideoPlayedCompletion() {

            }
        });

//        playManger.prepareVideo(pathList);
        playManger.prepareAssetsVideo(pathAssetsList);


        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(PlayFiveActivity.this, MainActivity.class));
            finish();
        });

        binding.playBtn.setOnClickListener(v -> {
            if (playManger.isVideoPlaying()) {
                playManger.pauseVideo();
                binding.playBtn.setText("PLAY");
            } else {
                playManger.playVideo();
                binding.playBtn.setText("STOP");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playManger.releasePlayer();
    }

    private void initData() {
        viewList.add(binding.glPlayer1);
        viewList.add(binding.glPlayer2);
        viewList.add(binding.glPlayer3);
        viewList.add(binding.glPlayer4);

        pathList.add(ROOT_PATH + "/2024-05-23-Thu-08-21-08-26/2024-05-23-08-23-00-DUA.mp4");
        pathList.add(ROOT_PATH + "/2024-05-23-Thu-08-21-08-26/2024-05-23-08-23-00-DUF.mp4");
        pathList.add(ROOT_PATH + "/2024-05-23-Thu-08-21-08-26/2024-05-23-08-23-00-DUI.mp4");
        pathList.add(ROOT_PATH + "/2024-05-23-Thu-08-21-08-26/2024-05-23-08-23-00-DUR.mp4");

        try {
            AssetFileDescriptor afd1 = getApplication().getAssets().openFd("video/video1.mp4");
            AssetFileDescriptor afd2 = getApplication().getAssets().openFd("video/video2.mp4");
            AssetFileDescriptor afd3 = getApplication().getAssets().openFd("video/video3.mp4");
            AssetFileDescriptor afd4 = getApplication().getAssets().openFd("video/video4.mp4");
            pathAssetsList.add(afd1);
            pathAssetsList.add(afd2);
            pathAssetsList.add(afd3);
            pathAssetsList.add(afd4);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}