/* Copyright (C) 2016-2018 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: AGPLv3. For commercial license please contact us.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://doubango.org
*/
#ifndef _ULTIMATE_ALPR_SDK_LIB_API_PUBLIC_H_
#define _ULTIMATE_ALPR_SDK_LIB_API_PUBLIC_H_

#include "ultimate_alpr_sdk_config.h"

#include <stdint.h>
#include <vector>

/* Patching external namespaces (use -DULTIMATE_ALPR_NAMESPACE=... at compile time to set one if changed) */
#if defined(ULTIMATE_ALPR_NAMESPACE)
#	define SDK_ULTIMATE_ALPR_NAMESPACE ULTIMATE_ALPR_NAMESPACE
#else
#	define SDK_ULTIMATE_ALPR_NAMESPACE ultimateAlpr
#endif

/* Forward declarations from ultimateALPR library */
namespace SDK_ULTIMATE_ALPR_NAMESPACE {
	class UltAlprRecognizer;
};

ULTIMATE_ALPR_SDK_NAMESPACE_BEGIN()

typedef int ultAplrSdkInt;
typedef float ultAplrSdkFloat;
typedef unsigned long ultAplrSdkCharCode;

// Forward declarations
template <class T> struct ultAlprSdkGhostObj;

/**
* << Documentation here >>
*/
struct ultAplrSdkPoint {
	ultAplrSdkInt x;
	ultAplrSdkInt y;
};

/**
* << Documentation here >>
*/
struct ultAplrSdkRect {
	ultAplrSdkInt left;
	ultAplrSdkInt top;
	ultAplrSdkInt right;
	ultAplrSdkInt bottom;
	ultAplrSdkRect(ultAplrSdkInt left_ = 0, ultAplrSdkInt top_ = 0, ultAplrSdkInt right_ = 0, ultAplrSdkInt bottom_ = 0) : left(left_), top(top_), right(right_), bottom(bottom_) {  }
	static ultAplrSdkRect makeFromWidthHeight(ultAplrSdkInt x, ultAplrSdkInt y, ultAplrSdkInt width, ultAplrSdkInt height) {
		return ultAplrSdkRect(x, y, x + width, y + height);
	}
};

/**
* << Documentation here >>
*/
struct ultAplrSdkDigit {
	ultAplrSdkRect location;
	ultAplrSdkFloat confidence;
	ultAplrSdkCharCode charCode;
};

/**
* << Documentation here >>
*/
struct ultAplrSdkPlate {
	std::vector<ultAplrSdkDigit> digits;
};

/**
* << Documentation here >>
*/
struct ultAplrSdkResult 
{
public:

private:
};

/**
* << Documentation here >>
*/
class ULTIMATE_ALPR_SDK_API ultAplrSdk
{
public:
	ultAplrSdk();
	virtual ~ultAplrSdk();

private:
	ultAlprSdkGhostObj<SDK_ULTIMATE_ALPR_NAMESPACE::UltAlprRecognizer>* m_pRecognizer;
};

ULTIMATE_ALPR_SDK_NAMESPACE_END()

#endif /* _ULTIMATE_ALPR_SDK_LIB_API_PUBLIC_H_ */
