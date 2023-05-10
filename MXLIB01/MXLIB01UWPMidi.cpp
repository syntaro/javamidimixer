#include "pch.h"

#include "MXLIB01UWPMidi.h"
#include <dbghelp.h>

JavaVM* _javaVM;
jclass _javaClass = NULL;

jmethodID cbCallText, cbCallShortMessage, cbCallLongMessage, cbDeviceListed;

DWORD exceptionCode;
DWORD exceptionParamCount;
ULONG_PTR exceptionInformaiton[EXCEPTION_MAXIMUM_PARAMETERS];

void JNICALL JNI_StartLibrary(JNIEnv* env, jobject obj)
{
    exceptionCode = 0;
    __try
    {
        staticManager.InitObject();
    }
    __except (systemExceptionMyHandler("JNI_StartLibrary", GetExceptionInformation()))
    {
        return;
    }
}

jint JNICALL JNI_InputDevicesRoomSize(JNIEnv* env, jobject obj)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.InRoomSize();
    }
    __except (systemExceptionMyHandler("JNI_InputDevicesRoomSize", GetExceptionInformation()))
    {
        return 0;
    }

}

jstring JNICALL JNI_InputDeviceName(JNIEnv* env, jobject obj, jint device)
{
    exceptionCode = 0;
    __try
    {
        const wchar_t* str = staticManager.InName(device);
        return env->NewString((uint16_t*)str, wcslen(str));
    }
    __except (systemExceptionMyHandler("JNI_InputDeviceName", GetExceptionInformation()))
    {
        return NULL;
    }
}

jstring JNICALL JNI_InputDeviceId(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        const wchar_t* str = staticManager.InId(device);
        return env->NewString((uint16_t*)str, wcslen(str));
    }
    __except (systemExceptionMyHandler("JNI_InputDeviceId", GetExceptionInformation()))
    {
        return NULL;
    }
}

jboolean JNICALL JNI_InputIsOpen(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.InIsOpen(device);
    }
    __except (systemExceptionMyHandler("JNI_InputIsOpen", GetExceptionInformation()))
    {
        return JNI_FALSE;
    }
}

jboolean  JNI_InputOpen(JNIEnv* env, jobject obj, jint  device, jlong timeout)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.InOpen(device, timeout);
    }
    __except (systemExceptionMyHandler("JNI_InputOpen", GetExceptionInformation()))
    {
        return JNI_FALSE;
    }
}

void JNICALL JNI_InputClose(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        staticManager.InClose(device);
    }
    __except (systemExceptionMyHandler("JNI_InputClose", GetExceptionInformation()))
    {
    }
}

jint JNICALL JNI_OutputDevicesRoomSize(JNIEnv* env, jobject obj)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.OutRoomSize();
    }
    __except (systemExceptionMyHandler("JNI_OutputDevicesRoomSize", GetExceptionInformation()))
    {
        return 0;
    }
}

jstring JNICALL JNI_OutputDeviceName(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        const wchar_t* str = staticManager.OutName(device);
        return env->NewString((uint16_t*)str, wcslen(str));
    }
    __except (systemExceptionMyHandler("JNI_OutputDeviceName", GetExceptionInformation()))
    {
        return NULL;
    }
}

jstring JNICALL JNI_OutputDeviceId(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        const wchar_t* str = staticManager.OutId(device);
        return env->NewString((uint16_t*)str, wcslen(str));
    }
    __except (systemExceptionMyHandler("JNI_OutputDeviceId", GetExceptionInformation()))
    {
        return NULL;
    }
}

jboolean JNICALL JNI_OutputDeviceIsOpen(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.OutIsOpen(device);
    }
    __except (systemExceptionMyHandler("JNI_OutputDeviceIsOpen", GetExceptionInformation()))
    {
        return JNI_FALSE;
    }
}

jboolean  JNI_OutputOpen(JNIEnv* env, jobject obj, jint  device, jlong timeout)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.OutOpen(device, timeout);
    }
    __except (systemExceptionMyHandler("JNI_OutputOpen", GetExceptionInformation()))
    {
        return JNI_FALSE;
    }
}

void JNICALL JNI_OutputClose(JNIEnv* env, jobject obj, jint  device)
{
    exceptionCode = 0;
    __try
    {
        staticManager.OutClose(device);
    }
    __except (systemExceptionMyHandler("JNI_OutputClose", GetExceptionInformation()))
    {
    }
}

