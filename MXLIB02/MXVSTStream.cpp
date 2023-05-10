#include "pch.h"
#include "MXVSTStream.h"
#include "MXVSTOperator.h"
#include "strconv.h"

MXVSTStream* _stream;

MXVSTStream* getMXStream() {
    if (_stream == nullptr) {
        _stream = new MXVSTStream();
    }
    return _stream;
}

MXVSTStream::MXVSTStream() {
    _initDone = false;
    _openedStream = nullptr;
    _initRetcode = paNoError;
    _sampleRate = 44100;
    _blockSize = 1024;
    _bufSize = 0;
    ensureBufferSize(4096);
    _continuousSamples = 0;
}

MXVSTStream::~MXVSTStream() {
    closeStream();
}

bool MXVSTStream::Initialize() {
    if (!_initDone) {
        _initRetcode = Pa_Initialize();
        if (_initRetcode != paNoError) {
            debugNumber("Pa_Initialize Failed ", _initRetcode);
            return false;
        }
        _initDone = true;
        _countHostApi = Pa_GetHostApiCount();
        _countHostDevice = Pa_GetDeviceCount();
    }

    return true;
}

bool MXVSTStream::isSuspended() {
    return _suspend;
}

int MXVst_AudioCallback(const void* inputBuffer, void* pOutputBuffer, unsigned long framesPerBuffer, const PaStreamCallbackTimeInfo* timeInfo, PaStreamCallbackFlags statusFlags, void* pUserData)
{
    float* outputBuffer = static_cast<float*>(pOutputBuffer);
    
    getMXStream()->processAudio(outputBuffer, framesPerBuffer);

    return 0;
}

bool MXVSTStream::closeStream() {
    if (_openedStream != nullptr) {
        debugText("PA_CloseStream");
        Pa_CloseStream(_openedStream);
        _openedStream = nullptr;
        return true;
    }
    return false;
}

bool MXVSTStream::isOpen() {
    if (_openedStream != nullptr) {
        return true;
    }
    return false;
}

bool MXVSTStream::openStream(int num, int sampleRate, int blockSize) {
    PaError err;

    if (_openedStream != nullptr) {
        return true;
    }

    const PaDeviceInfo* device = Pa_GetDeviceInfo(num);

    bool vstReinit = false;

    if (_sampleRate != sampleRate || _blockSize != blockSize) {
        vstReinit = true;
    }

    if (vstReinit) {
        _suspend = true;
        _sampleRate = sampleRate;
        _blockSize = blockSize;
        for (int x = 0; x < MAX_SYNTH; ++x) {
            MXVSTInstrument* vst = getOperator()->getSynth(false, x);
            if (vst != nullptr) {
                if (vst->isOpen()) {
                    __try {
                        vst->_easyVst->suspendVST();
                    }
                    __except (systemExceptionMyHandler("reinitSynth", GetExceptionInformation())) {

                    }
                }
            }
        }
        for (int x = 0; x < MAX_EFFECT; ++x) {
            MXVSTInstrument* vst = getOperator()->getSynth(true, x);
            if (vst != nullptr) {
                if (vst->isOpen()) {
                    __try {
                        vst->_easyVst->suspendVST();
                    }
                    __except (systemExceptionMyHandler("reinitEffect", GetExceptionInformation())) {

                    }
                }
            }
        }
    }

    PaStreamParameters output;
    output.device = num;
    output.sampleFormat = paFloat32;
    output.channelCount = 2;
    output.suggestedLatency = device->defaultLowOutputLatency;
    output.hostApiSpecificStreamInfo = nullptr;

    if (_openedStream != nullptr) {
        Pa_StopStream(_openedStream);
        _openedStream = nullptr;
    }

    err = Pa_OpenStream(&_openedStream, nullptr, &output, sampleRate, blockSize, 0, MXVst_AudioCallback, nullptr);
    if (err != paNoError) {
        return false;
    }

    err = Pa_StartStream(_openedStream);
    if (err != paNoError) {
        return false;
    }
    if (vstReinit) {
        for (int x = 0; x < MAX_SYNTH; ++x) {
            MXVSTInstrument* vst = getOperator()->getSynth(false, x);
            if (vst != nullptr) {
                if (vst->isOpen()) {
                    __try {
                        vst->_easyVst->resumeVST();
                    }
                    __except (systemExceptionMyHandler("reopenSynth", GetExceptionInformation())) {

                    }
                }
            }
        }
        for (int x = 0; x < MAX_EFFECT; ++x) {
            MXVSTInstrument* vst = getOperator()->getSynth(true, x);
            if (vst != nullptr) {
                if (vst->isOpen()) {
                    __try {
                        vst->_easyVst->resumeVST();
                    }
                    __except (systemExceptionMyHandler("reopenEffect", GetExceptionInformation())) {

                    }
                }
            }
        }
        _suspend = false;
    }

    return true;
}

int MXVSTStream::countStream() {
    return Pa_GetDeviceCount();
}

bool MXVSTStream::nameOfStream(std::wstring& ret, int number) {
    const PaDeviceInfo* info = Pa_GetDeviceInfo(number);
    if (info != nullptr) {
        ret = utf8_to_wide(info->name);
        return true;
    }
    return false;
}

