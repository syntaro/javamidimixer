// pch.h: プリコンパイル済みヘッダー ファイルです。
// 次のファイルは、その後のビルドのビルド パフォーマンスを向上させるため 1 回だけコンパイルされます。
// コード補完や多くのコード参照機能などの IntelliSense パフォーマンスにも影響します。
// ただし、ここに一覧表示されているファイルは、ビルド間でいずれかが更新されると、すべてが再コンパイルされます。
// 頻繁に更新するファイルをここに追加しないでください。追加すると、パフォーマンス上の利点がなくなります。

#ifndef PCH_H
#define PCH_H

#include "framework.h"
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

#include <future>
#include <string>
#include <Windows.h>
#include "c:/github/fluidsynth-2.4.6-win10-x64/include/fluidsynth.h"

extern void printStackTrace();
extern int systemExceptionMyHandler(const wchar_t* funcName, struct _EXCEPTION_POINTERS* ep);
extern void debugText(const wchar_t* t);
extern void debugText2(const wchar_t* t, const wchar_t* param);
extern void debugNumber(const wchar_t* t, const long num);
extern void debugDouble(const wchar_t* t, const double num);

#endif //PCH_H
