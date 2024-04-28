// pch.h: プリコンパイル済みヘッダー ファイルです。
// 次のファイルは、その後のビルドのビルド パフォーマンスを向上させるため 1 回だけコンパイルされます。
// コード補完や多くのコード参照機能などの IntelliSense パフォーマンスにも影響します。
// ただし、ここに一覧表示されているファイルは、ビルド間でいずれかが更新されると、すべてが再コンパイルされます。
// 頻繁に更新するファイルをここに追加しないでください。追加すると、パフォーマンス上の利点がなくなります。

#ifndef PCH_H
#define PCH_H

// プリコンパイルするヘッダーをここに追加します
#include "framework.h"
#include "jni.h"

#include <future>
#include <stdio.h>
#include <wchar.h>
#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include <thread>
#include <Windows.h>

#include <public.sdk/source/vst/hosting/plugprovider.h>
#include <public.sdk/source/vst/hosting/module.h>
#include <public.sdk/source/vst/hosting/hostclasses.h>
#include <public.sdk/source/vst/hosting/eventlist.h>
#include <public.sdk/source/vst/hosting/parameterchanges.h>
#include <public.sdk/source/vst/hosting/processdata.h>
#include "public.sdk/source\vst/vstpresetfile.h"
#include "public.sdk/\source/\vst/vsteditcontroller.h""
#include <pluginterfaces/vst/ivsteditcontroller.h>
#include <pluginterfaces/vst/ivstprocesscontext.h>
#include <pluginterfaces/vst/ivstparameterchanges.h>
#include <pluginterfaces/gui/iplugview.h>
#include <portaudio.h>
#include <pa_asio.h>

#include "timeinfo.h"

static const double TEMPO = 120.0;

extern void noticeTaskDone(const int task, int result);
extern void refBlackListed(jboolean effect, jint rack);
extern void refAttachOnly();

extern void printStackTrace();
extern int systemExceptionMyHandler(const wchar_t* funcName, struct _EXCEPTION_POINTERS* ep);
extern void debugText(const wchar_t* t);
extern void debugText2(const wchar_t* t, const wchar_t* param);
extern void debugNumber(const wchar_t* t, const long num);
extern void debugDouble(const wchar_t* t, const double num);

#define MAX_SYNTH 16
#define MAX_EFFECT 2


using namespace Steinberg;
using namespace Steinberg::Vst;

#endif //PCH_H
