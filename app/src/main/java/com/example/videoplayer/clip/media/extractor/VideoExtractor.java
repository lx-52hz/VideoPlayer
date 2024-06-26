package com.example.videoplayer.clip.media.extractor;

import android.media.MediaFormat;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public final class VideoExtractor implements IExtractor {
    private final MMExtractor mMediaExtractor;

    @NonNull
    public MediaFormat getFormat() {
        return this.mMediaExtractor.getVideoFormat();
    }

    public int readBuffer(@NonNull ByteBuffer byteBuffer) {
        return this.mMediaExtractor.readBuffer(byteBuffer);
    }

    public long getCurrentTimestamp() {
        return this.mMediaExtractor.getCurrentTimestamp();
    }

    public int getSampleFlag() {
        return this.mMediaExtractor.getSampleFlag();
    }

    public long seek(long pos) {
        return this.mMediaExtractor.seek(pos);
    }

    public void setStartPos(long pos) {
        this.mMediaExtractor.setStartPos(pos);
    }

    public void stop() {
        this.mMediaExtractor.stop();
    }

    public VideoExtractor(@NonNull String path) {
        super();
        this.mMediaExtractor = new MMExtractor(path);
    }
}
