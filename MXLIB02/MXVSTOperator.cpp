#include "pch.h"
#include "MXVSTOperator.h"
#include "strconv.h"
#include "boost/lockfree/queue.hpp"

#define TITLE TEXT("MIXRecipe VST Window")
#define MDI_FRAME TEXT("FRAMEWINDOW")
#define MDI_CLIENT TEXT("MDICLIENT")
#define MDI_CHILD TEXT("MDICHILD")

#define ID_CHILDWND 0x100

extern void handleOpenCloseWindow(HWND hWnd);
HINSTANCE hInst;

int idChildWnd = 50000;
HWND hWndAppFrame = NULL;
HWND hClientWnd = NULL;

HWND makeVSTView(std::string title, int width, int height)
{
	if (hWndAppFrame == NULL) {
		hWndAppFrame = CreateWindow(
			MDI_FRAME, TITLE,
			WS_OVERLAPPEDWINDOW | WS_VISIBLE,
			CW_USEDEFAULT, CW_USEDEFAULT,
			CW_USEDEFAULT, CW_USEDEFAULT,
			NULL, NULL, hInstance, NULL
		);

		if (hWndAppFrame == NULL) {
			//TODO MessageBox
			return 0;
		}
	}
	std::wstring str2 = utf8_to_wide(title);
	MDICREATESTRUCT mdic;

	mdic.szClass = MDI_CHILD;
	mdic.szTitle = TITLE;
	mdic.x = mdic.y = mdic.cx = mdic.cy = 0;
	mdic.style = WS_CHILD;
	mdic.lParam = 0;

	HWND hwnd = CreateMDIWindow(
		MDI_CHILD, str2.c_str(), 0,
		100, 50, width, height + 40,
		hClientWnd, hInstance, (LPARAM)&mdic
	);


	if (hwnd == NULL) return 0;

	return hwnd;
}


//フレームウィンドウプロシージャ
LRESULT CALLBACK FrameWndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	HINSTANCE hInstance = GetModuleHandle(0);
	HWND h;
	CLIENTCREATESTRUCT ccs;

	static int testDataIndex;

	switch (message)
	{
	case WM_CREATE:
		ccs.hWindowMenu = GetSubMenu(GetMenu(hWnd), 1);
		ccs.idFirstChild = idChildWnd;

		//クライアントウィンドウの作成
		hClientWnd = CreateWindow(MDI_CLIENT, NULL,
			WS_CHILD | WS_CLIPCHILDREN | WS_VISIBLE | WS_VSCROLL | WS_HSCROLL,
			0, 0, 0, 0, hWnd, (HMENU)1, hInstance, &ccs);
		return 0;

	case WM_SYSCOMMAND:
		if (wParam == SC_CLOSE) {
			PostMessage(hWnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
			return 0;
		}
			break;
	case WM_COMMAND:
		break;

	case WM_DESTROY:
		return 0;
	}
	return DefFrameProc(hWnd, hClientWnd, message, wParam, lParam);
}


