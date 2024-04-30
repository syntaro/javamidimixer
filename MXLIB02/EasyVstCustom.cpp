#include "pch.h"
#include "strconv.h"
#include "EasyVSTCustom.h"
#include "NoteMessage.h"
#include "MXVSTStream.h"
#include "MXVSTOperator.h"

#ifdef DEBUG
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Debug/base.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Debug/pluginterfaces.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Debug/sdk.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Debug/sdk_hosting.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Debug/sdk_common.lib")
#else
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Release/base.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Release/pluginterfaces.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Release/sdk.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Release/sdk_hosting.lib")
#pragma comment(lib,"C:/github/vst3sdk_build/lib/Release/sdk_common.lib")
#endif

HostApplication* EasyVstCustom::_standardPluginContext = nullptr;
int EasyVstCustom::_standardPluginContextRefCount = 0;

EasyVstCustom::EasyVstCustom()
{
}

EasyVstCustom::~EasyVstCustom()
{
	closeVstEditor();
	_view = nullptr;
	//reset();
}

bool EasyVstCustom::init(const std::string& path, int symbolicSampleSize, bool realtime)
{
	int sampleRate = getMXStream()->getSampleRate();
	int blockSize = getMXStream()->getBlockSize();

	reset();
	++_standardPluginContextRefCount;
	if (!_standardPluginContext) {
		_standardPluginContext = owned(NEW HostApplication());
		PluginContextFactory::instance().setPluginContext(_standardPluginContext);
	}

	_path = path;

	_symbolicSampleSize = symbolicSampleSize;
	_realtime = realtime;

	_processSetup.processMode = _realtime ? Vst::ProcessModes::kRealtime : kPrefetch;
	_processSetup.symbolicSampleSize = symbolicSampleSize;
	_processSetup.sampleRate = sampleRate;
	_processSetup.maxSamplesPerBlock = 4096;

	debugNumber(L"first sampleRate", sampleRate);
	debugNumber(L"first blockSize", blockSize);

	_processData.numSamples = blockSize;
	_processData.symbolicSampleSize = _symbolicSampleSize;
	_processData.processContext = &_processContext;

	std::string error;
	_module = VST3::Hosting::Module::create(path, error);
	if (!_module) {
		_printError(error);
		return false;
	}

	VST3::Hosting::PluginFactory factory = _module->getFactory();
	for (auto& classInfo : factory.classInfos()) {
		if (classInfo.category() == kVstAudioEffectClass) {
			/*
			std::cout << "**Name " << classInfo.name() << std::endl;
			std::cout << "ID " << classInfo.ID().toString() << std::endl;
			std::cout << "Category " << classInfo.category() << std::endl;
			std::cout << "Vendor " << classInfo.vendor() << std::endl;
			std::cout << "Version " << classInfo.version() << std::endl;
			std::cout << "VST SDK " << classInfo.sdkVersion() << std::endl;
			std::cout << "SubCategory " << classInfo.subCategoriesString() << std::endl;
			std::cout << "Flags " << classInfo.classFlags() << std::endl;
			std::cout << std::endl;
			*/

			_plugProvider = owned(NEW PlugProvider(factory, classInfo, true));
			if (!_plugProvider) {
				_printError("No PlugProvider found");
				return false;
			}

			_component = _plugProvider->getComponent();

			_audioEffect = FUnknownPtr<IAudioProcessor>(_component);
			if (!_audioEffect) {
				_printError("Could not get audio processor from VST");
				return false;
			}

			_editController = _plugProvider->getController();

			_name = classInfo.name();

			FUnknownPtr<IProcessContextRequirements> contextRequirements(_audioEffect);
			if (contextRequirements) {
				auto flags = contextRequirements->getProcessContextRequirements();
#define PRINT_FLAG(x) if (flags & IProcessContextRequirements::Flags::x) { _printDebug(#x); }
				PRINT_FLAG(kNeedSystemTime)
					PRINT_FLAG(kNeedContinousTimeSamples)
					PRINT_FLAG(kNeedProjectTimeMusic)
					PRINT_FLAG(kNeedBarPositionMusic)
					PRINT_FLAG(kNeedCycleMusic)
					PRINT_FLAG(kNeedSamplesToNextClock)
					PRINT_FLAG(kNeedTempo)
					PRINT_FLAG(kNeedTimeSignature)
					PRINT_FLAG(kNeedChord)
					PRINT_FLAG(kNeedFrameRate)
					PRINT_FLAG(kNeedTransportState)
#undef PRINT_FLAG
			}

			/* プログラムチェンジの準備 */

			if (true){
				//SynthTAROU
				FUnknownPtr<IMidiMapping> contextRequirements(_midiMapping);
				
				_editController->queryInterface(IMidiMapping::iid, (void**)&_midiMapping);
				_editController->queryInterface(IUnitInfo::iid, (void**)&_unitInfo);

				int idx, num = _editController->getParameterCount();
				int ch = 0;
				for (idx = 0; idx < num; ++idx) {
					if (_editController->getParameterInfo(idx, _paramInfo) == kResultOk
						&& (_paramInfo.flags & ParameterInfo::kIsProgramChange) != 0) {
						_programParameterID[ch]  = _paramInfo.id;
						_programUnitID[ch] = _paramInfo.unitId;
						ch++;
					}
				}
			}

			_numInAudioBuses = _component->getBusCount(MediaTypes::kAudio, BusDirections::kInput);
			debugNumber(L"numInAudioBuses", _numInAudioBuses);

			for (int i = 0; i < _numInAudioBuses; ++i) {
				BusInfo info;
				_component->getBusInfo(kAudio, kInput, i, info);
				_inAudioBusInfos.push_back(info);
				setBusActive(kAudio, kInput, i, false);

				SpeakerArrangement speakerArr;
				_audioEffect->getBusArrangement(kInput, i, speakerArr);
				_inSpeakerArrs.push_back(speakerArr);
			}


			_numInEventBuses = _component->getBusCount(MediaTypes::kEvent, BusDirections::kInput);
			debugNumber(L"numInEventBuses", _numInEventBuses);

			for (int i = 0; i < _numInEventBuses; ++i) {
				BusInfo info;
				_component->getBusInfo(kEvent, kInput, i, info);
				_inEventBusInfos.push_back(info);
				setBusActive(kEvent, kInput, i, false);
			}

			_numOutAudioBuses = _component->getBusCount(MediaTypes::kAudio, BusDirections::kOutput);
			debugNumber(L"numOutAudioBuses", _numOutAudioBuses);

			for (int i = 0; i < _numOutAudioBuses; ++i) {
				BusInfo info;
				_component->getBusInfo(kAudio, kOutput, i, info);
				_outAudioBusInfos.push_back(info);
				setBusActive(kAudio, kOutput, i, false);

				SpeakerArrangement speakerArr;
				_audioEffect->getBusArrangement(kOutput, i, speakerArr);
				_outSpeakerArrs.push_back(speakerArr);
			}

			_numOutEventBuses = _component->getBusCount(MediaTypes::kEvent, BusDirections::kOutput);
			debugNumber(L"numOutEventBuses", _numOutEventBuses);

			for (int i = 0; i < _numOutEventBuses; ++i) {
				BusInfo info;
				_component->getBusInfo(kEvent, kOutput, i, info);
				_outEventBusInfos.push_back(info);
				setBusActive(kEvent, kOutput, i, false);
			}

			debugText(L"setBusArrangements");

			tresult res = _audioEffect->setBusArrangements(_inSpeakerArrs.data(), _numInAudioBuses, _outSpeakerArrs.data(), _numOutAudioBuses);
			if (res != kResultTrue) {
				_printError("Failed to set bus arrangements");
				//return false;
			}

			debugText(L"setupProcessing");

			res = _audioEffect->setupProcessing(_processSetup);
			if (res == kResultOk) {
				_processData.prepare(*_component, blockSize, _processSetup.symbolicSampleSize);
				if (_numInEventBuses > 0) {
					_inputEvents = new EventList[_numInEventBuses];
				}
				if (_numOutEventBuses > 0) {
					_outputEvents = new EventList[_numOutEventBuses];
				}
			}
			else {
				_printError("Failed to setup VST processing");
				return false;
			}

			if (_component->setActive(true) != kResultTrue) {
				_printError("Failed to activate VST component");
				return false;
			}
		}
	}

	return true;
}

