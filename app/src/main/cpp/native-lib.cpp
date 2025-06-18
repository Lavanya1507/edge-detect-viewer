#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define LOG_TAG "EdgeDetectionJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace cv;


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_edgedetectionviewer_MainActivity_processFrameNative(
        JNIEnv *env,
        jobject /* this */,
        jlong matAddrInput) {
    cv::Mat &input = *(cv::Mat *) matAddrInput;

    if (input.empty()) {
        LOGD("Empty input in processFrameNative.");
        return 0;
    }

    //convertin gto grayscale and processing the edges using canny
    cv::Mat gray, edges;
    cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    cv::Canny(gray, edges, 50, 150);
    cv::cvtColor(edges, input, cv::COLOR_GRAY2RGBA);

    LOGD("Edge detection done in processFrameNative.");
    return (jlong) &input;
}

//opengl can't work in mat so we need textures
extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgedetectionviewer_GLRenderer_updateTextureFromFrame(
        JNIEnv *env,
        jobject /* this */,
        jlong matAddrInput,
        jint textureId) {

    cv::Mat &input = *(cv::Mat *) matAddrInput;
    //app crashing on different devices so cheks added to fix it, worked just fine on original device.
    if (input.empty()) {
        LOGD(" Skip: input Mat is empty.");
        return;
    }

    if (input.cols <= 0 || input.rows <= 0) {
        LOGD(" Invalid input size: %d x %d", input.cols, input.rows);
        return;
    }
    //we want pixel format to be rgba(cv_8uc4)
    if (input.type() != CV_8UC4) {
        LOGD(" Invalid input type: expected CV_8UC4, got type=%d", input.type());
        return;
    }


    cv::Mat gray,edges;
    //converting to grayscale and applying canny
    try {
        LOGD(" All checks passed. Calling cvtColor.");
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    } catch (cv::Exception &e) {
        LOGD(" cv::cvtColor threw an exception: %s", e.what());
        return;
    }
    cv::Canny(gray, edges, 50, 150);


    cv::Mat edgeRgb;
    cv::cvtColor(edges, edgeRgb, cv::COLOR_GRAY2RGB);


    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_RGB,
                 edgeRgb.cols,
                 edgeRgb.rows,
                 0,
                 GL_RGB,
                 GL_UNSIGNED_BYTE,
                 edgeRgb.data);

    // using Texture filtering
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, 0);

    LOGD("Texture updated successfully from OpenCV Mat.");
}
