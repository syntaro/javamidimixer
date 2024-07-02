// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include "MXLIB01UWPMidi.h"
#include <dbghelp.h>

#pragma comment(lib, "imagehlp.lib")

HANDLE hProcess;
BOOL hSymInit;

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        hProcess = GetCurrentProcess();
        hSymInit = SymInitialize(hProcess, NULL, TRUE);
        break;
    case DLL_PROCESS_DETACH:
        if (hSymInit) {
            SymCleanup(hProcess);
        }
        break;
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
        break;
    }
    return TRUE;
}


void printStackTrace()
{
    if (hProcess == 0) {
        hProcess = GetCurrentProcess();
        hSymInit = SymInitialize(hProcess, NULL, TRUE);
    }
    if (!hSymInit) {
        debugText(L"no symbol found\n");
        return;
    }

    const int MAX_SYMBOL_NAME_LEN = 256;
    const int MAX_FRAMES_TO_CAPTURE = 100;

    void* symbol[sizeof(SYMBOL_INFO) + MAX_SYMBOL_NAME_LEN];
    reinterpret_cast<SYMBOL_INFO*>(symbol)->SizeOfStruct
        = sizeof(SYMBOL_INFO);
    reinterpret_cast<SYMBOL_INFO*>(symbol)->MaxNameLen
        = MAX_SYMBOL_NAME_LEN;

    void* stack[MAX_FRAMES_TO_CAPTURE];
    const WORD frames = CaptureStackBackTrace(
        0
        , MAX_FRAMES_TO_CAPTURE
        , stack
        , NULL
    );

    std::wstringstream ss;

    ss << L"Frames " << std::dec << frames << L"\n";

    for (WORD i = 0; i < frames; i++)
    {
        SymFromAddr(
            hProcess
            , reinterpret_cast<DWORD64>(stack[i])
            , 0
            , reinterpret_cast<SYMBOL_INFO*>(symbol)
        );
        ss << std::hex << (i + 1);
        ss << L":";
        ss << reinterpret_cast<SYMBOL_INFO*>(symbol)->Name;
        ss << L"@";
        ss << std::hex << reinterpret_cast<SYMBOL_INFO*>(symbol)->Address;
        ss << L"\n";
    }
    debugText(ss.str());
}


const wchar_t* exceptionName(int code) {
    switch (code) {
    case EXCEPTION_ACCESS_VIOLATION:
        return L"EXCEPTION_ACCESS_VIOLATION";
    case EXCEPTION_ARRAY_BOUNDS_EXCEEDED:
        return L"EXCEPTION_ARRAY_BOUNDS_EXCEEDED";
    case EXCEPTION_BREAKPOINT:
        return L"EXCEPTION_BREAKPOINT";
    case EXCEPTION_DATATYPE_MISALIGNMENT:
        return L"EXCEPTION_DATATYPE_MISALIGNMENT";
    case EXCEPTION_FLT_DENORMAL_OPERAND:
        return L"EXCEPTION_FLT_DENORMAL_OPERAND";
    case EXCEPTION_FLT_DIVIDE_BY_ZERO:
        return L"EXCEPTION_FLT_DIVIDE_BY_ZERO";
    case EXCEPTION_FLT_INEXACT_RESULT:
        return L"EXCEPTION_FLT_INEXACT_RESULT";
    case EXCEPTION_FLT_INVALID_OPERATION:
        return L"EXCEPTION_FLT_INVALID_OPERATION";
    case EXCEPTION_FLT_OVERFLOW:
        return L"EXCEPTION_FLT_OVERFLOW";
    case EXCEPTION_FLT_STACK_CHECK:
        return L"EXCEPTION_FLT_STACK_CHECK";
    case EXCEPTION_FLT_UNDERFLOW:
        return L"EXCEPTION_FLT_UNDERFLOW";
    case EXCEPTION_ILLEGAL_INSTRUCTION:
        return L"EXCEPTION_ILLEGAL_INSTRUCTION";
    case EXCEPTION_IN_PAGE_ERROR:
        return L"EXCEPTION_IN_PAGE_ERROR";
    case EXCEPTION_INT_DIVIDE_BY_ZERO:
        return L"EXCEPTION_INT_DIVIDE_BY_ZERO";
    case EXCEPTION_INT_OVERFLOW:
        return L"EXCEPTION_INT_OVERFLOW";
    case EXCEPTION_INVALID_DISPOSITION:
        return L"EXCEPTION_INVALID_DISPOSITION";
    case EXCEPTION_NONCONTINUABLE_EXCEPTION:
        return L"EXCEPTION_NONCONTINUABLE_EXCEPTION";
    case EXCEPTION_PRIV_INSTRUCTION:
        return L"EXCEPTION_PRIV_INSTRUCTION";
    case EXCEPTION_SINGLE_STEP:
        return L"EXCEPTION_SINGLE_STEP";
    case EXCEPTION_STACK_OVERFLOW:
        return L"EXCEPTION_STACK_OVERFLOW";
    case 0:
        return L"No Problem";
    case 3765269347:
        return L"AVC_IoException";

    }
    return NULL;

}

int systemExceptionMyHandler(const wchar_t* funcName, struct _EXCEPTION_POINTERS* ep)
{
    debugText2(L"systemExcectionMyHandler at ", funcName);

    DWORD code = ep->ExceptionRecord->ExceptionCode;
    DWORD flags = ep->ExceptionRecord->ExceptionFlags;
    PVOID address = ep->ExceptionRecord->ExceptionAddress;
    DWORD paramCount = ep->ExceptionRecord->NumberParameters;
    ULONG_PTR* information = ep->ExceptionRecord->ExceptionInformation;
    /*
        exceptionCode = ep->ExceptionRecord->ExceptionCode;
        exceptionParamCount = ep->ExceptionRecord->NumberParameters;

        for (unsigned int i = 0; i < EXCEPTION_MAXIMUM_PARAMETERS; ++i) {
            if (i + 1 <= paramCount) {
                exceptionInformaiton[i] = information[i];
            }
            else {
                exceptionInformaiton[i] = 0;
            }
        }
        const wchar_t* ex = exceptionName(code);
        if (ex == NULL) {
            std::wstring str = L"Unknown Errror ";
            str += std::to_wstring(code);
            debugText(str.c_str());
            return EXCEPTION_CONTINUE_SEARCH;
        }
    */
    debugText2(L"  Is ", exceptionName(code));
    printStackTrace();

    return EXCEPTION_EXECUTE_HANDLER;
}

void debugText(std::wstring t) {
    debugText(t.c_str());
}

void debugText(const wchar_t* t) {
    const jchar* jch = (const jchar*)t;
    refCallText(jch);
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
