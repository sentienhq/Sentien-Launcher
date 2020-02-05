LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := SentienLauncher
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := SentienLauncher

sentien_launcher_root  := $(LOCAL_PATH)
sentien_launcher_dir   := app
sentien_launcher_out   := $(PWD)/$(OUT_DIR)/target/common/obj/APPS/$(LOCAL_MODULE)_intermediates
sentien_launcher_build := $(sentien_launcher_root)/$(sentien_launcher_dir)/build
sentien_launcher_apk   := build/outputs/apk/$(sentien_launcher_dir)-release-unsigned.apk

$(sentien_launcher_root)/$(sentien_launcher_dir)/$(sentien_launcher_apk):
	rm -Rf $(sentien_launcher_build)
	mkdir -p $(sentien_launcher_out)
	ln -sf $(sentien_launcher_out) $(sentien_launcher_build)
	cd $(sentien_launcher_root)/$(sentien_launcher_dir) && gradle assembleRelease

LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(sentien_launcher_dir)/$(sentien_launcher_apk)
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

include $(BUILD_PREBUILT)
