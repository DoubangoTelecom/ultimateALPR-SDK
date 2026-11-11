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
			[--type <host-type>]

	Example:
		runtimeKey \
			--json false \
			--assets C:/Projects/GitHub/ultimate/ultimateMRZ/SDK_dist/assets
		
*/

#include <ultimateALPR-SDK-API-PUBLIC.h>

#include <map>
#if defined(_WIN32)
#include <algorithm> // std::replace
#endif

using namespace ultimateAlprSdk;

static void printUsage(const std::string& message = "");
static bool parseArgs(int argc, char* argv[], std::map<std::string, std::string >& values);

/*
* Entry point
*/
int main(int argc, char *argv[])
{
	// Parsing args
	std::map<std::string, std::string > args;
	if (!parseArgs(argc, argv, args)) {
		printUsage();
		return -1;
	}
	bool rawInsteadOfJSON = false;
	if (args.find("--json") != args.end()) {
		rawInsteadOfJSON = (args["--json"].compare("true") != 0);
	}

	// Build JSON string
	std::string jsonConfig = std::string("\"platform\": \"auto\", \"backend\": \"auto\",");
	if (args.find("--assets") != args.end()) {
		std::string assetsFolder = args["--assets"];
#if defined(_WIN32)
		std::replace(assetsFolder.begin(), assetsFolder.end(), '\\', '/');
#endif
		jsonConfig += std::string("\"assets_folder\": \"") + assetsFolder + std::string("\"");
	}
	if (args.find("--type") != args.end()) {
		jsonConfig += (jsonConfig.empty() ? "" : ",")
			+ std::string("\"license_host_type\": \"") + args["--type"] + std::string("\"");
	}
	if (args.find("--usbserial") != args.end()) {
		jsonConfig += (jsonConfig.empty() ? "" : ",")
			+ std::string("\"license_usbserial\": \"") + args["--usbserial"] + std::string("\"");
	}
	jsonConfig = "{" + jsonConfig + "}";

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

	// DeInitialize the engine
	ULTALPR_SDK_ASSERT(UltAlprSdkEngine::deInit().isOK());

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
		"--type: Defines how the license is attached to the machine/host. Possible values are 'aws-instance', 'aws-byol', 'azure-instance' or 'azure-byol'. Default: null.\n"
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