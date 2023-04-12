package org.koin.example

interface Heater {
    fun on()
    fun off()
    val isHot: Boolean
}