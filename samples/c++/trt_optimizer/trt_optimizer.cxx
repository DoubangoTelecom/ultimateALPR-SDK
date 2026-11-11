/* Copyright (C) 2011-2026 Doubango Telecom <https://www.doubango.org>
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
		trt_optimizer --assets C:/Projects/GitHub/ultimate/ultimateALPR/assets
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>

#include <map>

using namespace ultimateAlprSdk;

static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char* argv[], std::map<std::string, std::string >& values);

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	ULTALPR_SDK_PRINT_INFO("\n** This application is used to generate optimized NVIDIA TensorRT models **\n");

	// Parsing args
	std::map<std::string, std::string > args;
	if (!parseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}

	// Make sure assets folder is provided
	if (args.find("--assets") == args.end()) {
		printUsage("--assets is missing");
		return -1;
	}

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
		"--assets: Path to the assets folder containing the configuration files and models. More info at https://www.doubango.org/SDKs/anpr/docs/Configuration_options.html#assets-folder. \n"
		"********************************************************************************\n"
	);
}

static bool parseArgs(int argc, char* argv[], std::map<std::string, std::string >& values)
{
	ULTALPR_SDK_ASSERT(argc > 0 && argv != nullptr);

	values.clear();

	// Make sure the number of arguments is even
	if ((argc - 1) & 1) {
		ULTALPR_SDK_PRINT_ERROR("Number of args must be even");
		return false;
	}

	// Parsing
	for (int index = 1; index < argc; index += 2) {
		std::string key = argv[index];
		if (key.size() < 2 || key[0] != '-' || key[1] != '-') {
			ULTALPR_SDK_PRINT_ERROR("Invalid key: %s", key.c_str());
			return false;
		}
		values[key] = argv[index + 1];
	}

	return true;
}