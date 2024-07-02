#pragma once

/* from https://github.com/iffyloop/EasyVst */

#include <Windows.h>
#include <sstream>
#include <iostream>
#include <string>

#include <portaudio.h>
#include <pa_asio.h>

#include <public.sdk/source/vst/hosting/plugprovider.h>
#include <public.sdk/source/vst/hosting/module.h>
#include <public.sdk/source/vst/hosting/hostclasses.h>
#include <public.sdk/source/vst/hosting/eventlist.h>
#include <public.sdk/source/vst/hosting/parameterchanges.h>
#include <public.sdk/source/vst/hosting/processdata.h>
#include <pluginterfaces/vst/ivsteditcontroller.h>
#include <pluginterfaces/vst/ivstprocesscontext.h>
#include <pluginterfaces/gui/iplugview.h>
#include <pluginterfaces/vst/vsttypes.h>
#include <pluginterfaces/base/ftypes.h>

#include "boost/lockfree/queue.hpp"

class EasyVstCustom {
public:
	EasyVstCustom();
	~EasyVstCustom();

	bool init(const std::string& path, int symbolicSampleSize, bool realtime);
	bool suspendVST();
	bool resumeVST();
	void destroy();

	ProcessContext* processContext();
	void setProcessContext(ProcessContext *context);
	void setProcessing(bool processing);
	bool processVST(int numSamples);
	
	const BusInfo* busInfo(MediaType type, BusDirection direction, int which);
	int numBuses(MediaType type, BusDirection direction);
	void setBusActive(MediaType type, BusDirection direction, int which, bool active);
	
	AudioBusBuffers* audioBusBuffer(BusDirection  directtion, int bus);
	EventList* eventList(BusDirection direction, int bus);
	ParameterChanges* parameterChanges(BusDirection direction);

	IEditController* getController();
	IComponent* getComponent();

	const char* name();
	bool openVstEditor();
	bool closeVstEditor();
	bool savePreset(const char* utfPath);
	bool loadPreset(const char* utfPath);
	HWND getHWnd();
	bool isOpen();
	void reset();
	void postProgramChange(int channel, int program);
	void postControlChange(int channel, int ccnumber, int program);

	void pushEvent(Event* ev);
	Event* popEvent();
	void pushShortMessage(uint32_t dword);
	void pushLongMessage(const uchar *data, size_t length);

	tresult getCCMappingInfo(int channel, int cc, ParamID& param);

	bool isSuspended();

private:

	bool _suspended;

	void _superDestroty(bool decrementRefCount);

	std::vector<BusInfo> _inAudioBusInfos, _outAudioBusInfos;

	std::vector<BusInfo> _inEventBusInfos, _outEventBusInfos;

	std::vector<SpeakerArrangement> _inSpeakerArrs, _outSpeakerArrs;

	VST3::Hosting::Module::Ptr _module = nullptr;

	IPtr<PlugProvider> _plugProvider = nullptr;
	IPtr<IComponent> _component = nullptr;
	IPtr<IAudioProcessor> _audioEffect = nullptr;
	IPtr<IEditController> _editController = nullptr;
	IPtr<IMidiMapping> _midiMapping = nullptr;
	IPtr<IUnitInfo> _unitInfo = nullptr;

	HostProcessData _processData = {};
	ProcessSetup _processSetup = {};
	ProcessContext _processContext = {};

	IPtr<IPlugView> _view = nullptr;
	HWND _window = 0;

	int _symbolicSampleSize = 0;
	bool _realtime = false;

	int _numInAudioBuses, _numInEventBuses, _numOutAudioBuses, _numOutEventBuses;

	std::string _path;
	std::string _name;

	static HostApplication* _standardPluginContext;
	static int _standardPluginContextRefCount;

	ParamID _programParameterID[16];
	UnitID _programUnitID[16];
	ParameterInfo _paramInfo = { 0 };

	EventList* _inputEvents = nullptr;
	EventList* _outputEvents = nullptr;
	ParameterChanges _inputParameterChanges = {};
	ParameterChanges _outputParameterChanges = {};


	boost::lockfree::queue<Event*> _pooledMessage;
};

void PRINT_DEBUG(const std::wstring& str);
void PRINT_ERROR(const std::wstring& str);
void PRINT_DEBUG(const std::string& str);
void PRINT_ERROR(const std::string& str);
void PRINT_DEBUG(const wchar_t* str);
void PRINT_ERROR(const wchar_t* str);

#define _printDebug PRINT_DEBUG
#define _printError PRINT_ERROR
