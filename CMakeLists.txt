cmake_minimum_required(VERSION 3.5.1)

set(CMAKE_VERBOSE_MAKEFILE ON)

message(STATUS "Building using CMake version: ${CMAKE_VERSION}")

string(REGEX MATCH "^[0-9]+\\.[0-9]+\\.[0-9]+" DH_BASE_VERSION "${DH_VERSION}")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_BUILD_TYPE Debug)

project(deephaven_cpp VERSION "${DH_BASE_VERSION}")

find_package(Threads REQUIRED)

# This branch assumes that gRPC and all its dependencies are already installed
# on this system, so they can be located by find_package().

# Find Protobuf installation
# Looks for protobuf-config.cmake file installed by Protobuf's cmake installation.
set(protobuf_MODULE_COMPATIBLE TRUE)
find_package(Protobuf CONFIG REQUIRED)
message(STATUS "Using protobuf ${Protobuf_VERSION}")

set(_PROTOBUF_LIBPROTOBUF protobuf::libprotobuf)
set(_REFLECTION gRPC::grpc++_reflection)
find_program(_PROTOBUF_PROTOC protoc)

# Find gRPC installation
# Looks for gRPCConfig.cmake file installed by gRPC's cmake installation.
find_package(gRPC CONFIG REQUIRED)
message(STATUS "Using gRPC ${gRPC_VERSION}")

set(_GRPC_GRPCPP gRPC::grpc++)
find_program(_GRPC_CPP_PLUGIN_EXECUTABLE grpc_cpp_plugin)

set(PROTO_INPUT_DIR "${PROJECT_SOURCE_DIR}/protos")
file(GLOB PROTO_FILES "${PROTO_INPUT_DIR}/*.proto")
set(PROTO_OUTPUT_DIR "${CMAKE_BINARY_DIR}/gen/proto")
file(MAKE_DIRECTORY ${PROTO_OUTPUT_DIR})

foreach(file ${PROTO_FILES})
    execute_process(COMMAND ${_PROTOBUF_PROTOC} -I ${PROTO_INPUT_DIR} --proto_path=${PROTO_INPUT_DIR} --cpp_out=${PROTO_OUTPUT_DIR} --grpc_out=${PROTO_OUTPUT_DIR} --plugin=protoc-gen-grpc=${_GRPC_CPP_PLUGIN_EXECUTABLE} ${file}
        WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}/protos/
        RESULT_VARIABLE PROTO_RESULT
        OUTPUT_VARIABLE PROTO_OUTPUT)
endforeach()

file(GLOB PROTO_SRC "${PROTO_OUTPUT_DIR}/*.pb.cc")
file(GLOB PROTO_INC "${PROTO_OUTPUT_DIR}/*.pb.h")

# include generated headers
include_directories("${CMAKE_BINARY_DIR}/gen")

add_library(dh_grpc_proto
    ${PROTO_SRC}
    ${PROTO_INC})
target_link_libraries(dh_grpc_proto
    ${_REFLECTION}
    ${_GRPC_GRPCPP}
    ${_PROTOBUF_LIBPROTOBUF})

# Targets greeter_[async_](client|server)
foreach(_target example)
  add_executable(${_target} "${_target}.cc")
  target_link_libraries(${_target}
    dh_grpc_proto
    ${_REFLECTION}
    ${_GRPC_GRPCPP}
    ${_PROTOBUF_LIBPROTOBUF})
endforeach()
