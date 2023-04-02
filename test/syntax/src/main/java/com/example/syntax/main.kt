package com.example.syntax

fun main() {
    L()
}

abstract class I{
    init {
        println(this is L)
    }
}

class L : I()