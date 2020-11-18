cmake_minimum_required(VERSION 3.0)

project(trt_optimizer VERSION 1.0.0 LANGUAGES CXX C)

#### ultimate Libraries ####
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../SDK_dev/lib build/ultimateALPR/SDK_dev)

include_directories(
	${CMAKE_CURRENT_SOURCE_DIR}/../../../../SDK_dev/lib/include
)

set(trt_optimizer_SOURCES 
	trt_optimizer.cxx
)

###### The executable ######
add_executable(trt_optimizer ${trt_optimizer_SOURCES})

###### 3rd parties libs ######
target_link_libraries(trt_optimizer ${LIB_LINK_SCOPE} ultimate_alpr-sdk)
add_dependencies(trt_optimizer ultimate_alpr-sdk)

###### Install Libs ######
install(TARGETS trt_optimizer DESTINATION bin)
