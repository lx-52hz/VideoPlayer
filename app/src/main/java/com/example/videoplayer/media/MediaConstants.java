package com.example.videoplayer.media;

public class MediaConstants {

    public enum VerticesType {
        DEFAULT_VERTICES,
        LEFT_TOP_VERTICES,
        RIGHT_TOP_VERTICES,
        LEFT_BOTTOM_VERTICES,
        RIGHT_BOTTOM_VERTICES
    }

    public static final float[] DEFAULT_VERTICES = {
        -1.0f,  -1.0f,  0,  0.f,  0.f,
         1.0f,  -1.0f,  0,  1.f,  0.f,
        -1.0f,   1.0f,  0,  0.f,  1.f,
         1.0f,   1.0f,  0,  1.f,  1.f,
    };

    public static final float[] LEFT_TOP_VERTICES = {
        -1.0f,   0.0f,  0,  0.f,  1.f,
        -1.0f,   1.0f,  0,  0.f,  0.f,
         0.0f,   0.0f,  0,  1.f,  1.f,
         0.0f,   1.0f,  0,  1.f,  0.f,
    };

    public static final float[] RIGHT_TOP_VERTICES = {
         0.0f,   0.0f,  0,  0.f,  0.f,
         1.0f,   0.0f,  0,  1.f,  0.f,
         0.0f,   1.0f,  0,  0.f,  1.f,
         1.0f,   1.0f,  0,  1.f,  1.f,
    };

    public static final float[] LEFT_BOTTOM_VERTICES = {
        -1.0f,  -1.0f,  0,  0.f,  0.f,
         0.0f,  -1.0f,  0,  1.f,  0.f,
        -1.0f,   0.0f,  0,  0.f,  1.f,
         0.0f,   0.0f,  0,  1.f,  1.f,
    };

    public static final float[] RIGHT_BOTTOM_VERTICES = {
         0.0f,  -1.0f,  0,  0.f,  0.f,
         1.0f,  -1.0f,  0,  1.f,  0.f,
         0.0f,   0.0f,  0,  0.f,  1.f,
         1.0f,   0.0f,  0,  1.f,  1.f,
    };
}
