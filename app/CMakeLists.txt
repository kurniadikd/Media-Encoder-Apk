cmake_minimum_required(VERSION 3.22.1)
project("videoencoder_jni")

add_library(
    videoencoder_jni
    SHARED
    src/main/cpp/libvips_jni.cpp
)

find_library(log-lib log)
find_library(dl-lib dl)

target_link_libraries(
    videoencoder_jni
    ${log-lib}
    ${dl-lib}
)
