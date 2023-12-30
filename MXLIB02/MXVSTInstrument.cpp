#include "pch.h"
#include "MXVSTInstrument.h"
#include "strconv.h"
#include "MXVSTOperator.h"

using std::cout; using std::endl;
using std::chrono::duration_cast;
using std::chrono::milliseconds;
using std::chrono::seconds;
using std::chrono::system_clock;

extern MXVSTInstrument* _arraySynth[16];

MXVSTInstrument::MXVSTInstrument() {
    _whenClose = -1;
    _easyVst = nullptr;
    _busesVolumeCount = 0;
    _busesVolume = nullptr;

    _blackList = false;
    _insertBalance = 0;
    _auxSend = 0;
}

MXVSTInstrument::~MXVSTInstrument() {
    std::cout << "Destruct" << std::endl;
}

bool MXVSTInstrument::load(const std::wstring& path) {
    unload();
    _path = path;
    std::string utfPath = wide_to_utf8(_path);

    EasyVstCustom *vst = new EasyVstCustom();

    if (!vst->init(utfPath.c_str(), kSample32, true)) {
        std::cerr << "Failed to init VST" << std::endl;
        return false;
    }

    int numAudioOutBuses = vst->numBuses(kAudio, kOutput);
    if (numAudioOutBuses < 1) {
        std::cerr << "Incorrect bus configuration" << std::endl;
        return false;
    }

    int numEventInBuses = vst->numBuses(kEvent, kInput);
    /*
    if (numEventInBuses < 1) {
        std::cerr << "Incorrect bus configuration" << std::endl;
        return false;
    }
    */

    if (numAudioOutBuses >= 1) {
        makeBusRoom(numAudioOutBuses - 1);
    }

    for (int i = 0; i < numEventInBuses; ++i) {
        vst->setBusActive(kEvent, kInput, i, true);
    }

    for (int i = 0; i < numAudioOutBuses; ++i) {
        _busesVolume[i] = 1;
        vst->setBusActive(kAudio, kOutput, i, true);
    }

    int numAudioInBuses = vst->numBuses(kAudio, kInput);

    for (int i = 0; i < numAudioInBuses; ++i) {
        if (i == 0) {
            vst->setBusActive(kAudio, kInput, i, true);
            vst->audioBusBuffer(kInput, i)->silenceFlags = 0;
        }
        else {
            vst->setBusActive(kAudio, kInput, i, false);
            vst->audioBusBuffer(kInput, i)->silenceFlags = 0xff;
        }
    }
    _easyVst = vst;
    vst->setProcessing(true);

    return true;
}

void MXVSTInstrument::unload(void) {
    if (_easyVst != nullptr) {
        if (_blackList) {
            std::cout << "BL Bye" << std::endl;
            _easyVst = nullptr;
        }
        else {
            std::cout << "Doing Unload " << std::endl;
            int num = _easyVst->numBuses(kAudio, kOutput);
            for (int i = 0; i < num; ++i) {
                _easyVst->setBusActive(kAudio, kOutput, i, false);
            }
            _easyVst->reset();
            _easyVst = nullptr;
        }
    }
}

void MXVSTInstrument::makeBusRoom(int bus) {
    if (_busesVolumeCount < bus + 1) {
        float* newVolume = new float[bus + 1];
        for (int i = 0; i < bus + 1; ++i) {
            if (i < _busesVolumeCount) {
                newVolume[i] = _busesVolume[i];
            }
            else {
                newVolume[i] = 0;
            }
        }
        float* delPtr = _busesVolume;
        _busesVolumeCount = bus + 1;
        _busesVolume = newVolume;
        delete delPtr;
    }
}

int MXVSTInstrument::getBusCount() {
    return _busesVolumeCount;
}

float MXVSTInstrument::getBusVolume(int bus) {
    makeBusRoom(bus);
    return _busesVolume[bus];
}

void MXVSTInstrument::setBusVolume(int bus, float vol1) {
    makeBusRoom(bus);
    _busesVolume[bus] = vol1;
}

void MXVSTInstrument::setBusAcive(int bus, bool active) {
    makeBusRoom(bus);
    _easyVst->setBusActive(kAudio, kOutput, bus, active);
}

double currentTimeMilllies() {
    auto millisec_since_epoch = duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
    return millisec_since_epoch;
}

