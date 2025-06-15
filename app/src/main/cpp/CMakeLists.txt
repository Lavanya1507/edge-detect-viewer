cmake_minimum_required(VERSION 3.22.1)
project("edgedetectionviewer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Load OpenCV
set(OpenCV_DIR "C:/Users/Lavanya/Downloads/opencv-4.11.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)

add_library(${CMAKE_PROJECT_NAME} SHARED native-lib.cpp)
include_directories(${OpenCV_INCLUDE_DIRS})
# Find Android and OpenGL ES system libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(GLESv2-lib GLESv2)
find_library(EGL-lib EGL)

# Link everything
target_link_libraries(${CMAKE_PROJECT_NAME}
        ${log-lib}
        ${android-lib}
        ${GLESv2-lib}
        ${EGL-lib}
        ${OpenCV_LIBS}
)