jboolean JNICALL JNI_OutputShortMessage(JNIEnv* env, jobject obj, jint  device, jint  message)
{
    exceptionCode = 0;
    __try
    {
        int command = (message >> 16) & 0xff;
        int data1 = (message >> 8) & 0xff;
        int data2 = (message) & 0xff;

        return staticManager.OutShortMessage(env, device, message);
    }
    __except (systemExceptionMyHandler("JNI_OutputShortMessage", GetExceptionInformation()))
    {
        staticManager.OutClose(device);
        return JNI_FALSE;
    }
}

jboolean JNICALL JNI_OutputLongMessage(JNIEnv* env, jobject obj, jint device, jbyteArray data)
{
    exceptionCode = 0;
    __try
    {
        return staticManager.OutLongMessage(env, device, data);
    }
    __except (systemExceptionMyHandler("JNI_OutputLongMessage", GetExceptionInformation()))
    {
        staticManager.OutClose(device);
        return JNI_FALSE;
    }
}

 

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_1) != JNI_OK) {
        return JNI_ERR;
    }
    _javaVM = vm;

    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("jp/synthtarou/midimixer/windows/MXLIB01UWPMidi");
    if (c == nullptr) return JNI_ERR;
    /*
        Type Signature 	Javaのデータ型
        Z 	boolean
        B 	byte
        C 	char
        S 	short
        I 	int
        J 	long
        F 	float
        D 	double
        L fully-qualified-class; 	完全修飾指定
        Ex) java.lang.String;
        [type 	配列
        Ex) int[]の場合
        [I
        (arg-types)ret-type 	メソッドの型
        Ex) void main(int argc, String[] args);
        (I[Ljava.lang.String;)V
        V 	void
    */


    // Register your class' native methods.
    static JNINativeMethod methods[] = {
        { (char*)"StartLibrary", (char*)"()V", reinterpret_cast<void*>(JNI_StartLibrary)},

        { (char*)"InputDevicesRoomSize", (char*)"()I", reinterpret_cast<void*>(JNI_InputDevicesRoomSize)},
        { (char*)"InputDeviceName", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_InputDeviceName)},
        { (char*)"InputDeviceId", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_InputDeviceId)},

        { (char*)"InputOpen", (char*)"(IJ)Z", reinterpret_cast<void*>(JNI_InputOpen)},
        { (char*)"InputClose", (char*)"(I)V", reinterpret_cast<void*>(JNI_InputClose)},

        { (char*)"InputIsOpen", (char*)"(I)Z", reinterpret_cast<void*>(JNI_InputIsOpen)},

        { (char*)"OutputDevicesRoomSize", (char*)"()I", reinterpret_cast<void*>(JNI_OutputDevicesRoomSize)},
        { (char*)"OutputDeviceName", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_OutputDeviceName)},
        { (char*)"OutputDeviceId", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_OutputDeviceId)},
        { (char*)"OutputIsOpen", (char*)"(I)Z", reinterpret_cast<void*>(JNI_OutputDeviceIsOpen)},

        { (char*)"OutputOpen", (char*)"(IJ)Z", reinterpret_cast<void*>(JNI_OutputOpen)},
        { (char*)"OutputClose", (char*)"(I)V", reinterpret_cast<void*>(JNI_OutputClose)},

        { (char*)"OutputShortMessage", (char*)"(II)Z", reinterpret_cast<void*>(JNI_OutputShortMessage)},
        { (char*)"OutputLongMessage", (char*)"(I[B)Z", reinterpret_cast<void*>(JNI_OutputLongMessage)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

    _javaClass = c;
    cbCallShortMessage = env->GetStaticMethodID(_javaClass, "cbCallShortMessage", "(II)V");
    cbCallLongMessage = env->GetStaticMethodID(_javaClass, "cbCallLongMessage", "(I[B)V");
    cbCallText = env->GetStaticMethodID(_javaClass, "cbCallText", "(Ljava/lang/String;)V");
    cbDeviceListed = env->GetStaticMethodID(_javaClass, "cbDeviceListed", "()V");
    
    //if (cbCallShortMessage == nullptr) return JNI_ERR;
    //if (cbCallText == nullptr) return JNI_ERR;
    //if (cbCallLongMessage == nullptr) return JNI_ERR;

    debugText("MXLIB01 -> 2023-3-24");

    return JNI_VERSION_1_1;
}
