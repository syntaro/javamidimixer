#pragma once

#include "pch.h"
#include <string>
#include "MXStereoBuffer.h"

class MXVSTStream {
public:
	MXVSTStream();
	~MXVSTStream();

	bool Initialize();

	bool openStream(int index, int sampleRate, int latency);
	bool isOpen();
	bool closeStream();

	int countStream();
	bool nameOfStream(std::wstring& ret, int number);
	bool typeNameOfStream(std::wstring& ret, int number);

	bool reinit(int sampleRate, int blockSize);

	int getSampleRate();
	int getBlockSize();

	bool isSuspended();
	void processAudio(float* outputBuffer, unsigned long framesPerBuffer);

private:
	TimeInfo _ti;
	int _sampleRate;
	int _blockSize;
	bool _initDone;
	bool _suspend;
	PaError _initRetcode;
	PaStream* _openedStream;

	PaHostApiIndex _countHostApi;
	PaDeviceIndex _countHostDevice;

	bool ensureBufferSize(long size);

	int _bufSize;
	MXStereoBuffer _bufResult;
	MXStereoBuffer _bufInsert;
	MXStereoBuffer _bufInsertResult;
	MXStereoBuffer _bufAuxSendResult;

	long _continuousSamples;
};

extern MXVSTStream* getMXStream();
