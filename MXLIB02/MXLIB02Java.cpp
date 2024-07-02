#include "pch.h"

#include "MXVSTOperator.h"

extern jclass _javaClass;
extern JavaVM* _javavm;
jmethodID cbCallText, cbTaskDone, cbBlackListed;

jclass _javaClass = NULL;
JavaVM* _javaVM;


typedef void(*CallText_t2)(const wchar_t* text);
typedef void(*CallBack_t2)(const jint callNumber);


void refTaskDone(const jint task, const jint result) {
    JNIEnv* env2 = NULL;
    _javaVM->AttachCurrentThread((void**)&env2, NULL);
    env2->CallStaticVoidMethod(_javaClass, cbTaskDone, task, result);
}

void refBlackListed(jboolean effect, jint rack) {
    JNIEnv* env2 = NULL;
    _javaVM->AttachCurrentThread((void**)&env2, NULL);
    env2->CallStaticVoidMethod(_javaClass, cbBlackListed, effect, rack);
}

void refAttachOnly() {
    JNIEnv* env2 = NULL;
    _javaVM->AttachCurrentThread((void**)&env2, NULL);
}


void noticeTaskDone(const int task, const int result) {
    refTaskDone(task, result);
}

bool stringFromJava(std::wstring& ret, JNIEnv* env, jstring jstr) {
    int len = env->GetStringLength(jstr);
    const jchar* from = env->GetStringChars(jstr, NULL);
    wchar_t* to = new wchar_t[len + 1];
    for (int i = 0; i < len; ++i) {
        to[i] = from[i];
    }
    to[len] = 0;
    ret.clear();
    ret.append(to);
    env->ReleaseStringChars(jstr, from);
    return true;
}

jstring stringToJava(JNIEnv* env, std::wstring& str) {
    return env->NewString((const jchar*)str.c_str(), str.length());
}

void JNICALL JNI_PostInitializeStream(JNIEnv* env, jobject obj, jint task) {
    __try
    {
        getOperator()->postInitializeStream(task);
    }
    __except (systemExceptionMyHandler(L"JNI_postInitializeStream", GetExceptionInformation()))
    {
        noticeTaskDone(task, Thread_Exception);
    }
}

void JNICALL JNI_PostOpenStream(JNIEnv* env, jobject obj, jint stream, int sampleRate, jint latency, jint task)
{
    __try
    {
        getOperator()->postOpenStream(stream, sampleRate, latency, task);
    }
    __except (systemExceptionMyHandler(L"JNI_OpenStream", GetExceptionInformation()))
    {
        noticeTaskDone(task, Thread_Exception);
    }
}

void JNICALL JNI_PostCloseStream(JNIEnv* env, jobject obj, jint task) {
    __try
    {
        getOperator()->postCloseStream(task);
    }
    __except (systemExceptionMyHandler(L"JNI_CloseStream", GetExceptionInformation()))
    {
        noticeTaskDone(task, Thread_Exception);
    }
}

jboolean JNICALL JNI_isStreamOpen(JNIEnv* en, jobject obj) {
    __try
    {
        return getMXStream()->isOpen();
    }
    __except (systemExceptionMyHandler(L"JNI_isStreamOpen", GetExceptionInformation()))
    {
    }
    return false;
}

void CAPSULE_postLaunchVST(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    std::wstring uniPath;
    stringFromJava(uniPath, env, path);
    getOperator()->postLaunchVST(effect, synth, uniPath, task);
}


void JNICALL JNI_postLaunchVST(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    __try
    {
        MXVSTInstrument* vst = nullptr;
        __try
        {
            vst = getOperator()->getSynth(effect, synth);
            if (vst != nullptr) {
                vst->_blackList = false;
                CAPSULE_postLaunchVST(env, obj, effect, synth, path, task);
            }
            else {
                noticeTaskDone(task, Thread_NoSuccess);
            }
        }
        __except (systemExceptionMyHandler(L"JNI_postLaunchVST Parent", GetExceptionInformation()))
        {
            if (vst != nullptr) {
                vst->_blackList = true;
                noticeTaskDone(task, Thread_Exception);
                refBlackListed(effect, synth);
            }
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postLaunchVST", GetExceptionInformation()))
    {
    }
}

jboolean JNICALL JNI_isLaunchedVST(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint task) {
    __try
    {
        return getOperator()->isOpen(effect, synth);
    }
    __except (systemExceptionMyHandler(L"JNI_isLaunchedVST", GetExceptionInformation()))
    {
        return false;
    }
}

void JNICALL JNI_postOpenEditor(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint task, jint whenClose) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr && vst->_blackList == false) {
            getOperator()->postOpenEditor(effect, synth, task, whenClose);
        }
        else {
            noticeTaskDone(task, Thread_NoSuccess);
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postOpenEditor", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            noticeTaskDone(task, Thread_Exception);
            refBlackListed(effect, synth);
        }
    }
}

