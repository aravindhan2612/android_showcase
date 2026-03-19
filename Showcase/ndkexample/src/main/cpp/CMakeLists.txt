cmake_minimum_required(VERSION 4.1.2)

project("counterapp")

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

#to support 16 page size
target_link_options(
        native-lib
        PRIVATE
        "-Wl,-z,max-page-size=16384"
)

target_link_libraries(
        native-lib
)