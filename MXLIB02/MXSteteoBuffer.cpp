#include "pch.h"
#include "MXStereoBuffer.h"

MXStereoBuffer::MXStereoBuffer() {
	left = new Sample32[4096];
	right = new Sample32[4096];
	capacity = 4096;
}

MXStereoBuffer::~MXStereoBuffer() {
	if (left != nullptr) {
		delete left;
	}
	if (right != nullptr) {
		delete right;
	}
}

bool MXStereoBuffer::ensure(long size) {
	if (size <= capacity) {
		return true;
	}

	Sample32* newleft = new Sample32[size];
	Sample32* newright = new Sample32[size];

	if (newleft != nullptr && newright != nullptr) {
		if (left != nullptr) {
			delete left;
			left = nullptr;
		}
		if (right != nullptr) {
			delete right;
			right = nullptr;
		}

		left = newleft;
		right = newright;
		capacity = size;
		return true;
	}
	return false;
}

void MXStereoBuffer::zero() {
	if (left != nullptr) {
		memset(left, 0, sizeof(Sample32) * capacity);
	}
	if (right != nullptr) {
		memset(right, 0, sizeof(Sample32) * capacity);
	}
}
