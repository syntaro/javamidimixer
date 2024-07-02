#pragma once

#include "pch.h"
#include "EasyVSTCustom.h"
#include "NoteMessage.h"
#include <vector>
#include <algorithm>
#include "MXStereoBuffer.h"

class MXVSTInstrument  {
public:
	MXVSTInstrument();
	~MXVSTInstrument();

	bool load(const std::wstring& path);
	bool isOpen();
	bool isActive();
	bool processAudio(TimeInfo& ti, MXStereoBuffer* inputBuffer, MXStereoBuffer* outputBuffer, float outputVolumme, MXStereoBuffer* efxBuffer, float efxVolume);
	void unload();

	EasyVstCustom* _easyVst;
	std::wstring _path;
	bool savePreset(std::wstring& path);
	bool loadPreset(std::wstring& path);

	void makeBusRoom(int bus);
	int getBusCount();
	float getBusVolume(int bus);
	void setBusVolume(int bus, float vol1);
	void setBusAcive(int bus, bool active);

	void setInsertBalance(float bal);
	float getInsertBalance();
	void setAuxSend(float bal);
	float getAuxSend();

	int _whenClose;
	bool _blackList;

private:
	int _busesVolumeCount;
	float _insertBalance;
	float _auxSend;
	float* _busesVolume;
};

