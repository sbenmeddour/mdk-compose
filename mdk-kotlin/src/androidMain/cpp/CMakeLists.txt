cmake_minimum_required(VERSION 3.10.2)

project("mdk-jni-android")

set(MDK_SDK_URL "https://sourceforge.net/projects/mdk-sdk/files/nightly/mdk-sdk-android.7z")
set(MDK_SDK_SAVE ${CMAKE_CURRENT_SOURCE_DIR}/mdk-sdk-android.7z)

if (NOT EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/mdk-sdk/lib/cmake/FindMDK.cmake)
    if (NOT EXISTS ${MDK_SDK_SAVE})
        message("Downloading mdk-sdk from ${MDK_SDK_URL}")
        file(DOWNLOAD ${MDK_SDK_URL} ${MDK_SDK_SAVE} SHOW_PROGRESS)
    endif()
    execute_process(
            COMMAND ${CMAKE_COMMAND} -E tar "xvf" ${MDK_SDK_SAVE} "--format=7zip"
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            OUTPUT_STRIP_TRAILING_WHITESPACE
            RESULT_VARIABLE EXTRACT_RET
    )
    if (NOT EXTRACT_RET EQUAL 0)
        file(REMOVE ${MDK_SDK_SAVE})
        message(FATAL_ERROR "Failed to extract mdk-sdk. You can download manually from ${MDK_SDK_URL} and extract to ${CMAKE_CURRENT_SOURCE_DIR}")
    endif()
endif()

include(${CMAKE_CURRENT_SOURCE_DIR}/mdk-sdk/lib/cmake/FindMDK.cmake)

set(CMAKE_C_VISIBILITY_PRESET hidden)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)

add_library(
        mdk-jni
        SHARED
        mdk-kotlin/mdk.jni.cpp
        mdk-kotlin/java_listener.hpp
        mdk-kotlin/utils.hpp
        mdk-kotlin/utils.cpp
)

find_package(Vulkan REQUIRED)
include_directories(${Vulkan_INCLUDE_DIRS})

target_link_libraries(
        mdk-jni
        PRIVATE
        Vulkan::Vulkan
        mdk
        -llog -landroid)