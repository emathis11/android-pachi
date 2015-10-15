#ifndef PACHI_ANDROID_UTIL_H
#define PACHI_ANDROID_UTIL_H

// The function "stpcpy" doesn't exist in the Android NDK (version r8b)
char* stpcpy(char *dest, const char *src);

#endif
