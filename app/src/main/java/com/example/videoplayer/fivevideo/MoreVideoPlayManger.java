package com.example.videoplayer.fivevideo;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.videoplayer.media.MediaGLView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoreVideoPlayManger {

    private static final String TAG = "MediaPlayManger";

    private static volatile MoreVideoPlayManger playManger;
    private List<MediaPlayer> playerList = new ArrayList<>();
    private List<MediaGLView> viewList = new ArrayList<>();
    private int defaultWidth = 0;
    private PlayerMangerListener listener;
    private boolean isPlaying = false;

    private MoreVideoPlayManger() { }

    public static MoreVideoPlayManger getInstance() {
        if (playManger == null) {
            synchronized (MoreVideoPlayManger.class) {
                if (playManger == null) {
                    playManger = new MoreVideoPlayManger();
                }
            }
        }
        return playManger;
    }

    public void initPlayerList(List<MediaGLView> viewList, int width, PlayerMangerListener listener) {
        this.viewList = viewList;
        this.defaultWidth = width;
        this.listener = listener;

        for (int i = 0; i < viewList.size(); i++) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            if (i != 0) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
            }
            playerList.add(mediaPlayer);

            MediaGLView mediaGLView = viewList.get(i);
            mediaGLView.setSurfaceListener(surface -> {
                mediaPlayer.setSurface(surface);
                mediaGLView.releaseSurface();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                listener.onVideoPlayedCompletion();
            });
        }

    }

    public boolean prepareVideo(List<String> videoPathList) {
        if (playerList.isEmpty() || videoPathList.isEmpty()) {
            return false;
        }
        try {
            for (int i = 0; i < videoPathList.size(); i++) {
                MediaPlayer mediaPlayer = playerList.get(i);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(videoPathList.get(i));
                mediaPlayer.prepare();

                // 修改显示尺寸，确认显示尺寸
                fixScreenSize(mediaPlayer, viewList.get(i));

                mediaPlayer.start();
            }
            isPlaying = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean prepareAssetsVideo(List<AssetFileDescriptor> videoPathList) {
        if (playerList.isEmpty() || videoPathList.isEmpty()) {
            return false;
        }
        try {
            for (int i = 0; i < videoPathList.size(); i++) {
                AssetFileDescriptor descriptor = videoPathList.get(i);
                MediaPlayer mediaPlayer = playerList.get(i);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                mediaPlayer.prepare();

                // 修改显示尺寸，确认显示尺寸
                fixScreenSize(mediaPlayer, viewList.get(i));

                mediaPlayer.start();
            }
            isPlaying = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void pauseVideo() {
        if (isPlaying) {
            for (MediaPlayer mediaPlayer : playerList) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
            isPlaying = false;
        }
    }

    public void playVideo() {
        if (!isPlaying) {
            for (MediaPlayer mediaPlayer : playerList) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
            isPlaying = true;
        }
    }

    public boolean isVideoPlaying() {
        return isPlaying;
    }

    public void releasePlayer() {
        if (!playerList.isEmpty()) {
            for (MediaPlayer mediaPlayer : playerList) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
            }
        }
        if (!viewList.isEmpty()) {
            for (MediaGLView mediaGLView : viewList) {
                if (mediaGLView != null) {
                    mediaGLView.releaseAll();
                }
            }
        }
    }

    private void fixScreenSize(MediaPlayer mediaPlayer, MediaGLView mediaGLView) {
        // 获取原始视频宽高
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mediaGLView.getLayoutParams();
        layoutParams.width = defaultWidth;
        layoutParams.height = layoutParams.width * videoHeight / videoWidth;
        // 重新设置窗口尺寸高
        mediaGLView.setLayoutParams(layoutParams);
    }

    public interface PlayerMangerListener {

        void onVideoDurationInitialized(int millDuration);

        void onVideoCurrentPositionChanged(int millPosition);

        void onVideoPlayedCompletion();
    }
}
