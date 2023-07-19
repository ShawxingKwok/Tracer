package com.example.testlib

import com.example.supertestlib.Sphere
import pers.shawxingkwok.tracer.Tracer

@Tracer.Nodes(Nature::class)
open class Earth(override val __Nature: Nature) : Sphere(), EarthTracer {
    override val _Earth: Earth get() = this

    val china = China()
    val modifier = Modifier()
    override val radius: Long = 10000L

}