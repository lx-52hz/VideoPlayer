package com.example.videoplayer.clip.media.decoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface IDecoderStateListener {
    void decoderPrepare(@Nullable BaseDecoder decoder);

    void decoderReady(@Nullable BaseDecoder decoder);

    void decoderRunning(@Nullable BaseDecoder decoder);

    void decoderPause(@Nullable BaseDecoder decoder);

    void decodeOneFrame(@Nullable BaseDecoder decoder, @NonNull Frame frame);

    void decoderFinish(@Nullable BaseDecoder decoder);

    void decoderDestroy(@Nullable BaseDecoder decoder);

    void decoderError(@Nullable BaseDecoder decoder, @NonNull String msg);
}
