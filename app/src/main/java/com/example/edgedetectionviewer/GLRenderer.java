package com.example.edgedetectionviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.opencv.core.Mat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLRenderer implements GLSurfaceView.Renderer {

    private int[] textureId = new int[1];
    private volatile Mat currentFrame;
    private long nativeMatAddr = 0;

    private int shaderProgram;
    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;

    static {
        System.loadLibrary("edgedetectionviewer");
    }

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vTexCoord;" +
                    "varying vec2 texCoord;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  texCoord = vTexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 texCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, texCoord);" +
                    "}";

    // to upadte texture from frame
    public native void updateTextureFromFrame(long matAddrInput, int textureId);

    // Allow MainActivity to pass camera frame
    public void updateFrame(Mat frame) {
        if (frame != null) {
            if (currentFrame != null) {
                currentFrame.release();
            }
            currentFrame = frame.clone();
            nativeMatAddr = currentFrame.getNativeObjAddr();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);

        // generating textures
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (nativeMatAddr != 0 && currentFrame.width()>0) {
            updateTextureFromFrame(nativeMatAddr, textureId[0]);
        }

        drawTexturedQuad();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }


    private void drawTexturedQuad() {
        float[] vertexCoords = {
                -1.0f,  1.0f,   // Top-left
                -1.0f, -1.0f,   // Bottom-left
                1.0f,  1.0f,   // Top-right
                1.0f, -1.0f    // Bottom-right
        };

        float[] textureCoords = {
                0.0f, 0.0f,   // Top-left
                0.0f, 1.0f,   // Bottom-left
                1.0f, 0.0f,   // Top-right
                1.0f, 1.0f    // Bottom-right
        };

        // shaders and linking
        if (shaderProgram == 0) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            shaderProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(shaderProgram, vertexShader);
            GLES20.glAttachShader(shaderProgram, fragmentShader);
            GLES20.glLinkProgram(shaderProgram);
        }

        GLES20.glUseProgram(shaderProgram);

        // handles
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoord");
        textureHandle  = GLES20.glGetUniformLocation(shaderProgram, "uTexture");

        // Prepare the vertex buffer
        FloatBuffer vertexBuffer = floatBuffer(vertexCoords);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Preparing texture coord buffer
        FloatBuffer texCoordBuffer = floatBuffer(textureCoords);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        // Binding the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glUniform1i(textureHandle, 0);

        // Drawing quad using 2 triangles
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // disabling the attributes
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private FloatBuffer floatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }


}