LRESULT CALLBACK ChildWndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam) {
	switch (message) {
	case WM_CLOSE:
		PostMessage(hWnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
		return 0;

	case WM_SIZING:
		return 0;

	case WM_SYSCOMMAND:
		if (wParam == SC_CLOSE) {
			PostMessage(hWnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
			return 0;
		}
		if (wParam == SC_RESTORE || wParam == SC_MINIMIZE) {
			LRESULT before = DefMDIChildProc(hWnd, message, wParam, lParam);
			handleOpenCloseWindow(hWnd);
			return before;
		}
		if (wParam == SC_MAXIMIZE) {
			return 0;
		}
		break;

	case WM_CREATE: {
		HMENU hMenu = GetSystemMenu(hWnd, 0);
		RemoveMenu(hMenu, SC_CLOSE, MF_BYCOMMAND);  //閉じるボタン
		RemoveMenu(hMenu, SC_MAXIMIZE, MF_BYCOMMAND); // 最大化ボタン
		//RemoveMenu(hMenu, SC_MINIMIZE, MF_BYCOMMAND) ' 最小化ボタン
		//RemoveMenu(hMenu, SC_RESTORE, MF_BYCOMMAND) ' 元に戻すボタン
		break;
	}
	case WM_DESTROY:
		return 0;
	}
	return DefMDIChildProc(hWnd, message, wParam, lParam);
}

void ThreadFunc(MXVSTOperator* ui) {
	MSG msg;
	PeekMessage(&msg, NULL, 0, 0, 0);
	::SetEvent(ui->_threadInit);

	BOOL bRet;
	WNDCLASS winFrame;

	winFrame.style = CS_HREDRAW | CS_VREDRAW;
	winFrame.lpfnWndProc = FrameWndProc;
	winFrame.cbClsExtra = winFrame.cbWndExtra = 0;
	winFrame.hInstance = hInstance;
	winFrame.hIcon = LoadIcon(NULL, IDI_APPLICATION);
	winFrame.hCursor = LoadCursor(NULL, IDC_ARROW);
	winFrame.hbrBackground = (HBRUSH)(COLOR_APPWORKSPACE + 1);
	winFrame.lpszMenuName = NULL;
	winFrame.lpszClassName = MDI_FRAME;

	RegisterClass(&winFrame);

	WNDCLASS winChild;

	winChild.style = CS_HREDRAW | CS_VREDRAW;
	winChild.lpfnWndProc = ChildWndProc;
	winChild.cbClsExtra = winChild.cbWndExtra = 0;
	winChild.hInstance = hInstance;
	winChild.hIcon = LoadIcon(NULL, IDI_APPLICATION);
	winChild.hCursor = LoadCursor(NULL, IDC_ARROW);
	winChild.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);
	winChild.lpszMenuName = NULL;
	winChild.lpszClassName = MDI_CHILD;

	RegisterClass(&winChild);

	hWndAppFrame = NULL;

	refAttachOnly();

	while (true) {
		__try {
			bRet = GetMessage(&msg, NULL, 0, 0);
			if (bRet == -1) {
				debugText(L"Handle Error");
				continue;
			}
			if (bRet > 0) {
				if (ui != NULL) {
					__try {
						if (ui != nullptr) {
							if (ui->processQueuedThreadCommand() > 0) {
							}
						}
						else {
							debugText(L"Handle Error");
						}
					}
					__except (systemExceptionMyHandler(L"ThreadFunc2", GetExceptionInformation()))
					{
					}
				}
				TranslateMessage(&msg);
				DispatchMessage(&msg);
			}
			if (bRet == 0) {
				debugText(L"GetMessage WM_QUIT");
				break;
			}
			if (ui->_quitThread) {
				debugText(L"GetMessage QuitThread");
				break;
			}
		}
		__except (systemExceptionMyHandler(L"ThreadFunc", GetExceptionInformation()))
		{
		}
	}
	debugText(L"endthreadex");
	::_endthreadex(0);
	debugText(L"~endthreadex");
}

MXVSTOperator::MXVSTOperator() {
	//インスタンスハンドルの取得


	_masterVolume = 0.1F;

	this->_threadInit = ::CreateEvent(
		NULL      // SECURITY_ATTRIBUTES構造体
		, TRUE      // リセットのタイプ( TRUE: 自動 / FALSE: 手動 )
		, FALSE     // 初期状態( TRUE: シグナル状態 / FALSE: 非シグナル状態 )
		, NULL      // イベントオブジェクトの名前
	);
	
	_quitThread = false;
	_thread = new std::thread(ThreadFunc, this);
	::WaitForSingleObject(this->_threadInit, INFINITE);
}

MXVSTOperator::~MXVSTOperator() {
	for (int i = 0; i < MAX_SYNTH; ++i) {
		MXVSTInstrument* vst = getSynth(false, i);
		if (vst != nullptr && vst->isOpen()) {
			postRemoveSynth(false, i, 0);
		}
	}
	for (int i = 0; i < MAX_EFFECT; ++i) {
		MXVSTInstrument* vst = getSynth(true, i);
		if (vst != nullptr && vst->isOpen()) {
			postRemoveSynth(true, i, 0);
		}
	}
	if (_thread != NULL) {
		delete _thread;
		_thread = NULL;
	}
}

bool MXVSTOperator::isOpen(bool effect, int synth) {
	MXVSTInstrument* vst = getSynth(effect, synth);
	if (vst != nullptr) {
		return vst->isOpen();
	}
	return false;
}

int MXVSTOperator::processQueuedThreadCommand() {
	int count = 0;
	while (!_quitThread) {
		ThreadCommandSturct* command = nullptr;
		int task(0), synth(0), result = 0;
		bool effect(false);
		MXVSTInstrument* vst = nullptr;
		__try
		{
			command = peekThreadCommand();
			if (command == nullptr) {
				break;
			}
			count++;
			synth = command->synth;
			effect = command->isEffect;
			task = command->task;
			result = Thread_NoSuccess;
			switch (command->command) {
			case Thread_InitalizeStream:
				if (getMXStream()->Initialize()) {
					result = Thread_Success;
				}
				break;

			case Thread_OpenStream:
				if (getMXStream()->openStream(synth, command->sampleRate, command->latency)) {
					result = Thread_Success;
				}
				break;

			case Thread_CloseStream:
				if (getMXStream()->closeStream()) {
					result = Thread_Success;
				}
				break;

			case Thread_OpenVSTEditor:
				vst = getSynth(effect, synth);
				if (vst != nullptr && vst->isOpen()) {
					debugText2(L"openVstEditor", vst->_path.c_str());
					if (vst->_easyVst->openVstEditor()) {
						result = Thread_Success;
					}
				}
				break;

			case Thread_CloseVSTEditor:
				vst = getSynth(effect, synth);
				if (vst != nullptr && vst->isOpen()) {
					debugText2(L"closeVstEditor", vst->_path.c_str());
					if (vst->_easyVst->closeVstEditor()) {
						result = Thread_Success;
					}
				}
				break;

			case Thread_LaunchVST: {
				vst = getSynth(effect, synth);
				if (vst != nullptr && command->fileName != nullptr) {
					vst->load(*command->fileName);
					result = Thread_Success;
				}
				break;
			}
			case Thread_RemoveVST: {
				vst = getSynth(effect, synth);
				if (vst != nullptr) {
					debugText(L"postRemoveSynth");
					if (vst->isOpen()) {
						vst->_easyVst->closeVstEditor();
					}
					vst->unload();
					result = Thread_Success;
				}
				break;
			}
			case Thread_LoadPreset: {
				vst = getSynth(effect, synth);
				if (vst != nullptr && vst->isOpen()) {
					if (command->fileName != nullptr) {
						vst->loadPreset(*command->fileName);
						result = Thread_Success;
					}
				}
				break;
			}
			case Thread_SavePreset: {
				vst = getSynth(effect, synth);
				if (vst != nullptr && vst->isOpen()) {
					if (command->fileName != nullptr) {
						vst->savePreset(*command->fileName);
						result = Thread_Success;
					}
				}
				break;
			}
			case Thread_WaitQueue:
				SetEvent(command->receiveFlag);
				result = Thread_Success;
				break;

			}
		}
		__except (systemExceptionMyHandler(L"processQueuedThreadCommand", GetExceptionInformation()))
		{
			result = Thread_Exception;
			debugNumber(L"command was", command->command);
			if (vst != nullptr) {
				refBlackListed(effect, synth);
			}
		}
		__try 
		{
			noticeTaskDone(task, result);
			setRecycle(command);
		}
		__except (systemExceptionMyHandler(L"Trusing", GetExceptionInformation()))
		{
		}
	}
	return count;
}

MXVSTInstrument* MXVSTOperator::getSynth(bool effect, int x) {
	if (!effect) {
		if (x >= 0 && x < MAX_SYNTH) {
			if (_arraySynth[x] == nullptr) {
				_arraySynth[x] = new MXVSTInstrument();
			}
			return _arraySynth[x];
		}
	}
	else {
		if (x >= 0 && x < MAX_EFFECT) {
			if (_arrayEffect[x] == nullptr) {
				_arrayEffect[x] = new MXVSTInstrument();
			}
			return _arrayEffect[x];
		}
	}
	return nullptr;
}

void MXVSTOperator::postThreadCommand(ThreadCommandSturct* command) {
	while (!_threadCommand.push(command)) {

	}

	PostThreadMessage(GetThreadId(_thread->native_handle()), WM_USER + 0x100, 0, 0);
}

ThreadCommandSturct* MXVSTOperator::peekThreadCommand() {
	if (_threadCommand.empty()) {
		return nullptr;
	}
	ThreadCommandSturct* ret;
	while (!_threadCommand.pop(ret)) {

	}
	return ret;
}

void MXVSTOperator::postLaunchVST(bool effect, int synth, std::wstring& path, int task) {
	ThreadCommandSturct* data = createCommand();
	std::wstring* param = new std::wstring(path);
	data->synth = synth;
	data->isEffect = effect;
	data->command = Thread_LaunchVST;
	data->task = task;
	data->fileName = param;
	postThreadCommand(data);
}

void MXVSTOperator::postOpenEditor(bool effect, int synth, int task, int whenclose) {
	ThreadCommandSturct* data = createCommand();
	data->isEffect = effect;
	data->synth = synth;
	data->command = Thread_OpenVSTEditor;
	data->task = task;
	_arraySynth[synth]->_whenClose = whenclose;
	postThreadCommand(data);
}

void MXVSTOperator::postCloseEditor(bool effect, int synth, int task) {
	ThreadCommandSturct* data = createCommand();
	data->isEffect = effect;
	data->synth = synth;
	data->command = Thread_CloseVSTEditor;
	data->task = task;
	postThreadCommand(data);
}

void MXVSTOperator::postRemoveSynth(bool effect, int synth, int task) {
	ThreadCommandSturct* data = createCommand();

	data->isEffect = effect;
	data->synth = synth;
	data->command = Thread_RemoveVST;
	data->task = task;
	postThreadCommand(data);
}

void MXVSTOperator::postQuit(int task) {
	if (_thread != NULL) {
		_quitThread = true;
		getMXStream()->quiting();
		PostThreadMessage(GetThreadId(_thread->native_handle()), WM_QUIT, 0, 0);
		for (int x = 0; x < MAX_SYNTH; ++x) {
			MXVSTInstrument* vst = getOperator()->getSynth(false, x);
			__try {
				if (vst != nullptr && vst->_path.empty() == false) {
					debugText2(L"Delete VSTClassObject for ", vst->_path.c_str());
					delete vst->_easyVst;
					vst->_easyVst = nullptr;
				}
			}
			__except (systemExceptionMyHandler(L"ThreadFunc2", GetExceptionInformation()))
			{
				debugText(L"Exception when FreeVST");
			}
		}
		/*
		if (_thread != NULL) {
			delete _thread;
			_thread = NULL;
		}*/
	}
}

void MXVSTOperator::postLoadPreset(bool effect, int synth, std::wstring& path, int task) {
	ThreadCommandSturct* data = createCommand();
	std::wstring* param = new std::wstring(path);
	data->isEffect = effect;
	data->synth = synth;
	data->command = Thread_LoadPreset;
	data->task = task;
	data->fileName = param;
	postThreadCommand(data);
}

void MXVSTOperator::postSavePreset(bool effect, int synth, std::wstring& path, int task) {
	ThreadCommandSturct* data = createCommand();
	std::wstring* param = new std::wstring(path);
	data->isEffect = effect;
	data->synth = synth;
	data->command = Thread_SavePreset;
	data->task = task;
	data->fileName = param;
	postThreadCommand(data);
}

void MXVSTOperator::postInitializeStream(int task) {
	ThreadCommandSturct* data = createCommand();
	data->command = Thread_InitalizeStream;
	data->task = task;
	postThreadCommand(data);
}

void MXVSTOperator::postOpenStream(int stream, int sampleRate, int latency, int task) {
	ThreadCommandSturct* data = createCommand();
	data->synth = stream;
	data->command = Thread_OpenStream;
	data->sampleRate = sampleRate;
	data->task = task;
	data->latency = latency;
	postThreadCommand(data);
}

void MXVSTOperator::postCloseStream(int task) {
	ThreadCommandSturct* data = createCommand();
	data->command = Thread_CloseStream;
	data->task = task;
	postThreadCommand(data);
}

void MXVSTOperator::waitQueued(int task) {
	ThreadCommandSturct* command = createCommand();
	HANDLE receiveFlag = ::CreateEvent(NULL, TRUE, FALSE, NULL);

	command->task = 0;
	command->command = Thread_WaitQueue;
	command->task = task;
	command->receiveFlag = receiveFlag;
	postThreadCommand(command);
	
	PostThreadMessage(GetThreadId(_thread->native_handle()), WM_USER + 0x100, 0, 0);
	::WaitForSingleObject(receiveFlag, INFINITE);
	CloseHandle(receiveFlag);
}

void handleOpenCloseWindow(HWND hWnd) {
	for (int i = 0; i < MAX_SYNTH; ++i) {
		MXVSTInstrument* vst = getOperator()->getSynth(false, i);
		if (vst != nullptr && vst->_easyVst != nullptr) {
			if (vst->_easyVst->getHWnd() == hWnd) {
				if (vst->_whenClose >= 0) {
					noticeTaskDone(vst->_whenClose, Thread_Success);
				}
				return;
			}
		}
	}
	for (int i = 0; i < MAX_EFFECT; ++i) {
		MXVSTInstrument* vst = getOperator()->getSynth(true, i);
		if (vst != nullptr && vst->_easyVst != nullptr) {
			if (vst->_easyVst->getHWnd() == hWnd) {
				if (vst->_whenClose >= 0) {
					noticeTaskDone(vst->_whenClose, Thread_Success);
				}
				return;
			}
		}
	}
}


MXVSTOperator* _synthRack = nullptr;

MXVSTOperator* getOperator() {
	if (_synthRack == NULL) {
		_synthRack = new MXVSTOperator();
	}
	return _synthRack;
}

float MXVSTOperator::getMasterVolume() {
	return _masterVolume;
}

void MXVSTOperator::setMasterVolume(float vol1) {
	_masterVolume = vol1;
}

boost::lockfree::queue<ThreadCommandSturct*> _recycleBin(128);
int countNew = 0;

void MXVSTOperator::setRecycle(ThreadCommandSturct* command) {
	while (!_recycleBin.push(command)) {

	}

	if (command->fileName != nullptr) {
		delete command->fileName;
	}
	command->command = Thread_None;
	command->synth = 0;
	command->latency = 0;
	command->sampleRate = 0;
	command->fileName = nullptr;
	command->receiveFlag = nullptr;
	command->task = 0;
}

ThreadCommandSturct* MXVSTOperator::createCommand() {
	if (_recycleBin.empty()) {
		++countNew;
		debugNumber(L"Not Recycle, Count New = ", countNew);
		return new ThreadCommandSturct();
	}
	ThreadCommandSturct* ret;
	while (!_recycleBin.pop(ret)) {

	}
	return ret;
}