void JNICALL JNI_postCloseEditor(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint task) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr) {
            getOperator()->postCloseEditor(effect, synth, task);
        }
        else {
            noticeTaskDone(task, Thread_NoSuccess);
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postCloseEditor", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            noticeTaskDone(task, Thread_Exception);
            refBlackListed(effect, synth);
        }
    }
}

jboolean  JNI_isEditorOpen(JNIEnv* env, jobject obj, jboolean  effect, jint synth) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr && vst->isOpen()) {
            return vst->_easyVst->getHWnd() != 0;
        }
    }
    __except (systemExceptionMyHandler(L"JNI_isEditorOpen", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            refBlackListed(effect, synth);
        }
    }
    return false;
}

jboolean  JNI_isBlackListed(JNIEnv* env, jobject obj, jboolean  effect, jint synth) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr && vst->_blackList) {
            return true;
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postRemoveSynth", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            refBlackListed(effect, synth);
            return true;
        }
    }
    return false;
}

void JNICALL JNI_postRemoveSynth(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint task) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr) {
            getOperator()->postRemoveSynth(effect, synth, task);
        }
        else {
            noticeTaskDone(task, Thread_NoSuccess);
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postRemoveSynth", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            noticeTaskDone(task, Thread_Exception);
            refBlackListed(effect, synth);
        }
    }
}

void CAPSULE_savePreset(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    std::wstring uniPath;
    stringFromJava(uniPath, env, path);
    getOperator()->postSavePreset(effect, synth, uniPath, task);
}

void JNICALL JNI_savePreset(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr && vst->_blackList == false) {
            CAPSULE_savePreset(env, obj, effect, synth, path, task);
        }
        else {
            noticeTaskDone(task, Thread_NoSuccess);
        }
    }
    __except (systemExceptionMyHandler(L"JNI_savePluginState", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            noticeTaskDone(task, Thread_Exception);
            refBlackListed(effect, synth);
        }
    }
}

void CAPSULE_loadPreset(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    std::wstring uniPath;
    stringFromJava(uniPath, env, path);
    getOperator()->postLoadPreset(effect, synth, uniPath, task);
}

void JNICALL JNI_loadPreset(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jstring path, jint task) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);
        if (vst != nullptr && vst->_blackList == false) {
            CAPSULE_loadPreset(env, obj, effect, synth, path, task);
        }
        else {
            noticeTaskDone(task, Thread_NoSuccess);
        }
    }
    __except (systemExceptionMyHandler(L"JNI_loadPluginState", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            noticeTaskDone(task, Thread_Exception);
            refBlackListed(effect, synth);
        }
    }
}

void JNICALL JNI_waitQueued(JNIEnv* env, jobject obj, jint task) {
    __try
    {
        getOperator()->waitQueued(task);
    }
    __except (systemExceptionMyHandler(L"JNI_waitQueued", GetExceptionInformation()))
    {
    }
}

jboolean JNI_postShortMessage(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint message) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);

        if (vst != nullptr && vst->_blackList == false) {
            EasyVstCustom* easy = vst->_easyVst;
            if (easy != nullptr) {
                easy->pushShortMessage(message);
            }
            return TRUE;
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postShortMessage", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            refBlackListed(effect, synth);
        }
    }
    return false;
}

jboolean JNICALL JNI_postLongMessage(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jbyteArray data) {
    MXVSTInstrument* vst = nullptr;
    __try
    {
        vst = getOperator()->getSynth(effect, synth);

        if (vst != nullptr && vst->_blackList == false) {
            EasyVstCustom* easy= vst->_easyVst;
            if (easy != nullptr) {
                int len = env->GetArrayLength(data);
                jbyte* ptr = env->GetByteArrayElements(data, NULL);
                easy->pushLongMessage((const uchar*)ptr, len);
                env->ReleaseByteArrayElements(data, ptr, 0);
                return TRUE;
            }
        }
    }
    __except (systemExceptionMyHandler(L"JNI_postLongMessage", GetExceptionInformation()))
    {
        if (vst != nullptr) {
            vst->_blackList = true;
            refBlackListed(effect, synth);
        }
    }
    return false;
}

jint JNICALL JNI_countStream(JNIEnv* env, jobject obj) {
    __try
    {
        return getMXStream()->countStream();
    }
    __except (systemExceptionMyHandler(L"JNI_countStream", GetExceptionInformation()))
    {
        return 0;
    }
}

jstring JNICALL JNI_nameOfStream(JNIEnv* env, jobject obj, jint synth) {
    __try
    {
        std::wstring* name = new std::wstring();
        getMXStream()->nameOfStream(*name, synth);
        jstring ret = env->NewString((const jchar*)name->c_str(), name->size());
        delete name;
        return ret;
    }
    __except (systemExceptionMyHandler(L"JNI_nameOfStream", GetExceptionInformation()))
    {
        return nullptr;
    }
}

