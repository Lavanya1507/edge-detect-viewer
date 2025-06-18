package com.example.edgedetectionviewer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.opengl.GLSurfaceView;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView cameraView;
    private GLRenderer renderer;


    static {
        System.loadLibrary("edgedetectionviewer");

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Failed to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully");
        }
    }
    public native long processFrameNative(long matAddrInput);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GLSurfaceView glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); //OpenGL ES 2.0: mention the version
        glSurfaceView.setRenderer(renderer=new GLRenderer()); //making  a custom renderer
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        // Initializing cameraView before using it
        cameraView = findViewById(R.id.camera_view);
        if (cameraView == null) {
            Log.e("MainActivity", "cameraView is null! Check if R.id.camera_view exists in XML.");
            return;
        }

        // request camera permission using a prompt
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            cameraView.setCameraPermissionGranted();
            initializeCameraView();
        }
    }

    private void initializeCameraView() {
        cameraView = findViewById(R.id.camera_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCameraPermissionGranted();
        cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        cameraView.setCvCameraViewListener(this);
        cameraView.enableView();
    }

    public native String stringFromJNI();

    @Override
    //100 is camera request code + sometimes grantResults are empty
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (cameraView != null) {
                cameraView.setCameraPermissionGranted();
                initializeCameraView();
            }
        } else {
            Log.e("CameraPermission", "Permission denied by user.");
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("Camera", "Camera started with resolution: " + width + "x" + height);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d("Camera", "Camera stopped.");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("Camera", "Frame received.");
        Mat rgba = inputFrame.rgba();
        renderer.updateFrame(rgba);
        // Send frame to C++ using JNI(pipeline)
        processFrameNative(rgba.getNativeObjAddr());

        return rgba;
    }
    //these are to release resources: to not cause memory leaks
    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }
}
