package test

import pers.apollokwok.tracer.common.annotations.Tracer

abstract class AF<T> : @Tracer.Declare(false) List<T & Any>