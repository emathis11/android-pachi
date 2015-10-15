LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

APP_PLATFORM    := android-5
LOCAL_MODULE    := pachi

LOCAL_LDFLAGS += -fPIE -pie
LOCAL_CPP_FLAGS := -fno-exceptions -fPIE -pie
LOCAL_CFLAGS    := -std=c99 -fPIE -pie \
-I$(LOCAL_PATH) \
-I$(LOCAL_PATH)/joseki \
-I$(LOCAL_PATH)/distributed \
-I$(LOCAL_PATH)/playout \
-I$(LOCAL_PATH)/montecarlo \
-I$(LOCAL_PATH)/random \
-I$(LOCAL_PATH)/patternplay \
-I$(LOCAL_PATH)/patternscan \
-I$(LOCAL_PATH)/replay \
-I$(LOCAL_PATH)/tactics \
-I$(LOCAL_PATH)/uct \
-I$(LOCAL_PATH)/uct/policy

LOCAL_SRC_FILES := \
\
android/util.c \
fbook.c \
board.c \
network.c \
gtp.c \
chat.c \
move.c \
ownermap.c \
pachi.c \
pattern.c \
pattern3.c \
patternprob.c \
patternsp.c \
playout.c \
probdist.c \
random.c \
stone.c \
timeinfo.c \
t-unit/test.c \
montecarlo/montecarlo.c \
joseki/base.c \
joseki/joseki.c \
distributed/distributed.c \
distributed/merge.c \
distributed/protocol.c \
patternplay/patternplay.c \
patternscan/patternscan.c \
playout/light.c \
playout/moggy.c \
random/random.c \
replay/replay.c \
tactics/1lib.c \
tactics/2lib.c \
tactics/ladder.c \
tactics/nakade.c \
tactics/nlib.c \
tactics/selfatari.c \
tactics/util.c \
uct/policy/generic.c \
uct/policy/ucb1.c \
uct/policy/ucb1amaf.c \
uct/dynkomi.c \
uct/plugins.c \
uct/prior.c \
uct/search.c \
uct/slave.c \
uct/tree.c \
uct/uct.c \
uct/walk.c \
#uct/plugin/example.c \
#uct/plugin/wolf.c \

include $(BUILD_EXECUTABLE)
