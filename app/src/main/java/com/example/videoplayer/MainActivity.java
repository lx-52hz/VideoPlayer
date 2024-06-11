package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.videoplayer.clip.ClipActivity;
import com.example.videoplayer.databinding.ActivityMainBinding;
import com.example.videoplayer.fivevideo.PlayFiveActivity;
import com.example.videoplayer.fivevideo.PlayFiveOnHuaweiActivity;
import com.example.videoplayer.glplayer.GLPlayerActivity;
import com.example.videoplayer.media.MediaActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private final static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initListener();
    }

    private void initListener() {
        mainBinding.playFullVideo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PlayFullScreenActivity.class)));
        mainBinding.play1Video.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OneVideoActivity.class)));
        mainBinding.play5VideoHuawei.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PlayFiveOnHuaweiActivity.class)));
        mainBinding.playClipVideo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ClipActivity.class)));
        mainBinding.playVideoWithGl.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GLPlayerActivity.class)));
        mainBinding.playMediaPlayer.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MediaActivity.class)));
        mainBinding.play5VideoMedia.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PlayFiveActivity.class)));
    }

    private void requestPermission() {
        int permission1 = ActivityCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[0]);
        int permission2 = ActivityCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[1]);
        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}