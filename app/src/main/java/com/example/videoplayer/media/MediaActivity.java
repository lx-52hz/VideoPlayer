package com.example.videoplayer.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;

import com.example.videoplayer.R;
import com.example.videoplayer.databinding.ActivityMediaBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MediaActivity extends AppCompatActivity {

    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video5.mp4";
    private MediaPlayer mMediaPlayer;

    private MediaGLView.SurfaceListener listener = new MediaGLView.SurfaceListener() {

        @Override
        public void onSurfaceCreated(Surface surface) {
            mMediaPlayer.setSurface(surface);
            binding.mediaSurfaceView.releaseSurface();
            try {
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    ActivityMediaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_media);

        binding.mediaSurfaceView.setSurfaceListener(listener);

        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}