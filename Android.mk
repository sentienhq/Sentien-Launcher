LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ZenoLauncher
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := ZenoLauncher

zeno_root  := $(LOCAL_PATH)
zeno_dir   := app
zeno_out   := $(PWD)/$(OUT_DIR)/target/common/obj/APPS/$(LOCAL_MODULE)_intermediates
zeno_build := $(zeno_root)/$(zeno_dir)/build
zeno_apk   := build/outputs/apk/$(zeno_dir)-release-unsigned.apk

$(zeno_root)/$(zeno_dir)/$(zeno_apk):
	rm -Rf $(zeno_build)
	mkdir -p $(zeno_out)
	ln -sf $(zeno_out) $(zeno_build)
	cd $(zeno_root)/$(zeno_dir) && gradle assembleRelease

LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(zeno_dir)/$(zeno_apk)
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

include $(BUILD_PREBUILT)
