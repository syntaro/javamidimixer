#pragma once

extern void refCallText(const jchar* text);
extern void refCallShortMessage(jint device, jint message);
extern void refCallLongMessage(jint device, jbyteArray ptr);
extern void refCallDeviceListed();

extern jclass _javaClass;
extern JavaVM* _javavm;

extern DWORD exceptionCode;
extern DWORD exceptionParamCount;
extern ULONG_PTR exceptionInformaiton[EXCEPTION_MAXIMUM_PARAMETERS];
