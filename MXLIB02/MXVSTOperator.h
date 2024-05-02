#pragma once

#include "pch.h"
#include "NoteMessage.h"
#include "MXVSTInstrument.h"
#include <vector>
#include <list>
#include <thread>
#include <algorithm>
#include "ThreadCommand.h"
#include "MXVSTStream.h"
#include "boost/lockfree/queue.hpp"

class MXVSTOperator {
public:
	MXVSTOperator();
	~MXVSTOperator();
	int processQueuedThreadCommand();
	void postThreadCommand(ThreadCommandSturct* command);
	ThreadCommandSturct* peekThreadCommand();

	void setRecycle(ThreadCommandSturct* command);
	ThreadCommandSturct* createCommand();

	bool isOpen(bool effect, int synth);
	void postLaunchVST(bool effect, int synth, std::wstring& path, int task);
	void postOpenEditor(bool effect, int synth, int task, int whenclose);
	void postCloseEditor(bool effect, int synth, int task);
	void postRemoveSynth(bool effect, int synth, int task);
	void postLoadPreset(bool effect, int synth, std::wstring& path, int task);
	void postSavePreset(bool effect, int synth, std::wstring& path, int task);

	void postInitializeStream(int task);
	void postOpenStream(int stream, int sampleRate, int latency, int task);
	void postCloseStream(int task);

	void waitQueued(int task);
	void postQuit(int task);

	bool _quitThread;
	HANDLE _threadInit;

	MXVSTInstrument* getSynth(bool isEffector, int synth);
	float getMasterVolume();
	void setMasterVolume(float vol1);

private:

	MXVSTInstrument* _arraySynth[MAX_SYNTH];
	MXVSTInstrument* _arrayEffect[MAX_EFFECT];
	std::thread* _thread;
	float _masterVolume;
	boost::lockfree::queue<void*> _threadCommand;
};

extern MXVSTOperator* getOperator();
extern void handleOpenCloseWindow(HWND hWnd);
extern HWND hWndAppFrame;
