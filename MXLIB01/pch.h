// pch.h: This is a precompiled header file.
// Files listed below are compiled only once, improving build performance for future builds.
// This also affects IntelliSense performance, including code completion and many code browsing features.
// However, files listed here are ALL re-compiled if any one of them is updated between builds.
// Do not add files here that you will be updating frequently as this negates the performance advantage.

#ifndef PCH_H
#define PCH_H

// COM
#include <Unknwn.h>

// UserConsentVerifier (C++/WinRT language projection)
#include <winrt/base.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Foundation.Collections.h>
#include <winrt/Windows.Security.Credentials.UI.h>
#include <winrt/Windows.ApplicationModel.DataTransfer.h>
#include <winrt/Windows.Devices.Enumeration.h>
#include <winrt/Windows.Devices.Midi.h>
#include <winrt/Windows.Storage.Streams.h>
// UserConsentVerifier interop (for parts not supported by C++/WinRT)
#include <UserConsentVerifierInterop.h>

// add headers that you want to pre-compile here
#include "jni.h"
#include <thread>
#include <mutex>
#include <future>
#include <stdio.h>
#include <wchar.h>
#include <string>
#include <vector>
#include <iostream>
#include <sstream>

extern void printStackTrace();
extern int systemExceptionMyHandler(const char* funcName, struct _EXCEPTION_POINTERS* ep);
extern void debugText(const char* t);
extern void debugText2(const char* t, const char* param);
extern void debugNumber(const char* t, const long num);
extern void debugDouble(const char* t, const double num);

extern void printStackTrace();

#endif //PCH_H
