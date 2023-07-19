package com.example.testlib

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
abstract class Nature : NatureTracer {
    abstract val earth: Earth
}