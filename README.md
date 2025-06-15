# edge-detect-viewer

This a real-time Android app that captures camera frames, detects edges using OpenCV, and renders the result with OpenGL ES.

---

##  Features Implemented

- Real-time camera feed using OpenCV `JavaCameraView`
- Edge detection using `cv::Canny` via JNI (C++)
- OpenGL ES 2.0 rendering pipeline
- Texture updates from OpenCV frames to GLSurfaceView
- JNI + native code integration (NDK)

---

##  Screenshots
![UI Preview1](AppScreenshots/UiPreview1)
![UI Preview2](AppScreenshots/UiPreview2)
![UI Preview3](AppScreenshots/UiPreview3)

---

## ⚙ Setup Instructions

> Ensure the following are set up in Android Studio:

### 1. NDK and CMake
- Go to **File > Project Structure > SDK Location**
- Set **NDK path** or install via SDK Manager

### 2. OpenCV
- Download [OpenCV Android SDK](https://opencv.org/releases/)
- Extract and copy `OpenCV-android-sdk/sdk/native/libs` and `jni/include` to your project
- Add OpenCV `.so` files under `src/main/jniLibs/`
- Load the OpenCV library in `MainActivity`:

```java
static {
    System.loadLibrary("opencv_java4");
    System.loadLibrary("edgedetectionviewer");
}



Architecture Overview-
Frame Flow:
1.OpenCV captures frames using JavaCameraView

2.Frame passed to GLRenderer.updateFrame(Mat)

3.JNI method updateTextureFromFrame() processes the frame using Canny edge detection in native C++

4.Processed frame is uploaded as a texture using OpenGL ES and drawn in GLSurfaceView

Components:
1.MainActivity.java: handles camera input and OpenCV initialization

2.GLRenderer.java: OpenGL logic for rendering frames

3.native-lib.cpp: C++ logic for edge detection + OpenGL texture updates

4.CMakeLists.txt: builds native code using NDK
