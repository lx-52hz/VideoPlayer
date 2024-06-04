package com.example.videoplayer.clip.media.extractor;

import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

public interface IExtractor {
    @Nullable
    MediaFormat getFormat();

    int readBuffer(@NonNull ByteBuffer buffer);

    long getCurrentTimestamp();

    int getSampleFlag();

    long seek(long pos);

    void setStartPos(long pos);

    void stop();
}
