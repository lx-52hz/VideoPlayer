package com.example.videoplayer.glplayer;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.util.GlProgram;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import java.io.IOException;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

@OptIn(markerClass = UnstableApi.class)
public final class BitmapVideoProcessor implements PlayerGLSurfaceView.VideoProcessor {

    private static final String TAG = "BitmapOverlayVP";
    private static final int OVERLAY_WIDTH = 512;
    private static final int OVERLAY_HEIGHT = 256;

    private final Context context;
    private final Paint paint;
    private final int[] textures;
    private final Bitmap overlayBitmap;
    private final Bitmap logoBitmap;
    private final Canvas overlayCanvas;

    private GlProgram program;

    private float bitmapScaleX;
    private float bitmapScaleY;

    public BitmapVideoProcessor(Context context) {

        this.context = context.getApplicationContext();

        paint = new Paint();
        paint.setTextSize(64);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        textures = new int[1];
        overlayBitmap = Bitmap.createBitmap(OVERLAY_WIDTH, OVERLAY_HEIGHT, Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);

        try {
            // 图片
            logoBitmap = ((BitmapDrawable) context.getPackageManager().getApplicationIcon(context.getPackageName())).getBitmap();
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void initialize() {
        try {
            program = new GlProgram(context,
                    "bitmap_video_processor_vertex.glsl",
                    "bitmap_video_processor_fragment.glsl");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to initialize the shader program", e);
            return;
        }

        program.setBufferAttribute("aFramePosition", GlUtil.getNormalizedCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE);
        program.setBufferAttribute("aTexCoords", GlUtil.getTextureCoordinateBounds(), GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE);

        // 生成并配置当前绑定的纹理
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // 在纹理被缩小的时候使用邻近过滤,被放大时使用线性过滤
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        // 纹理环绕方式,重复纹理图像(纹理坐标超出默认范围时生效)
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        // 生成一个纹理
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, overlayBitmap, 0);
    }

    @Override
    public void setSurfaceSize(int width, int height) {
        bitmapScaleX = (float) width / OVERLAY_WIDTH;
        bitmapScaleY = (float) height / OVERLAY_HEIGHT;
    }

    @Override
    public void draw(int frameTexture, long frameTimestampUs, float[] transformMatrix) {
        // Draw to the canvas and store it in a texture.
        String text = String.format(Locale.US, "%.02f", frameTimestampUs / (float) C.MICROS_PER_SECOND);
        overlayBitmap.eraseColor(Color.TRANSPARENT);
        overlayCanvas.drawBitmap(logoBitmap, 32, 32, paint);
        overlayCanvas.drawText(text, 200, 130, paint);

        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, overlayBitmap);
        try {
            GlUtil.checkGlError();
        } catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to populate the texture", e);
        }

        // Run the shader program.
        GlProgram program = checkNotNull(this.program);
        program.setSamplerTexIdUniform("uTexSampler0", frameTexture, 0);
        program.setSamplerTexIdUniform("uTexSampler1", textures[0], 1);
        program.setFloatUniform("uScaleX", bitmapScaleX);
        program.setFloatUniform("uScaleY", bitmapScaleY);
        program.setFloatsUniform("uTexTransform", transformMatrix);
        try {
            program.bindAttributesAndUniforms();
        } catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to update the shader program", e);
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        try {
            GlUtil.checkGlError();
        } catch (GlUtil.GlException e) {
            Log.e(TAG, "Failed to draw a frame", e);
        }
    }

    @Override
    public void release() {
        if (program != null) {
            try {
                program.delete();
            } catch (GlUtil.GlException e) {
                Log.e(TAG, "Failed to delete the shader program", e);
            }
        }
    }
}