bool EasyVstCustom::isSuspended() {
	return _suspended;
}

bool EasyVstCustom::suspendVST() {
	_suspended = true;
	_audioEffect->setProcessing(false);
	_component->setActive(false);

	return true;
}

bool EasyVstCustom::resumeVST() {
	int sampleRate = getMXStream()->getSampleRate();
	int blockSize = getMXStream()->getBlockSize();

	_processData.unprepare();

	_processSetup.processMode = _realtime ? Vst::ProcessModes::kRealtime : 0;
	_processSetup.symbolicSampleSize = _symbolicSampleSize;
	_processSetup.sampleRate = sampleRate;
	_processSetup.maxSamplesPerBlock = 4096;

	_processData.prepare(*_component, blockSize, _processSetup.symbolicSampleSize);

	tresult a = _audioEffect->setupProcessing(_processSetup);
	tresult b = _component->setActive(true);
	tresult c = _audioEffect->setProcessing(true);
	debugText2(L"setupProcessing ", a != kResultOk ? L"false" : L"true");
	debugText2(L"setActive ", b != kResultOk ? L"false" : L"true");
	debugText2(L"setProcessing ", c != kResultOk ? L"false" : L"true");
	_suspended = false;
	return true;
}

void EasyVstCustom::destroy()
{
	_superDestroty(true);
}