bool MXVSTStream::typeNameOfStream(std::wstring& ret, int number) {
    if (number < 0 || number >= countStream()) {
        return false;
    }
    const PaDeviceInfo* info = Pa_GetDeviceInfo(number);
    if (info == nullptr) {
        return false;
    }

    const PaHostApiInfo* host= Pa_GetHostApiInfo(info->hostApi);

    ret = utf8_to_wide(host->name);
    return true;
}

int MXVSTStream::getSampleRate() {
    return _sampleRate;
}

int MXVSTStream::getBlockSize() {
    return _blockSize;
}

ProcessContext processContext = {};

void MXVSTStream::processAudio(float* outputBuffer, unsigned long framesPerBuffer) {
    __try
    {
        int sampleRate = getMXStream()->getSampleRate();
        int blockSize = getMXStream()->getBlockSize();

        if (blockSize != framesPerBuffer) {
            // for safe
            return;
        }

        _ti.is_playing_ = true;
        _ti.sample_pos_ = _continuousSamples;
        _ti.sample_length_ = framesPerBuffer;
        _ti.sample_rate_ = sampleRate;
        _ti.tempo_ = 120;
        _ti.ppq_pos_ = this->_continuousSamples / ((60.0 / TEMPO) * static_cast<double>(sampleRate));
        _ti.clock = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::system_clock::now().time_since_epoch()).count();

        this->_continuousSamples += framesPerBuffer;

        memset(outputBuffer, 0, sizeof(float) * framesPerBuffer * 2);

        if (isSuspended()) {
            return;
        }
        if (ensureBufferSize(framesPerBuffer) == false) {
            return;
        }
        int64 systemTime = 0;
        MXVSTOperator* ope = getOperator();
        for (int i = 0; i < MAX_SYNTH; ++i) {
            MXVSTInstrument* vst = nullptr;
            MXVSTInstrument* synth = nullptr;
            vst = ope->getSynth(false, i);
            synth = vst;

            __try
            {
                if (vst != nullptr && vst->isActive()) {
                    double insertBalance = vst->getInsertBalance();
                    double sumVolume = ope->getMasterVolume();
                    double mainVolume = sumVolume * (1 - insertBalance);
                    double effectVolume = sumVolume * insertBalance;
                    _bufResult.zero();
                    _bufInsert.zero();
                    if (!vst->processAudio(_ti, nullptr, &_bufResult, mainVolume, &_bufInsert, effectVolume)) {
                        vst->_blackList = true;
                        refBlackListed(false, i);
                    }
                }else {
                    continue;
                }

            }
            __except (systemExceptionMyHandler("processAudio#1", GetExceptionInformation()))
            {
                if (vst != nullptr) {
                    vst->_blackList = true;
                    refBlackListed(false, i);
                }
            }

            vst = ope->getSynth(true, 0);
            __try
            {
                if (vst != nullptr && vst->isOpen() && synth->getInsertBalance() > 0) {
                    _bufInsertResult.zero();
                    if (vst->processAudio(_ti, &_bufInsert, &_bufInsertResult, 1, nullptr, 0) == false) {
                        vst->_blackList = true;
                        refBlackListed(true, 0);
                    }
                    else {
                        for (int i = 0; i < framesPerBuffer; ++i) {
                            _bufResult.left[i] += _bufInsertResult.left[i];
                            _bufResult.right[i] += _bufInsertResult.right[i];
                        }
                    }
                }
            }
            __except (systemExceptionMyHandler("processAudio#2", GetExceptionInformation()))
            {
                if (vst != nullptr) {
                    vst->_blackList = true;
                    refBlackListed(true, 0);
                }
            }
            vst = ope->getSynth(true, 1);
            __try
            {
                if (vst != nullptr && vst->isOpen() && synth->getAuxSend() > 0) {
                    _bufAuxSendResult.zero();
                    if (vst->processAudio(_ti, &_bufResult, &_bufAuxSendResult, synth->getAuxSend(), nullptr, 0) == false) {
                        vst->_blackList = true;
                        refBlackListed(true, 0);
                    }
                    else {
                        for (int i = 0; i < framesPerBuffer; ++i) {
                            _bufResult.left[i] += _bufAuxSendResult.left[i];
                            _bufResult.right[i] += _bufAuxSendResult.right[i];
                        }
                    }
                }
            }
            __except (systemExceptionMyHandler("processAudio#3", GetExceptionInformation()))
            {
                if (vst != nullptr) {
                    vst->_blackList = true;
                    refBlackListed(true, 1);
                }
            }
            for (int i = 0; i < framesPerBuffer; ++i) {
                outputBuffer[i * 2 + 0] += _bufResult.left[i];
                outputBuffer[i * 2 + 1] += _bufResult.right[i];
            }
        }
    }
    __except (systemExceptionMyHandler("processAudio outSide", GetExceptionInformation()))
    {
    }
}

bool MXVSTStream::ensureBufferSize(long size) {
    if (_bufSize < size) {
        _suspend = true;
        bool result;
        _bufSize = size;
        result = _bufResult.ensure(size);
        result &= _bufInsert.ensure(size);
        result &= _bufInsertResult.ensure(size);
        result &= _bufAuxSendResult.ensure(size);
        _suspend = false;
        return result;
    }

    return true;
}
