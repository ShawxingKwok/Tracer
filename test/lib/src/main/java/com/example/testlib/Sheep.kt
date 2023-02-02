package com.example.testlib

class Sheep : Animal() {
    override val name get() = "S"
    override val _Animal: Animal get() = this
}