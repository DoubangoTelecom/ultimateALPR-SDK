/* Copyright (C) 2016-2018 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: AGPLv3. For commercial license please contact us.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://doubango.org
*/
#ifndef _ULTIMATE_ALPR_SDK_LIB_CONFIG_H_
#define _ULTIMATE_ALPR_SDK_LIB_CONFIG_H_

// Windows's symbols export
#if (defined(WIN32) || defined(_WIN32) || defined(_WIN32_WCE) || defined(_WIN16) || defined(_WIN64) || defined(__WIN32__) || defined(__TOS_WIN__) || defined(__WINDOWS__)) && !defined(ULTIMATE_ALPR_SDK_STATIC)
#	if defined(ULTIMATE_ALPR_SDK_EXPORTS)
# 		define ULTIMATE_ALPR_SDK_API		__declspec(dllexport)
#	else
# 		define ULTIMATE_ALPR_SDK_API		__declspec(dllimport)
#	endif
#else
# 	define ULTIMATE_ALPR_SDK_API
#endif

// namespace (you can update the namespace using CFLAGS+=-DULTIMATE_ALPR_SDK_NAMESPACE=YourNameSpace)
#if !defined ULTIMATE_ALPR_SDK_NAMESPACE
#	define ULTIMATE_ALPR_SDK_NAMESPACE ultimateAlprSdk
#endif
#if !defined(ULTIMATE_ALPR_SDK_NAMESPACE_BEGIN)
#	define ULTIMATE_ALPR_SDK_NAMESPACE_BEGIN() namespace ULTIMATE_ALPR_SDK_NAMESPACE {
#endif
#if !defined(ULTIMATE_ALPR_SDK_NAMESPACE_END)
#	define ULTIMATE_ALPR_SDK_NAMESPACE_END() }
#endif

#if !defined(ULTIMATE_ALPR_SDK_NUM_THREADS)
#	define ULTIMATE_ALPR_SDK_NUM_THREADS	COMPV_NUM_THREADS_MULTI
#endif

#if !defined(ULTIMATE_ALPR_SDK_DEBUG_LEVEL)
#	define ULTIMATE_ALPR_SDK_DEBUG_LEVEL	COMPV_DEBUG_LEVEL_INFO
#endif

#define ULTIMATE_ALPR_SDK_OBJECT_DECLARE_PTRS(objName) \
	class UltAlprSdk##objName;  \
	typedef CompVPtr<UltAlprSdk##objName*> UltAlprSdk##objName##Ptr;  \
	typedef UltAlprSdk##objName##Ptr* UltAlprSdk##objName##PtrPtr;

// Must be at the bottom to make sure we can redefine all macros
#if defined(HAVE_CONFIG_H)
#include <config.h>
#endif

#endif /* _ULTIMATE_ALPR_SDK_LIB_CONFIG_H_ */
