cmake_minimum_required(VERSION 3.10)
project(implicit_graph)
find_package(JNI REQUIRED)

if(MSVC)
    add_compile_options(/bigobj)
    set(CMAKE_C_COMPILER "cl")
    set(CMAKE_CXX_COMPILER "cl")
else()
    set(CMAKE_C_COMPILER "gcc")
    set(CMAKE_CXX_COMPILER "g++")
endif()

set(CMAKE_CXX_STANDARD 17)

add_library(implicit_graph SHARED implicit_graph_core.cpp)
target_include_directories(implicit_graph PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}
)
