package com.example.videoplayer.clip.media.decoder;

import android.media.MediaCodec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

public class Frame {
    private ByteBuffer buffer = null;

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    @Nullable
    public final ByteBuffer getBuffer() {
        return this.buffer;
    }

    public final void setBuffer(@Nullable ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @NonNull
    public final MediaCodec.BufferInfo getBufferInfo() {
        return this.bufferInfo;
    }

    public final void setBufferInfo(@NonNull MediaCodec.BufferInfo info) {
        this.bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags);
    }
}
