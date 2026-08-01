#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <string>

#define LOG_TAG "VipsJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef int (*vips_init_fn)(const char*);
typedef void* (*vips_image_new_from_file_fn)(const char*, ...);
typedef int (*vips_resize_fn)(void*, void**, double, ...);
typedef int (*vips_jpegsave_fn)(void*, const char*, ...);
typedef int (*vips_pngsave_fn)(void*, const char*, ...);
typedef int (*vips_webpsave_fn)(void*, const char*, ...);
typedef int (*vips_heifsave_fn)(void*, const char*, ...);
typedef int (*vips_jxlsave_fn)(void*, const char*, ...);
typedef int (*vips_image_get_width_fn)(void*);
typedef int (*vips_image_get_height_fn)(void*);
typedef void (*vips_error_clear_fn)(void);
typedef void (*g_object_unref_fn)(void*);

static void* libvips_handle = nullptr;
static vips_init_fn fn_vips_init = nullptr;
static vips_image_new_from_file_fn fn_vips_image_new_from_file = nullptr;
static vips_resize_fn fn_vips_resize = nullptr;
static vips_jpegsave_fn fn_vips_jpegsave = nullptr;
static vips_pngsave_fn fn_vips_pngsave = nullptr;
static vips_webpsave_fn fn_vips_webpsave = nullptr;
static vips_heifsave_fn fn_vips_heifsave = nullptr;
static vips_jxlsave_fn fn_vips_jxlsave = nullptr;
static vips_image_get_width_fn fn_vips_image_get_width = nullptr;
static vips_image_get_height_fn fn_vips_image_get_height = nullptr;
static vips_error_clear_fn fn_vips_error_clear = nullptr;
static g_object_unref_fn fn_g_object_unref = nullptr;

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    libvips_handle = dlopen("libvips.so", RTLD_NOW | RTLD_GLOBAL);
    if (!libvips_handle) {
        LOGE("dlopen libvips.so failed: %s", dlerror());
        return JNI_VERSION_1_6;
    }

    fn_vips_init = (vips_init_fn)dlsym(libvips_handle, "vips_init");
    fn_vips_image_new_from_file = (vips_image_new_from_file_fn)dlsym(libvips_handle, "vips_image_new_from_file");
    fn_vips_resize = (vips_resize_fn)dlsym(libvips_handle, "vips_resize");
    fn_vips_jpegsave = (vips_jpegsave_fn)dlsym(libvips_handle, "vips_jpegsave");
    fn_vips_pngsave = (vips_pngsave_fn)dlsym(libvips_handle, "vips_pngsave");
    fn_vips_webpsave = (vips_webpsave_fn)dlsym(libvips_handle, "vips_webpsave");
    fn_vips_heifsave = (vips_heifsave_fn)dlsym(libvips_handle, "vips_heifsave");
    fn_vips_jxlsave = (vips_jxlsave_fn)dlsym(libvips_handle, "vips_jxlsave");
    fn_vips_image_get_width = (vips_image_get_width_fn)dlsym(libvips_handle, "vips_image_get_width");
    fn_vips_image_get_height = (vips_image_get_height_fn)dlsym(libvips_handle, "vips_image_get_height");
    fn_vips_error_clear = (vips_error_clear_fn)dlsym(libvips_handle, "vips_error_clear");
    fn_g_object_unref = (g_object_unref_fn)dlsym(libvips_handle, "g_object_unref");

    if (fn_vips_init && fn_vips_init("MediaEncoder") == 0) {
        LOGI("libvips dynamically loaded & initialized via dlsym!");
    } else {
        LOGE("fn_vips_init failed or symbol not found");
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL
Java_com_example_videoencoder_engine_VideoEncodingEngine_00024VipsJni_encodeVipsImage(
        JNIEnv* env,
        jobject thiz,
        jstring inputPath,
        jstring outputPath,
        jstring format,
        jint quality,
        jint targetWidth,
        jint targetHeight
) {
    if (!fn_vips_image_new_from_file) {
        LOGE("libvips dlsym symbols not initialized");
        return JNI_FALSE;
    }

    const char* in_str = env->GetStringUTFChars(inputPath, nullptr);
    const char* out_str = env->GetStringUTFChars(outputPath, nullptr);
    const char* fmt_str = env->GetStringUTFChars(format, nullptr);

    void* in = fn_vips_image_new_from_file(in_str, NULL);
    if (!in) {
        LOGE("vips_image_new_from_file failed for %s", in_str);
        if (fn_vips_error_clear) fn_vips_error_clear();
        env->ReleaseStringUTFChars(inputPath, in_str);
        env->ReleaseStringUTFChars(outputPath, out_str);
        env->ReleaseStringUTFChars(format, fmt_str);
        return JNI_FALSE;
    }

    void* resized = in;
    int w = fn_vips_image_get_width ? fn_vips_image_get_width(in) : 0;
    int h = fn_vips_image_get_height ? fn_vips_image_get_height(in) : 0;

    if (targetWidth > 0 && targetHeight > 0 && w > 0 && (w != targetWidth || h != targetHeight)) {
        double scale = (double)targetWidth / w;
        if (fn_vips_resize && fn_vips_resize(in, &resized, scale, NULL) == 0) {
            if (fn_g_object_unref) fn_g_object_unref(in);
        } else {
            resized = in;
        }
    }

    std::string fmt(fmt_str);
    int ret = -1;

    if ((fmt == "JPEG" || fmt == "JPG") && fn_vips_jpegsave) {
        ret = fn_vips_jpegsave(resized, out_str, "Q", quality, "optimize_coding", 1, "interlace", 1, NULL);
    } else if (fmt == "PNG" && fn_vips_pngsave) {
        ret = fn_vips_pngsave(resized, out_str, "Q", quality, "compression", 6, "interlace", 1, NULL);
    } else if (fmt == "WEBP" && fn_vips_webpsave) {
        ret = fn_vips_webpsave(resized, out_str, "Q", quality, "effort", 4, NULL);
    } else if (fmt == "AVIF" && fn_vips_heifsave) {
        ret = fn_vips_heifsave(resized, out_str, "Q", quality, "compression", 2, NULL); // 2 = AV1
    } else if (fmt == "HEIC" && fn_vips_heifsave) {
        ret = fn_vips_heifsave(resized, out_str, "Q", quality, "compression", 1, NULL); // 1 = HEVC
    } else if (fmt == "JXL" && fn_vips_jxlsave) {
        ret = fn_vips_jxlsave(resized, out_str, "Q", quality, "effort", 4, NULL);
    } else if (fn_vips_webpsave) {
        ret = fn_vips_webpsave(resized, out_str, "Q", quality, NULL);
    }

    if (fn_g_object_unref) fn_g_object_unref(resized);

    env->ReleaseStringUTFChars(inputPath, in_str);
    env->ReleaseStringUTFChars(outputPath, out_str);
    env->ReleaseStringUTFChars(format, fmt_str);

    if (ret != 0) {
        LOGE("vips save failed for format %s", fmt_str);
        if (fn_vips_error_clear) fn_vips_error_clear();
        return JNI_FALSE;
    }

    LOGI("Successfully encoded image using libvips (dlsym): %s -> %s", in_str, out_str);
    return JNI_TRUE;
}

} // extern "C"
