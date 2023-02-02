package com.example.supertestlib

abstract class Sphere : Form{
    abstract val radius: Long
    val core = Core()

    class Core
}

interface Form