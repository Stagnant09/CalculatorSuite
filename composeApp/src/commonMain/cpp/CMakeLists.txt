cmake_minimum_required(VERSION 3.10)
project(implicit_graph)

set(CMAKE_CXX_STANDARD 17)

add_library(implicit_graph SHARED implicit_graph_core.cpp)
target_include_directories(implicit_graph PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
# If exprtk.hpp is in a subfolder, adjust include dirs.
# Optionally set visibility flags for Windows.