jstring JNICALL JNI_typeNameOfStream(JNIEnv* env, jobject obj, jint synth) {
    __try
    {
        std::wstring* name = new std::wstring();
        getMXStream()->typeNameOfStream(*name, synth);
        jstring ret = env->NewString((const jchar*)name->c_str(), name->size());
        delete name;
        return ret;
    }
    __except (systemExceptionMyHandler(L"JNI_typeNameOfStream", GetExceptionInformation()))
    {
        return nullptr;
    }
}

jfloat JNICALL JNI_getMasterVolume(JNIEnv* env, jobject obj) {
    __try
    {
        return getOperator()->getMasterVolume();
    }
    __except (systemExceptionMyHandler(L"JNI_getMasterVolume", GetExceptionInformation()))
    {
        return 0;
    }
}

void JNICALL JNI_setMasterVolume(JNIEnv* env, jobject obj, jfloat volume) {
    __try
    {
        getOperator()->setMasterVolume(volume);
    }
    __except (systemExceptionMyHandler(L"JNI_setMasterVolume", GetExceptionInformation()))
    {
    }
}

jint JNICALL JNI_getBusCount(JNIEnv* env, jobject obj, jboolean  effect, jint synth) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(effect, synth);
        int x = vst->getBusCount();
        return x;
    }
    __except (systemExceptionMyHandler(L"JNI_getBusCount", GetExceptionInformation()))
    {
        debugNumber(L"getBusCount effect = ", effect ? 1 : 0);
        debugNumber(L"getBusCount synth = ", synth);
        return 0;
    }
}
    
jfloat JNICALL JNI_getBusVolume(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint bus) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(effect, synth);
        return vst->getBusVolume(bus);
    }
    __except (systemExceptionMyHandler(L"JNI_getBusVolume", GetExceptionInformation()))
    {
        debugNumber(L"effect", effect ? 1 : 0);
        debugNumber(L"synth", synth);
        debugNumber(L"bus", bus);
        return 0;
    }
}

void JNICALL JNI_setBusVolume(JNIEnv* env, jobject obj, jboolean  effect, jint synth, jint bus, jfloat volume) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(effect, synth);
        return vst->setBusVolume(bus, volume);
    }
    __except (systemExceptionMyHandler(L"JNI_setBusVolume", GetExceptionInformation()))
    {
    }
}

void JNICALL JNI_stopEngine(JNIEnv* env, jobject obj, jint task) {
    __try
    {
        debugText(L"postQuit");
        getOperator()->postQuit(task);
    }
    __except (systemExceptionMyHandler(L"JNI_setBusVolume", GetExceptionInformation()))
    {
    }
    std::cout << "final exit" << std::endl;
    exit(0);
    std::cout << "final abort" << std::endl;
    abort();
}


void JNICALL JNI_setInsertBalance(JNIEnv* env, jobject obj, jint synth, jfloat balance) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(false, synth);
        vst->setInsertBalance(balance);
    }
    __except (systemExceptionMyHandler(L"JNI_setInsertBalance", GetExceptionInformation()))
    {
    }
}

float JNICALL JNI_getInsertBalance(JNIEnv* env, jobject obj, jint synth) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(false, synth);
        return vst->getInsertBalance();
    }
    __except (systemExceptionMyHandler(L"JNI_getInsertBalance", GetExceptionInformation()))
    {
        return 0;
    }
}


void JNICALL JNI_setAuxSend(JNIEnv* env, jobject obj, jint synth, jfloat balance) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(false, synth);
        vst->setAuxSend(balance);
    }
    __except (systemExceptionMyHandler(L"JNI_setAuxSend", GetExceptionInformation()))
    {
    }
}