void EasyVstCustom::reset()
{
	_superDestroty(false);
}

BOOL bufferToFile(const char* utfPath, IBStream& buffer) {
	FILE* fp;
	if (fopen_s(&fp, utfPath, "wb") != 0) {
		return FALSE;
	}
	int trans;
	char* data = new char[16384];
	buffer.seek(0, IBStream::kIBSeekSet);
	while (true) {
		trans = 0;
		tresult ret = buffer.read(data, 16384, &trans);
		if (trans == 0) {
			_printError(L"buffer.read 0 byte");
			break;
		}
		size_t wrote = ::fwrite(data, 1, trans, fp);
		if (wrote != trans) {
			::fclose(fp);
			remove(utfPath);
			return FALSE;
		}
	}
	::fclose(fp);
	delete data;
	return TRUE;
}

BOOL fileToBuffer(const char* utfPath, IBStream& buffer) {
	FILE* fp;
	if (fopen_s(&fp, utfPath, "rb") != 0) {
		return FALSE;
	}
	int trans;
	char* data = new char[16384];
	buffer.seek(0, IBStream::kIBSeekSet);
	while (true) {
		trans = fread(data, 1, 16384, fp);
		if (trans <= 0) {
			break;
		}
		buffer.write(data, trans);
	}
	::fclose(fp);
	buffer.seek(0, IBStream::kIBSeekSet);
	delete data;
	return TRUE;
}

bool EasyVstCustom::savePreset(const char* utfPath) {
	bool success0 = FALSE, success1 = FALSE;
	try {
		BufferStream buffer0;
		BufferStream buffer1;
		FUID uid;
		_component->getControllerClassId((char*)&uid);

		success0 = PresetFile::savePreset(
			&buffer0,
			uid,
			_component,
			_editController
		);
		success1 = PresetFile::savePreset(
			&buffer1,
			uid,
			_component,
			_editController
		);

		if (success0) {
			int64 pos0, pos1;
			buffer0.tell(&pos0);
			buffer1.tell(&pos1);
			if (pos0 < pos1) {
				return bufferToFile(utfPath, buffer1);
			}
			else {
				return bufferToFile(utfPath, buffer0);
			}
		}
		if (!success0) {
			_printError(L"false from #savePreset");
		}
	}
	catch (...) {
		_printError("something happens in savePreset");
	}
	return success0;
}

bool EasyVstCustom::loadPreset(const char* utfPath) {
	bool success = FALSE;
	try {
		BufferStream buffer;
		FUID uid;
		_component->getControllerClassId((char*)&uid);

		success = fileToBuffer(utfPath, buffer);

		if (success) {
			success = PresetFile::loadPreset(
				&buffer,
				uid,
				_component,
				_editController
			);
		}

		if (!success) {
			_printError(L"false from #loadPreset");
		}
	}
	catch (...) {
		_printError(L"something happens in loadPreset");
	}
	return success;
}

