package com.example.syntax

import java.io.File

fun main() {
    arrayOf(
        // "/Users/william/IdeaProjects/library/KDataStore/api/src/main/java/pers/shawxingkwok/kdatastore"
        "processor/src/main/java/pers/apollokwok/tracer/common",
    )
    .sumOf { path ->
        File(path).codeLinesSize().also { println("$path: $it") }
    }
    .let(::println)
}

private fun File.codeLinesSize(): Int{
    val (files, dirs) = listFiles()!!.partition { it.isFile }

    val sizeInFiles = files
        .filter { it.name.endsWith("kt") }
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