package com.example.videoplayer.clip.oepngl;
public interface IDrawer {
    void setVideoSize(int videoW , int videoH);
    void setWorldSize(int worldW, int worldH);
    void setAlpha(Float alpha);
    void draw();
    void setTextureID(int id);
    void release();
}
