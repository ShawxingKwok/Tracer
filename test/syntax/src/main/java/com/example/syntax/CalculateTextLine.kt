package com.example.syntax

import java.io.File

fun main() {
    arrayOf(
        "common/annotations/src/main/java",
        "common/processor/src/main/java",
        "shared/src/main/java",
        "android/compiler/src/main/java",
    )
    .sumOf { path ->
        File(path).codeLinesSize().also { println("$path: $it") }
    }
    .let(::println)
}

private fun File.codeLinesSize(): Int{
    val (files, dirs) = listFiles()!!.partition { it.isFile }

    val sizeInFiles = files
        .flatMap(File::readLines)
        .map(String::trim)
        .count {
            it.length > 1
            && it != ") {"
            && it != "){"
            && !it.startsWith("*")
            && !it.startsWith("/")
            && !it.startsWith("import ")
            && !it.startsWith("package ")
        }

    val sizeInDirs = dirs.sumOf { it.codeLinesSize() }

    return sizeInFiles + sizeInDirs
}