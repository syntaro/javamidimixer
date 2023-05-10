#pragma once

#include "pch.h"


class MXStereoBuffer {
public:
	MXStereoBuffer();
	~MXStereoBuffer();

	bool ensure(long size);

	Sample32* left;
	Sample32* right;
	int capacity;
	void zero();
private:
};

