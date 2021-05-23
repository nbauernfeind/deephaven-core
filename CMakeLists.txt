cmake_minimum_required(VERSION 3.5)
message(STATUS "Building using CMake version: ${CMAKE_VERSION}")

set(DH_VERSION "0.1.0-SNAPSHOT")
string(REGEX MATCH "^[0-9]+\\.[0-9]+\\.[0-9]+" DH_BASE_VERSION "${DH_VERSION}")

option(ARROW_LINK_SHARED "Link to the Arrow shared library" ON)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_BUILD_TYPE Release)

project(deephaven_cpp VERSION "${DH_BASE_VERSION}")

find_package(Arrow REQUIRED)

message(STATUS "Arrow version: ${ARROW_VERSION}")
message(STATUS "Arrow SO version: ${ARROW_FULL_SO_VERSION}")

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_executable(example src/example.cc)
if (ARROW_LINK_SHARED)
  target_link_libraries(example PRIVATE arrow_shared)
  target_link_libraries(example PRIVATE /usr/local/opt/apache-arrow/lib/libarrow_flight.dylib)
else()
  set(THREADS_PREFER_PTHREAD_FLAG ON)
  find_package(Threads REQUIRED)
  target_link_libraries(example PRIVATE arrow_static Threads::Threads)
  target_link_libraries(example PRIVATE /usr/local/opt/apache-arrow/lib/libarrow_flight.dylib Threads::Threads)
endif()
