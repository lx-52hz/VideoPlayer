package com.example.videoplayer.media;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.LongDef;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MediaPlayManger {

    private static final String TAG = "MediaPlayManger";

    private static volatile MediaPlayManger playManger;
    private MediaPlayer mMediaPlayer;
    private PlayerMangerListener listener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MediaPlayManger() { }

    public static MediaPlayManger getInstance() {
        if (playManger == null) {
            synchronized (MediaPlayManger.class) {
                if (playManger == null) {
                    playManger = new MediaPlayManger();
                }
            }
        }
        return playManger;
    }

    public void initPlayer(MediaGLView mediaGLView, PlayerMangerListener listener) {
        this.listener = listener;
        mMediaPlayer = new MediaPlayer();
        // 创建监听，初始化player
        mediaGLView.setSurfaceListener(surface -> {
            if (mMediaPlayer == null) {
                Log.e(TAG, "initPlayer: player is null");
            }
            mMediaPlayer.setSurface(surface);
            mediaGLView.releaseSurface();
        });

        // 播放完成回调
        mMediaPlayer.setOnCompletionListener(mp -> listener.onVideoPlayedCompletion());
    }

    public void prepareVideo(String videoPath, boolean needPlay) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.setDataSource(videoPath);
                mMediaPlayer.prepare();

                // 获取第一帧画面
                mMediaPlayer.start();
                // 通知视频总时长
                listener.onVideoDurationInitialized(mMediaPlayer.getDuration());
                // 显示视频第一帧
                if (!needPlay && mMediaPlayer.isPlaying()) {
                    handler.postDelayed(() -> mMediaPlayer.pause(), 10);
                }
                // 更新进度条
                updateCurrentPosition();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void pauseVideo() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void playVideo() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            // 更新进度条
            updateCurrentPosition();
        }
    }

    public boolean isVideoPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int getVideoDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seekToPlay(int position) {
        Log.d(TAG, "seekToPlay: " + position);
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST);
            // 更新进度条
            updateCurrentPosition();
        }
    }

    public void updatePlayProgress(SeekMode seekMode) {
        int currentPosition = mMediaPlayer.getCurrentPosition();
        int seekToPosition = 0;
        if (seekMode == SeekMode.REWIND_10) {
            if (currentPosition < 10 * 1000) {
                seekToPosition = 0;
            } else {
                seekToPosition = currentPosition - 10 * 1000;
            }
        } else if (seekMode == SeekMode.FORWARD_10) {
            int duration = mMediaPlayer.getDuration();

            Log.d(TAG, "updatePlayProgress: " + duration + " " + currentPosition);
            if ((currentPosition + 10 * 1000) > duration) {
                seekToPosition = duration;
            } else {
                seekToPosition = currentPosition + 10 * 1000;
            }
        }
        Log.d(TAG, "updatePlayProgress: " + seekToPosition);
        seekToPlay(seekToPosition);
    }

    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    public void updateCurrentPosition() {
        int currentPosition = mMediaPlayer.getCurrentPosition();
        listener.onVideoCurrentPositionChanged(currentPosition);
        if (mMediaPlayer.isPlaying()) {
            // 如果在播放，就循环调用自己，触发进度条更新
            handler.postDelayed(this::updateCurrentPosition, 100);
        }
    }

    public static String millTimeToClock(int currentPosition, boolean isTotalTime) {
        if (isTotalTime) {
            int time = (int) Math.ceil(currentPosition / 1000f);
            int minutes = (time % 3600) / 60;
            int seconds = time % 60;
            return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
        } else {
            return String.format(Locale.CHINA, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                    TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
        }
    }

    public interface PlayerMangerListener {

        void onVideoDurationInitialized(int millDuration);

        void onVideoCurrentPositionChanged(int millPosition);

        void onVideoPlayedCompletion();
    }

    public enum SeekMode {
        FORWARD_10, REWIND_10
    }
}
