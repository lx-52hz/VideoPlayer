package com.example.videoplayer.clip.media.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MMExtractor {

    private static final String TAG = "MMExtractor";

    private MediaExtractor mExtractor = new MediaExtractor();
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    private long mCurSampleTime;
    private int mCurSampleFlag;
    private long mStartPos;

    public final MediaFormat getVideoFormat() {
        mVideoTrack = getMediaTrackIndex("video/");
        return mExtractor.getTrackFormat(this.mVideoTrack);
    }

    public final MediaFormat getAudioFormat() {
        mAudioTrack = getMediaTrackIndex("audio/");
        return mExtractor.getTrackFormat(this.mAudioTrack);
    }

    public final int readBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        this.selectSourceTrack();

        int readSampleCount = mExtractor.readSampleData(byteBuffer, 0);
        if (readSampleCount < 0) {
            return -1;
        } else {
            this.mCurSampleTime = mExtractor.getSampleTime();
            this.mCurSampleFlag = mExtractor.getSampleFlags();
            mExtractor.advance();
            return readSampleCount;
        }
    }

    private final void selectSourceTrack() {
        if (this.mVideoTrack >= 0) {
            mExtractor.selectTrack(this.mVideoTrack);
        } else if (this.mAudioTrack >= 0) {
            mExtractor.selectTrack(this.mAudioTrack);
        }

    }

    public final long seek(long pos) {
        if (mExtractor == null) {
            return 0;
        }

        mExtractor.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        return mExtractor.getSampleTime();
    }

    public final void stop() {
        this.mExtractor.release();
        this.mExtractor = null;
    }

    public final void setStartPos(long pos) {
        this.mStartPos = pos;
    }

    public final long getCurrentTimestamp() {
        return this.mCurSampleTime;
    }

    public final int getSampleFlag() {
        return this.mCurSampleFlag;
    }

    public MMExtractor(String path) {
        if (mExtractor != null) {
            try {
                mExtractor.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getMediaTrackIndex(String mediaType) {
        int trackIndex = -1;
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            //获取视频所在轨道
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            Log.d(TAG,"mime: " + mime + " trackFormat: " + mediaFormat);
            if (mime != null && !"".equals(mime) && mime.startsWith(mediaType)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }
}
