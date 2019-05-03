/* Copyright (C) 2016-2018 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: AGPLv3. For commercial license please contact us.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://doubango.org
*/
#include "ultimate_alpr_sdk_api_public.h"
#include "ultimate_alpr_sdk_api_private.h"
#include <ultimate_alpr_api_private.h>

using namespace ULTIMATE_ALPR_NAMESPACE;

// make sure the patched namespaces are correct
#if ULTIMATE_ALPR_NAMESPACE != SDK_ULTIMATE_ALPR_NAMESPACE
#	error "Patched namespace not correct"
#endif

ULTIMATE_ALPR_SDK_NAMESPACE_BEGIN()

ultAplrSdk::ultAplrSdk()
{
	/* Init the base engine. Nop if already done */
	COMPV_CHECK_CODE_ASSERT(UltBaseEngine::init());

	/* Create recognizer */
	UltAlprRecognizerPtr recognizer_;
	COMPV_CHECK_CODE_ASSERT(UltAlprRecognizer::newObj(&recognizer_));
	m_pRecognizer = new ultAlprSdkGhostObj<UltAlprRecognizer>(recognizer_);
	COMPV_ASSERT(m_pRecognizer != nullptr);
}

ultAplrSdk::~ultAplrSdk()
{
	delete m_pRecognizer, m_pRecognizer = nullptr;
}

ULTIMATE_ALPR_SDK_NAMESPACE_END()
