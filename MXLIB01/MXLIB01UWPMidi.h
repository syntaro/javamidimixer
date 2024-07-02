#pragma once

#include "pch.h"

class MXDeviceManager {
    bool initFlag;

public:
    void InitObject();

    void InReload();
    size_t InRoomSize();
    const wchar_t* InName(int device);
    const wchar_t* InId(int device);

    bool InOpen(int device, long timeout);
    bool InIsOpen(int device);
    void InClose(int device);

    void OutReload();
    size_t OutRoomSize();
    const wchar_t* OutName(int device);
    const wchar_t* OutId(int device);

    bool OutOpen(int device, long timeout);
    bool OutIsOpen(int device);
    void OutClose(int device);

    bool OutShortMessage(JNIEnv* env, int port, int message);
    bool OutLongMessage(JNIEnv* env, int port,  jbyteArray data);
};


extern void refCallText(const jchar* text);
extern void refCallShortMessage(jint device, jint message);
extern void refCallLongMessage(jint device, jbyteArray ptr);
extern void refCallDeviceListed();

extern MXDeviceManager *staticManager;
extern jclass _javaClass;
extern JavaVM* _javavm;

extern DWORD exceptionCode;
extern DWORD exceptionParamCount;
extern ULONG_PTR exceptionInformaiton[EXCEPTION_MAXIMUM_PARAMETERS];
