cmake_minimum_required(VERSION 3.10)
project(mfswordmod)

set(CMAKE_C_STANDARD 11)

add_library(mfswordmod SHARED
    mfswordmod_native.c
    dobby.c
)

target_link_libraries(mfswordmod
    log
    dl
)