bool EasyVstCustom::processVST(int numSamples)
{
	__try {
		if (isOpen() == false) {
			return true;
		}
		if (_suspended) {
			return true;
		}

		_processData.inputEvents = _inputEvents;
		_processData.outputEvents = _outputEvents;
		_processData.inputParameterChanges = &_inputParameterChanges;
		_processData.outputParameterChanges = &_outputParameterChanges;
		_processData.numSamples = numSamples;

		if (_audioEffect != nullptr) {
			tresult result = _audioEffect->process(_processData);
			if (result != kResultOk) {
				return false;
			}
		}
		return true;
	}
	__except (systemExceptionMyHandler(L"processVST", GetExceptionInformation())) {
	}
	return false;
}

const BusInfo* EasyVstCustom::busInfo(MediaType type, BusDirection direction, int which)
{
	if (type == kAudio) {
		if (direction == kInput) {
			return &_inAudioBusInfos[which];
		}
		else if (direction == kOutput) {
			return &_outAudioBusInfos[which];
		}
		else {
			return nullptr;
		}
	}
	else if (type == kEvent) {
		if (direction == kInput) {
			return &_inEventBusInfos[which];
		}
		else if (direction == kOutput) {
			return &_outEventBusInfos[which];
		}
		else {
			return nullptr;
		}
	}
	else {
		return nullptr;
	}
}

int EasyVstCustom::numBuses(MediaType type, BusDirection direction)
{
	if (type == kAudio) {
		if (direction == kInput) {
			return _component->getBusCount(MediaTypes::kAudio, BusDirections::kInput);
		}
		else if (direction == kOutput) {
			return _component->getBusCount(MediaTypes::kAudio, BusDirections::kOutput);
		}
		else {
			return 0;
		}
	}
	else if (type == kEvent) {
		if (direction == kInput) {
			return _component->getBusCount(MediaTypes::kEvent, BusDirections::kInput);
		}
		else if (direction == kOutput) {
			return _component->getBusCount(MediaTypes::kEvent, BusDirections::kOutput);
		}
		else {
			return 0;
		}
	}
	else {
		return 0;
	}
}

void EasyVstCustom::setBusActive(MediaType type, BusDirection direction, int which, bool active)
{
	_component->activateBus(type, direction, which, active);
}

void EasyVstCustom::setProcessing(bool processing)
{
	tresult c = _audioEffect->setProcessing(processing);
}

ProcessContext* EasyVstCustom::processContext()
{
	return &_processContext;
}

void EasyVstCustom::setProcessContext(ProcessContext *context)
{
	_processContext = *context;
}

AudioBusBuffers* EasyVstCustom::audioBusBuffer(BusDirection  direction, int bus) {
	if (direction == kInput && _processData.inputs != NULL) {
		return &_processData.inputs[bus];
	}
	else if (direction == kOutput && _processData.outputs != NULL) {
		return &_processData.outputs[bus];
	}
	else {
		return nullptr;
	}
}

EventList* EasyVstCustom::eventList(BusDirection direction, int bus)
{
	if (direction == kInput) {
		return static_cast<EventList*>(_inputEvents);
	}
	else if (direction == kOutput) {
		return static_cast<EventList*>(_outputEvents);
	}
	else {
		return nullptr;
	}
}

ParameterChanges* EasyVstCustom::parameterChanges(BusDirection direction)
{
	if (direction == kInput) {
		return &_inputParameterChanges;
	}
	else if (direction == kOutput) {
		return &_outputParameterChanges;
	}
	else {
		return nullptr;
	}
}


#include <process.h>
extern HWND makeVSTView(std::string title, int width, int height);

bool EasyVstCustom::openVstEditor()
{
	if (!_editController) {
		_printError("VST does not provide an edit controller");
		return false;
	}

	if (_view == nullptr) {

		_view = _editController->createView(ViewType::kEditor);
		if (!_view) {
			_printError("EditController does not provide its own view");
			return false;
		}
	}

	ViewRect viewRect = {};
	if (_view->getSize(&viewRect) != kResultOk) {
		_printError("Failed to get editor view size");
		return false;
	}

#ifdef _WIN32
	if (_view->isPlatformTypeSupported(Steinberg::kPlatformTypeHWND) != Steinberg::kResultTrue) {
		_printError("Editor view does not support HWND");
		return false;
	}
#else
	_printError("Platform is not supported yet");
	return false;
#endif
	_window = makeVSTView(_name.c_str(), viewRect.getWidth(), viewRect.getHeight());
	if (_window == NULL) {
		_printError("makeVSTView returned NULL");
		return false;
	}

#ifdef _WIN32
	int ret = _view->attached(_window, Steinberg::kPlatformTypeHWND);
	if (ret != Steinberg::kResultOk) {
		_printError(L"Failed to attach editor view to HWND retcode");
		return false;
	}
#endif

	return true;
}

