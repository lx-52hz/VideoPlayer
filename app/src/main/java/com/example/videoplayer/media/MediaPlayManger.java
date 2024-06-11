package com.example.videoplayer.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MediaPlayManger {

    private static final String TAG = "MediaPlayManger";

    private static volatile MediaPlayManger playManger;
    private MediaPlayer mMediaPlayer;
    private PlayerMangerListener listener;
    private MediaGLView mediaGLView;
    private int defaultWidth = 0;
    private int screenWidth = 0;
    private boolean isFullScreen = false;

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

    public void initPlayer(Context context, MediaGLView mediaGLView, int width, PlayerMangerListener listener) {
        this.mediaGLView = mediaGLView;
        this.defaultWidth = width;
        this.listener = listener;
        mMediaPlayer = new MediaPlayer();
        // 创建监听，初始化player
        mediaGLView.setSurfaceListener(surface -> {
            mMediaPlayer.setSurface(surface);
            mediaGLView.releaseSurface();
        });

        // 播放完成回调
        mMediaPlayer.setOnCompletionListener(mp -> {
            listener.onVideoPlayedCompletion();
        });

        // 获取全屏宽度
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getCurrentWindowMetrics().getBounds().width();
    }

    public boolean prepareVideo(String videoPath, boolean needPlay) {
        if (mMediaPlayer != null && !"".equals(videoPath) && new File(videoPath).exists()) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(videoPath);
                mMediaPlayer.prepare();

                // 修改显示尺寸，确认显示尺寸
                fixScreenSize();

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
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public boolean prepareVideo(AssetFileDescriptor afd, boolean needPlay) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mMediaPlayer.prepare();

                // 修改显示尺寸，确认显示尺寸
                fixScreenSize();

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
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
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

    public void seekToPlay(int position) {
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
            if ((currentPosition + 10 * 1000) > duration) {
                seekToPosition = duration;
            } else {
                seekToPosition = currentPosition + 10 * 1000;
            }
        }
        seekToPlay(seekToPosition);
    }

    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mediaGLView != null) {
            mediaGLView.releaseAll();
        }
    }

    public void updateCurrentPosition() {
        int currentPosition = mMediaPlayer.getCurrentPosition();
        if (mMediaPlayer.getDuration() - currentPosition < 100) {
            currentPosition = mMediaPlayer.getDuration();
        }
        listener.onVideoCurrentPositionChanged(currentPosition);
        if (mMediaPlayer.isPlaying()) {
            // 如果在播放，就循环调用自己，触发进度条更新
            handler.postDelayed(this::updateCurrentPosition, 100);
        }
    }

    public void fullScreen(boolean isFullScreen) {
        if (isFullScreen != this.isFullScreen) {
            this.isFullScreen = isFullScreen;
            fixScreenSize();
        }
    }

    private void fixScreenSize() {
        // 获取原始视频宽高
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mediaGLView.getLayoutParams();
        if (isFullScreen) {
            layoutParams.width = screenWidth;
        } else {
            layoutParams.width = defaultWidth;
        }
        layoutParams.height = layoutParams.width * videoHeight / videoWidth;
        // 重新设置窗口尺寸高
        mediaGLView.setLayoutParams(layoutParams);
    }

    public static String millTimeToClock(int currentPosition, boolean isTotalTime) {
        if (isTotalTime) {
            int time = (int) Math.ceil(currentPosition / 1000f);
            // 向上取整60.463 -> 61
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
