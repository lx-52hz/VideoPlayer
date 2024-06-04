package com.example.videoplayer.clip.media.decoder;

import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IDecoder extends Runnable {
    void pause();

    void goOn();

    long seekTo(long var1);

    long seekAndPlay(long var1);

    void stop();

    boolean isDecoding();

    boolean isSeeking();

    boolean isStop();

    void setStateListener(@Nullable IDecoderStateListener var1);

    int getWidth();

    int getHeight();

    long getDuration();

    long getCurTimeStamp();

    int getRotationAngle();

    @Nullable
    MediaFormat getMediaFormat();

    int getTrack();

    @NonNull
    String getFilePath();

    @NonNull
    IDecoder withoutSync();
}

