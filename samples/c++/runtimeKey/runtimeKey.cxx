/* Copyright (C) 2011-2020 Doubango Telecom <https://www.doubango.org>
* File author: Mamadou DIOP (Doubango Telecom, France).
* License: For non commercial use only.
* Source code: https://github.com/DoubangoTelecom/ultimateALPR-SDK
* WebSite: https://www.doubango.org/webapps/alpr/
*/

/*
	https://github.com/DoubangoTelecom/ultimateALPR/blob/master/SDK_dist/samples/c++/runtimeKey/README.md
	Usage: 
		runtimeKey \
			[--json <json-output:bool>] \
			[--assets <path-to-assets-folder>]

	Example:
		runtimeKey \
			--json false \
			--assets C:/Projects/GitHub/ultimate/ultimateMRZ/SDK_dist/assets
		
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
	// Parsing args
	std::map<std::string, std::string > args;
	if (!alprParseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	bool rawInsteadOfJSON = false;
	if (args.find("--json") != args.end()) {
		rawInsteadOfJSON = (args["--json"].compare("true") != 0);
	}
	std::string jsonConfig;
	if (args.find("--assets") != args.end()) {
		jsonConfig = std::string("{ \"assets_folder\": \"") + args["--assets"] + std::string("\" }");
	}
	else {
		jsonConfig = "{}";
	}

	// Initialize the engine
	ULTALPR_SDK_ASSERT(UltAlprSdkEngine::init(jsonConfig.c_str()).isOK());

	// Request runtime license key
	const UltAlprSdkResult result = UltAlprSdkEngine::requestRuntimeLicenseKey(rawInsteadOfJSON);
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
	
	ULTALPR_SDK_PRINT_INFO("Press any key to terminate !!");
	getchar();

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
		"runtimeKey\n"
		"\t[--json <json-output:bool>] \n"
		"\n"
		"Options surrounded with [] are optional.\n"
		"\n"
		"--json: Whether to output the runtime license key as JSON string intead of raw string. Default: true.\n"
		"--assets: Path to the assets folder containing the configuration files and models. Default value is the current folder.\n"
		"********************************************************************************\n"
	);
}