bool MXVSTInstrument::processAudio(TimeInfo& ti, MXStereoBuffer* inputBuffer, MXStereoBuffer* outputBuffer, float outputVolumme, MXStereoBuffer* efxBuffer, float efxVolume) {
    if (isActive() == false) {
        return true;
    }

    int framesPerBuffer = ti.sample_length_;

    ProcessContext* processContext = _easyVst->processContext();

    processContext->state = ProcessContext::kPlaying;
    processContext->sampleRate = ti.sample_rate_;
    processContext->projectTimeSamples = ti.sample_pos_;
    processContext->state |= ProcessContext::kTempoValid;
    processContext->tempo = TEMPO;
    processContext->state |= ProcessContext::kTimeSigValid;
    processContext->timeSigNumerator = 4;
    processContext->timeSigDenominator = 4;
    processContext->state |= ProcessContext::kContTimeValid;
    processContext->continousTimeSamples = ti.sample_pos_;
    processContext->state |= ProcessContext::kProjectTimeMusicValid;
    processContext->projectTimeMusic = ti.ppq_pos_;
    processContext->state |= ProcessContext::kSystemTimeValid;
    processContext->systemTime = ti.clock;
    EventList* eventList = _easyVst->eventList(kInput, 0);

    int inputBuses = _easyVst->numBuses(kAudio, kInput);

    if (inputBuses > 0 && inputBuffer != nullptr) {
        AudioBusBuffers* bus = _easyVst->audioBusBuffer(kInput, 0);
        bus->silenceFlags = 0;
        int channel = bus->numChannels;
        if (channel >= 2) {
            Sample32* left = bus->channelBuffers32[0];
            Sample32* right = bus->channelBuffers32[1];
            memcpy(left, inputBuffer->left, sizeof(Sample32) * framesPerBuffer);
            memcpy(right, inputBuffer->right, sizeof(Sample32) * framesPerBuffer);
        }
        else if (channel > 0) {
            Sample32* left = bus->channelBuffers32[0];
            for (unsigned long i = 0; i < framesPerBuffer; ++i) {
                left[i] = (inputBuffer->left[i] + inputBuffer->right[i]) / 2;
            }
        }
    }

    if (eventList != nullptr) {
        while (true) {
            Event* note = _easyVst->popEvent();
            if (note == nullptr) {
                break;
            }
            eventList->addEvent(*note);
            setEventRecycled(note);
        }
    }

    bool black = false;
    if (_easyVst->processVST(framesPerBuffer) == false) {
        black = true;
    }

    if (eventList != nullptr) {
        eventList->clear();
    }

    ParameterChanges* paramChange = _easyVst->parameterChanges(kInput);
    if (paramChange != nullptr && paramChange->getParameterCount() > 0) {
        paramChange->clearQueue();
    }

    int audioBuses = _easyVst->numBuses(kAudio, kOutput);
    for (int i = 0; i < audioBuses; ++i) {
        AudioBusBuffers* bus = _easyVst->audioBusBuffer(kOutput, i);
        Sample32* left;
        Sample32* right;
        if (_easyVst->busInfo(kAudio, kOutput, i)->channelCount >= 2) {
            left = bus->channelBuffers32[0];
            right = bus->channelBuffers32[1];
        }
        else {
            left = bus->channelBuffers32[0];
            right = left;
        }

        double busVolume = getBusVolume(i);
        for (unsigned long i = 0; i < framesPerBuffer; ++i) {
            if (outputBuffer != nullptr) {
                outputBuffer->left[i] += left[i] * busVolume * outputVolumme;
                outputBuffer->right[i] += right[i] * busVolume * outputVolumme;
            }
            if (efxBuffer != nullptr) {
                efxBuffer->left[i] += left[i] * busVolume * efxVolume;
                efxBuffer->right[i] += right[i] * busVolume * efxVolume;
            }
        }
    }

    return black ? false : true;;
}

bool MXVSTInstrument::savePreset(std::wstring& path) {
    if (isOpen() == false) {
        return false;
    }
    //2回実行しないとダメなケースがある
    _easyVst->savePreset(path.c_str());
    return _easyVst->savePreset(path.c_str());
}

bool  MXVSTInstrument::loadPreset(std::wstring& path) {
    if (isOpen() == false) {
        return false;
    }
    return _easyVst->loadPreset(path.c_str());
}

bool MXVSTInstrument::isOpen() {
    if (_easyVst != nullptr) {
        return _easyVst->isOpen();
    }
    return false;
}

bool MXVSTInstrument::isActive() {
    if (_blackList) {
        return false;
    }
    if (_easyVst != nullptr) {
        return _easyVst->isOpen() && !_easyVst->isSuspended();
    }
    return false;
}

void MXVSTInstrument::setInsertBalance(float bal) {
    _insertBalance = bal;
}

float MXVSTInstrument::getInsertBalance() {
    return _insertBalance;
}

void MXVSTInstrument::setAuxSend(float bal) {
    _auxSend = bal;
}

float MXVSTInstrument::getAuxSend() {
    return _auxSend;
}