float JNICALL JNI_getAuxSend(JNIEnv* env, jobject obj, jint synth) {
    __try
    {
        MXVSTInstrument* vst = getOperator()->getSynth(false, synth);
        return vst->getAuxSend();
    }
    __except (systemExceptionMyHandler(L"JNI_getAuxSend", GetExceptionInformation()))
    {
        return 0;
    }
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_1) != JNI_OK) {
        return JNI_ERR;
    }
    _javaVM = vm;

    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("jp/synthtarou/midimixer/windows/MXLIB02VST3");
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
        { (char*)"postInitializeStream", (char*)"(I)V", reinterpret_cast<void*>(JNI_PostInitializeStream)},

        { (char*)"countStream", (char*)"()I", reinterpret_cast<void*>(JNI_countStream)},
        { (char*)"nameOfStream", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_nameOfStream)},
        { (char*)"typeNameOfStream", (char*)"(I)Ljava/lang/String;", reinterpret_cast<void*>(JNI_typeNameOfStream)},

        { (char*)"postOpenStream", (char*)"(IIII)V", reinterpret_cast<void*>(JNI_PostOpenStream)},
        { (char*)"postCloseStream", (char*)"(I)V", reinterpret_cast<void*>(JNI_PostCloseStream)},
        { (char*)"isStreamOpen", (char*)"()Z", reinterpret_cast<void*>(JNI_isStreamOpen)},

        { (char*)"postLaunchVST", (char*)"(ZILjava/lang/String;I)V", reinterpret_cast<void*>(JNI_postLaunchVST)},
        { (char*)"isLaunchedVST", (char*)"(ZI)Z", reinterpret_cast<void*>(JNI_isLaunchedVST)},

        { (char*)"postOpenEditor", (char*)"(ZIII)V", reinterpret_cast<void*>(JNI_postOpenEditor)},
        { (char*)"postCloseEditor", (char*)"(ZII)V", reinterpret_cast<void*>(JNI_postCloseEditor)},
        { (char*)"isEditorOpen", (char*)"(ZI)Z", reinterpret_cast<void*>(JNI_isEditorOpen)},
        { (char*)"isBlackListed", (char*)"(ZI)Z", reinterpret_cast<void*>(JNI_isBlackListed)},

        { (char*)"postRemoveSynth", (char*)"(ZII)V", reinterpret_cast<void*>(JNI_postRemoveSynth)},

        { (char*)"savePreset", (char*)"(ZILjava/lang/String;I)V", reinterpret_cast<void*>(JNI_savePreset)},
        { (char*)"loadPreset", (char*)"(ZILjava/lang/String;I)V", reinterpret_cast<void*>(JNI_loadPreset)},

        { (char*)"waitQueued", (char*)"(I)V", reinterpret_cast<void*>(JNI_waitQueued)},
        { (char*)"postShortMessage", (char*)"(ZII)Z", reinterpret_cast<void*>(JNI_postShortMessage)},
        { (char*)"postLongMessage", (char*)"(ZI[B)Z", reinterpret_cast<void*>(JNI_postLongMessage)},

        { (char*)"getMasterVolume", (char*)"()F", reinterpret_cast<void*>(JNI_getMasterVolume)},
        { (char*)"setMasterVolume", (char*)"(F)V", reinterpret_cast<void*>(JNI_setMasterVolume)},
        { (char*)"getBusCount", (char*)"(ZI)I", reinterpret_cast<void*>(JNI_getBusCount)},
        { (char*)"getBusVolume", (char*)"(ZII)F", reinterpret_cast<void*>(JNI_getBusVolume)},
        { (char*)"setBusVolume", (char*)"(ZIIF)V", reinterpret_cast<void*>(JNI_setBusVolume)},

        { (char*)"getInsertBalance", (char*)"(I)F", reinterpret_cast<void*>(JNI_getInsertBalance)},
        { (char*)"setInsertBalance", (char*)"(IF)V", reinterpret_cast<void*>(JNI_setInsertBalance)},
        { (char*)"getAuxSend", (char*)"(I)F", reinterpret_cast<void*>(JNI_getAuxSend)},
        { (char*)"setAuxSend", (char*)"(IF)V", reinterpret_cast<void*>(JNI_setAuxSend)},

        { (char*)"stopEngine", (char*)"(I)V", reinterpret_cast<void*>(JNI_stopEngine)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

    _javaClass = c;
    cbCallText = env->GetStaticMethodID(_javaClass, "cbCallText", "(Ljava/lang/String;)V");
    cbTaskDone = env->GetStaticMethodID(_javaClass, "cbTaskDone", "(II)V");
    cbBlackListed = env->GetStaticMethodID(_javaClass, "cbBlackListed", "(ZI)V");

    debugText(L"MXLIB02 2023-03-24");

    return JNI_VERSION_1_1;
}


void refCallText(const jchar* text) {
    JNIEnv* env2 = nullptr;
    _javaVM->AttachCurrentThread((void**)&env2, nullptr);

    jstring str = env2->NewString((uint16_t*)text, wcslen((const wchar_t*)text));
    env2->CallStaticVoidMethod(_javaClass, cbCallText, str);
}

void debugText(const wchar_t* t) {
    const jchar* jch = (const jchar*)t;
    refCallText(jch);
}

void debugText(const std::wstring t) {
    refCallText((jchar*)t.c_str());
}

void debugText2(const wchar_t* t, const wchar_t* param) {
    std::wstring str;
    str.append(t);
    str.append(param);
    debugText(str);
}

void debugNumber(const wchar_t* t, const long num) {
    std::wstring str;
    str.append(t);
    str.append(std::to_wstring(num));
    debugText(str);
}

void debugDouble(const wchar_t* t, const double num) {
    std::wstring str;
    str.append(t);
    str.append(std::to_wstring(num));
    debugText(str);
}