bool EasyVstCustom::closeVstEditor()
{
	if (_window) {

		_view->removed();
		DestroyWindow(_window);
		_window = nullptr;
		return true;
	}
	return false;
}

const char* EasyVstCustom::name()
{
	return _name.c_str();
}

HWND EasyVstCustom::getHWnd()
{
	return _window;
}

bool EasyVstCustom::isOpen()
{
	return _editController != nullptr;
}

IEditController* EasyVstCustom::getController() {
	return _editController;
}

IComponent* EasyVstCustom::getComponent() {
	return _component;
}

void EasyVstCustom::_superDestroty(bool decrementRefCount)
{
	_editController = nullptr;
	_audioEffect = nullptr;
	_component = nullptr;
	_plugProvider = nullptr;
	_module = nullptr;
	//_midiMapping = nullptr;
	//_unitInfo = nullptr;

	_inAudioBusInfos.clear();
	_outAudioBusInfos.clear();

	_inEventBusInfos.clear();
	_outEventBusInfos.clear();

	_inSpeakerArrs.clear();
	_outSpeakerArrs.clear();

	if (_processData.inputEvents) {
		delete[] static_cast<EventList*>(_processData.inputEvents);
	}
	if (_processData.outputEvents) {
		delete[] static_cast<EventList*>(_processData.outputEvents);
	}

	Event* evt;
	while (_pooledMessage.empty() == false) {
		while (!_pooledMessage.pop(evt)) {

		}
		setEventRecycled(evt);
	}

	_processData.unprepare();
	
	_processSetup = {};
	_processContext = {};

	_symbolicSampleSize = 0;
	_realtime = false;

	_path = "";
	_name = "";

	if (decrementRefCount) {
		/*
		if (_standardPluginContextRefCount > 0) {
			--_standardPluginContextRefCount;
		}
		if (_standardPluginContext && _standardPluginContextRefCount == 0) {
			PluginContextFactory::instance().setPluginContext(nullptr);
			_standardPluginContext->release();
			delete _standardPluginContext;
			_standardPluginContext = nullptr;
		}
		*/
	}
}


#define DEBUG 1

void PRINT_DEBUG(const std::wstring& str) {
#if DEBUG
	debugText(str.c_str());
#endif
}
void PRINT_ERROR(const std::wstring& str) {
	debugText(str.c_str());
}
void PRINT_DEBUG(const std::string& str) {
#if DEBUG
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	std::wstring str2 = converter.from_bytes(str); 
	debugText(str2.c_str());
#endif
}
void PRINT_ERROR(const std::string& str) {
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	std::wstring str2 = converter.from_bytes(str);
	debugText(str2.c_str());
}
void PRINT_DEBUG(const wchar_t* str) {
#if DEBUG
	debugText(str);
#endif
}
void PRINT_ERROR(const wchar_t* str) {
	debugText(str);
}

tresult EasyVstCustom::getCCMappingInfo(int channel, int cc, ParamID& param) {
	if (_midiMapping == nullptr) {
		debugText(L"midiMapping not avail");
		return kResultFalse;
	}
	tresult res = _midiMapping->getMidiControllerAssignment(0, channel, cc, param);
	if (res != kResultTrue) {
		debugText(L"midiMapping not assigned");
		return kResultFalse;
	}
	return kResultTrue;
}

void EasyVstCustom::postProgramChange(int channel, int program) {
	double newProgram;
	newProgram = _editController->plainParamToNormalized(_programParameterID[channel], program);
	ParameterChanges* changed = parameterChanges(kInput);
	if (changed != nullptr) {
		Steinberg::int32 retIndex = 0;
		IParamValueQueue* queue = changed->addParameterData(_programParameterID[channel], retIndex);
		if (queue != nullptr) {
			queue->addPoint(0, newProgram, retIndex);
		}
	}
	else {
		debugText(L"Can't add parameter");
	}
}

