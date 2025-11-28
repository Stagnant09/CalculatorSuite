cmake_minimum_required(VERSION 3.22.1)

set(ANDROID_PLATFORM 34)
set(CMAKE_SYSTEM_VERSION 34)

set(CMAKE_ANDROID_PAGE_SIZE 16384)
set(CMAKE_LINKER_FLAGS "${CMAKE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fuse-ld=lld")

add_library(
        implicit_graph
        SHARED
        implicit_graph.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        implicit_graph
        ${log-lib}
)
