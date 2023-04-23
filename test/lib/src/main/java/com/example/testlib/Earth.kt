package com.example.testlib

import com.example.supertestlib.Sphere
import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(Nature::class)
open class Earth(override val __Nature: Nature) : Sphere(), EarthTracer {
    override val _Earth: Earth get() = this

    val china = China()
    val modifier = Modifier()
    override val radius: Long = 10000L

}