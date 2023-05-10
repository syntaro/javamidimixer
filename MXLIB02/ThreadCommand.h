#include "pch.h"
#include <string>
#include "MXStereoBuffer.h"

#pragma once

enum ThreadResult {
	Thread_Exception = -1,
	Thread_Success = 0,
	Thread_NoSuccess = 1
};

enum ThreadCommand {
	Thread_None = 0,
	Thread_OpenVSTEditor = 1,
	Thread_CloseVSTEditor = 2,
	Thread_LaunchVST = 3,
	Thread_RemoveVST = 4,
	Thread_LoadPreset = 5,
	Thread_SavePreset = 6,

	Thread_InitalizeStream = 7,
	Thread_OpenStream = 8,
	Thread_CloseStream = 9,

	Thread_WaitQueue = 99,
	Thread_Quit = 100,
};

struct ThreadCommandSturct {
	ThreadCommand command;
	bool isEffect;
	int synth;
	int latency;
	int sampleRate;
	std::wstring* fileName;
	void* receiveFlag;
	int task;
};

