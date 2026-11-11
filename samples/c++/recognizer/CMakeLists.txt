cmake_minimum_required (VERSION 3.10)

project (recognizer)

include(${CMAKE_CURRENT_SOURCE_DIR}/../common.cmake)
 
include_directories(
	${CMAKE_CURRENT_SOURCE_DIR}/../../../c++
)

set (SOURCE ${PROJECT_NAME}.cxx)

add_executable (${PROJECT_NAME} ${SOURCE})

target_link_libraries(${PROJECT_NAME} 
	PRIVATE ${SDK_DIST_CPP_ALPR_SDK}
	"-Wl,--unresolved-symbols=ignore-in-shared-libs"
)
if (NOT "${SDK_DIST_CPP_ALPR_SDK}" MATCHES "\.so$" AND NOT "${SDK_DIST_CPP_ALPR_SDK}" MATCHES "\.lib$")
	add_dependencies(${PROJECT_NAME} ${SDK_DIST_CPP_ALPR_SDK})
endif ()

install(TARGETS ${PROJECT_NAME} DESTINATION bin)