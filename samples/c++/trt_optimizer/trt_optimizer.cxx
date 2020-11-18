/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/

/*
	https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/trt_optimizer/README.md
	Usage: 
		trt_optimizer --assets <path-to-assets-folder>

	Example:
		trt_optimizer --assets C:/Projects/GitHub/ultimate/ultimateMRZ/SDK_dist/assets
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>
#include "../alpr_utils.h"

using namespace ultimateAlprSdk;

static void printUsage(const std::string& message = "");

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	ULTALPR_SDK_PRINT_INFO("\n** This application is used to generate optimized NVIDIA TensorRT models **\n");

	// Parsing args
	std::map<std::string, std::string > args;
	if (!alprParseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}

	// Make sure assets folder is provided
	if (args.find("--assets") == args.end()) {
		printUsage("--assets is missing");
		return -1;
	}

	// Initialize the engine
#if 0 // You cannot try to init the engine without he models
	ULTALPR_SDK_ASSERT(UltAlprSdkEngine::init(jsonConfig.c_str()).isOK());
#endif

	// Optimize NVIDIA TensorRT models
	const UltAlprSdkResult result = UltAlprSdkEngine::optimizeTRT(args["--assets"].c_str());
	if (result.isOK()) {
		ULTALPR_SDK_PRINT_INFO("\n\n%s\n\n",
			result.json()
		);
	}
	else {
		ULTALPR_SDK_PRINT_ERROR("\n\n*** Failed: code -> %d, phrase -> %s ***\n\n",
			result.code(),
			result.phrase()
		);
	}
	
	ULTALPR_SDK_PRINT_INFO("Done: Press any key to terminate !!");
	getchar();

	// DeInitialize the engine
#if 0
	ULTALPR_SDK_ASSERT(UltAlprSdkEngine::deInit().isOK());
#endif

	return 0;
}

/*
* Print usage
*/
static void printUsage(const std::string& message /*= ""*/)
{
	if (!message.empty()) {
		ULTALPR_SDK_PRINT_ERROR("%s", message.c_str());
	}

	ULTALPR_SDK_PRINT_INFO(
		"\n********************************************************************************\n"
		"trt_optimizer\n"
		"\t--assets <path-to-assets-folder> \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n"
		"********************************************************************************\n"
	);
}
