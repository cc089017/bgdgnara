cmake_minimum_required(VERSION 3.22.1)
project("root_detector")

add_library(
    root_detector
    SHARED
    root_detector.cpp
)

find_library(log-lib log)

target_link_libraries(
    root_detector
    ${log-lib}
)
