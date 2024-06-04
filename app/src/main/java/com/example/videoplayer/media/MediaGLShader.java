package com.example.videoplayer.media;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MediaGLShader {
    private static final String TAG = "MediaGLShader";

    private Context context;

    private int program = 0;

    public MediaGLShader(@NotNull Context con, String vertexAssetsPath, String fragmentAssetsPath) {
        this.context = con;
        // 从文件获取着色器程序代码
        String vertexGLSL = getGLSLContent(vertexAssetsPath);
        String fragmentGLSL = getGLSLContent(fragmentAssetsPath);
        // 创建着色器
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexGLSL);
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentGLSL);

        // 创建并连接着色器程序
        program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: \n" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
            }
        }

        // 删除原始着色器
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
    }

    public int getShaderProgram() {
        return program;
    }

    private String getGLSLContent(String assetsPath) {
        if ("".equals(assetsPath)) {
            Log.e(TAG, "The file cannot be accessed or does not exist: " + assetsPath);
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = context.getAssets().open(assetsPath);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            Log.e(TAG, "getGLSLContent: " + assetsPath);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while closing resources: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while closing resources: " + e.getMessage());
            }
        }
        return stringBuilder.toString();
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ": \n" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}