void EasyVstCustom::postControlChange(int channel, int ccnumber, int value) {
	ParamID param;
	if (getCCMappingInfo(channel, ccnumber, param) == kResultOk) {
		//ok
	}
	else {
		Event* evt = useEventRecycled();
		evt->type = Event::EventTypes::kLegacyMIDICCOutEvent;
		evt->midiCCOut.channel = channel;
		evt->midiCCOut.controlNumber = ccnumber;
		evt->midiCCOut.value = value;
		pushEvent(evt);
		return;
	}

	double dataD;// = _editController->plainParamToNormalized(param, value);
	//if (ccnumber == 129) {
		dataD = value / 127.0f;
	//}
	ParameterChanges* changed = parameterChanges(kInput);
	if (changed != nullptr) {
		Steinberg::int32 retIndex = 0;
		IParamValueQueue* queue = changed->addParameterData(param, retIndex);
		if (queue != nullptr) {
			queue->addPoint(0, dataD, retIndex);
		}
	}
	else {
		debugText(L"Can't add parameter");
	}
}

void EasyVstCustom::pushLongMessage(const uchar* data, size_t length) {
	if (isOpen() == false) {
		return;
	}
	uchar* newData = new uchar[length];
	memcpy(newData, data, length);
	Event* evt = useEventRecycled();
	evt->type = Event::EventTypes::kDataEvent;
	evt->data.bytes = newData;
	evt->data.size = length;
	pushEvent(evt);
}

void EasyVstCustom::pushShortMessage(uint32_t dword) {
	if (isOpen() == false) {
		return;
	}

	int status = (dword >> 16) & 0xff;
	int data1 = (dword >> 8) & 0xff;
	int data2 = (dword) & 0xff;

	int command = status & 0xf0;
	int channel = status & 0x0f;

	bool isNoteOn = (command == 0x90);
	bool isNoteOff = (command == 0x80);

	if (isNoteOn && data2 == 0) {
		isNoteOff = true;
		isNoteOn = false;
	}
	if (isNoteOn) {
		Event* evt = useEventRecycled();
		evt->type = Event::EventTypes::kNoteOnEvent;
		evt->noteOn.channel = channel;
		evt->noteOn.pitch = data1;
		evt->noteOn.tuning = 0.0f;
		evt->noteOn.velocity = data2 / 127.0f;
		evt->noteOn.noteId = data1;
		pushEvent(evt);
		return;
	}
	else if (isNoteOff) {
		Event* evt = useEventRecycled();
		evt->type = Event::EventTypes::kNoteOffEvent;
		evt->noteOff.channel = channel;
		evt->noteOff.pitch = data1;
		evt->noteOff.tuning = 0.0f;
		evt->noteOff.velocity = data2 / 127.0f;
		evt->noteOff.noteId = data1;
		pushEvent(evt);
	}
	else if (command == 0xa0) {
		Event* evt = useEventRecycled();
		evt->type = Event::EventTypes::kPolyPressureEvent;
		evt->midiCCOut.channel = channel;
		evt->midiCCOut.controlNumber = data1;
		evt->midiCCOut.value = data2;
		pushEvent(evt);
	}
	else if (command == 0xb0) {
		if (data1 <= 119) {
			postControlChange(channel, data1, data2);
		}
		else {
			//Channel Mode Message
		}
	}
	else if (command == 0xc0) {
		//Program Chaneg (DATA1)
		postProgramChange(channel, data1);
	}
	else if (command == 0xd0) {
		postControlChange(channel, 128, data1);
	}
	else if (command == 0xe0) {
		postControlChange(channel, 129, data2);
	}
}

void EasyVstCustom::pushEvent(Event* ev) {
	while (!_pooledMessage.push(ev)) {
	}
}

Event* EasyVstCustom::popEvent() {
	Event* evt;
	if (_pooledMessage.empty()) {
		return nullptr;
	}
	while(!_pooledMessage.pop(evt)) {
		if (getOperator()->_quitThread) {
			return nullptr;
		}
	}
	return evt;
}

